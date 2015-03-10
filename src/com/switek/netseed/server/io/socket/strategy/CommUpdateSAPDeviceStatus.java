package com.switek.netseed.server.io.socket.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.Controller;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.dao.DeviceDao;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;

/**
 * @author Lin 2015年3月4日
 * 设备的撤防（00）或布防（FF）
 */
public class CommUpdateSAPDeviceStatus extends CommStrategy{
	private SendPacket send=new SendPacket();
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		List<Map<String,Object>> replyBody=new ArrayList();
		String controllerId=packet.getControllerId();
		byte byteSubConId=packet.getExtensionId();
		String subcontrollerId=packet.getExtensionIdString();
		JSONObject jsonBody=JSONObject.fromObject(packet.getCommandDataString());
		JSONArray jsonArray=jsonBody.getJSONArray("Body");
		List<Map<String,String>> list=new ArrayList();
		for(int i=0;i<jsonArray.size();i++){
			Map<String,String> map=new HashMap();
			JSONObject object=jsonArray.getJSONObject(i);
			map.put("ControllerId", object.getString("ControllerId"));
			map.put("SubControllerId",object.getString("SubControllerId"));
			map.put("DeviceId", object.getString("DeviceId"));
			map.put("Status", object.getString("Status"));
			list.add(map);
		}
		DeviceDao dao=new DeviceDao();
		//先修改数据库,再修改内存中的device
		boolean daoResult=dao.updateSAPStatus(list);
		int resultCode=0;
		if(daoResult){
			for(Map<String,String> deviceMap:list){
				String dcontrollerId=deviceMap.get("ControllerId");
				String dsubControllerId=deviceMap.get("SubControllerId");
				String deviceId=deviceMap.get("DeviceId");
				String status=deviceMap.get("Status");
				Controller controller=DeviceManager.getController(dcontrollerId);
				SubController subController=controller.getSubController(dsubControllerId);
				Device device=subController.getDevice(deviceId);
				if(null!=device){
					device.setStatus(status);
				}else{
					Map<String,Object> replyMap=new HashMap();
					replyMap.put("DeviceId", deviceId);
					replyBody.add(replyMap);
					resultCode=ErrorCode.ERROR_COULDNOT_FIND_DEVICE;
				}
			}
		}else{
			resultCode=ErrorCode.ERROR_FAILED_UPDATEDB;
		}
		send.sendPacket(packet, controllerId, byteSubConId, packet.getCommandId(), resultCode, "1.0", replyBody);
	}
}
