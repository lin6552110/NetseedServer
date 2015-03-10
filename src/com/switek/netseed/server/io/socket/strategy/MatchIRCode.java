package com.switek.netseed.server.io.socket.strategy;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.IRCode;
import com.switek.netseed.server.bean.PacketSendResult;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.StandardIRCode;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;
import com.switek.netseed.util.FormatTransfer;

public class MatchIRCode extends CommStrategy{
	
	private SendPacket send=new SendPacket();
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
	
		String data = packet.getCommandDataString();
		JSONObject jsonObject = JSONObject.fromObject(data);
		JSONObject jsonBody = jsonObject.getJSONObject("Body");
		String deviceType = jsonBody.getString("Type");
		String brandCode = jsonBody.getString("BrandCode");
		// String deviceId = jsonBody.getString("DeviceId");
		int keyIndex = jsonBody.getInt("KeyIndex");
		short codeIndex = (short) jsonBody.getInt("IRCodeIndex");
		String key = (deviceType + "_" + brandCode).toUpperCase();
		StandardIRCode standardIRCode = DeviceManager.getStandardIRCode(key);
		String controllerId = packet.getControllerId();
		byte subcontrollerId = packet.getExtensionId();
		IRCode ircode = null;
		int resultCode = 0;
		if (standardIRCode == null) {
			resultCode = ErrorCode.ERROR_NO_IRCODE;
		} else {
			ircode = standardIRCode.getIRCode(codeIndex);
			if (ircode == null) {
				resultCode = ErrorCode.ERROR_NO_IRCODE;
			} else {
				SocketPacket conn = connect2Controller(controllerId);
				if (conn == null) {
					resultCode = ErrorCode.ERROR_COULDNOT_CONNECT_CTRL;
				} else {
					int len = 1 + 2 + ircode.getIRCodeData().length;
					byte[] cmdData = new byte[len];
					byte byteDeviceType = Byte.valueOf(deviceType);
					cmdData[0] = byteDeviceType;
					byte[] indexBytes = FormatTransfer.toLH(keyIndex);
					cmdData[1] = indexBytes[0];
					cmdData[2] = indexBytes[1];
					for (int i = 0; i < ircode.getIRCodeData().length; i++) {
						cmdData[3 + i] = ircode.getIRCodeData()[i];
					}

					PacketSendResult result = send.sendPacket(conn, controllerId,
							subcontrollerId,
							SocketPacket.COMMAND_ID_SC_MATCH_IRCODE, cmdData);
					if (!result.isSuccessful()) {
						ServerForm
								.showLog(MsgType.Warn,
										"Error occurs while communicating to controller.");
						resultCode = ErrorCode.ERROR_NETWORK_ISSUE;
					}
				}
			}
		}

		Map<String, Object> replyMap = new HashMap<>();

		String nextIndex = "-1";
		if (ircode != null) {
			if (ircode.getNext() != null) {
				nextIndex = String.valueOf(ircode.getNext().getKeyIndex());
			}
		}
		replyMap.put("NextIRCodeIndex", nextIndex);
		send.sendPacket(packet, controllerId, subcontrollerId,
				SocketPacket.COMMAND_ID_MATCH_IRCODE, resultCode, "1.0",
				replyMap);
	}
}
