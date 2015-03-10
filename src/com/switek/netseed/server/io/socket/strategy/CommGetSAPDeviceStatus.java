package com.switek.netseed.server.io.socket.strategy;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.Controller;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;

public class CommGetSAPDeviceStatus extends CommStrategy{
	private SendPacket send=new SendPacket();
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		List<Map<String,Object>> replyBody=new ArrayList();
		JSONObject jsonBody=JSONObject.fromObject(packet.getCommandDataString());
		JSONArray array=jsonBody.getJSONArray("Body");
		for(int i=0;i<array.size();i++){
			JSONObject jsonDevice=(JSONObject) array.get(i);
			String controllerId=jsonDevice.getString("ControllerId");
			String subcontrollerId=jsonDevice.getString("SubControllerId");
			
			Controller controller=DeviceManager.getController(controllerId);
			SubController subController=controller.getSubController(subcontrollerId);
			Hashtable< String, Device> devices=subController.getDevices();
			Enumeration<Device> enume=devices.elements();
			while(enume.hasMoreElements()){
				Device device=enume.nextElement();
				if(128<=device.getDeviceType()){
					Map<String,Object> replyMap=new HashMap();
					replyMap.put("ControllerId", controllerId);
					replyMap.put("SubControllerId", subcontrollerId);
					replyMap.put("DeviceId", device.getDeviceId());
					replyMap.put("Status", device.getStatus());
					replyBody.add(replyMap);
				}
			}
		}
		send.sendPacket(packet, packet.getControllerId(), packet.getExtensionId(), packet.getCommandId(), 0, "1.0", replyBody);
	}

}
