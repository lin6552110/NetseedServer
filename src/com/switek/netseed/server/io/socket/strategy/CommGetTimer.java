package com.switek.netseed.server.io.socket.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.ActionResult;
import com.switek.netseed.server.bean.ControllerTimer;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.dao.TimerDao;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;

/**
 * 2015年1月13日  林进铨
 * 获取根据中控ID获取所有的定时器列表
 * Server接收从APP发送的一个以上的中控ID
 * 根据中控ID从数据库中查询对应的TimerID
 * 根据TimerID从DeviceManager中的获取Timer对象
 */
public class CommGetTimer extends CommStrategy{
	
	private SendPacket send=new SendPacket();
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		ActionResult result=getTimer(packet);
		Map<String,Object> replyBody=new HashMap<>();
		replyBody.put("Timer", result.getOutputObject());
		send.sendPacket(packet, packet.getControllerId(), packet.getExtensionId(),
				packet.getCommandId(), result.getResultCode(), 
				"1.0", replyBody);
	}
	
	public ActionResult getTimer(SocketPacket packet){
		TimerDao dao=new TimerDao();
		ActionResult result=new ActionResult();
		JSONObject json=JSONObject.fromObject(packet.getCommandDataString());
		JSONArray controllerId=json.getJSONArray("Body");
		if(controllerId.size()<=0){
			result.setResultCode(ErrorCode.ERROR_INVALID_CONTROLLERID);
			return result;
		}
		
		List controllerList=new ArrayList<String>();
		for(int i=0;i<controllerId.size();i++){
			String id=controllerId.getJSONObject(i).getString("ControllerId");
			controllerList.add(id);
		}
		
		List<String> timerIdList=dao.getTimerId(controllerList);
		List<ControllerTimer> timerList=new ArrayList();
		for(int i=0;i<timerIdList.size();i++){
			String timerId=timerIdList.get(i);
			if(null!=timerId&&!timerId.equals("")){
				ControllerTimer timer=DeviceManager.getTimer(timerId);
				if(null!=timer&&!timer.isDeleted()){
					timerList.add(timer);									
				}
			}
		}
		result.setOutputObject(timerList);
		return result;
	}

}
