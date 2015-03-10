package com.switek.netseed.server.io.socket.strategy;

import net.sf.json.JSONObject;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.ActionResult;
import com.switek.netseed.server.bean.ControllerTimer;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;

public class CommRemoveTimer extends CommStrategy{
	
	private SendPacket send=new SendPacket();
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		ActionResult result = removeTimer(packet);
		send.sendPacketWithoutBody(packet, packet.getControllerId(), packet.getExtensionId(),
				packet.getCommandId(), result.getResultCode());
		
	}
	
	private ActionResult removeTimer(SocketPacket packet){
		
		ActionResult result = new ActionResult();
		JSONObject body = packet.getJsonBody();
		String timerId = body.getString("TimerId");
		
		ControllerTimer timer = DeviceManager.getTimers().get(timerId);
		if (timer == null){
			result.setResultCode(ErrorCode.ERROR_INVALID_TIMERID);
			return result;
		}

		boolean b = mDAL.removeTimer(timer);
		if (!b){
			result.setResultCode(ErrorCode.ERROR_FAILED_UPDATEDB);
			return result;
		}
		timer.setDeleted(true);
		return result;
		
	}
	

}
