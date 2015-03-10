package com.switek.netseed.server.io.socket.strategy;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.ActionResult;
import com.switek.netseed.server.bean.ControllerTimer;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;

public class CommDisableTimer extends CommStrategy{
	
	private SendPacket send=new SendPacket();
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		ActionResult result = disableTimer(packet);
		
		Map<String, Object> replyBody = new HashMap<>();		

		send.sendPacket(packet, packet.getControllerId(), packet.getExtensionId(),
				packet.getCommandId(), result.getResultCode(), "1.0", replyBody);
	}
	
	private ActionResult disableTimer(SocketPacket packet){
		
		ActionResult result = new ActionResult();
		JSONObject body = packet.getJsonBody();

		String Id = body.getString("Id");
		int nEnabled = body.getInt("Enabled");
		boolean enabled = (nEnabled == 1);
		
		logger.info("disableTimer: " + Id + ", Enabled: " + enabled);
		
		ControllerTimer timer = DeviceManager.getTimer(Id);
		if (timer ==null){
			result.setResultCode(ErrorCode.ERROR_INVALID_TIMERID);
			return result;
		}	
				
		boolean	b = mDAL.disableTimer(timer, enabled);
	
		if (!b){
			result.setResultCode(ErrorCode.ERROR_FAILED_UPDATEDB);
			return result;
		}		
		
		timer.setEnabled(enabled);
		DeviceManager.addTimer(timer);
		
		result.setOutputObject(timer);
		return result;		
	}

}
