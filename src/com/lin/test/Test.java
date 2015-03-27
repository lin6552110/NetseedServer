package com.lin.test;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.switek.netseed.server.Jpush;
import com.switek.netseed.server.Utils;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.JPushUser;
import com.switek.netseed.server.dao.ControllerDao;
import com.switek.netseed.server.dao.DeviceDao;
import com.switek.netseed.server.dao.IRCodeDao;
import com.switek.netseed.server.dao.JPushUserDao;

public class Test {
	public static void main(String[] args) {
//		TestClientHandler.mains();
//		TestClientHandler.mains();
//		TestClientHandler.mains();
//		TestClientHandler.mains();
		//System.out.println(Integer.toBinaryString((byte)0xff));
		//System.out.println(Integer.toBinaryString(12&0xff));
		//System.out.println((byte)0xff);
		//System.out.println(System.currentTimeMillis());
		//testFile();
//		int i=11;
//		int s=(9-i)*7+1;
//		System.out.println(s);
//		String str="000011110000111100001111";
//		str=str.substring(0, s);
//		System.out.println(str);
		//getIRCodeCount();
		//updateCodeDate();
		//testTable();
		//testTable2();
		//testTable3();
		//utilTest();
		//hashMap();
		//testByte();
		//jsonTest();
		//integer();
		//getDeviceIndex();
		//testBatch();
		//testDate();
		jpushUserDao();
		
	}
	
