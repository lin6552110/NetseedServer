package com.switek.netseed.server.io.socket.strategy;

import java.util.Collection;
import java.util.Date;

import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.Notification;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.Jpush;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.SubController;

public class CommPush2App extends CommStrategy{
	@Override
	public void analysisPacket(SocketPacket packet) {
		//获取设备信息
		String controllerId=packet.getControllerId();
		String subcontrollerId=packet.getExtensionIdString();
		String deviceId="";
		SubController subController=DeviceManager.getSubcontroller(subcontrollerId, subcontrollerId);
		Device device=subController.getDevice(deviceId);
		String deviceName=device.getDeviceName();
		/**
		 * 推送通知
		 * Audience 根据controllerId查询所有关联的registrationId
		 */
		Collection audience=null;
		Jpush jpush=new Jpush();
		Date pushDT=new Date();
		String alert=pushDT.getTime()+" " 
				+deviceName+"  报警了！" ;
		PushPayload payload=PushPayload.newBuilder()
				.setPlatform(Platform.all())
				.setAudience(Audience.registrationId(audience))
				.setNotification(Notification.android(alert, "通知", null))
				.build();
		
	//	PushResult result=jpush.push(payload);
		//保存推送记录
		
	}
	
	

}
