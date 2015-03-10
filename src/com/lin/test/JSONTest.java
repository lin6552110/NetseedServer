package com.lin.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.google.gson.JsonObject;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.dao.TimerDao;
import com.switek.netseed.server.io.socket.strategy.CommGetTimer;

public class JSONTest {
	public static void main(String[] args) {
		//test();
		jsonTest();
	}
	
	public static void testSql(){
		TimerDao dao=new TimerDao();
		CommGetTimer comm=new CommGetTimer();

		List controllerId=new ArrayList();
		controllerId.add("DE070507300E");
		controllerId.add("DE00");
		
		List<String> list=dao.getTimerId(controllerId);
		for(String str:list){
			System.out.println(str);
		}
	}
	
	
	
	public static void test(){
		JSONObject json=new JSONObject();
		
		json.put("OptCode", 3841);
		json.put("APIVer", "1.0");
		JSONObject con=new JSONObject();
		
		List ControllerId=new ArrayList<String>();
		Map map1=new HashMap<>();
		Map map2=new HashMap<>();
		map1.put("ControllerId","DE070507300E");
		map2.put("ControllerId","DE00");
		ControllerId.add(map1);
		ControllerId.add(map2);
//		ControllerId.add("123");
//		ControllerId.add("456");
		//con.put("controllerId", ControllerId);
		json.put("Body", ControllerId);
		System.out.println(json.toString());
		System.out.println(json.getJSONArray("Body"));
		JSONArray array=json.getJSONArray("Body");
		for(int i=0;i<array.size();i++){
			System.out.println(array.getJSONObject(i).get("ControllerId"));
		}
		
		SocketPacket packet=new SocketPacket(null);
		packet.setCommandDataString(json.toString());
		System.out.println(packet.getJsonBody());
		CommGetTimer comm=new CommGetTimer();
		comm.analysisPacket(packet);
		
		
	}
	
	public static void stringBuffTest(){
		StringBuffer paramsBuff=new StringBuffer();
		paramsBuff.append("(");
		
		paramsBuff.append("'"+123456+"',");
		paramsBuff.append("'"+"abcd"+"',");
		
		
		paramsBuff.replace( paramsBuff.length()-1, paramsBuff.length(), "");
		paramsBuff.append(")");
		System.out.println(paramsBuff);
	}
	
	public static void listTest(){
		List list=new ArrayList();
		list.add("1");
		list.add("2");
		list.add("3");
		System.out.println(list.toString());

		list.clear();
		System.out.println(list.toString());
		
		
	}
	
	public static void jsonTest(){
		JSONObject json=new JSONObject();
		
		
		json.put("lin", "aaa");
		
		System.out.println(json);
		if(json.containsKey("aaa")){
			System.out.println(json.getString("aaa"));
		}
		
		
	}

}
