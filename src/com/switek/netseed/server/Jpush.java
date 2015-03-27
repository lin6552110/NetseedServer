package com.switek.netseed.server;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import com.switek.netseed.server.dao.JPushUserDao;
import com.switek.netseed.server.dao.JpushHistoryDao;
import com.switek.netseed.server.ui.ServerForm;

public class Jpush {
	private static final int APPTYPE_NETSEED=1;
	private static final int APPTYPE_NETSEEDBOX=2;
	private static final int APPTYPE_LVLIAN=50000;
	private static final int APPTYPE_QIDI=50001;
	private static final int APPTYPE_SAIHONG=50002;
	private static final int APPTYPE_NEUTRAL=100000;
	
	private Logger logger=Logger.getLogger(Jpush.class);
	//netseed
	private String appKeyNetSeed="55dd6f70894e4956eb7937f3";
	private String  masterSecretNetSeed="914cbb4ad801612e434aab43";
	//启迪亿家
	private String appKeyQiDi="c783802e0c25176bd9d0b133";
	private String  masterSecretQiDi="e1e0643c39ee9b3bd58ca4e5";
	//中性版
	private String appKeyNeutral="941c99068823ae62caf40a1a";
	private String  masterSecretNeutral="0f7a5f2c9c6f2abb544b6d98";

	//If not present, the default value is 86400(s) (one day)
	private long timeToLive=60 * 60 * 24;
	private Device historyDevice;
	
	public JPushClient getJPushClient(int appType){
		switch (appType) {
		case Jpush.APPTYPE_NETSEED:
			return new JPushClient(masterSecretNetSeed, appKeyNetSeed);
		case Jpush.APPTYPE_QIDI:
			return new JPushClient(masterSecretQiDi, appKeyQiDi);
		case Jpush.APPTYPE_NEUTRAL:
			return new JPushClient(masterSecretNeutral, appKeyNeutral);
		default:
			return new JPushClient(masterSecretNetSeed, appKeyNetSeed);
		}
	}
	
	public PushResult push(PushPayload payload,int appType){
		JPushClient client=getJPushClient(appType);
		PushResult result=null;
		try {
			 ServerForm.showLog("JPUSH:"+payload.toString());
			 result=client.sendPush(payload);
			if(null!=result){
				ServerForm.showLog("JpushResult:"+result.toString()+"  "+result.isResultOK());
				if(result.isResultOK()){
					JpushHistoryDao dao=new JpushHistoryDao();
					dao.addJpushHistory(payload,historyDevice.getControllerId(),historyDevice.getDeviceId());
				}
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
	
	public PushResult push2APP(Device device,List<String> list,int appType){
		this.historyDevice=device;
		String controllerId=device.getControllerId();
		//根据中控Id获取可以推送的用户
		JPushUserDao dao=new JPushUserDao();
		//List<String> list=dao.registrationIdList(controllerId);
		String typeString=device.getDeviceTypeString();
		String event="";
		Device haoen=device;
		if(0!=haoen.getEventCode()){
			event="事件："+haoen.getEvent();
		}
		Date date=new Date();
		String strDate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
		String alert="设备："+typeString+"\t"+event+"\n"+strDate;
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
		PushResult result=push(payload,appType);
		return result;
	}
	
	public void push2APP(Device device){
		String controllerId=device.getControllerId();
		JPushUserDao dao=new JPushUserDao();
		HashMap<String,List<String>> map=dao.registrationIdAndTypeList(controllerId);
		if(!map.isEmpty()){
			Iterator iter=map.entrySet().iterator();
			while(iter.hasNext()){
				Map.Entry entry=(Map.Entry) iter.next();
				int appType=Integer.valueOf((String) entry.getKey());
				List<String> list=(List<String>) entry.getValue();
				push2APP(device,list,appType);
			}
		}
	}
	

}