	public static void testFile(){
		String url="E:\\测试\\abc\\cc\\01.bin";
		File file=new File(url);
		//file.getParentFile().mkdirs();
	//file.getParentFile().mkdirs();
	//file.mkdirs();
	try {
		file.createNewFile();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		System.out.println("isDirectory:   "+file.isDirectory());
		System.out.println("isFile：      "+file.isFile());
		System.out.println("isAbsolute    "+file.isAbsolute());
		System.out.println("isHidden    "+file.isHidden());
		System.out.println("exists()    "+file.exists());
		file.delete();
	}
	
	public static void getIRCodeCount(){
		IRCodeDao dao=new IRCodeDao();
		System.out.println(dao.getIRCodeCount(9, "0"));
	}
	public static void updateCodeDate(){
		ControllerDao dao=new ControllerDao();
		dao.codeUpdateDT("07DE01020002", new Date());
	}
	
	public static void testTable(){
		Name name=new Name();
		name.setAge(12);
		name.setName("lin");
		TestHashMap.table.put("lin", name);

	
		
	}
	
	public static void testTable2(){
		Name name2=new Name();
		name2=TestHashMap.getName("lin");
		System.out.println("name2:"+name2.getAge()+"   "+name2.getName());
		name2.setName("aaa");
		name2.setAge(18);
	}
	
	public static void testTable3(){
		Name name3=new Name();
		name3=TestHashMap.getName("lin");
		System.out.println("name3:"+name3.getAge()+"   "+name3.getName());
	}
	
	public static void utilTest(){
		byte[] con= Utils.getHexMBytesByStr("07DE01010001");
		System.out.println(Utils.getHexMBytesByStr("07DE01010001"));
	}
	
	public static void hashMap(){
		Map<Object,Object> map=new HashMap();
		for(int i=0;i<20;i++){
			map.put(i, i);
		}
		
		for(Map.Entry<Object,Object> entry : map.entrySet()){
			System.out.println("ket="+entry.getKey()+"    "+"value="+entry.getValue());
		}
		
	}
	
	public static void hashMap2(){
		Map<Object,Object> map=new HashMap();
		for(int i=0;i<20;i++){
			map.put(i, i);
		}
		
		for(Map.Entry<Object, Object> entry:map.entrySet()){
			System.out.println(entry.getKey()+"   "+entry.getValue());
		}
		
		Iterator it=map.keySet().iterator();
		while(it.hasNext()){
			Object obje=it.next();
			System.out.println(map.get(obje));
		}
		
	}
	
	public static void testFor(){
		int count=0;
		for(int i=0;i<10;i++){
			count=count++;
		}
		System.out.println(count);
	}
	
	public static void testByte(){
		int b=0xff;
		int[] in=new int[1];
		in[0]=b;
		System.out.println(Utils.ints2HexString(in));
		//Object o=b&0xff;
		String str=String.valueOf(b);
		//String str=Integer.toHexString(b);
		System.out.println(str);
		System.out.println();
	}
	
	public static void jsonTest(){
		JSONObject object=new JSONObject();
		int b=0xff;
		object.put("Status", b);
		System.out.println(object.toString());
	}
	public static void integer(){
		String str="FF12345600";
		String[] st=str.split("\\w{2}");
		byte[] deviceId=new byte[5];
		for(int i=0,j=0;i<5;i++,j=j+2){
			String s=str.substring(j, j+2);
			int id=Integer.valueOf(s, 16);
			deviceId[i]=(byte)id;
			System.out.println(id+"   "+s);
		}
//		int h=0xff;
//		byte[] b=str.getBytes();
//		for(String c:st){
//			System.out.println(c);
//		}
		
	}
	
	public static void getDeviceIndex(){
		Device device=new Device();
		String controllerId="DE070719E00C";
		String subcontrollerId="0";
		int deviceType=5;
		int index=device.getNewDeviceIndex(controllerId, subcontrollerId, deviceType);
		System.out.println(index);
	}
	
	public static void testJSONObject(){
		JSONObject json=new JSONObject();
		json.put("name", "lin");
		List  list=new ArrayList();
		JSONObject jsonObject=new JSONObject();
		jsonObject.put("ControllerId", "DE123");
		jsonObject.put("Status", 1);
		list.add(jsonObject);
		JSONObject jsonObject1=new JSONObject();
		jsonObject1.put("ControllerId", "DE456");
		jsonObject1.put("Status", 1);
		list.add(jsonObject1);
		JSONObject jsonObject2=new JSONObject();
		jsonObject2.put("ControllerId", "DE789");
		jsonObject2.put("Status", 0);
		list.add(jsonObject2);
		JSONObject jsonObject3=new JSONObject();
		jsonObject3.put("ControllerId", "DE012");
		jsonObject3.put("Status", 0);
		list.add(jsonObject3);
		
		//{"name":"lin","ControllerId":{"DE123","DE456","DE789","DE100"}}

		json.put("Controller", list);
		
		JSONArray array=json.getJSONArray("Controller");
		System.out.println(json.toString());
		for(Object o:array){
			JSONObject object=(JSONObject)o;
			String controllerId=object.getString("ControllerId");
			int status=object.getInt("Status");
			System.out.println("Controller："+controllerId+"   "+status);
		}
	}
	
	public static void  testBatch(){
		List<Map<String,String>> list=new ArrayList();
		Map<String , String> map1=new HashMap();
		map1.put("Status", "FF");
		map1.put("DeviceId", "DD070C010400");
		Map<String , String> map2=new HashMap();
		map2.put("Status", "FF");
		map2.put("DeviceId", "DD070C010600");
		Map<String , String> map3=new HashMap();
		map3.put("Status", "FF");
		map3.put("DeviceId", "DE070507302B");
		
		list.add(map1);
		list.add(map2);
		list.add(map3);
		
		//DeviceDao dao=new DeviceDao();
		//System.out.println(dao.updateSAPStatus(list));
		JSONObject object=new JSONObject();
		object.put("Body",list);
		System.out.println(object.toString());
		
		JSONArray array=object.getJSONArray("Body");
		System.out.println(array.size());
		for(Object o:array){
			System.out.println(o);
		}
		
	}
	
	public static void testDate(){
		Date d=new Date();
		String str=new SimpleDateFormat("MM-dd HH:mm:ss").format(d);
		System.out.println(str);
	}
	
	public static void jpushUserDao(){
//		Jpush jpush=new Jpush();
//		Device device=new Device();
//		device.setControllerId("DE070507E009");
//		device.setDeviceType(0x80);
//		device.setDeviceName("大门");
//		jpush.push2APP(device);
//		JSONObject o=new JSONObject();
//		o.put("Status", "2");
//		int i=o.getInt("Status");
//		System.out.println(i);
//		int s=Integer.valueOf("FF", 16);
//		System.out.println(s==0xff);
		byte[] byteDeviceId=new byte[5];
		byteDeviceId[0]=(byte)0x00;
		byteDeviceId[1]=(byte)0x00;
		byteDeviceId[2]=(byte)0x08;
		byteDeviceId[3]=(byte)0xAD;
		byteDeviceId[4]=(byte)0xE9;
		String deviceId=Utils.bytes2HexString2(byteDeviceId);
		System.out.println(deviceId);
	}
	
	
}
