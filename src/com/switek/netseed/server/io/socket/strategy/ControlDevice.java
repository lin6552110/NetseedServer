package com.switek.netseed.server.io.socket.strategy;

import net.sf.json.JSONObject;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.IRCode;
import com.switek.netseed.server.bean.PacketSendResult;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;
import com.switek.netseed.util.FormatTransfer;

public class ControlDevice extends CommStrategy{
	
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
		int keyIndex = jsonBody.getInt("KeyIndex");

		int resultCode = controlDevice(controllerId, subcontrollerId,
				deviceId, keyIndex);
		ServerForm.showLog(MsgType.Debug, "Result code: " + resultCode);
		send.sendPacketWithoutBody(packet, controllerId, packet.getExtensionId(),
				SocketPacket.COMMAND_ID_CONTROL_DEVICE, resultCode);
	}
	
	public  int controlDevice(String controllerId,
			String subcontrollerId, String deviceId, int keyIndex) {
		SubController subcontroller = DeviceManager.getSubcontroller(
				controllerId, subcontrollerId);

		if (subcontroller == null) {
			return ErrorCode.ERROR_INVALID_CONTROLLERID;
		}
		Device device = subcontroller.getDevice(deviceId);
		if (device == null) {
			return ErrorCode.ERROR_INVALID_DEVICEID;
		}

		int deviceType = device.getDeviceType();
		// boolean islearnedIRCode = device.isLearnedIRCode();

		// IRCode irCode;
		// if (!islearnedIRCode) {
		// String brandCode = device.getBrandCode();
		// StandardIRCode standardIRCode = DeviceManager.getStandardIRCode(
		// deviceType, brandCode);
		// if (standardIRCode == null) {
		// return ErrorCode.ERROR_COULDNOT_FIND_IRCODE;
		// }
		// int codeIndex = device.getIRCodeIndex();
		// irCode = standardIRCode.getIRCode(codeIndex);
		// } else {
		// irCode = device.getLearnedIRCode(keyIndex);
		// }
		//
		IRCode irCode = device.getLearnedIRCode(keyIndex);
		if (irCode == null && device.getStandardIRCode() != null) {
			irCode = device.getStandardIRCode();
		}
		if (irCode == null) {
			return ErrorCode.ERROR_COULDNOT_FIND_IRCODE;
		}

		byte[] codeData = irCode.getIRCodeData();
		int len = 1 + 2 + codeData.length;
		byte[] cmdDataBytes = new byte[len];
		cmdDataBytes[0] = (byte) deviceType;
		byte[] keyIndexBytes = FormatTransfer.toLH((short) keyIndex);
		cmdDataBytes[1] = keyIndexBytes[0];
		cmdDataBytes[2] = keyIndexBytes[1];
		for (int i = 0; i < codeData.length; i++) {
			cmdDataBytes[3 + i] = codeData[i];
		}

		/*
		 * APP向主控器发出一个控制设备某个功能的操作操作码：0x0007 假设控制从属设备为01，控制空调关机功能（数据为学习得到的数据）
		 * AA,AA, ??,??, DE,07,03,04,01,00， 01， 07,00，01(空调ID)，01,00（空调功能键编号）
		 * 01,02,03…（学习数据（40-500字节），？？，？？（校验和） 主控器收到数据无误后，回应APP AA,AA, 10,00,
		 * DE,07,03,04,01,00， 01， 07,00，01(空调ID)，01,00（空调功能键编号）？？，？？（校验和）
		 * 
		 * 数据库的关联关系： 学习码数据库 → 设备（空调、电视、机顶盒、DVD、其它）→ 品牌（格力、美的…） →
		 * 学习码组编号(0001、0002、0003…) → 每一组学习码功能键编号（0001、0002、0003…）→
		 * 学习数据（40-500BYTE）。 固定码数据库 → 设备（空调、电视、机顶盒、DVD、其它）→ 品牌（格力、美的…）→
		 * 固定码组编号（0001、0002、0003…）→ 固定码数据（40-500BYTE）"
		 */

		SocketPacket conn = connect2Controller(controllerId);
		if (conn == null) {
			ServerForm.showLog(MsgType.Debug,
					"Couldn't connect to controller: " + controllerId);
			return ErrorCode.ERROR_COULDNOT_CONNECT_CTRL;
		}
		
		//int codeType = irCode.getCodeType();
		short commandId = 0;
		if (irCode.getCodeType() == IRCode.CODE_TYPE_IR){
			commandId = SocketPacket.COMMAND_ID_SC_CONTROL_DEVICE;
		} else{
			commandId = SocketPacket.COMMAND_ID_SC_CONTROL_RFDEVICE;
			
			cmdDataBytes[1] = (byte)keyIndex;
			cmdDataBytes[2] = (byte)device.getDeviceIndex();
		}

		PacketSendResult result = send.sendPacket(conn, controllerId,
				Byte.valueOf(subcontrollerId), commandId, cmdDataBytes, true);
		if (!result.isSuccessful()) {
			return ErrorCode.ERROR_NETWORK_ISSUE;
		}

		return ErrorCode.NO_ERROR;

	}

}
