package com.switek.netseed.server.io.socket.strategy;

import net.sf.json.JSONObject;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.ActionResult;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;
import com.switek.netseed.server.ui.ServerForm;

public class CommAddNetseedDevice extends CommStrategy{
	
	private SendPacket send=new SendPacket();
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		ActionResult result = addNetseedDevice(packet);
		send.sendPacketWithoutBody(packet, packet.getControllerId(), packet.getExtensionId(),
				packet.getCommandId(), result.getResultCode());
	}
	
	public ActionResult addNetseedDevice(SocketPacket packet){
		
		ActionResult result = new ActionResult();

		JSONObject jsonBody = packet.getJsonBody();

		String controllerId = packet.getControllerId();
		String subcontrollerId = packet.getExtensionIdString();
		String deviceId = jsonBody.getString("DeviceId");
		SubController subcontroller = DeviceManager.getSubcontroller(controllerId, subcontrollerId);
		if (subcontroller==null){			
			result.setResultCode(ErrorCode.ERROR_INVALID_SUBCONTROLLERID);
			return result;
		}
		Device device = getDevice(controllerId, subcontrollerId, deviceId);	
		int resultCode = 0;
		if (device != null) {
			resultCode = ErrorCode.ERROR_INVALID_DEVICEID;
			result.setResultCode(resultCode);
			return result;
		}
		
		device = new Device(subcontroller);
		String deviceName = jsonBody.getString("DeviceName");
		int deviceType = jsonBody.getInt("Type");
		int deviceIndex = jsonBody.getInt("DeviceIndex");
		if(jsonBody.containsKey("CircuitCount")){
			int circuitCount = jsonBody.getInt("CircuitCount");
			device.setCircuitCount(circuitCount);
		}
		if(jsonBody.containsKey("BrandCode")){
			String brandCode = jsonBody.getString("BrandCode");
			device.setBrandCode(brandCode);
		}
		if(jsonBody.containsKey("Status")){
			String status=jsonBody.getString("Status");
			device.setStatus(status);
		}

		device.setControllerId(controllerId);
		device.setSubcontrollerId(subcontrollerId);
		device.setDeviceId(deviceId);
		device.setDeviceName(deviceName);
		device.setDeviceType(deviceType);
		device.setDeviceIndex(deviceIndex);
		device.setIRCodeIndex(-1);
		device.setLearnedIRCode(false);
		ServerForm
		.showLog(String.format("Will create a device %s for controller: %s / %s.", deviceId, controllerId, subcontrollerId));
		
		boolean addResult = mDAL.addDevice(device);
		if (!addResult) {
			ServerForm.errorMsg("Failed to create device.");
			resultCode = ErrorCode.ERROR_FAILED_CREATE_DEVICE;
			result.setResultCode(resultCode);
			return result;
		}
		// cache the device
		subcontroller.addDevice(device.getDeviceId(), device);
		return result;		
	}
	
	
}
