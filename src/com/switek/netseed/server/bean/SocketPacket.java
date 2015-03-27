package com.switek.netseed.server.bean;

import java.io.UnsupportedEncodingException;
import java.nio.channels.SocketChannel;
import java.util.List;

import org.apache.mina.core.session.IoSession;

import net.sf.json.JSONObject;

import com.switek.netseed.server.ui.ServerForm;

public class SocketPacket {

	public final static short COMMAND_ID_CS_TESTING_NETWORK = 0x0002;
	public final static short COMMAND_ID_CS_HEARTBEAT = 0x0003;
	public final static short COMMAND_ID_SC_CONFIG_SUBCONTROLLER = 0x0004;
	public final static short COMMAND_ID_SC_REMOVE_SUBCONTROLLER = 0x0005;
	public final static short COMMAND_ID_SC_LEARN_IRCODE = 0x0006;
	public final static short COMMAND_ID_SC_CONTROL_DEVICE = 0x0007;
	public final static short COMMAND_ID_SC_REQUEST_SENSOR_DATA = 0x000D;
	public final static short COMMAND_ID_SC_MATCH_IRCODE = 0x0010;
	public final static short COMMAND_ID_SC_LEARN_RFCODE = 0x0011;
	public final static short COMMAND_ID_SC_CONTROL_RFDEVICE = 0x0012;
	public final static short COMMAND_ID_CS_SAPDEVICE_ALARM=0x0015;
	
	public final static short COMMAND_ID_QUERY_CTR = 0x0F01;
	public final static short COMMAND_ID_CONNECT_CTR = 0x0F04;
	public final static short COMMAND_ID_AS_REMOVE_DEVICE = 0x0F05;
	public final static short COMMAND_ID_GET_SUBCONTROLLERS = 0x0F06;
	public final static short COMMAND_ID_AS_REMOVE_SUBCONTROLLER = 0x0F07;
	public final static short COMMAND_ID_CONFIG_SUBCONTROLLER = 0x0F08;
	public final static short COMMAND_ID_MATCH_IRCODE = 0x0F09;
	public final static short COMMAND_ID_AS_LEARN_IRCODE = 0x0F10;
	public final static short COMMAND_ID_SUBMIT_MATCH_IRCODE = 0x0F11;
	public final static short COMMAND_ID_CONTROL_DEVICE = 0x0F12;
	public final static short COMMAND_ID_GET_DEVICES = 0x0F13;
	public final static short COMMAND_ID_SA_LEARN_IRCODE_ACK = 0x0F14;
	public static final short COMMAND_ID_REQUEST_SENSOR_DATA = 0x0F15;
	public final static short COMMAND_ID_CHECK_MOBILEAPP_VERSION = 0x0F20;
	public static final short COMMAND_ID_GET_FILE_CONTENT = 0x0F21;
	public static final short COMMAND_ID_GET_DEVICETYPE = 0x0F22;
	public static final short COMMAND_ID_GET_BRANDCODE = 0x0F23;
	public static final short COMMAND_ID_CREATE_TIMER = 0x0F24;
	public static final short COMMAND_ID_REMOVE_TIMER = 0x0F25;
	public final static short COMMAND_ID_AS_LEARN_RFCODE = 0x0F26;
	public final static short COMMAND_ID_SA_LEARN_RFCODE_ACK = 0x0F27;
	public final static short COMMAND_ID_CONTROL_RFDEVICE = 0x0F28;
	public final static short COMMAND_ID_AS_GET_MAX_KEYINDEX = 0x0F29;
	public static final short COMMAND_ID_DISABLE_TIMER = 0x0F30;
	public static final short COMMAND_ID_ADD_NETSEED_DEVICE = 0xF31;
	public static final short COMMAND_ID_CONTROL_NETSEED_DEVICE = 0xF32;	
	public final static short COMMAND_ID_SC_CONTROL_NETSEED_DEVICE = 0x000F;
	public static final short COMMAND_ID_GET_TIMER = 0x0F33;
	public static final short COMMAND_ID_AS_GET_IRCODE_COUNT=0x0F34;
	public static final short COMMAND_ID_AS_GET_DEVICE_STATUS=0x0F35;
	public static final short COMMAND_ID_AS_UPDATE_PUSH_STATUS=0x0F36;
	public static final short COMMAND_ID_AS_UPDATE_ASPDEVICE_STATUS=0x0F37;
	public static final short COMMAND_ID_AS_ADD_SAPDEVICE=0x0F38;
	public static final short COMMAND_ID_AS_GET_SAPDEVICE_STATUS=0x0F39;
	public static final short COMMAND_ID_AS_JPUSH_HISTORY=0x0F3A;
	

