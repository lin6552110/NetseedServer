package com.switek.netseed.server.io.socket.strategy;

import java.util.HashMap;
import java.util.Map;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.ActionResult;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.PacketSendResult;
import com.switek.netseed.server.bean.SensorData;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;

public class CommRequestSensorData extends CommStrategy{
	
	private SendPacket send=new SendPacket();
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		String controllerId = packet.getControllerId();
		byte subcontrollerId = packet.getExtensionId();

		ActionResult result = requestSensorData(controllerId, subcontrollerId,
				packet);
		int resultCode = result.getResultCode();
		SensorData sensorData = (SensorData) result.getOutputObject();
		Map<String, Object> replyBody = new HashMap<>();
		if (sensorData != null) {
			replyBody.put("Temperature", sensorData.getTemperature());
			replyBody.put("Humidity", sensorData.getHumidity());
			replyBody.put("LastEditDT", sensorData.getLastEditDT());
		}
		send.sendPacket(packet, controllerId, subcontrollerId,
				SocketPacket.COMMAND_ID_REQUEST_SENSOR_DATA, resultCode, "1.0",
				replyBody);
	}
	
	private ActionResult requestSensorData(String controllerId,
			byte subcontrollerId, SocketPacket packet) {
		ActionResult result = new ActionResult();
		SubController subcontroller = DeviceManager.getSubcontroller(
				controllerId, subcontrollerId);
		int resultCode;
		if (subcontroller == null) {
			resultCode = ErrorCode.ERROR_INVALID_SUBCONTROLLERID;
			result.setResultCode(resultCode);
			ServerForm.warnMsg(String.format(
					"Invalid controller or subcontroller %s, %s.",
					controllerId, String.valueOf(subcontrollerId & 0xff)));
			return result;
		}
		SocketPacket conn = connect2Controller(controllerId);
		if (conn == null) {
			resultCode = ErrorCode.ERROR_COULDNOT_CONNECT_CTRL;
			result.setResultCode(resultCode);
			return result;
		}

		ServerForm
				.showLog(MsgType.Debug,
						"Sending the request to sensor and waiting for the feedback...");
		PacketSendResult sendResult = send.sendPacket(conn, controllerId,
				subcontrollerId,
				SocketPacket.COMMAND_ID_SC_REQUEST_SENSOR_DATA, null, true);
		// check the ack from controller
		SocketPacket feedbackPacket = sendResult.getFeedbackPacket();
		if (!sendResult.isSuccessful()
				|| sendResult.getFeedbackPacket() == null) {
			resultCode = ErrorCode.ERROR_NETWORK_ISSUE;
			result.setResultCode(resultCode);
			return result;
		}

		if (feedbackPacket.getCommandId() != SocketPacket.COMMAND_ID_SC_REQUEST_SENSOR_DATA) {
			resultCode = ErrorCode.ERROR_NETWORK_ISSUE;
			result.setResultCode(resultCode);
			return result;
		}

		SensorData sensorData = null;
		try {
			sensorData = SensorData.parse(feedbackPacket.getCommandData());
			result.setOutputObject(sensorData);
		} catch (Exception e) {
			resultCode = ErrorCode.ERROR_READ_SENSOR_DATA;
			result.setResultCode(resultCode);
			return result;
		}

		return result;
	}
	
}
