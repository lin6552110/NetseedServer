package com.switek.netseed.server.io.socket.strategy;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.Controller;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;
import com.switek.netseed.server.ui.ServerForm;

public class CommControllerReplied extends CommStrategy{
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		ServerForm.debugMsg("Save replied packet." + packet.getCommandId());
		saveRepliedPacket(packet);
		
	}
	
	private void saveRepliedPacket(SocketPacket packet) {
		String key;
		if (packet.getCommandId() == SocketPacket.COMMAND_ID_SC_CONFIG_SUBCONTROLLER
				|| packet.getCommandId() == SocketPacket.COMMAND_ID_SC_REMOVE_SUBCONTROLLER) {
			key = packet.getControllerId() + "_" + packet.getCommandId();
		} else {
			key = packet.getControllerId() + "_"
					+ packet.getExtensionIdString() + "_"
					+ packet.getCommandId();
		}
		SendPacket.mRepliedPackets.put(key, packet);
		Controller controller = DeviceManager.getController(packet
				.getControllerId());
		if (controller != null) {
			controller.setLastHeartbeatDT(System.currentTimeMillis());
		}
	}

}
