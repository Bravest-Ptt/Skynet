package bravest.ptt.skynet.core.nat;

import bravest.ptt.skynet.core.tcpip.CommonMethods;

public class NatSession {

    public int RemoteIP;
    public short RemotePort;
    public String RemoteHost;
    public int BytesSent;
    public int PacketSent;
    public long LastNanoTime;
    public boolean IsHttpsSession;
    public String RequestUrl; //HTTP请求的url， HTTPS请求则为空
    public String Method; //HTTP请求方法

    @Override
    public String toString() {
        return String.format("%s/%s:%d packet: %d", RemoteHost, CommonMethods.ipIntToString(RemoteIP),
                RemotePort & 0xFFFF, PacketSent);
    }
}
