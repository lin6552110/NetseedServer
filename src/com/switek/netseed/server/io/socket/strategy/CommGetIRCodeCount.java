package com.switek.netseed.server.io.socket.strategy;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import com.switek.netseed.server.bean.ActionResult;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.dao.IRCodeDao;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;

public class CommGetIRCodeCount extends CommStrategy{
	private SendPacket send=new SendPacket();
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		ActionResult result=getIRCodeCount(packet);
		Map<String,Object> replyBody=new HashMap<>();
		replyBody.put("IRCodeCount", result.getOutputObject());
		send.sendPacket(packet
						, packet.getControllerId()
						, packet.getExtensionId()
						, packet.getCommandId()
						, result.getResultCode()
						, "1.0"
						, replyBody);
	}
	
	private ActionResult getIRCodeCount(SocketPacket packet){
		ActionResult result=new ActionResult();
		String data=packet.getCommandDataString();
		JSONObject object=JSONObject.fromObject(data);
		JSONObject jsonBody=object.getJSONObject("Body");
		int deviceType=jsonBody.getInt("DeviceType");
		String brandCode=jsonBody.getString("BrandCode");
		IRCodeDao dao=new IRCodeDao();
		int count=dao.getIRCodeCount(deviceType, brandCode);
		result.setOutputObject(count);
		return result;
	}

}
