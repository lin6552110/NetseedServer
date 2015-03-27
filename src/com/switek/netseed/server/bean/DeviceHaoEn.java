package com.switek.netseed.server.bean;

public class DeviceHaoEn extends Device{
	private long lastHeartbeatTime=0;
	private byte eventCode=0;
	private String event="";
	public static final byte EVENCODE_ID_ALARM=0x0C;//报警
	public static final byte EVENCODE_ID_HEARTBEAT=0x0D;//周期测试
	public static final byte EVENCODE_ID_TAMPER_ALARM=0x0A;//防拆报警
	public static final byte  EVENCODE_ID_UNDER_VOLTAGE=0x03;//电池欠压
	
	public long getLastHeartbeatTime() {
		return lastHeartbeatTime;
	}
	public void setLastHeartbeatTime(long lastHeartbeatTime) {
		this.lastHeartbeatTime = lastHeartbeatTime;
	}
	public byte getEventCode() {
		
		return eventCode;
	}
	public void setEventCode(byte eventCode) {
		this.eventCode = (byte) (eventCode&15);
		switch(this.eventCode){
			case EVENCODE_ID_ALARM:
				event="报警";
				break;
			case EVENCODE_ID_HEARTBEAT:
				event="周期测试";
				break;
			case EVENCODE_ID_TAMPER_ALARM:
				event="防拆报警";
				break;
			case EVENCODE_ID_UNDER_VOLTAGE:
				event="电池欠压";
				break;
		}
	}
	public String getEvent() {
		return event;
	}
	
	
	
	
}
