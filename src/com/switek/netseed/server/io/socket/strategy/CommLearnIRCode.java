package com.switek.netseed.server.io.socket.strategy;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.LearnIRCode;
import com.switek.netseed.server.bean.LearnIRCodeResult;
import com.switek.netseed.server.bean.PacketSendResult;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;
import com.switek.netseed.util.FormatTransfer;

public class CommLearnIRCode extends CommStrategy {
	private SendPacket sendPacket=new SendPacket();
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		String data = packet.getCommandDataString();

		JSONObject jsonObject = JSONObject.fromObject(data);
		JSONObject jsonBody = jsonObject.getJSONObject("Body");
		String controllerId = packet.getControllerId();
		byte subcontrollerId = packet.getExtensionId();
		String strSubcontrollerId = String.valueOf(subcontrollerId & 0xff);

		LearnIRCodeResult result = learnIRCode(controllerId,
				strSubcontrollerId, jsonBody, packet);
		int resultCode = result.getResultCode();
		String deviceId = result.getDeviceId();
		Map<String, Object> replyBody = new HashMap<>();
		replyBody.put("DeviceId", deviceId);
		sendPacket.sendPacket(packet, controllerId, subcontrollerId,
				SocketPacket.COMMAND_ID_AS_LEARN_IRCODE, resultCode, "1.0",
				replyBody);

	}
	
	private LearnIRCodeResult learnIRCode(String controllerId,
			String subcontrollerId, JSONObject jsonBody, SocketPacket packet) {
		String deviceId = jsonBody.getString("DeviceId");
		String deviceName = jsonBody.getString("DeviceName");
		String jsonData="";
		int deviceType = jsonBody.getInt("Type");
		byte byteDeviceType = (byte) deviceType;
		short keyIndex = (short) jsonBody.getInt("KeyIndex");
		if(jsonBody.containsKey("Json")){
			jsonData=jsonBody.getString("Json");
		}
		String brandCode = "OTHERS";
		int resultCode = 0;

		LearnIRCodeResult learnResult = new LearnIRCodeResult();
		learnResult.setDeviceId(deviceId);

		SubController subcontroller = DeviceManager.getSubcontroller(
				controllerId, subcontrollerId);
		if (subcontroller == null) {
			resultCode = ErrorCode.ERROR_INVALID_CONTROLLERID;
			ServerForm.warnMsg(String.format(
					"Invalid controller %s or subcontroller %s.", controllerId,
					subcontrollerId));
			learnResult.setResultCode(resultCode);
			return learnResult;
		}
		SocketPacket conn = connect2Controller(controllerId);
		if (conn == null) {
			resultCode = ErrorCode.ERROR_COULDNOT_CONNECT_CTRL;
			learnResult.setResultCode(resultCode);
			return learnResult;
		}
		int len = 1 + 2;
		byte[] cmdData = new byte[len];

		cmdData[0] = byteDeviceType;
		byte[] indexBytes = FormatTransfer.toLH(keyIndex);
		cmdData[1] = indexBytes[0];
		cmdData[2] = indexBytes[1];

		ServerForm.showLog(MsgType.Debug,
				"Sending the learn request to controller, waiting for ACK...");

		PacketSendResult result = sendPacket.sendPacket(conn, controllerId,
				packet.getExtensionId(),
				SocketPacket.COMMAND_ID_SC_LEARN_IRCODE, cmdData, true);
		// check the ack from controller
		SocketPacket feedbackPacket = result.getFeedbackPacket();
		if (!result.isSuccessful() || result.getFeedbackPacket() == null) {
			resultCode = ErrorCode.ERROR_NETWORK_ISSUE;
			learnResult.setResultCode(resultCode);
			return learnResult;
		}
		if (feedbackPacket.getCommandId() != SocketPacket.COMMAND_ID_SC_LEARN_IRCODE) {
			resultCode = ErrorCode.ERROR_NETWORK_ISSUE;
			learnResult.setResultCode(resultCode);
			return learnResult;
		}

		sendPacket.removeRepliedPacket(controllerId, subcontrollerId,
				SocketPacket.COMMAND_ID_SC_LEARN_IRCODE);

		ServerForm.showLog(MsgType.Debug, "Sending ACK to app...");
		PacketSendResult sendResult =sendPacket.sendPacketWithoutBody(packet,
				controllerId, packet.getExtensionId(),
				SocketPacket.COMMAND_ID_SA_LEARN_IRCODE_ACK, 0);
		if (sendResult == null || !sendResult.isSuccessful()) {
			resultCode = ErrorCode.ERROR_NETWORK_ISSUE;
			learnResult.setResultCode(resultCode);
			return learnResult;
		}

		try {
			ServerForm.showLog(MsgType.Debug,
					"Waiting for the IRCode from controller.");
			// read learned IR code from controller.
			SocketPacket packetLearned = sendPacket.waitRepliedPacket(controllerId,
					subcontrollerId, SocketPacket.COMMAND_ID_SC_LEARN_IRCODE);
			if (packetLearned.getCommandId() != SocketPacket.COMMAND_ID_SC_LEARN_IRCODE) {
				resultCode = ErrorCode.ERROR_READ_LEARNED_IRCODE;
				learnResult.setResultCode(resultCode);
				return learnResult;
			}

			LearnIRCode learnIRCode = LearnIRCode.parse(packetLearned
					.getCommandData());
			learnIRCode.setJsonData(jsonData);
			if (learnIRCode.getKeyIndex() != keyIndex) {
				resultCode = ErrorCode.ERROR_LEARNED_IRCODE_KEYINDEX_MISMATCH;
				learnResult.setResultCode(resultCode);
				return learnResult;
			}
			Device device;
			boolean isNewDevice = Device.isNewDevice(deviceId);
			if (isNewDevice) {
				// Auto create a device;
				device = new Device(subcontroller);

				device.setControllerId(controllerId);
				device.setSubcontrollerId(subcontrollerId);
				device.setDeviceName(deviceName);
				device.setDeviceType(deviceType);
				device.setBrandCode(brandCode);
				device.setIRCodeIndex(-1);
				device.setLearnedIRCode(true);
				

				if (deviceType == Device.DEVICE_TYPE_AC) {
					if (subcontroller.existsDevice(Device.DEVICE_TYPE_AC)) {
						ServerForm
								.warnMsg("The controller has an AC device already: "
										+ controllerId + "-" + subcontrollerId);
						resultCode = ErrorCode.ERROR_AC_ALREADY_EXISTS;
						learnResult.setResultCode(resultCode);
						return learnResult;
					}
				}

				ServerForm
						.showLog("Will create a device automatic for controller: "
								+ controllerId + "-" + subcontrollerId);
				boolean addResult = mDAL.addDevice(device);
				if (!addResult) {
					ServerForm.errorMsg("Failed to create device.");
					resultCode = ErrorCode.ERROR_FAILED_CREATE_DEVICE;
					learnResult.setResultCode(resultCode);
					return learnResult;
				}

				// cache the device
				subcontroller.addDevice(device.getDeviceId(), device);
				learnResult.setDeviceId(device.getDeviceId());
				ServerForm.showLog("The new device Id is: "
						+ device.getDeviceId());

			} else {
				device = getDevice(controllerId, subcontrollerId, deviceId);
				if (device == null) {
					resultCode = ErrorCode.ERROR_INVALID_DEVICEID;
					learnResult.setResultCode(resultCode);
					return learnResult;
				}
			}

			boolean exists = device.existsIRCode(keyIndex);
			boolean updateResult;
			if (!exists) {
				updateResult = mDAL.addLearnedIRCode(device, learnIRCode);
			} else {
				updateResult = mDAL.updateLearnedIRCode(device, learnIRCode);
			}
			if (!updateResult) {
				resultCode = ErrorCode.ERROR_FAILED_UPDATEDB;
				learnResult.setResultCode(resultCode);
				return learnResult;
			}
			// update cached IR code.
			device.putIRCode(keyIndex, learnIRCode);
		} catch (Exception e) {
			ServerForm.showLog(e);
			resultCode = ErrorCode.ERROR_READ_LEARNED_IRCODE;
		}

		learnResult.setResultCode(resultCode);
		return learnResult;
		
	}
	
	
}