	public static final int PACKET_LEN_WITHOUTDATA = 2 + 2 + 6 + 1 + 2 + 2;
	public static final int DATA_START_INDEX = 2 + 2 + 6 + 1 + 2;

	// private boolean isKeepTheConnection = false;
	// /**
	// * @return the isKeepTheConnection
	// */
	// public boolean isKeepTheConnection() {
	// return isKeepTheConnection;
	// }
	//
	// /**
	// * @param isKeepTheConnection the isKeepTheConnection to set
	// */
	// public void setKeepTheConnection(boolean isKeepTheConnection) {
	// this.isKeepTheConnection = isKeepTheConnection;
	// }

	private IoSession fromClient = null;

	/**
	 * @return the fromClient
	 */
	public IoSession getFromClient() {
		return fromClient;
	}

	public SocketPacket(IoSession channel) {
		this.fromClient = channel;
	}

	String packetType;

	public String getPacketType() {
		return packetType;
	}

	/**
	 * @param packetType
	 *            the packetType to set
	 */
	private void setPacketType(String packetType) {
		this.packetType = packetType;
	}

	private String controllerId = "";
	private byte extensionId = 0;
	private short commandId = 0;
	private byte[] commmandData = null;
	private String commandDataString = "";

	/**
	 * @return the commandDataString
	 */
	public String getCommandDataString() {
		return commandDataString;
	}
	
	public boolean isFromApp(){
		return getCommandId() >= COMMAND_ID_QUERY_CTR; 
	}

	/**
	 * @param commandDataString
	 *            the commandDataString to set
	 */
	public void setCommandDataString(String commandDataString) {
		this.commandDataString = commandDataString;
	}

	private byte[] crc;
	private List<Byte> rawdata = null;

	/**
	 * @return the rawdata
	 */
	public List<Byte> getRawdata() {
		return rawdata;
	}

	/**
	 * @param rawdata
	 *            the rawdata to set
	 */
	public void setRawdata(List<Byte> rawdata) {
		this.rawdata = rawdata;

		if (rawdata == null) {
			return;
		}

		int dataLen = rawdata.size() - PACKET_LEN_WITHOUTDATA;
		commandData = new byte[dataLen];
		for (int i = 0; i < dataLen; i++) {
			commandData[i] = rawdata.get(DATA_START_INDEX + i);
		}
		String string = "";
		try {
			string = new String(commandData, "utf-8");
		} catch (UnsupportedEncodingException e) {
			ServerForm.showLog(e);
		}
		setCommandDataString(string);
	}

	private byte[] commandData = null;

	public byte[] getCommandData() {
		return commandData;
	}

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
	 * @return the extensionId
	 */
	public byte getExtensionId() {
		return extensionId;
	}

	public String getExtensionIdString() {
		return String.valueOf(extensionId & 0xff);
	}

	/**
	 * @param extensionId
	 *            the extensionId to set
	 */
	public void setExtensionId(byte extensionId) {
		this.extensionId = extensionId;
	}

	/**
	 * @return the commandId
	 */
	public short getCommandId() {
		return commandId;
	}

