package com.switek.netseed.server.bean;

public class LearnIRCodeResult extends ActionResult{


	String deviceId="";
	/**
	 * @return the deviceId
	 */
	public String getDeviceId() {
		return deviceId;
	}
	/**
	 * @param deviceId the deviceId to set
	 */
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	boolean need2Reply=true;
	/**
	 * @return the need2Reply
	 */
	public boolean isNeed2Reply() {
		return need2Reply;
	}
	/**
	 * @param need2Reply the need2Reply to set
	 */
	public void setNeed2Reply(boolean need2Reply) {
		this.need2Reply = need2Reply;
	}
}
