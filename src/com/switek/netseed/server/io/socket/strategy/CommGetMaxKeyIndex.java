package com.switek.netseed.server.io.socket.strategy;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import net.sf.json.JSONObject;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.ActionResult;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.IRCode;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;

public class CommGetMaxKeyIndex extends CommStrategy{
	
	private SendPacket send=new SendPacket();
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		ActionResult result = getMaxKeyIndex(packet);
		Map<String, Object> replyBody = new HashMap<>();
		if(result.isSuccessful()){
			replyBody.put("MaxKeyIndex", result.getOutputObject());
		}		

		send.sendPacket(packet, packet.getControllerId(), packet.getExtensionId(),
				packet.getCommandId(), result.getResultCode(), "1.0", replyBody);		
	}
	
	private ActionResult getMaxKeyIndex(SocketPacket packet) {
		ActionResult result = new ActionResult();
		String controllerId = packet.getControllerId();
		String subcontrollerId = packet.getExtensionIdString();
				
		JSONObject body = packet.getJsonBody();
		String deviceId = body.getString("DeviceId");
		SubController  subctrl = DeviceManager.getSubcontroller(controllerId, subcontrollerId);
		if(subctrl ==null){
			result.setResultCode(ErrorCode.ERROR_INVALID_SUBCONTROLLERID);
			return result;
		}
		Device device = subctrl.getDevice(deviceId);
		if (device == null){
			result.setResultCode(ErrorCode.ERROR_INVALID_DEVICEID);
			return result;
		}
		
		Hashtable<Integer, IRCode> codes = device.getLearnedIRCodes();
		Enumeration<IRCode> en = codes.elements();

		int maxKeyIndex = -1;
		while (en.hasMoreElements()) {
			IRCode code = en.nextElement();
			if (code.getKeyIndex() > maxKeyIndex){
				maxKeyIndex = code.getKeyIndex();
			}
		}
		result.setOutputObject(maxKeyIndex);
		return result;
	}

}
