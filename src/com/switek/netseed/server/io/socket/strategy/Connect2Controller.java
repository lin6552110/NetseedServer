package com.switek.netseed.server.io.socket.strategy;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;

public class Connect2Controller extends CommStrategy{

	private SendPacket send=new SendPacket();
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		String controllerId = packet.getControllerId();
		String subcontrollerId = packet.getExtensionIdString();

		SubController subcontroller = DeviceManager.getSubcontroller(
				controllerId, subcontrollerId);
		int resultCode = 0;

		Map<String, Object> replyBody = new HashMap<>();
		if (!getPacketAPIVersion(packet).equals("1.1")) {
			resultCode = ErrorCode.ERROR_NEWVERSION_APP_REQUIRED;
		} else {

			if (subcontroller == null) {
				resultCode = ErrorCode.ERROR_INVALID_SUBCONTROLLERID;
			} else {
				SocketPacket conn = connect2Controller(controllerId);

				boolean connected = (conn != null);
				long lastPressedKeyIndex = -1;
				// String status = "";
				// String description = "";
				if (connected) {
					JSONObject bodyJsonObject = packet.getJsonBody();
					int deviceType = bodyJsonObject.getInt("DeviceType");
					int deviceIndex = 0;
					if (bodyJsonObject.containsKey("DeviceIndex")){
						deviceIndex = bodyJsonObject.getInt("DeviceIndex");
					}
					Device device = subcontroller.getDeviceByType(deviceType, deviceIndex);
					if (device != null) {
						lastPressedKeyIndex = device.getLastPressedKey();
					}

					// status = "00";
					ServerForm.showLog(MsgType.Debug,
							"Connected to controller " + controllerId);
				} else {
					resultCode = ErrorCode.ERROR_COULDNOT_CONNECT_CTRL;
					// status = "01";
					ServerForm.showLog(MsgType.Warn,
							"Couldn't connect to controller: " + controllerId);
				}

				// replyBody.put("ControllerId", controllerId);
				// replyBody.put("Status", status);
				// replyBody.put("Description", description);
				replyBody.put("LastPressedKeyIndex", lastPressedKeyIndex);
			}
		}
		send.sendPacket(packet, controllerId, (byte) 0,
				SocketPacket.COMMAND_ID_CONNECT_CTR, resultCode, "1.0",
				replyBody);
		
	}
}
