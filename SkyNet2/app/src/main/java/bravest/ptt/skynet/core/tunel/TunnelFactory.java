package bravest.ptt.skynet.core.tunel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import bravest.ptt.skynet.core.nat.NatSession;
import bravest.ptt.skynet.core.nat.NatSessionManager;

public class TunnelFactory {

	public static Tunnel wrap(SocketChannel channel, Selector selector) {
		Tunnel tunnel = new RawTunnel(channel, selector);
		NatSession session = NatSessionManager.getSession((short) channel.socket().getPort());
		if (session != null) {
			tunnel.setIsHttpsRequest(session.IsHttpsSession);
		}
		return tunnel;
	}

	public static Tunnel createTunnelByConfig(InetSocketAddress destAddress, Selector selector) throws IOException {
		//TODO 这里只是简单创建一个RawTunnel，日后可以根据代理类型创建不同的Tunnel
		return new RemoteTunnel(destAddress, selector);
	}
}
