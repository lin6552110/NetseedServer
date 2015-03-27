package com.switek.netseed.server.io.socket.strategy;

import java.util.Date;

import javax.print.attribute.standard.Severity;



import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.Jpush;
import com.switek.netseed.server.Utils;
import com.switek.netseed.server.bean.Controller;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.DeviceHaoEn;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;
import com.switek.netseed.server.ui.ServerForm;

public class HaoEn extends CommStrategy{
	private SendPacket send=new SendPacket();
	@Override
	public void analysisPacket(SocketPacket packet){
		int resultCode=0;
		String controllerId=packet.getControllerId();
		byte subcontrollerId=packet.getExtensionId();
		String strSubcontrollerId=packet.getExtensionIdString();
		short commandId=packet.getCommandId();
		
		Controller controller=DeviceManager.getController(controllerId);
		SubController subController=controller.getSubController(strSubcontrollerId);
		byte[] data=packet.getCommandData();
		byte[] deviceIdByte=new byte[5];
		if(7==data.length){
			for(int i=0;i<5;i++){
				deviceIdByte[i]=data[i];
			}
			deviceIdByte[4]=(byte) (deviceIdByte[4]&0xF0);
			String deviceId=Utils.bytes2HexString2(deviceIdByte);
			ServerForm.showLog("DEVICEID:"+deviceId);
			Device device= subController.getDevice(deviceId);
			if(null!=device){
				device.setEventCode(data[4]);
				switch(device.getEventCode()){
					case DeviceHaoEn.EVENCODE_ID_ALARM:
						alarm(device);
						break;
					case DeviceHaoEn.EVENCODE_ID_HEARTBEAT:
						heartbeat(device);
						break;
					case DeviceHaoEn.EVENCODE_ID_TAMPER_ALARM:
						alarm(device);
						break;
					case DeviceHaoEn.EVENCODE_ID_UNDER_VOLTAGE:
						alarm(device);
						break;
				}
			}else{
				resultCode=40003;
			}
		}else{
			resultCode=10004;
		}
		ServerForm.showLog("ResultCode:"+resultCode);
		byte[] cmdData=new byte[1];
		cmdData[0]=(byte)0xFF;
		send.sendPacket(packet, controllerId, subcontrollerId, commandId, cmdData);
	}
	
	public void heartbeat(Device device){
		device.setLastHeartbeatTime(new Date().getTime());
	}
	
	public void alarm(Device device){
		String status=device.getStatus();
		int s=Integer.valueOf(status, 16);
		if(0xFF==s){
			String even=device.getEvent();
			ServerForm.showLog("推送："+"{'deviceId':'"+device.getDeviceId()+"','even':'"+even+"','time':'"+new Date().getTime()+"'}\n");
			Jpush jpush=new Jpush();
			jpush.push2APP(device);
		}
	}

}
