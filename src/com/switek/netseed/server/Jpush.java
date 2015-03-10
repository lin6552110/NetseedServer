package com.switek.netseed.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import cn.jpush.api.JPushClient;
import cn.jpush.api.common.resp.APIConnectionException;
import cn.jpush.api.common.resp.APIRequestException;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;

import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.JPushUser;
import com.switek.netseed.server.dao.JPushUserDao;
import com.switek.netseed.server.ui.ServerForm;

public class Jpush {
	private Logger logger=Logger.getLogger(Jpush.class);
	private String  masterSecret="eab2c0239436dd6cc06d76a3";
	private String appKey="2ecebea4db0baef8a0418481";
	//If not present, the default value is 86400(s) (one day)
	private long timeToLive=60 * 60 * 24;
	
	public PushResult push(PushPayload payload){
		JPushClient client=new JPushClient(masterSecret, appKey);
		PushResult result=null;
		try {
			 ServerForm.showLog("JPUSH:"+payload.toString());
			 result=client.sendPush(payload);
			if(null!=result){
				ServerForm.showLog("JpushResult:"+result.toString()+"  "+result.isResultOK());
			}
		} catch (APIConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ServerForm.showLog(e);
		} catch (APIRequestException e) {
			// TODO Auto-generated catch block
			ServerForm.showLog(e);
		}
		return result;
		
	}
	
	public PushResult push2APP(Device device){
		String controllerId=device.getControllerId();
		//根据中控Id获取可以推送的用户
		JPushUserDao dao=new JPushUserDao();
		List<String> list=dao.registrationIdList(controllerId);
		String typeString=device.getDeviceTypeString();
		Date date=new Date();
		String strDate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
		String alert="设备："+typeString+strDate;
		String title="安防消息";
		
		PushPayload payload=PushPayload.newBuilder()
				.setAudience(Audience.registrationId(list))
				.setPlatform(Platform.all())
				.setNotification(Notification.newBuilder()
						.addPlatformNotification(AndroidNotification.newBuilder()
								.setAlert(alert)
								.setTitle(title)
								.build()
								)
						.addPlatformNotification(IosNotification.newBuilder()
								.setAlert(alert)
								.build()
								)
						.build()
						)
				.build();
		PushResult result=push(payload);
		return result;
	}
	

}