	/**
	 * @param commandId
	 *            the commandId to set
	 */
	public void setCommandId(short commandId) {
		this.commandId = commandId;

		String typeString = "0x" + Integer.toHexString(commandId).toUpperCase() + "_";
		switch (commandId) {
		case SocketPacket.COMMAND_ID_CS_TESTING_NETWORK:
			typeString += "COMMAND_ID_CS_TESTING_NETWORK";
			break;
		case SocketPacket.COMMAND_ID_CS_HEARTBEAT:
			typeString += "COMMAND_ID_CS_HEARTBEAT";
			break;
		case SocketPacket.COMMAND_ID_SC_CONFIG_SUBCONTROLLER:
			typeString += "COMMAND_ID_SC_CONFIG_SUBCONTROLLER";
			break;
		case SocketPacket.COMMAND_ID_SC_REMOVE_SUBCONTROLLER:
			typeString += "COMMAND_ID_SC_REMOVE_SUBCONTROLLER";
			break;
		case SocketPacket.COMMAND_ID_SC_LEARN_IRCODE:
			typeString += "COMMAND_ID_SC_LEARN_IRCODE";
			break;
		case SocketPacket.COMMAND_ID_SC_CONTROL_DEVICE:
			typeString += "COMMAND_ID_SC_CONTROL_DEVICE";
			break;
		case SocketPacket.COMMAND_ID_SC_REQUEST_SENSOR_DATA:
			typeString += "COMMAND_ID_SC_REQUEST_SENSOR_DATA";
			break;
		case SocketPacket.COMMAND_ID_CS_SAPDEVICE_ALARM:
			typeString += "COMMAND_ID_CS_SAPDEVICE_ALARM";
			break;
		case SocketPacket.COMMAND_ID_QUERY_CTR:
			typeString += "COMMAND_ID_QUERY_CTR";
			break;
		case SocketPacket.COMMAND_ID_CONNECT_CTR:
			typeString += "COMMAND_ID_CONNECT_CTR";
			break;
		case SocketPacket.COMMAND_ID_AS_REMOVE_DEVICE:
			typeString += "COMMAND_ID_AS_REMOVE_DEVICE";
			break;
		case SocketPacket.COMMAND_ID_GET_SUBCONTROLLERS:
			typeString += "COMMAND_ID_GET_SUBCONTROLLERS";
			break;
		case SocketPacket.COMMAND_ID_AS_REMOVE_SUBCONTROLLER:
			typeString += "COMMAND_ID_AS_REMOVE_SUBCONTROLLER";
			break;
		case SocketPacket.COMMAND_ID_CONFIG_SUBCONTROLLER:
			typeString += "COMMAND_ID_CONFIG_SUBCONTROLLER";
			break;
		case SocketPacket.COMMAND_ID_MATCH_IRCODE:
			typeString += "COMMAND_ID_MATCH_IRCODE";
			break;
		case SocketPacket.COMMAND_ID_AS_LEARN_IRCODE:
			typeString += "COMMAND_ID_AS_LEARN_IRCODE";
			break;
		case SocketPacket.COMMAND_ID_SUBMIT_MATCH_IRCODE:
			typeString += "COMMAND_ID_SUBMIT_MATCH_IRCODE";
			break;
		case SocketPacket.COMMAND_ID_CONTROL_DEVICE:
			typeString += "COMMAND_ID_CONTROL_DEVICE";
			break;
		case SocketPacket.COMMAND_ID_GET_DEVICES:
			typeString += "COMMAND_ID_GET_DEVICES";
			break;
		case SocketPacket.COMMAND_ID_SA_LEARN_IRCODE_ACK:
			typeString += "COMMAND_ID_SA_LEARN_IRCODE_ACK";
			break;
		case SocketPacket.COMMAND_ID_REQUEST_SENSOR_DATA:
			typeString += "COMMAND_ID_REQUEST_SENSOR_DATA";
			break;
		case SocketPacket.COMMAND_ID_CHECK_MOBILEAPP_VERSION:
			typeString += "COMMAND_ID_CHECK_MOBILEAPP_VERSION";
			break;
		case SocketPacket.COMMAND_ID_GET_FILE_CONTENT:
			typeString += "COMMAND_ID_GET_FILE_CONTENT";
			break;
		case SocketPacket.COMMAND_ID_SC_LEARN_RFCODE:
			typeString += "COMMAND_ID_SC_LEARN_RFCODE";
			break;
		case SocketPacket.COMMAND_ID_SC_CONTROL_RFDEVICE:
			typeString += "COMMAND_ID_SC_CONTROL_RFDEVICE";
			break;
		case SocketPacket.COMMAND_ID_CREATE_TIMER:
			typeString += "COMMAND_ID_CREATE_TIMER";
			break;
		case SocketPacket.COMMAND_ID_REMOVE_TIMER:
			typeString += "COMMAND_ID_REMOVE_TIMER";
			break;
		case SocketPacket.COMMAND_ID_AS_LEARN_RFCODE:
			typeString += "COMMAND_ID_AS_LEARN_RFCODE";
			break;
		case SocketPacket.COMMAND_ID_SA_LEARN_RFCODE_ACK:
			typeString += "COMMAND_ID_SA_LEARN_RFCODE_ACK";
			break;
		case SocketPacket.COMMAND_ID_CONTROL_RFDEVICE:
			typeString += "COMMAND_ID_CONTROL_RFDEVICE";
			break;
		case COMMAND_ID_AS_GET_MAX_KEYINDEX:
			typeString += "COMMAND_ID_AS_GET_MAX_KEYINDEX";
			break;
		case COMMAND_ID_DISABLE_TIMER:
			typeString += "COMMAND_ID_DISABLE_TIMER";
			break;
		case COMMAND_ID_ADD_NETSEED_DEVICE:
			typeString += "COMMAND_ID_ADD_NETSEED_DEVICE";
			break;
		case COMMAND_ID_GET_BRANDCODE:
			typeString +="COMMAND_ID_GET_BRANDCODE";
			break;
		case COMMAND_ID_GET_TIMER:
			typeString +="COMMAND_ID_GET_TIMER";
			break;
		case COMMAND_ID_AS_GET_IRCODE_COUNT:
			typeString +="COMMAND_ID_AS_GET_IRCODE_COUNT";
			break;
		case COMMAND_ID_AS_GET_DEVICE_STATUS:
			typeString +="COMMAND_ID_AS_GET_DEVICE_STATUS";
			break;
		case COMMAND_ID_AS_UPDATE_PUSH_STATUS:
			typeString +="COMMAND_ID_AS_UPDATE_PUSH_STATUS";
			break;
		case COMMAND_ID_AS_ADD_SAPDEVICE:
			typeString +="COMMAND_ID_AS_ADD_SAPDEVICE";
			break;
		case COMMAND_ID_AS_JPUSH_HISTORY:
			typeString +="COMMAND_ID_AS_JPUSH_HISTORY";
			break;
		default:
			typeString += "_UNKNOWN";
			break;
		}

		setPacketType(typeString);
	}

	/**
	 * @return the commmandData
	 */
	public byte[] getCommmandData() {
		return commmandData;
	}

	/**
	 * @param commmandData
	 *            the commmandData to set
	 */
	public void setCommmandData(byte[] commmandData) {
		this.commmandData = commmandData;
	}

	/**
	 * @return the crc
	 */
	public byte[] getCrc() {
		return crc;
	}

	/**
	 * @param crc
	 *            the crc to set
	 */
	public void setCrc(byte[] crc) {
		this.crc = crc;
	}

	long receiveTime = 0;

	/**
	 * @return the receiveTime
	 */
	public long getReceiveTime() {
		return receiveTime;
	}

	/**
	 * @param receiveTime
	 *            the receiveTime to set
	 */
	public void setReceiveTime(long receiveTime) {
		this.receiveTime = receiveTime;
	}

	public JSONObject getJsonBody() {
		JSONObject jsonObject = JSONObject.fromObject(getCommandDataString());
		return jsonObject.getJSONObject("Body");
	}

}
