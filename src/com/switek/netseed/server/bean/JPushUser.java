package com.switek.netseed.server.bean;

public class JPushUser {
	private String registrationId;
	private String platform;
	private int status;
	private String controllerId;
	private String tag;
	private int appType=1;
	public String getRegistrationId() {
		return registrationId;
	}
	public void setRegistrationId(String registrationId) {
		this.registrationId = registrationId;
	}
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getControllerId() {
		return controllerId;
	}
	public void setControllerId(String controllerId) {
		this.controllerId = controllerId;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public String toString(){
		return controllerId+"  "+registrationId+"  "+platform+"  "+tag+"  "+appType;
	}
	
	public int getAppType() {
		return appType;
	}
	public void setAppType(int appType) {
		this.appType = appType;
	}

	
}
