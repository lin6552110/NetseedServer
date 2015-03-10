package com.switek.netseed.server.io.socket.strategy;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.google.gson.JsonObject;
import com.switek.netseed.server.bean.ActionResult;
import com.switek.netseed.server.bean.JPushUser;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.dao.JPushUserDao;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;

/**
 * @author Lin 2015年3月2日
 * 注册Jpush或者更新推送状态
 */
public class CommUpdatePushStatus extends CommStrategy{

	private SendPacket send=new SendPacket();
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		byte subcontrollerId=packet.getExtensionId();
		short commandId=packet.getCommandId();
		JSONObject jsonBody=packet.getJsonBody();
		String registrationId=jsonBody.getString("RegistrationId");
		String platform=jsonBody.getString("Platform");
		JSONArray jsonArray=jsonBody.getJSONArray("Controller");
		String controllerId=packet.getControllerId();
		int resultCode=0;
		JPushUser jpushUser=new JPushUser();
		jpushUser.setPlatform(platform);
		jpushUser.setRegistrationId(registrationId);
		if(jsonBody.containsKey("Tag")){
			String tag=jsonBody.getString("Tag");
			if(!(null==tag||tag.equals(""))){
				jpushUser.setTag(tag);
			}
		}
		JPushUserDao dao=new JPushUserDao();
		dao.delete(registrationId);
		for(Object o:jsonArray){
			JSONObject jsonController=(JSONObject)o;
			String jsonControllerId=jsonController.getString("ControllerId");
			int status=jsonController.getInt("Status");
			jpushUser.setControllerId(jsonControllerId);
			jpushUser.setStatus(status);
			if(!dao.insert(jpushUser)){
				resultCode=30001;
			}
		}
		Map<String, Object> replyBody = new HashMap<>();
		send.sendPacket(packet, controllerId, subcontrollerId, commandId, resultCode, "1.0", replyBody);
	}	
}
