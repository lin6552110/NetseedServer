package com.switek.netseed.server.bean;

import java.util.ArrayList;
import java.util.List;

public class StandardIRCode {

	public StandardIRCode() {
	}
	
	int deviceType;
	String brandCode = "";
	List<IRCode> IRCodeList = new ArrayList<>();
	
	
	/**
	 * @return the iRCodeList
	 */
	public List<IRCode> getIRCodeList() {
		return IRCodeList;
	}
	/**
	 * @param iRCodeList the iRCodeList to set
	 */
	public void setIRCodeList(List<IRCode> iRCodeList) {
		IRCodeList = iRCodeList;
	}
	/**
	 * @return the deviceType
	 */
	public int getDeviceType() {
		return deviceType;
	}
	/**
	 * @param deviceType the deviceType to set
	 */
	public void setDeviceType(int deviceType) {
		this.deviceType = deviceType;
	}
	/**
	 * @return the brandCode
	 */
	public String getBrandCode() {
		return brandCode;
	}
	/**
	 * @param brandCode the brandCode to set
	 */
	public void setBrandCode(String brandCode) {
		this.brandCode = brandCode;
	}
	
	public IRCode getLastIRCode(){
		int count = getIRCodeList().size();
		if (count ==  0){
			return null;
		}
		
		return getIRCodeList().get(count - 1);
	}
	
	
	public IRCode getIRCode(int index){
		for (IRCode ircode : getIRCodeList()) {
			if (ircode.getKeyIndex() == index){
				return ircode;
			}
		}
		
		return null;
	}
	
	

}
