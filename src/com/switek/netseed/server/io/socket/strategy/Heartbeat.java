package com.switek.netseed.server.io.socket.strategy;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.SessionManager;
import com.switek.netseed.server.bean.Controller;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;

public class Heartbeat extends CommStrategy{
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub

		String controllerId = packet.getControllerId();
		ServerForm.showLog(MsgType.Debug, "Received Heartbeat of "
				+ controllerId);

		// byte[] timestampBytes = packet.getCommandData();
		// long sendTimestamp = FormatTransfer.lBytesToLong(timestampBytes);
		// long spent = packet.getReceiveTime() - sendTimestamp;
		// if (warnTimeSpent==-1){
		// warnTimeSpent = Long.parseLong(Settings.getSetting("warnTimeSpent"));
		// }
		// if (spent >= warnTimeSpent){
		// ServerForm.warnMsg("Spent " + spent);
		// }
		//
		Controller controller = DeviceManager.getController(controllerId);
		if (controller != null) {
			ServerForm.showLog(MsgType.Debug, "Update heartbeat time for "
					+ controllerId);
			controller.setLastHeartbeatDT(System.currentTimeMillis());
		}

		ServerForm.showLog(MsgType.Debug,
				"Save the connection." + packet.getFromClient());
		SessionManager.keepConnection(controllerId, packet);
		
	}

}
