package com.switek.netseed.server.jpush;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;

import com.switek.netseed.server.bean.Device;

public class Push {
	private static final String masterSecret="668cda650d0a459f3918bcc8";
	private static final String appKey="f5e68c6839a0c7e159ca93ba";
	private static JPushClient client=new JPushClient(masterSecret, appKey);
	
	private Push(){}
	
	
	public static JPushClient getJPushClient(){
		return client;
	}
	

	
	
	
	
	

}
