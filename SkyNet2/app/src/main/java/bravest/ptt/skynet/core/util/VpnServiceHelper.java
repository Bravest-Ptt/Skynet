package bravest.ptt.skynet.core.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.net.DatagramSocket;
import java.net.Socket;

import bravest.ptt.skynet.core.service.SkyNetVpnService;
import bravest.ptt.skynet.core.tcpip.IPHeader;
import bravest.ptt.skynet.core.tcpip.UDPHeader;

public class VpnServiceHelper {

	public static final int START_VPN_SERVICE_REQUEST_CODE = 2015;
	private static SkyNetVpnService sVpnService;

	public static void onVpnServiceCreated(SkyNetVpnService vpnService) {
		sVpnService = vpnService;
	}

	public static void onVpnServiceDestroy() {
		sVpnService = null;
	}

	public static void sendUDPPacket(IPHeader ipHeader, UDPHeader udpHeader) {
		if (sVpnService != null) {
			sVpnService.sendUDPPacket(ipHeader, udpHeader);
		}
	}

	public static boolean protect(Socket socket) {
		if (sVpnService != null) {
			return sVpnService.protect(socket);
		}
		return false;
	}

	public static boolean protect(DatagramSocket socket) {
		if (sVpnService != null) {
			return sVpnService.protect(socket);
		}
		return false;
	}

	public static boolean vpnRunningStatus() {
		if (sVpnService != null) {
			return sVpnService.vpnRunningStatus();
		}
		return false;
	}

	public static void changeVpnRunningStatus(Context context, boolean isStart) {
		if (context == null) {
			return;
		}
		if (isStart) {
			Intent intent = SkyNetVpnService.prepare(context);
			if (intent == null) {
				startVpnService(context);
			} else {
				if (context instanceof Activity) {
					((Activity) context).startActivityForResult(intent, START_VPN_SERVICE_REQUEST_CODE);
				}
			}
		} else {
			boolean stopStatus = false;
			sVpnService.setVpnRunningStatus(stopStatus);
		}
	}

	public static void startVpnService(Context context) {
		if (context == null) {
			return;
		}

		context.startService(new Intent(context, SkyNetVpnService.class));
	}
}
