package com.switek.netseed.server.io.socket.strategy;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.Brand;
import com.switek.netseed.server.bean.Controller;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.DeviceTypeDef;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.IRCode;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;
import com.switek.netseed.util.MyBase64;

public class CommGetDevices extends CommStrategy{
	
	private SendPacket send=new SendPacket();
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
	
		String controllerId = packet.getControllerId();
		byte subcontrollerId = packet.getExtensionId();
		String strSubcontrollerId = String.valueOf(subcontrollerId & 0xff);
		boolean isAllSubcontrollers = (subcontrollerId == (byte) 0xff);

		Controller controller = DeviceManager.getController(controllerId);
		List<Map<String, Object>> replyBody = new ArrayList<>();

		String data = packet.getCommandDataString();

		JSONObject jsonObject = JSONObject.fromObject(data);
		JSONObject jsonBody = jsonObject.getJSONObject("Body");
		long lastUpdateDT = jsonBody.getLong("LatestIRCodeDT");

		int resultCode = 0;
		if (controller != null) {
			boolean validSubcontrollerId = isAllSubcontrollers;
			Hashtable<String, SubController> subcontrollers = controller
					.getSubControllers();
			Enumeration<SubController> en = subcontrollers.elements();
			while (en.hasMoreElements()) {
				SubController subcontroller = en.nextElement();
				if (isAllSubcontrollers
						|| subcontroller.getControllerId().equals(
								strSubcontrollerId)) {
					if (!isAllSubcontrollers
							&& subcontroller.getControllerId().equals(
									strSubcontrollerId)) {
						validSubcontrollerId = true;
					}
					Enumeration<Device> enDevice = subcontroller.getDevices()
							.elements();
					while (enDevice.hasMoreElements()) {
						Device device = enDevice.nextElement();
						Map<String, Object> replyBodyMap = new HashMap<>();
						replyBodyMap.put("ExtId",
								subcontroller.getControllerId());
						int deviceType = device.getDeviceType();
						String brandCode = device.getBrandCode();
						DeviceTypeDef def = DeviceManager
								.getDeviceTypeDef(deviceType);
						String deviceTypeName = "UNKNOWN";
						String brandName = "UNKNOWN";
						if (def != null) {
							deviceTypeName = def.getName();
							Brand brand = def.getBrand(brandCode);
							if (brand != null) {
								brandName = brand.getBrandName();
							}
						}
						replyBodyMap.put("DeviceId", device.getDeviceId());
						replyBodyMap.put("DeviceName", device.getDeviceName());
						replyBodyMap.put("DeviceType", deviceType);
						replyBodyMap.put("DeviceIndex", device.getDeviceIndex());
						replyBodyMap.put("DeviceTypeName", deviceTypeName);
						replyBodyMap.put("BrandCode", brandCode);
						replyBodyMap.put("BrandName", brandName);
						replyBodyMap.put("CircuitCount", device.getCircuitCount());
						replyBodyMap.put("Json", device.getJsonData());
						
						// replyBodyMap.put("IsLearnedIRCode",
						// device.isLearnedIRCode());
						replyBodyMap
								.put("IRCodeIndex", device.getIRCodeIndex());
						String standardIRCode = "";
						boolean isIRCodeUpdated = false;
						if (device.getStandardIRCode() != null
								&& device.getLastEditDT() > lastUpdateDT) {
							isIRCodeUpdated = true;

							IRCode irCode = device.getStandardIRCode();
							byte[] codeData = null;
							if (irCode == null) {
								codeData = new byte[0];
								logger.warn("NULL IRCODE");
							} else {
								codeData = irCode.getIRCodeData();
							}

							standardIRCode = MyBase64.encode(codeData);
							replyBodyMap.put("StandardIRCodeUpdateDT",
									device.getLastEditDT());
						}
						replyBodyMap.put("StandardIRCode", standardIRCode);

						List<Map<String, Object>> IRCodeNodeList = new ArrayList<>();
						if (device.getDeviceType() != 0) {
							// if (!device.isLearnedIRCode()) {
							// if (device.getLastEditDT() > lastUpdateDT) {
							// isIRCodeUpdated = true;
							// Map<String, Object> IRCodeMap = new HashMap<>();
							//
							// IRCodeMap.put("KeyIndex",
							// device.getIRCodeIndex());
							//
							// IRCode irCode =
							// DeviceManager.getStandardIRCode(device.getDeviceType(),
							// device.getBrandCode(),
							// device.getIRCodeIndex());
							//
							// byte[] codeData = null;
							// if (irCode == null) {
							// codeData = new byte[0];
							// logger.warn("NULL IRCODE");
							// } else {
							// codeData = irCode.getIRCodeData();
							// }
							//
							// IRCodeMap.put("IRCode",
							// MyBase64.encode(codeData));
							//
							// IRCodeMap.put("LastEditDT",
							// device.getLastEditDT());
							// IRCodeNodeList.add(IRCodeMap);
							// }
							// } else {
							Enumeration<IRCode> enIRCode = device
									.getLearnedIRCodes().elements();
							while (enIRCode.hasMoreElements()) {
								IRCode irCode = enIRCode.nextElement();
								if (irCode.getLastEditDT() > lastUpdateDT) {
									isIRCodeUpdated = true;
									Map<String, Object> IRCodeMap = new HashMap<>();
									IRCodeMap.put("LastEditDT",
											irCode.getLastEditDT());
									IRCodeMap.put("KeyIndex",
											irCode.getKeyIndex());
									IRCodeMap.put("IRCode", MyBase64
											.encode(irCode.getIRCodeData()));
									IRCodeMap.put("CodeType", irCode.getCodeType());
									IRCodeMap.put("Json", irCode.getJsonData());
									IRCodeNodeList.add(IRCodeMap);
								}
							}
							// }
						}

						replyBodyMap.put("IRCode", IRCodeNodeList);

						replyBodyMap.put("IRCodeUpdated", isIRCodeUpdated);
						replyBody.add(replyBodyMap);
					}
				}
			}

			if (!validSubcontrollerId) {
				resultCode = ErrorCode.ERROR_INVALID_SUBCONTROLLERID;
			}
		} else {
			resultCode = ErrorCode.ERROR_INVALID_CONTROLLERID;
		}

		send.sendPacket(packet, controllerId, subcontrollerId,
				SocketPacket.COMMAND_ID_GET_DEVICES, resultCode, "1.0",
				replyBody);
	}

}
