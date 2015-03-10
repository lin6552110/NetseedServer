package com.lin.test;

import java.util.Date;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.mysql.jdbc.log.Log;

import cn.jpush.api.JPushClient;
import cn.jpush.api.common.resp.APIConnectionException;
import cn.jpush.api.common.resp.APIRequestException;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.Notification;

public class JpushTest {
	private static Logger logger=Logger.getLogger(JpushTest.class);
	private static String masterSecret="2a665f775ed54c670d9a28bb";
	private static String appKey="7bd4c8d23c3e4e01aa79ddaa";
	private static long timeToLive=60;
	private static JPushClient client=null;
	public static void main(String[] args) {
		client=new JPushClient(masterSecret, appKey);
		PushPayload payload=sendMessage1();
		try {
			PushResult result=client.sendPush(payload);
			System.out.println(result.toString());
			if(null!=result){
				System.out.println(result.isResultOK());
			}
			logger.info("Got result"+result);
		} catch (APIConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("connection error.should retry later",e);
		} catch (APIRequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static PushPayload sendMessage(){
		JsonObject json=new JsonObject();
		json.addProperty("name", "lin");
		json.addProperty("age", 12);
		json.addProperty("sex", 1);
		return PushPayload.messageAll(json.toString());
		
		//return PushPayload.alertAll("llllllllll");
//		JsonObject json=new JsonObject();
//		json.addProperty("name","lin");
//		json.addProperty("age", 13);
//		return PushPayload.fromJSON(json.toString());
	}
	public static PushPayload sendMessage1(){
		Date pushDT=new Date();
		String dateStr=pushDT.toLocaleString();
		String alert="设备:房门(门磁)  \n"+"状态:打开 \n";
		//String alert="设备：房门(门磁)";
		return PushPayload.newBuilder()
				.setPlatform(Platform.all())
				.setAudience(Audience.all())
				.setNotification(Notification.android(alert, "安防消息", null))
				.setMessage(Message.newBuilder().setMsgContent(alert+"时间:"+dateStr).build())
				.build();
	}
	
	public static void test(){
		System.out.println(" "+sendMessage1().toString());
	}

}
