package com.switek.netseed.server.io.socket.strategy;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.Jpush;
import com.switek.netseed.server.Utils;
import com.switek.netseed.server.bean.ActionResult;
import com.switek.netseed.server.bean.Controller;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.dao.DeviceDao;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;
import com.switek.netseed.server.ui.ServerForm;

/**
 * @author Lin 2015年3月2日
 *	主控向服务器查询安防设备状态
 */
public class CommSAPDeviceAlarm extends CommStrategy{
	private SendPacket send=new SendPacket();
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		String controllerId=packet.getControllerId();
		byte subcontrollerId=packet.getExtensionId();
		String strSubcontrollerId=packet.getExtensionIdString();
		//String strSubcontrollerId=String.valueOf(subcontrollerId&0xFF);
		short commandId=packet.getCommandId();
		byte[] data=packet.getCommandData();
		byte[] byteDeviceId=new byte[5];
		for(int i=0;i<5;i++){
			byteDeviceId[i]=data[2+i];
		}
		String deviceId=Utils.bytes2HexString2(byteDeviceId);
		System.out.println(deviceId+"   "+controllerId+"   "+strSubcontrollerId );
		Controller controller=DeviceManager.getController(controllerId);
		SubController subController=controller.getSubController(strSubcontrollerId);
		Device device=subController.getDevice(deviceId);
		String status=device.getStatus();
		byte[] cmdData=new byte[1];
		int s=Integer.valueOf(status, 16);
		cmdData[0]=(byte)0xFF;
		if(0xFF==s){
			Jpush jpush=new Jpush();
			jpush.push2APP(device);
		}
		send.sendPacket(packet, controllerId, subcontrollerId, commandId, cmdData);
	}
	
	//通知中控修改设备的撤防布防状态
	public void message2Controller(){
		
	}
	

}
