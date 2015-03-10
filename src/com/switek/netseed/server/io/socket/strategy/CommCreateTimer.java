package com.switek.netseed.server.io.socket.strategy;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.ActionResult;
import com.switek.netseed.server.bean.ControllerTimer;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.bean.TimerStep;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;

public class CommCreateTimer extends CommStrategy{
	
	private SendPacket send=new SendPacket();
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		ActionResult result = createTimer(packet);
		
		Map<String, Object> replyBody = new HashMap<>();
		if(result.isSuccessful()){
			replyBody.put("TimerId", ((ControllerTimer)result.getOutputObject()).getTimerId());
		}		

		send.sendPacket(packet, packet.getControllerId(), packet.getExtensionId(),
				packet.getCommandId(), result.getResultCode(), "1.0", replyBody);
	}
	
private ActionResult createTimer(SocketPacket packet){
		
		ActionResult result = new ActionResult();
		JSONObject body = packet.getJsonBody();
		String name = body.getString("Name");
		int weekdays = body.getInt("Weekdays");
		String time = body.getString("Time");
		
		String Id = "";
		if(body.containsKey("Id")){
			Id = body.getString("Id");
		}
		
		boolean isNewTimer = ControllerTimer.isNewTimer(Id);
		
		if(!isNewTimer){
			ControllerTimer timer = DeviceManager.getTimer(Id);
			if (timer ==null){
				result.setResultCode(ErrorCode.ERROR_INVALID_TIMERID);
				return result;
			}
		}
		
		ControllerTimer timer = new ControllerTimer();
		timer.setTimerId(Id);
		timer.setName(name);
		timer.setWeekDays(weekdays);
		timer.setTime(time);
		
		JSONArray steps = body.getJSONArray("Steps");
		int seqNo = 10;
		for (int i = 0; i < steps.size(); i++) {
				JSONObject step = (JSONObject) steps.get(i);
				String stepName = step.getString("Name");
				String controllerId = step.getString("ControllerId");
				String subcontrollerId = step.getString("ExtId");
				String deviceId = step.getString("DeviceId");
				SubController sub = DeviceManager.getSubcontroller(controllerId, subcontrollerId);
				if (sub == null){
					result.setResultCode(ErrorCode.ERROR_INVALID_CONTROLLERID);
					return result;
				}
				Device device = sub.getDevice(deviceId);
				if (device ==null){
					result.setResultCode(ErrorCode.ERROR_INVALID_DEVICEID);
					return result;
				}
				
				int keyIndex = step.getInt("Key"	);
				int delay = step.getInt("Delay");
				
				int value = 0;
				if(step.containsKey("Value")){
					value = step.getInt("Value");
				}
				
				
				TimerStep timerStep = new TimerStep();
				timerStep.setName(stepName);
				timerStep.setControllerId(controllerId);
				timerStep.setSubcontrollerId(subcontrollerId);
				timerStep.setDeviceId(deviceId);
				timerStep.setValue(value);
				
				timerStep.setKeyIndex(keyIndex);
				timerStep.setDelay(delay);
				timerStep.setSeqNo(seqNo);
				seqNo += 10;
				
				timer.addStep(timerStep);
		}
		
		boolean b = false;
		if (isNewTimer){
			b = mDAL.createTimer(timer);
		}else{
			b = mDAL.updateTimer(timer);
		}
		
		if (!b){
			result.setResultCode(ErrorCode.ERROR_FAILED_UPDATEDB);
			return result;
		}		
			
		DeviceManager.addTimer(timer);
		
		result.setOutputObject(timer);
		return result;		
	}

}
