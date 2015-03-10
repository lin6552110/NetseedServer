package com.switek.netseed.server.io.socket.strategy;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.SessionManager;
import com.switek.netseed.server.Utils;
import com.switek.netseed.server.bean.Controller;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;

public class ControllerTestingNetwork extends CommStrategy{
	
	private SendPacket send=new SendPacket();
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		String Id = packet.getControllerId();
		ServerForm.showLog(MsgType.Info, "Controller: " + Id
				+ " is registering.");

		if (!registerController(packet)) {
			return;
		}

		send.sendPacket(packet, packet.getControllerId(), packet.getExtensionId(),
				SocketPacket.COMMAND_ID_CS_TESTING_NETWORK, null);
		
	}
	
	private boolean registerController(SocketPacket packet) {

		// check whether registered
		String Id = packet.getControllerId();
		Controller controller = DeviceManager.getController(Id);
		String macAddress = Utils.bytes2HexString2(packet.getCommandData());
		boolean isExists = (controller != null);
		boolean result = mDAL.registerController(Id, Id, macAddress, isExists);

		if (result) {
			if (!isExists) {
				controller = new Controller();
				controller.setControllerId(Id);
				controller.setControllerName(String.valueOf(Id));
				SubController subController = new SubController(controller);
				subController
						.setControllerId(Controller.DEFAULT_SUB_CONTROLLER_ID);
				subController.setControllerName(Id);
				controller.addSubController(
						Controller.DEFAULT_SUB_CONTROLLER_ID, subController);
				DeviceManager.addController(controller);
			}
			controller.setLastHeartbeatDT(System.currentTimeMillis());
			controller.setMacAddress(macAddress);

			SessionManager.keepConnection(Id, packet);
		}

		if (!result) {
			ServerForm.errorMsg("Failed to register the controller.");
		}
		return result;

	}
}
