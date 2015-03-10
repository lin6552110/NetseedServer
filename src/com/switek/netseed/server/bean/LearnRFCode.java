package com.switek.netseed.server.bean;

public class LearnRFCode extends LearnIRCode {

	
	private int deviceIndex=0;
	
	
	public int getDeviceIndex() {
		return deviceIndex;
	}


	public void setDeviceIndex(int deviceIndex) {
		this.deviceIndex = deviceIndex;
	}


	public static LearnRFCode parse(byte[] rawdata) throws IllegalArgumentException  {
		if (rawdata.length <= 3){
			throw new IllegalArgumentException("Data must more than 3 bytes.");
		}
		LearnRFCode learnRFCode = new LearnRFCode();
		byte deviceTypeByte = rawdata[0];
		int nKeyIndex = rawdata[1];
		int nDeviceIndex = rawdata[2];

		int len = rawdata.length - 3;
		byte[] IRCodeBytes = new byte[len];
		for (int i = 3; i < rawdata.length; i++) {
			IRCodeBytes[i-3] = rawdata[i];	
		}
		
		learnRFCode.setDeviceType(deviceTypeByte & 0xff);
		learnRFCode.setKeyIndex(nKeyIndex & 0xff);
		learnRFCode.setDeviceIndex(nDeviceIndex & 0xff);
		learnRFCode.setIRCodeData(IRCodeBytes);
		
		return learnRFCode;
		
	}

}
