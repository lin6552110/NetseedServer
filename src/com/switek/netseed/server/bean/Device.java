package com.switek.netseed.server.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.dao.DeviceDao;

public class Device {

	public Device(){
		
	}
	public static final int DEVICE_TYPE_AC = 1;
	public static final int DEVICE_TYPE_TV = 2;
	public static final int DEVICE_TYPE_SOCKET = 6;
	public static final int DEVICE_TYPE_CURTAIN = 9;
	public static final int DEVICE_TYPE_DOORCONTACT=0x80;
	public static final int DEVICE_TYPE_SMARTSENS=0x81;
	public static final int DEVICE_TYPE_INDOOR_INTRARED_RALARM_SIRENN=0x82;
	public static final int DEVICE_TYPE_BREAK_GLASS_DETECTOR=0x83;
	public static final int DEVICE_TYPE_PHOTOELECTRIC_SMOKE_DETECTOR=0x84;
	public static final int DEVICE_TYPE_PANIC_ALARMS_SOS_BUTTON=0x85;
	public static final int DEVICE_TYPE_GAS_LEAKAGE_ALARM=0x86;
	public static final int DEVICE_TYPE_OUTDOOR_PIR_SENSOR=0x87;
	public static final int DEVICE_TYPE_CURTIAN_DETECTOR=0x88;
	public static final int DEVICE_TYPE_LEAK_ALARM=0x89;
	
	SubController owner;
	int circuitCount = 0;
	private String jsonData="";
	
	public int getCircuitCount() {
		return circuitCount;
	}

	public void setCircuitCount(int circuitCount) {
		this.circuitCount = circuitCount;
	}

	long lastEditDT = 0;

	/**
	 * @return the lastEditDT
	 */
	public long getLastEditDT() {
		return lastEditDT;
	}

	/**
	 * @param lastEditDT
	 *            the lastEditDT to set
	 */
	public void setLastEditDT(long lastEditDT) {
		this.lastEditDT = lastEditDT;
	}

	/**
	 * @return the owner
	 */
	public SubController getOwner() {
		return owner;
	}

	public Device(SubController owner) {
		this.owner = owner;
		this.controllerId = owner.getParent().getControllerId();
		this.subcontrollerId = owner.getControllerId();
	}

	public static final String DEVICE_ID_NEW = "{NEW_DEVICE}";

	String controllerId = "";
	String subcontrollerId = "";
	
	IRCode standardIRCode = null;
	
	public IRCode getStandardIRCode() {

		// Use standard IRCode if IRCode Index >= 0
		if (getIRCodeIndex() >= 0) {
			IRCode irCode = DeviceManager.getStandardIRCode(getDeviceType(),
					getBrandCode(), getIRCodeIndex());

			return irCode;
		}
		return null;
	}
	
//	public void setStandardIRCode(IRCode standardIRCode) {
//		this.standardIRCode = standardIRCode;
//	}

	/**
	 * @return the controllerId
	 */
	public String getControllerId() {
		return controllerId;
	}

	/**
	 * @param controllerId
	 *            the controllerId to set
	 */
	public void setControllerId(String controllerId) {
		this.controllerId = controllerId;
	}

	/**
	 * @return the subcontrollerId
	 */
	public String getSubcontrollerId() {
		return subcontrollerId;
	}

	/**
	 * @param subcontrollerId
	 *            the subcontrollerId to set
	 */
	public void setSubcontrollerId(String subcontrollerId) {
		this.subcontrollerId = subcontrollerId;
	}

	String deviceId = DEVICE_ID_NEW;
	String deviceName = "";
	int deviceType;
	String brandCode = "";
	int IRCodeIndex = -1;
	boolean isLearnedIRCode = false;
	String status = "";
	Date registerDT;
	String registerBy = "";

	Hashtable<Integer, IRCode> learnedIRCodes = new Hashtable<>();

	/**
	 * @return the deviceId
	 */
	public String getDeviceId() {
		return deviceId;
	}

	public static boolean isNewDevice(String deviceId) {
		return deviceId.equalsIgnoreCase(DEVICE_ID_NEW);
	}

	/**
	 * @return the iRCodes
	 */
	public Hashtable<Integer, IRCode> getLearnedIRCodes() {
		return learnedIRCodes;
	}

	public boolean existsIRCode(int keyIndex) {
		return getLearnedIRCodes().containsKey(keyIndex);
	}

	public IRCode getLearnedIRCode(int keyIndex) {
		return getLearnedIRCodes().get(keyIndex);
	}

	public void putIRCode(int keyIndex, IRCode IRCode) {
		getLearnedIRCodes().put(keyIndex, IRCode);
	}

	/**
	 * @param iRCodeList
	 *            the iRCodes to set
	 */
	public void setIRCodes(Hashtable<Integer, IRCode> IRCodeList) {
		learnedIRCodes = IRCodeList;
	}

