package com.switek.netseed.server.io.socket.strategy;

import com.switek.netseed.server.bean.SocketPacket;

public class CommContext {
	private CommStrategy comm;
	private SocketPacket packet;

	public CommContext(SocketPacket packet) {
		// TODO Auto-generated constructor stub
		this.packet=packet;
		switch (packet.getCommandId()) {
			case SocketPacket.COMMAND_ID_CS_TESTING_NETWORK:
				comm=new ControllerTestingNetwork();
				break;
			case SocketPacket.COMMAND_ID_CS_HEARTBEAT:
				comm=new Heartbeat();
				break;
			case SocketPacket.COMMAND_ID_QUERY_CTR:
				comm=new QueryController();
				break;
			case SocketPacket.COMMAND_ID_CONNECT_CTR:
				comm=new Connect2Controller();
				break;
			case SocketPacket.COMMAND_ID_CONTROL_DEVICE:
			case SocketPacket.COMMAND_ID_CONTROL_RFDEVICE:
				comm=new ControlDevice();
				break;
			case SocketPacket.COMMAND_ID_GET_SUBCONTROLLERS:
				comm=new GetSubcontrollers();
				break;
			case SocketPacket.COMMAND_ID_MATCH_IRCODE:
				comm=new MatchIRCode();
				break;
			case SocketPacket.COMMAND_ID_SUBMIT_MATCH_IRCODE:
				comm=new SubmitIRCodeMatch();
				break;
			case SocketPacket.COMMAND_ID_AS_LEARN_IRCODE:
				comm=new CommLearnIRCode();
				break;
			case SocketPacket.COMMAND_ID_AS_LEARN_RFCODE:
				comm=new CommLearnRFCode();
				break;
			case SocketPacket.COMMAND_ID_AS_REMOVE_DEVICE:
				comm=new CommRemoveDevice();
				break;
			case SocketPacket.COMMAND_ID_AS_REMOVE_SUBCONTROLLER:
				comm=new CommRemoveSubcontroller();
				break;
			case SocketPacket.COMMAND_ID_GET_DEVICES:
				comm=new CommGetDevices();
				break;
			case SocketPacket.COMMAND_ID_CONFIG_SUBCONTROLLER:
				comm=new CommConfigSubcontroller();
				break;
			case SocketPacket.COMMAND_ID_REQUEST_SENSOR_DATA:
				comm=new CommRequestSensorData();
				break;
			case SocketPacket.COMMAND_ID_CHECK_MOBILEAPP_VERSION:
				comm=new CommCheckAppVersion();
				break;
			case SocketPacket.COMMAND_ID_GET_FILE_CONTENT:
				comm=new CommGetFileContent();
				break;
			case SocketPacket.COMMAND_ID_SC_CONTROL_DEVICE:
			case SocketPacket.COMMAND_ID_SC_CONTROL_RFDEVICE:
				comm=new CommControlDeviceACK();
				break;
			case SocketPacket.COMMAND_ID_GET_DEVICETYPE:
				comm=new CommGetDeviceType();
				break;
			case SocketPacket.COMMAND_ID_GET_BRANDCODE:
				comm=new CommGetBrandCode();
				break;
			case SocketPacket.COMMAND_ID_CREATE_TIMER:
				comm=new CommCreateTimer();
				break;
			case SocketPacket.COMMAND_ID_DISABLE_TIMER:
				comm=new CommDisableTimer();
				break;
			case SocketPacket.COMMAND_ID_REMOVE_TIMER:
				comm=new CommRemoveTimer();
				break;
			case SocketPacket.COMMAND_ID_AS_GET_MAX_KEYINDEX:				
				comm=new CommGetMaxKeyIndex();
				break;	
			case SocketPacket.COMMAND_ID_SC_REMOVE_SUBCONTROLLER:
			case SocketPacket.COMMAND_ID_SC_CONFIG_SUBCONTROLLER:
			case SocketPacket.COMMAND_ID_SC_LEARN_IRCODE:
			case SocketPacket.COMMAND_ID_SC_LEARN_RFCODE:
			case SocketPacket.COMMAND_ID_SC_REQUEST_SENSOR_DATA:
				comm=new CommControllerReplied();
				break;
			case SocketPacket.COMMAND_ID_ADD_NETSEED_DEVICE:
				comm=new CommAddNetseedDevice();
				break;
			case SocketPacket.COMMAND_ID_CONTROL_NETSEED_DEVICE:
				comm=new CommControlNetseedDevice();
				break;
			case SocketPacket.COMMAND_ID_SC_CONTROL_NETSEED_DEVICE:
				comm=new CommControlNetseedDeviceACK();
				break;
			case SocketPacket.COMMAND_ID_GET_TIMER:
				comm=new CommGetTimer();
				break;
			case SocketPacket.COMMAND_ID_AS_GET_IRCODE_COUNT:
				comm=new CommGetIRCodeCount();
				break;
			case SocketPacket.COMMAND_ID_AS_GET_DEVICE_STATUS:
				comm=new CommGetDeviceStatus();
				break;
			case SocketPacket.COMMAND_ID_AS_UPDATE_PUSH_STATUS:
				comm=new CommUpdatePushStatus();
				break;
			case SocketPacket.COMMAND_ID_CS_SAPDEVICE_ALARM:
				comm=new CommSAPDeviceAlarm();
				break;
			case SocketPacket.COMMAND_ID_AS_UPDATE_ASPDEVICE_STATUS:
				comm=new CommUpdateSAPDeviceStatus();
				break;
			case SocketPacket.COMMAND_ID_AS_ADD_SAPDEVICE:
				comm=new CommAddSAPDevice();
				break;
			case SocketPacket.COMMAND_ID_AS_GET_SAPDEVICE_STATUS:
				comm=new CommGetSAPDeviceStatus();
			default:
				break;
		}
	}
	
	public void analysisPacket(){
		if(null!=comm){
			comm.analysisPacket(packet);
		}
	}
}
