package com.switek.netseed.server.io.socket.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.dao.JpushHistoryDao;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;


public class CommJpushHistory extends CommStrategy{
	private SendPacket send=new SendPacket();
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		JSONObject jsonBody=packet.getJsonBody();
		String registrationId=jsonBody.getString("RegistrationId");
		int status=jsonBody.getInt("Status");
		String controllerId="";
		String deviceId="";
		long startDT=0;
		long endDT=0;
		JSONArray array=new JSONArray();
		
		if(jsonBody.containsKey("ControllerID")){
			controllerId=jsonBody.getString("ControllerId");
		}
		if(jsonBody.containsKey("DeviceId")){
			deviceId=jsonBody.getString("DeviceId");
		}
		if(jsonBody.containsKey("StartDT")){
			startDT=jsonBody.getLong("StartDT");
		}
		if(jsonBody.containsKey("EndDT")){
			endDT=jsonBody.getLong("EndDt");
		}
		if(jsonBody.containsKey("DeleteDT")){
			array=jsonBody.getJSONArray("DeleteDT");
		}
		
		JpushHistoryDao dao=new JpushHistoryDao();
		List<Map<String,Object>> list=new ArrayList();
		if(0==status){
			list=dao.getJpushHistory(registrationId, controllerId, deviceId, startDT, endDT);
		}else if(1==status){
			if(array.size()>0){
				long[] date=new long[array.size()];
				for(int i=0;i<array.size();i++){
					date[i]=array.getLong(i);
				}
				dao.deleteJpushHistory(registrationId, date);
			}else{
				dao.deleteJpushHistory(registrationId);
			}
		}
		send.sendPacket(packet, packet.getControllerId(), packet.getExtensionId()
				, packet.getCommandId(), 0, "1.0", list);
	}
	

}
