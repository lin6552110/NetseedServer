package com.switek.netseed.server.io.socket.strategy;

import java.util.HashMap;

import net.sf.json.JSONObject;

import com.google.gson.JsonObject;
import com.switek.netseed.server.bean.ActionResult;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.PacketSendResult;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;
/**
 * @author Lin 2015年3月5日
 * 
 */
public class CommAddSAPDevice extends CommStrategy{
	private SendPacket send=new SendPacket();
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		String controllerId=packet.getControllerId();
		byte subcontrollerIdShort=packet.getExtensionId();
		String subcontrollerId=packet.getExtensionIdString();
		short commandId=packet.getCommandId();
		
		JSONObject jsonBody=packet.getJsonBody();
		String deviceId=jsonBody.getString("DeviceId");
		String deviceName=jsonBody.getString("DeviceName");
		int deviceType=jsonBody.getInt("Type");
		int deviceIndex=new Device().getNewDeviceIndex(controllerId, subcontrollerId, deviceType);
		
		jsonBody.put("DeviceIndex", deviceIndex);
		String brandCode=jsonBody.getString("BrandCode");
		int command=jsonBody.getInt("Command");
		String Status=jsonBody.getString("Status");
		int resultCode=0;
		byte[] cmdData=new byte[8];
		cmdData[0]=(byte)command;
		cmdData[1]=(byte)deviceType;
		cmdData[2]=(byte)deviceIndex;
		for(int i=0,j=0;i<5;i++,j=j+2){
			String s=deviceId.substring(j, j+2);
			int id=Integer.valueOf(s, 16);
			cmdData[i+3]=(byte)id;
		}
		if(0==command){
			JSONObject json=JSONObject.fromObject(packet.getCommandDataString());
			json.remove("Body");
			json.put("Body", jsonBody);
			packet.setCommandDataString(json.toString());
			resultCode=addSAPDevice(packet,cmdData,deviceId);
		}else if(1==command){
			resultCode=deleteSAPDevice(packet,cmdData);
		}
		HashMap<String, Object> replyBody=new HashMap<String,Object>();
		if(0==command){
			replyBody.put("DeviceIndex", deviceIndex);
		}
		send.sendPacket(packet, controllerId, subcontrollerIdShort, commandId, resultCode, "1.0", replyBody);
	}
	
	public int addSAPDevice(SocketPacket packet,byte[] cmdData,String deviceId){
		int resultCode=0;
		Device device=getDevice(packet.getControllerId(), packet.getExtensionIdString(), deviceId);
		if(null!=device){
			resultCode = ErrorCode.ERROR_INVALID_DEVICEID;
		}else{
			resultCode=connect2Controller(packet,cmdData);
			if(0==resultCode){
				CommAddNetseedDevice comm=new CommAddNetseedDevice();
				ActionResult addResult=comm.addNetseedDevice(packet);
				resultCode=addResult.getResultCode();
			}
		}
		return resultCode;
	}
	
	public int deleteSAPDevice(SocketPacket packet,byte[] cmdData){
		int resultCode=0;
		resultCode=connect2Controller(packet,cmdData);
		if(0==resultCode){
			CommRemoveDevice comm=new CommRemoveDevice();
			ActionResult deleteResult=comm.removeDevice(packet);
			resultCode=deleteResult.getResultCode();
		}
		return resultCode;
	}
	
	private int connect2Controller(SocketPacket packet,byte[] cmdData){
		String controllerId=packet.getControllerId();
		byte subcontrollerId=packet.getExtensionId();
		short commandId=20;
		SocketPacket sendPacket=connect2Controller(controllerId);
		PacketSendResult result = new PacketSendResult();
		result=send.sendPacket(sendPacket, controllerId, subcontrollerId,commandId, cmdData);
		return result.getResultCode();
	}
}