	/**
	 * @param deviceId
	 *            the deviceId to set
	 */
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	/**
	 * @return the deviceName
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * @param deviceName
	 *            the deviceName to set
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	/**
	 * @return the deviceType
	 */
	public int getDeviceType() {
		return deviceType;
	}

	public boolean isSensor() {
		return getDeviceType() == 0;
	}

	/**
	 * @param deviceType
	 *            the deviceType to set
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
	 * @param brandCode
	 *            the brandCode to set
	 */
	public void setBrandCode(String brandCode) {
		this.brandCode = brandCode;
	}

	/**
	 * Get the index of standard IR code.
	 * 
	 * @return the iRCodeIndex
	 */
	public int getIRCodeIndex() {
		return IRCodeIndex;
	}

	/**
	 * @param iRCodeIndex
	 *            the iRCodeIndex to set
	 */
	public void setIRCodeIndex(int iRCodeIndex) {
		IRCodeIndex = iRCodeIndex;
	}

	/**
	 * @return the isLearnedIRCode
	 */
	public boolean isLearnedIRCode() {
		return isLearnedIRCode;
	}

	/**
	 * @param isLearnedIRCode
	 *            the isLearnedIRCode to set
	 */
	public void setLearnedIRCode(boolean isLearnedIRCode) {
		this.isLearnedIRCode = isLearnedIRCode;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the registerDT
	 */
	public Date getRegisterDT() {
		return registerDT;
	}

	/**
	 * @param registerDT
	 *            the registerDT to set
	 */
	public void setRegisterDT(Date registerDT) {
		this.registerDT = registerDT;
	}

	/**
	 * @return the registerBy
	 */
	public String getRegisterBy() {
		return registerBy;
	}

	/**
	 * @param registerBy
	 *            the registerBy to set
	 */
	public void setRegisterBy(String registerBy) {
		this.registerBy = registerBy;
	}

	long lastPressedKey=0;

	/**
	 * @return the lastPressedKey
	 */
	public long getLastPressedKey() {
		return lastPressedKey;
	}

	/**
	 * @param lastPressedKey the lastPressedKey to set
	 */
	public void setLastPressedKey(long lastPressedKey) {
		this.lastPressedKey = lastPressedKey;
	}	
	
	int deviceIndex=0;

	public int getDeviceIndex() {
		return deviceIndex;
	}

	public void setDeviceIndex(int deviceIndex) {
		this.deviceIndex = deviceIndex;
	}

	public String getJsonData() {
		return jsonData;
	}

	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
	}
	
	public  int getNewDeviceIndex(String controllerId,String subcontrollerId,int deviceType){
		List list=new ArrayList();
		DeviceDao dao=new DeviceDao();
		int newDeviceIndex=0;
		list=dao.getDeviceIndexByType(controllerId,subcontrollerId,deviceType);
		for(int i=0;i<list.size();i++){
			if(i!=(int)list.get(i)){
				newDeviceIndex=i;
				return newDeviceIndex;
			}
		}
		if(0==newDeviceIndex){
			newDeviceIndex=list.size();
		}	
		
		return newDeviceIndex;
	}
	
	public String getDeviceTypeString(){
		String str="";
		switch (deviceType) {
		case Device.DEVICE_TYPE_DOORCONTACT:
			str=deviceName+"(门磁 )";
			break;
		case Device.DEVICE_TYPE_SMARTSENS:
			str=deviceName+"(红外感应器)";
			break;
		case Device.DEVICE_TYPE_INDOOR_INTRARED_RALARM_SIRENN:
			str=deviceName+"(红外感应报警)";
			break;
		case Device.DEVICE_TYPE_PHOTOELECTRIC_SMOKE_DETECTOR:
			str=deviceName+"(光电感烟探测器)";
			break;
		case Device.DEVICE_TYPE_BREAK_GLASS_DETECTOR:
			str=deviceName+"(玻璃破碎感应器)";
			break;
		case Device.DEVICE_TYPE_PANIC_ALARMS_SOS_BUTTON:
			str=deviceName+"(SOS按钮)";
			break;
		case Device.DEVICE_TYPE_GAS_LEAKAGE_ALARM:
			str=deviceName+"(燃气感应)";
			break;
		case Device.DEVICE_TYPE_OUTDOOR_PIR_SENSOR:
			str=deviceName+"(红外栏栅)";
			break;
		case Device.DEVICE_TYPE_CURTIAN_DETECTOR:
			str=deviceName+"(帘幕感应)";
			break;
		case Device.DEVICE_TYPE_LEAK_ALARM:
			str=deviceName+"(漏水报警)";
			break;
		default:
			str=deviceName;
			break;
		}
		return str;
	}
	
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
