package com.switek.netseed.server.bean;

import com.switek.netseed.util.FormatTransfer;


public class LearnIRCode extends IRCode {

	public LearnIRCode() {
	}
	
	int deviceType;
	
	/**
	 * @return the type
	 */
	public int getDeviceType() {
		return deviceType;
	}
	/**
	 * @param type the type to set
	 */
	public void setDeviceType(int deviceType) {
		this.deviceType = deviceType;
	}
	
	
	//学习码转换：设备类型（1个字节）、按键序数（2个字节）、学习码（若干字节）
	public static LearnIRCode parse(byte[] rawdata) throws IllegalArgumentException  {
		if (rawdata.length <= 3){
			throw new IllegalArgumentException("Data must more than 3 bytes.");
		}
		LearnIRCode learnIRCode = new LearnIRCode();
		byte deviceTypeByte = rawdata[0];
		byte[] keyIndexBytes = new byte[2];
		keyIndexBytes[0] = rawdata[1];
		keyIndexBytes[1] = rawdata[2];

		int len = rawdata.length - 3;
		byte[] IRCodeBytes = new byte[len];
		for (int i = 3; i < rawdata.length; i++) {
			IRCodeBytes[i-3] = rawdata[i];	
		}
		
		learnIRCode.setDeviceType(deviceTypeByte & 0xff);
		learnIRCode.setKeyIndex(FormatTransfer.lBytesToShort(keyIndexBytes));
		learnIRCode.setIRCodeData(IRCodeBytes);
		
		return learnIRCode;
		
	}
}
