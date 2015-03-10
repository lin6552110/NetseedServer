package com.switek.netseed.server.bean;

public class TimerStep {

	public TimerStep() {
	
	}
	
	String name="";
	String controllerId="";
	String subcontrollerId = "";
	public String getControllerId() {
		return controllerId;
	}
	public void setControllerId(String controllerId) {
		this.controllerId = controllerId;
	}
	public String getSubcontrollerId() {
		return subcontrollerId;
	}
	public void setSubcontrollerId(String subcontrollerId) {
		this.subcontrollerId = subcontrollerId;
	}

	String deviceId = "";
	int keyIndex = 0;
	int delay = 500;
	int seqNo = 0;
	int value = 0;
	
	
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public int getKeyIndex() {
		return keyIndex;
	}
	public void setKeyIndex(int keyIndex) {
		this.keyIndex = keyIndex;
	}
	public int getDelay() {
		return delay;
	}
	public void setDelay(int delay) {
		this.delay = delay;
	}
	public int getSeqNo() {
		return seqNo;
	}
	public void setSeqNo(int seqNo) {
		this.seqNo = seqNo;
	}
}
