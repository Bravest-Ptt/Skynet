package bravest.ptt.skynet.core.proxy;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import bravest.ptt.skynet.core.ProxyConfig;
import bravest.ptt.skynet.core.nat.NatSession;
import bravest.ptt.skynet.core.nat.NatSessionManager;
import bravest.ptt.skynet.core.tcpip.CommonMethods;
import bravest.ptt.skynet.core.tunel.Tunnel;
import bravest.ptt.skynet.core.tunel.TunnelFactory;

public class TcpProxyServer implements Runnable {

    private static final String TAG = "TcpProxyServer";

    public boolean Stopped;
    public short Port;

    Selector mSelector;
    ServerSocketChannel mServerSocketChannel;
    Thread mServerThread;

    public TcpProxyServer(int port) throws IOException {
        mSelector = Selector.open();
        mServerSocketChannel = ServerSocketChannel.open();
        mServerSocketChannel.configureBlocking(false);
        mServerSocketChannel.socket().bind(new InetSocketAddress(port));
        mServerSocketChannel.register(mSelector, SelectionKey.OP_ACCEPT);
        this.Port = (short) mServerSocketChannel.socket().getLocalPort();

        Log.i(TAG, "AsyncTcpServer listen on " + mServerSocketChannel.socket().getInetAddress().toString()
                + ":" + (this.Port & 0xFFFF) + "success.");
    }

    /**
     * 启动TcpProxyServer线程
     */
    public void start() {
        mServerThread = new Thread(this, "TcpProxyServerThread");
        mServerThread.start();
    }

    public void stop() {
        this.Stopped = true;
        if (mSelector != null) {
            try {
                mSelector.close();
                mSelector = null;
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
                Log.e(TAG, "TcpProxyServer mSelector.close() catch an exception: " + ex);
            }
        }

        if (mServerSocketChannel != null) {
            try {
                mServerSocketChannel.close();
                mServerSocketChannel = null;
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
                Log.e(TAG, "TcpProxyServer mServerSocketChannel.close() catch an exception: " + ex);
            }
        }
    }


    @Override
    public void run() {
        try {
            while (true) {
                mSelector.select();
                Iterator<SelectionKey> keyIterator = mSelector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isValid()) {
                        try {
                            if (key.isReadable()) {
                                ((Tunnel) key.attachment()).onReadable(key);
                            } else if (key.isWritable()) {
                                ((Tunnel) key.attachment()).onWritable(key);
                            } else if (key.isConnectable()) {
                                ((Tunnel) key.attachment()).onConnectable();
                            } else if (key.isAcceptable()) {
                                onAccepted(key);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace(System.err);
                            Log.e(TAG, "TcpProxyServer iterate SelectionKey catch an exception:" + ex);
                        }
                    }
                    keyIterator.remove();
                }

            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            Log.e(TAG, "TcpProxyServer catch an exception: " + e);
        } finally {
            this.stop();
            Log.i(TAG, "TcpServer thread exited.");
        }
    }

    InetSocketAddress getDestAddress(SocketChannel localChannel) {
        short portKey = (short) localChannel.socket().getPort();
        NatSession session = NatSessionManager.getSession(portKey);
        if (session != null) {
            if (ProxyConfig.Instance.needProxy(session.RemoteHost, session.RemoteIP)) {
                //TODO 完成跟具体的拦截策略？？？
                Log.i(TAG, String.format("%d/%d:[BLOCK] %s=>%s:%d\n",
                        NatSessionManager.getSessionCount(),
                        Tunnel.SessionCount,
                        session.RemoteHost,
                        CommonMethods.ipIntToString(session.RemoteIP),
                        session.RemotePort & 0xFFFF));

                return null;
            } else {
                return new InetSocketAddress(localChannel.socket().getInetAddress(), session.RemotePort & 0xFFFF);
            }
        }
        return null;
    }

    void onAccepted(SelectionKey key) {
        Tunnel localTunnel = null;
        try {
            SocketChannel localChannel = mServerSocketChannel.accept();
            localTunnel = TunnelFactory.wrap(localChannel, mSelector);

            InetSocketAddress destAddress = getDestAddress(localChannel);
            if (destAddress != null) {
                Tunnel remoteTunnel = TunnelFactory.createTunnelByConfig(destAddress, mSelector);
                //关联兄弟
                remoteTunnel.setIsHttpsRequest(localTunnel.isHttpsRequest());
                remoteTunnel.setBrotherTunnel(localTunnel);
                localTunnel.setBrotherTunnel(remoteTunnel);
                remoteTunnel.connect(destAddress); //开始连接
            } else {
                short portKey = (short) localChannel.socket().getPort();
                NatSession session = NatSessionManager.getSession(portKey);
                if (session != null) {
                    Log.i(TAG, String.format("Have block a request to %s=>%s:%d",
                            session.RemoteHost,
                            CommonMethods.ipIntToString(session.RemoteIP),
                            session.RemotePort & 0xFFFF));
                    localTunnel.sendBlockInformation();
                } else {
                    Log.i(TAG, String.format("Error: socket(%s:%d) have no session.",
                            localChannel.socket().getInetAddress().toString(),
                            portKey));
                }

                localTunnel.dispose();
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            Log.e(TAG, "TcpProxyServer onAccepted catch an exception: " + ex);

            if (localTunnel != null) {
                localTunnel.dispose();
            }
        }
    }
}
