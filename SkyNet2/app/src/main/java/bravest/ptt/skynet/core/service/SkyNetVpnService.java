package bravest.ptt.skynet.core.service;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import bravest.ptt.skynet.activity.HomeActivity;
import bravest.ptt.skynet.core.ProxyConfig;
import bravest.ptt.skynet.core.dns.DnsPacket;
import bravest.ptt.skynet.core.http.HttpRequestHeaderParser;
import bravest.ptt.skynet.core.nat.NatSession;
import bravest.ptt.skynet.core.nat.NatSessionManager;
import bravest.ptt.skynet.core.proxy.DnsProxy;
import bravest.ptt.skynet.core.proxy.TcpProxyServer;
import bravest.ptt.skynet.core.tcpip.CommonMethods;
import bravest.ptt.skynet.core.tcpip.IPHeader;
import bravest.ptt.skynet.core.tcpip.TCPHeader;
import bravest.ptt.skynet.core.tcpip.UDPHeader;
import bravest.ptt.skynet.core.util.VpnServiceHelper;
import bravest.ptt.skynet.event.VPNEvent;
import de.greenrobot.event.EventBus;

/**
 * Created by zengzheying on 15/12/28.
 */
public class SkyNetVpnService extends VpnService implements Runnable {

    private static final String TAG = "SkyNetVpnService";
    private static final Class<?> TARGET_ACTIVITY = HomeActivity.class;

    private static int ID;
    private static int LOCAL_IP;
    private boolean IsRunning = false;
    private Thread mVPNThread;
    private ParcelFileDescriptor mVPNInterface;
    private TcpProxyServer mTcpProxyServer;
    private DnsProxy mDnsProxy;
    private FileOutputStream mVPNOutputStream;

    private byte[] mPacket;
    private IPHeader mIPHeader;
    private TCPHeader mTCPHeader;
    private UDPHeader mUDPHeader;
    private ByteBuffer mDNSBuffer;
    private Handler mHandler;
    private long mSentBytes;
    private long mReceivedBytes;

    public SkyNetVpnService() {
        ID++;
        mHandler = new Handler();
        mPacket = new byte[20000];
        mIPHeader = new IPHeader(mPacket, 0);
        //Offset = ip报文头部长度
        mTCPHeader = new TCPHeader(mPacket, 20);
        mUDPHeader = new UDPHeader(mPacket, 20);
        //Offset = ip报文头部长度 + udp报文头部长度 = 28
        mDNSBuffer = ((ByteBuffer) ByteBuffer.wrap(mPacket).position(28)).slice();

        VpnServiceHelper.onVpnServiceCreated(this);

        Log.i(TAG, "New VPNService " + ID);
    }

    //启动Vpn工作线程
    @Override
    public void onCreate() {
        Log.i(TAG, "VPNService " + ID + "created.");
        mVPNThread = new Thread(this, "VPNServiceThread");
        mVPNThread.start();
        setVpnRunningStatus(true);
        notifyStatus(new VPNEvent(VPNEvent.Status.STARTING));
        super.onCreate();
    }

    //只设置IsRunning = true;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    //停止Vpn工作线程
    @Override
    public void onDestroy() {
        Log.i(TAG, "VPNService" + ID + "destroyed.");
        if (mVPNThread != null) {
            mVPNThread.interrupt();
        }
        VpnServiceHelper.onVpnServiceDestroy();
        super.onDestroy();
    }

    //发送UDP数据报
    public void sendUDPPacket(IPHeader ipHeader, UDPHeader udpHeader) {
        try {
            CommonMethods.ComputeUDPChecksum(ipHeader, udpHeader);
            this.mVPNOutputStream.write(ipHeader.mData, ipHeader.mOffset, ipHeader.getTotalLength());
        } catch (IOException e) {
            e.printStackTrace();

            Log.e(TAG, "VpnService send UDP packet catch an exception " + e);
        }
    }

    //建立VPN，同时监听出口流量
    private void runVPN() throws Exception {
        this.mVPNInterface = establishVPN();
        this.mVPNOutputStream = new FileOutputStream(mVPNInterface.getFileDescriptor());
        FileInputStream in = new FileInputStream(mVPNInterface.getFileDescriptor());
        int size = 0;
        while (size != -1 && IsRunning) {
            while ((size = in.read(mPacket)) > 0 && IsRunning) {
                if (mDnsProxy.Stopped || mTcpProxyServer.Stopped) {
                    in.close();
                    throw new Exception("LocalServer stopped.");
                }
                onIPPacketReceived(mIPHeader, size);
            }
            Thread.sleep(100);
        }
        in.close();
        disconnectVPN();
    }

