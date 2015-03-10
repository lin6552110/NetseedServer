package com.switek.netseed.server.io.socket.strategy;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.ActionResult;
import com.switek.netseed.server.bean.Controller;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.PacketSendResult;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;

public class CommControlNetseedDevice extends CommStrategy{
	
	private SendPacket send=new SendPacket();
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		String controllerId = packet.getControllerId();
		ServerForm.showLog(MsgType.Debug,
				"Try to control device of the contoller " + controllerId);

		String subcontrollerId = packet.getExtensionIdString();
		JSONObject jsonBody = packet.getJsonBody();
		String deviceId = jsonBody.getString("DeviceId");
		int circuitNo = jsonBody.getInt("CircuitNo");
		int value = jsonBody.getInt("Value");

		ActionResult result = controlNetseedDevice(controllerId, subcontrollerId,
				deviceId,  circuitNo, value);
		int resultCode = result.getResultCode();
		ServerForm.showLog(MsgType.Debug, "Result code: " + resultCode);
		if (resultCode != ErrorCode.NO_ERROR){
			send.sendPacketWithoutBody(packet, controllerId, packet.getExtensionId(),
					SocketPacket.COMMAND_ID_CONTROL_DEVICE, resultCode);
			}else{
			SocketPacket feedback = (SocketPacket) result.getOutputObject();
			
			byte[] body = feedback.getCommandData();
			circuitNo = body[7] & 0xff;
			value = body[8] & 0xff;		
		
			Map<String, Object> replyBody = new HashMap<>();		
			replyBody.put("CircuitIndex", circuitNo);
			replyBody.put("CircuitValue", value);

			send.sendPacket(packet, packet.getControllerId(), packet.getExtensionId(),
					SocketPacket.COMMAND_ID_CONTROL_DEVICE, result.getResultCode(), "1.0", replyBody);
		}
	}
	
	public  ActionResult controlNetseedDevice(String controllerId,
			String subcontrollerId, String deviceId, int circuitNo, int value) {
		SubController subcontroller = DeviceManager.getSubcontroller(
				controllerId, subcontrollerId);

		ActionResult actionResult = new ActionResult();
		if (subcontroller == null) {
			actionResult.setResultCode(ErrorCode.ERROR_INVALID_CONTROLLERID);
			return actionResult;
		}
		Device device = subcontroller.getDevice(deviceId);
		if (device == null) {
			actionResult.setResultCode(ErrorCode.ERROR_INVALID_DEVICEID);
			return actionResult;
		}

		int deviceType = device.getDeviceType();
		byte[] deviceIdBytes = Controller
				.convertControllerId2Bytes(deviceId);
		
		/*
		AA,AA,  16,00,  DE,07,03,04,00,00,  00,  0F,00,0C（设备类型ID）, (六个字节的设备ID数据)，03（回路编号），00（设备功能状态），??,??
		 */		
		int len = 1 + 6 + 1 + 1;
		byte[] cmdDataBytes = new byte[len];
		cmdDataBytes[0] = (byte) deviceType;		
		
		//device Id
		cmdDataBytes[1] = deviceIdBytes[0];
		cmdDataBytes[2] = deviceIdBytes[1];
		cmdDataBytes[3] = deviceIdBytes[2];
		cmdDataBytes[4] = deviceIdBytes[3];
		cmdDataBytes[5] = deviceIdBytes[4];
		cmdDataBytes[6] = deviceIdBytes[5];		
		
		cmdDataBytes[7] = (byte)circuitNo;
		
		cmdDataBytes[8] = (byte)value;
		
		
		/*
		主控器返回的数据为：
		A A,AA,  16,00,  DE,07,03,04,00,00,  00,  0F,00,0C（设备类型ID）, (六个字节的设备ID数据)，03（回路编号），00（设备功能状态），??,??
		 */
		SocketPacket conn = connect2Controller(controllerId);
		if (conn == null) {
			ServerForm.showLog(MsgType.Debug,
					"Couldn't connect to controller: " + controllerId);
			actionResult.setResultCode(ErrorCode.ERROR_COULDNOT_CONNECT_CTRL);
			return actionResult;
		}
		
		short commandId = SocketPacket.COMMAND_ID_SC_CONTROL_NETSEED_DEVICE;		

		PacketSendResult result = send.sendPacket(conn, controllerId,
				Byte.valueOf(subcontrollerId), commandId, cmdDataBytes, true);
		if (!result.isSuccessful()) {
			actionResult.setResultCode(ErrorCode.ERROR_NETWORK_ISSUE);
			return actionResult;
		}

		actionResult.setOutputObject(result.getFeedbackPacket());
		return actionResult;
	}
}
