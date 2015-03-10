package com.switek.netseed.server.io.socket.strategy;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.ActionResult;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;
import com.switek.netseed.util.MyBase64;

public class SubmitIRCodeMatch extends CommStrategy{
	private SendPacket send=new SendPacket();
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		ActionResult result = submitIRCodeMatch(packet);
		int resultCode = result.getResultCode();
		Map<String, Object> replyBody = new HashMap<>();
		String deviceId = "";
		String strIRCode = "";
		if (result.getOutputObject() != null) {
			Device device = (Device) result.getOutputObject();
			deviceId = device.getDeviceId();
			if (device.getStandardIRCode() !=null){
				strIRCode = MyBase64.encode(device.getStandardIRCode().getIRCodeData());
			}
		}
		replyBody.put("DeviceId", deviceId);
		replyBody.put("IRCode", strIRCode);

		send.sendPacket(packet, packet.getControllerId(), packet.getExtensionId(),
				SocketPacket.COMMAND_ID_SUBMIT_MATCH_IRCODE, resultCode, "1.0",
				replyBody);
	}
	
	/**
	 * The device is matched.
	 * 
	 * @param packet
	 */
	private ActionResult submitIRCodeMatch(SocketPacket packet) {
		ActionResult result = new ActionResult();
		String data = packet.getCommandDataString();
		String jsonData="";
		JSONObject jsonObject = JSONObject.fromObject(data);
		JSONObject jsonBody = jsonObject.getJSONObject("Body");
		String deviceId = jsonBody.getString("DeviceId");
		String deviceName = jsonBody.getString("Name");
		short codeIndex = (short) jsonBody.getInt("IRCodeIndex");
		int deviceType = jsonBody.getInt("Type");
		String brandCode = jsonBody.getString("BrandCode");
		if(jsonBody.containsKey("Json")){
			jsonData=jsonBody.getString("Json");
		}
		String extId = packet.getExtensionIdString();
		boolean isNewDevice = deviceId.equalsIgnoreCase(Device.DEVICE_ID_NEW);
		SubController subcontroller = DeviceManager.getSubcontroller(
				packet.getControllerId(), extId);
		int resultCode = ErrorCode.NO_ERROR;

		if (subcontroller == null) {
			resultCode = ErrorCode.ERROR_INVALID_CONTROLLERID;
			result.setResultCode(resultCode);
			return result;
		}

		Device device = null;

		boolean updateResult = false;
		if (isNewDevice) {

			if (deviceType == Device.DEVICE_TYPE_AC) {
				if (subcontroller.existsDevice(Device.DEVICE_TYPE_AC)) {
					ServerForm
							.warnMsg("The controller has an AC device already: "
									+ packet.getControllerId() + "-" + extId);
					resultCode = ErrorCode.ERROR_AC_ALREADY_EXISTS;
					result.setResultCode(resultCode);
					return result;
				}
			}

			ServerForm
					.showLog("Will create a device automatic for controller: "
							+ packet.getControllerId() + "-" + subcontroller);
			device = new Device(subcontroller);
			device.setControllerId(packet.getControllerId());
			device.setSubcontrollerId(extId);
			device.setDeviceName(deviceName);
			device.setDeviceType(deviceType);
			device.setBrandCode(brandCode);
			device.setIRCodeIndex(codeIndex);
			device.setLearnedIRCode(false);
			device.setJsonData(jsonData);
			
			updateResult = mDAL.addDevice(device);
			if (updateResult) {
				deviceId = device.getDeviceId();
				ServerForm.showLog(MsgType.Info, "The new device Id: "
						+ deviceId);
				subcontroller.addDevice(deviceId, device);
			}
		} else {
			device = subcontroller.getDevice(deviceId);
			if (device == null) {
				resultCode = ErrorCode.ERROR_INVALID_DEVICEID;
				result.setResultCode(resultCode);
				return result;
			}

			ServerForm.showLog("Update device: " + device);

			device.setControllerId(packet.getControllerId());
			device.setSubcontrollerId(extId);
			device.setDeviceName(deviceName);
			device.setDeviceType(deviceType);
			device.setBrandCode(brandCode);
			device.setIRCodeIndex(codeIndex);
			device.setLearnedIRCode(false);
			device.setJsonData(jsonData);
			
			updateResult = mDAL.updateDevice(device);
		}
		if (!updateResult) {
			resultCode = ErrorCode.ERROR_FAILED_UPDATEDB;
			ServerForm.errorMsg("Failed to add device.");
			result.setResultCode(resultCode);
			return result;
		}
		
		result.setResultCode(resultCode);
		result.setOutputObject(device);
		return result;

	}
}