    void onIPPacketReceived(IPHeader ipHeader, int size) throws IOException {

        int header = ipHeader.getProtocol();
        Log.d(TAG, "onIPPacketReceived: header = " + header);
        switch (header) {
            case IPHeader.TCP:
                TCPHeader tcpHeader = mTCPHeader;
                tcpHeader.mOffset = ipHeader.getHeaderLength(); //矫正TCPHeader里的偏移量，使它指向真正的TCP数据地址
                if (tcpHeader.getSourcePort() == mTcpProxyServer.Port) {

                    NatSession session = NatSessionManager.getSession(tcpHeader.getDestinationPort());
                    if (session != null) {
                        ipHeader.setSourceIP(ipHeader.getDestinationIP());
                        tcpHeader.setSourcePort(session.RemotePort);
                        ipHeader.setDestinationIP(LOCAL_IP);

                        CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);
                        mVPNOutputStream.write(ipHeader.mData, ipHeader.mOffset, size);
                        mReceivedBytes += size;
                    } else {
                        Log.i(TAG, "NoSession:" + ipHeader.toString() + " " + tcpHeader.toString());
                    }

                } else {

                    //添加端口映射
                    int portKey = tcpHeader.getSourcePort();
                    NatSession session = NatSessionManager.getSession(portKey);
                    if (session == null || session.RemoteIP != ipHeader.getDestinationIP() || session.RemotePort
                            != tcpHeader.getDestinationPort()) {
                        session = NatSessionManager.createSession(portKey, ipHeader.getDestinationIP(), tcpHeader
                                .getDestinationPort());
                    }

                    session.LastNanoTime = System.nanoTime();
                    session.PacketSent++; //注意顺序

                    int tcpDataSize = ipHeader.getDataLength() - tcpHeader.getHeaderLength();
                    if (session.PacketSent == 2 && tcpDataSize == 0) {
                        return; //丢弃tcp握手的第二个ACK报文。因为客户端发数据的时候也会带上ACK，这样可以在服务器Accept之前分析出HOST信息。
                    }

                    //分析数据，找到host
                    if (session.BytesSent == 0 && tcpDataSize > 10) {
                        int dataOffset = tcpHeader.mOffset + tcpHeader.getHeaderLength();
                        HttpRequestHeaderParser.parseHttpRequestHeader(session, tcpHeader.mData, dataOffset,
                                tcpDataSize);
                        Log.i(TAG, "Host:" + session.RemoteHost);
                        Log.i(TAG, "Request:" + session.Method + " " + session.RequestUrl);
                    }

                    //转发给本地TCP服务器
                    ipHeader.setSourceIP(ipHeader.getDestinationIP());
                    ipHeader.setDestinationIP(LOCAL_IP);
                    tcpHeader.setDestinationPort(mTcpProxyServer.Port);

                    CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);
                    mVPNOutputStream.write(ipHeader.mData, ipHeader.mOffset, size);
                    session.BytesSent += tcpDataSize; //注意顺序
                    mSentBytes += size;
                }
                break;
            case IPHeader.UDP:
                UDPHeader udpHeader = mUDPHeader;
                udpHeader.mOffset = ipHeader.getHeaderLength();
                if (ipHeader.getSourceIP() == LOCAL_IP && udpHeader.getDestinationPort() == 53) {
                    mDNSBuffer.clear();
                    mDNSBuffer.limit(udpHeader.getTotalLength() - 8);
                    DnsPacket dnsPacket = DnsPacket.fromBytes(mDNSBuffer);
                    if (dnsPacket != null && dnsPacket.Header.QuestionCount > 0) {
                        Log.i(TAG, "let the DnsProxy to process DNS request...\n");
                        Log.i(TAG, "Query " + dnsPacket.Questions[0].Domain);
                        mDnsProxy.onDnsRequestReceived(ipHeader, udpHeader, dnsPacket);
                    }
                }
                break;
        }

    }

    private void waitUntilPrepared() {
        while (prepare(this) != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, "waitUntilPrepared catch an exception " + e);
            }
        }
    }

    private ParcelFileDescriptor establishVPN() throws Exception {
        Builder builder = new Builder();
        builder.setMtu(ProxyConfig.Instance.getMTU());

        Log.i(TAG, "setMtu: " +  ProxyConfig.Instance.getMTU());

        ProxyConfig.IPAddress ipAddress = ProxyConfig.Instance.getDefaultLocalIP();
        LOCAL_IP = CommonMethods.ipStringToInt(ipAddress.Address);
        builder.addAddress(ipAddress.Address, ipAddress.PrefixLength);
        Log.i(TAG, "addAddress: " + ipAddress.Address + "/" + ipAddress.PrefixLength);

        for (ProxyConfig.IPAddress dns : ProxyConfig.Instance.getDnsList()) {
            builder.addDnsServer(dns.Address);
            Log.i("addDnsServer: %s\n", dns.Address);
        }

        if (ProxyConfig.Instance.getRouteList().size() > 0) {
            for (ProxyConfig.IPAddress routeAddress : ProxyConfig.Instance.getRouteList()) {
                builder.addRoute(routeAddress.Address, routeAddress.PrefixLength);
                Log.i(TAG, "addRoute: " + routeAddress.Address + "/" + routeAddress.PrefixLength);
            }
            builder.addRoute(CommonMethods.ipIntToInet4Address(ProxyConfig.FAKE_NETWORK_IP), 16);
            Log.i(TAG, "addRoute for FAKE_NETWORK:"
                    + CommonMethods.ipIntToString(ProxyConfig.FAKE_NETWORK_IP) + "/" + 16);
        } else {
            builder.addRoute("0.0.0.0", 0);
            Log.i(TAG, "addDefaultRoute: 0.0.0.0/0\n");
        }

        Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
        Method method = SystemProperties.getMethod("get", new Class[]{String.class});
        ArrayList<String> servers = new ArrayList<>();
        for (String name : new String[]{"net.dns1", "net.dns2", "net.dns3", "net.dns4",}) {
            String value = (String) method.invoke(null, name);
            if (value != null && !"".equals(value) && !servers.contains(value)) {
                servers.add(value);
                builder.addRoute(value, 32); //添加路由，使得DNS查询流量也走该VPN接口

                Log.i(TAG, "dns name = "+ name + ", value = " + value);
            }
        }

        Intent intent = new Intent(this, TARGET_ACTIVITY);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setConfigureIntent(pendingIntent);

        builder.setSession(ProxyConfig.Instance.getSessionName());
        ParcelFileDescriptor pfdDescriptor = builder.establish();
        notifyStatus(new VPNEvent(VPNEvent.Status.ESTABLISHED));
        return pfdDescriptor;
    }

    @Override
    public void run() {
        try {
            Log.i(TAG, "VPNService" + ID +  "work thread is Running...");

            waitUntilPrepared();

            /*
            * ProxyConfig need an instance of DomainFilter,
            * you should set an DomainFilter object by call the setDomainFilter() method
            * */
            //ProxyConfig.Instance.setDomainFilter(new BlackListFilter());
            //ProxyConfig.Instance.setBlockingInfoBuilder(new HtmlBlockingInfoBuilder());
            ProxyConfig.Instance.prepare();

            //启动TCP代理服务
            mTcpProxyServer = new TcpProxyServer(0);
            mTcpProxyServer.start();

            mDnsProxy = new DnsProxy();
            mDnsProxy.start();
            Log.i(TAG, "DnsProxy started.");

            ProxyConfig.Instance.onVpnStart(this);
            while (IsRunning) {
                runVPN();
            }
            ProxyConfig.Instance.onVpnEnd(this);

        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, "VpnService run catch an exception " + e);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "VpnService run catch an exception" + e);
        } finally {
            Log.i(TAG, "VpnService terminated");
            dispose();
        }
    }

    public void disconnectVPN() {
        try {
            if (mVPNInterface != null) {
                mVPNInterface.close();
                mVPNInterface = null;
            }
        } catch (Exception e) {
            //ignore
        }
        notifyStatus(new VPNEvent(VPNEvent.Status.UNESTABLISHED));
        this.mVPNOutputStream = null;
    }

    private synchronized void dispose() {
        //断开VPN
        disconnectVPN();

        //停止TCP代理服务
        if (mTcpProxyServer != null) {
            mTcpProxyServer.stop();
            mTcpProxyServer = null;
            Log.i(TAG, "TcpProxyServer stopped.");
        }

        if (mDnsProxy != null) {
            mDnsProxy.stop();
            mDnsProxy = null;
            Log.i(TAG, "DnsProxy stopped.");
        }

        stopSelf();
        setVpnRunningStatus(false);
//		System.exit(0);
    }

    private void notifyStatus(VPNEvent event) {
        EventBus.getDefault().post(event);
    }

    public boolean vpnRunningStatus() {
        return IsRunning;
    }

    public void setVpnRunningStatus(boolean isRunning) {
        IsRunning = isRunning;
    }
}
