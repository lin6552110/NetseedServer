package com.switek.netseed.server.io.socket;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.processors.JsonBeanProcessor;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;

import com.switek.netseed.server.AppManager;
import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.SessionManager;
import com.switek.netseed.server.Utils;
import com.switek.netseed.server.bean.ActionResult;
import com.switek.netseed.server.bean.AppVersion;
import com.switek.netseed.server.bean.Brand;
import com.switek.netseed.server.bean.ConfigSubcontrollerResult;
import com.switek.netseed.server.bean.Controller;
import com.switek.netseed.server.bean.ControllerTimer;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.DeviceTypeDef;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.IRCode;
import com.switek.netseed.server.bean.LearnIRCode;
import com.switek.netseed.server.bean.LearnIRCodeResult;
import com.switek.netseed.server.bean.LearnRFCode;
import com.switek.netseed.server.bean.PacketSendResult;
import com.switek.netseed.server.bean.SensorData;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.StandardIRCode;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.bean.TimerStep;
import com.switek.netseed.server.dal.IDAL;
import com.switek.netseed.server.dal.MySQLDAL;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;
import com.switek.netseed.util.CRC16;
import com.switek.netseed.util.FormatTransfer;
import com.switek.netseed.util.MyBase64;

public class AsyncSocketMsgHandlerBAK extends Thread {

	private static long CONNECT_TIMEOUT = 1 * 60 * 1000;
	private static final int READ_TIMEOUT = 1 * 20 * 1000;
	private static Logger logger = Logger
			.getLogger(AsyncSocketMsgHandler.class);

	static IDAL mDAL = MySQLDAL.newInstance();

	SocketPacket triggerPacket;

	public AsyncSocketMsgHandlerBAK(SocketPacket packet) {
		this.triggerPacket = packet;
	}

	@Override
	public void run() {
		try {
			// System.out.println(triggerPacket.getControllerId() + "," +
			// Thread.currentThread().getId() + ": " +
			// System.currentTimeMillis());
			if (triggerPacket != null) {
				handleMessage(triggerPacket);
			}
		} catch (Exception e) {
			ServerForm.showLog(MsgType.Error,
					"Error " + e.getLocalizedMessage(), e);
		}

	}

	private final static Hashtable<String, SocketPacket> mRepliedPackets = new Hashtable<>();

	private void saveRepliedPacket(SocketPacket packet) {
		String key;
		if (packet.getCommandId() == SocketPacket.COMMAND_ID_SC_CONFIG_SUBCONTROLLER
				|| packet.getCommandId() == SocketPacket.COMMAND_ID_SC_REMOVE_SUBCONTROLLER) {
			key = packet.getControllerId() + "_" + packet.getCommandId();
		} else {
			key = packet.getControllerId() + "_"
					+ packet.getExtensionIdString() + "_"
					+ packet.getCommandId();
		}
		mRepliedPackets.put(key, packet);

		Controller controller = DeviceManager.getController(packet
				.getControllerId());
		if (controller != null) {
			controller.setLastHeartbeatDT(System.currentTimeMillis());
		}
	}

	private static void removeRepliedPacket(String controllerId,
			String subcontrollerId, short commandId) {
		String key;
		if (commandId == SocketPacket.COMMAND_ID_CONFIG_SUBCONTROLLER
				|| commandId == SocketPacket.COMMAND_ID_SC_CONFIG_SUBCONTROLLER
				|| commandId == SocketPacket.COMMAND_ID_SC_REMOVE_SUBCONTROLLER) {
			key = controllerId + "_" + commandId;
		} else {
			key = controllerId + "_" + subcontrollerId + "_" + commandId;
		}
		mRepliedPackets.remove(key);
	}

	private static SocketPacket waitRepliedPacket(String controllerId,
			String subcontrollerId, short commandId) throws IOException {
		String key;
		if (commandId == SocketPacket.COMMAND_ID_CONFIG_SUBCONTROLLER
				|| commandId == SocketPacket.COMMAND_ID_SC_CONFIG_SUBCONTROLLER
				|| commandId == SocketPacket.COMMAND_ID_SC_REMOVE_SUBCONTROLLER) {
			key = controllerId + "_" + commandId;
		} else {
			key = controllerId + "_" + subcontrollerId + "_" + commandId;
		}
		SocketPacket packet = null;
		long startTime = System.currentTimeMillis();
		while (packet == null) {
			if ((System.currentTimeMillis() - startTime) >= READ_TIMEOUT) {
				throw new IOException("Read timeout.");
			}
			packet = mRepliedPackets.remove(key);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return packet;
	}

	/**
	 * 检查是否CRC正确
	 * 
	 * @param packetBytes
	 * @return
	 */
	public static boolean isValidCRC(List<Byte> packetBytes) {
		byte[] validCRC = CRC16.calcCRC(packetBytes, 2,
				packetBytes.size() - 2 - 1);
		int crcStartIndex = packetBytes.size() - 2;
		if (validCRC[0] == packetBytes.get(crcStartIndex)
				&& validCRC[1] == packetBytes.get(crcStartIndex + 1)) {
			return true;
		}
		return false;
	}

	private void handleMessage(SocketPacket packet) {

		ServerForm.showLog(
				MsgType.Debug,
				"****************************Received "
						+ packet.getPacketType() + ": "
						+ Utils.bytes2HexString(packet.getRawdata())
						+ " from: " + packet.getFromClient());
		if (packet.getCommandDataString().length() > 0 && packet.isFromApp()) {
			ServerForm.showLog(MsgType.Debug,
					"Data: " + packet.getCommandDataString());
		}

		try {
			// 考虑用命令模式。每种包都对应的解析执行类.
			switch (packet.getCommandId()) {
			case SocketPacket.COMMAND_ID_CS_TESTING_NETWORK:
				onControllerTestingNetwork(packet);
				break;
			case SocketPacket.COMMAND_ID_CS_HEARTBEAT:
				onHeartbeat(packet);
				break;
			case SocketPacket.COMMAND_ID_QUERY_CTR:
				onQueryController(packet);
				break;
			case SocketPacket.COMMAND_ID_CONNECT_CTR:
				connect2Controller(packet);
				break;
			case SocketPacket.COMMAND_ID_CONTROL_DEVICE:
				onControlDevice(packet);
				break;
			case SocketPacket.COMMAND_ID_CONTROL_RFDEVICE:
				onControlDevice(packet);
				break;
			case SocketPacket.COMMAND_ID_GET_SUBCONTROLLERS:
				onGetSubcontrollers(packet);
				break;
			case SocketPacket.COMMAND_ID_MATCH_IRCODE:
				onMatchIRCode(packet);
				break;
			case SocketPacket.COMMAND_ID_SUBMIT_MATCH_IRCODE:
				onSubmitIRCodeMatch(packet);
				break;
			case SocketPacket.COMMAND_ID_AS_LEARN_IRCODE:
				onLearnIRCode(packet);
				break;
			case SocketPacket.COMMAND_ID_AS_LEARN_RFCODE:
				onLearnRFCode(packet);
				break;
			case SocketPacket.COMMAND_ID_AS_REMOVE_DEVICE:
				onRemoveDevice(packet);
				break;
			case SocketPacket.COMMAND_ID_AS_REMOVE_SUBCONTROLLER:
				onRemoveSubcontroller(packet);
				break;
			case SocketPacket.COMMAND_ID_GET_DEVICES:
				onGetDevices(packet);
				break;
			case SocketPacket.COMMAND_ID_CONFIG_SUBCONTROLLER:
				onConfigSubcontroller(packet);
				break;
			case SocketPacket.COMMAND_ID_REQUEST_SENSOR_DATA:
				onRequestSensorData(packet);
				break;
			case SocketPacket.COMMAND_ID_CHECK_MOBILEAPP_VERSION:
				onCheckAppVersion(packet);
				break;
			case SocketPacket.COMMAND_ID_GET_FILE_CONTENT:
				onGetFileContent(packet);
				break;
			case SocketPacket.COMMAND_ID_SC_CONTROL_DEVICE:
			case SocketPacket.COMMAND_ID_SC_CONTROL_RFDEVICE:
				onControlDeviceACK(packet);
				break;
			case SocketPacket.COMMAND_ID_GET_DEVICETYPE:
				onGetDeviceType(packet);
				break;
			case SocketPacket.COMMAND_ID_GET_BRANDCODE:
				onGetBrandCode(packet);
				break;
			case SocketPacket.COMMAND_ID_CREATE_TIMER:
				onCreateTimer(packet);
				break;
			case SocketPacket.COMMAND_ID_DISABLE_TIMER:
				onDisableTimer(packet);
				break;
			case SocketPacket.COMMAND_ID_REMOVE_TIMER:
				onRemoveTimer(packet);
				break;
			case SocketPacket.COMMAND_ID_AS_GET_MAX_KEYINDEX:				
				onGetMaxKeyIndex(packet);
				break;	
			case SocketPacket.COMMAND_ID_SC_REMOVE_SUBCONTROLLER:
			case SocketPacket.COMMAND_ID_SC_CONFIG_SUBCONTROLLER:
			case SocketPacket.COMMAND_ID_SC_LEARN_IRCODE:
			case SocketPacket.COMMAND_ID_SC_LEARN_RFCODE:
			case SocketPacket.COMMAND_ID_SC_REQUEST_SENSOR_DATA:
				onControllerReplied(packet);
				break;

			case SocketPacket.COMMAND_ID_ADD_NETSEED_DEVICE:
				onAddNetseedDevice(packet);
				break;
			case SocketPacket.COMMAND_ID_CONTROL_NETSEED_DEVICE:
				onControlNetseedDevice(packet);
				break;
			case SocketPacket.COMMAND_ID_SC_CONTROL_NETSEED_DEVICE:
				onControlNetseedDeviceACK(packet);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			ServerForm.showLog(e);
			try {
				if (packet.getCommandId() >= SocketPacket.COMMAND_ID_QUERY_CTR) {
					Map<String, Object> replyBody = new HashMap<>();
					replyBody.put("ErrorMsg", e.getLocalizedMessage());
					sendPacket(packet, packet.getControllerId(),
							packet.getExtensionId(), packet.getCommandId(),
							ErrorCode.ERROR_UNKNOWN_EXCEPTION, "1.0", replyBody);
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		} finally {
			try {
				if (packet.getCommandId() >= SocketPacket.COMMAND_ID_QUERY_CTR) {
					System.out.println("关闭 session......");
					packet.getFromClient().close(true);
					
				}
			} catch (Exception e) {
				logger.error(e);
			}
		}

	}

	private void onControlNetseedDeviceACK(SocketPacket packet) {

		String controllerId = packet.getControllerId();
		String subcontrollerId = packet.getExtensionIdString();
		SubController subController = DeviceManager.getSubcontroller(
				controllerId, subcontrollerId);
		if (subController == null) {
			ServerForm.showLog(MsgType.Warn, String.format(
					"Invalid controller %s or subcontroller %s.", controllerId,
					subcontrollerId));
			return;
		}
		if (subController.getParent() != null) {
			subController.getParent().setLastHeartbeatDT(
					System.currentTimeMillis());
		}

		onControllerReplied(packet);

		byte[] body = packet.getCommandData();
		int deviceType = body[0] & 0xff;
		
		byte[] bytesDeviceId = new byte[6];
		for (int i = 0; i < 6; i++) {
			bytesDeviceId[i] = body[1+i];
		}	
		String deviceId = Utils.bytes2HexString2(bytesDeviceId);		
		ServerForm.debugMsg(String.format(
				"Controller %s, subcontroller %s, deviceId: %s.", controllerId,
				subcontrollerId, deviceId));
		
		int circuitNo = body[7] & 0xff;
		if (circuitNo <= 0 || circuitNo > 9){
			ServerForm.showLog(MsgType.Warn, String.format(
					"Invalid circuit No %s.", circuitNo));
			return;
		}
		int value = body[8] & 0xff;		
	
		Device device = subController.getDevice(deviceId);
		if (device == null) {
			ServerForm.showLog(MsgType.Warn, String.format(
					"Could not find device by Id %s.", deviceId));
			return;
		}
		
		String binaryString = Long.toBinaryString(device.getLastPressedKey());
		binaryString = StringUtils.leftPad(binaryString, 64, "0");
		
		String circuitValue = StringUtils.leftPad(Integer.toBinaryString(value), 8, "0");
		circuitValue= circuitValue.substring(1);

		ServerForm.debugMsg(String.format("LastPressedKey: %s, binaryString: %s, circuit No: %s, new circuit value: %s.", device.getLastPressedKey(), binaryString, circuitNo, circuitValue));
		//从右向左计算，最多9个回路(9*7=63bits)，最高位一个bit不用。
		
		int startIndex =  (9 - circuitNo)  * 7 + 1;
		int endIndex = startIndex + 7;
		
		binaryString = binaryString.substring(0, startIndex) + circuitValue + binaryString.substring(endIndex);
		BigInteger newKeyValue = new BigInteger(binaryString, 2);
		
		boolean result = mDAL.updateDeviceLastPressKey(device, newKeyValue.longValue());
		ServerForm.debugMsg(String.format(
				"Update last pressed key to %s for device: %s. %s.",
				newKeyValue.longValue(), device.getDeviceId(), result));

	}

	private void onControlNetseedDevice(SocketPacket packet) {
		String controllerId = packet.getControllerId();
		ServerForm.showLog(MsgType.Debug,
				"Try to control device of the contoller " + controllerId);

		String subcontrollerId = packet.getExtensionIdString();
		JSONObject jsonBody = packet.getJsonBody();
		String deviceId = jsonBody.getString("DeviceId");
		int circuitNo = jsonBody.getInt("CircuitNo");
		int value = jsonBody.getInt("Value");

		ActionResult result = controlNetseedDevice(controllerId, subcontrollerId,
				deviceId,  circuitNo, value);
		int resultCode = result.getResultCode();
		ServerForm.showLog(MsgType.Debug, "Result code: " + resultCode);
		if (resultCode != ErrorCode.NO_ERROR){
		sendPacketWithoutBody(packet, controllerId, packet.getExtensionId(),
				SocketPacket.COMMAND_ID_CONTROL_DEVICE, resultCode);
		}		else{
			SocketPacket feedback = (SocketPacket) result.getOutputObject();
			
			byte[] body = feedback.getCommandData();
			circuitNo = body[7] & 0xff;
			value = body[8] & 0xff;		
		
			Map<String, Object> replyBody = new HashMap<>();		
			replyBody.put("CircuitIndex", circuitNo);
			replyBody.put("CircuitValue", value);

			sendPacket(packet, packet.getControllerId(), packet.getExtensionId(),
					SocketPacket.COMMAND_ID_CONTROL_DEVICE, result.getResultCode(), "1.0", replyBody);
		}
	}
	
	

	public static ActionResult controlNetseedDevice(String controllerId,
			String subcontrollerId, String deviceId, int circuitNo, int value) {
		SubController subcontroller = DeviceManager.getSubcontroller(
				controllerId, subcontrollerId);

		ActionResult actionResult = new ActionResult();
		if (subcontroller == null) {
			actionResult.setResultCode(ErrorCode.ERROR_INVALID_CONTROLLERID);
			return actionResult;
		}
		Device device = subcontroller.getDevice(deviceId);
		if (device == null) {
			actionResult.setResultCode(ErrorCode.ERROR_INVALID_DEVICEID);
			return actionResult;
		}

		int deviceType = device.getDeviceType();
		byte[] deviceIdBytes = Controller
				.convertControllerId2Bytes(deviceId);
		
		/*
		AA,AA,  16,00,  DE,07,03,04,00,00,  00,  0F,00,0C（设备类型ID）, (六个字节的设备ID数据)，03（回路编号），00（设备功能状态），??,??
		 */		
		int len = 1 + 6 + 1 + 1;
		byte[] cmdDataBytes = new byte[len];
		cmdDataBytes[0] = (byte) deviceType;		
		
		//device Id
		cmdDataBytes[1] = deviceIdBytes[0];
		cmdDataBytes[2] = deviceIdBytes[1];
		cmdDataBytes[3] = deviceIdBytes[2];
		cmdDataBytes[4] = deviceIdBytes[3];
		cmdDataBytes[5] = deviceIdBytes[4];
		cmdDataBytes[6] = deviceIdBytes[5];		
		
		cmdDataBytes[7] = (byte)circuitNo;
		
		cmdDataBytes[8] = (byte)value;
		
		
		/*
		主控器返回的数据为：
		AA,AA,  16,00,  DE,07,03,04,00,00,  00,  0F,00,0C（设备类型ID）, (六个字节的设备ID数据)，03（回路编号），00（设备功能状态），??,??
		 */
		SocketPacket conn = connect2Controller(controllerId);
		if (conn == null) {
			ServerForm.showLog(MsgType.Debug,
					"Couldn't connect to controller: " + controllerId);
			actionResult.setResultCode(ErrorCode.ERROR_COULDNOT_CONNECT_CTRL);
			return actionResult;
		}
		
		short commandId = SocketPacket.COMMAND_ID_SC_CONTROL_NETSEED_DEVICE;		

		PacketSendResult result = sendPacket(conn, controllerId,
				Byte.valueOf(subcontrollerId), commandId, cmdDataBytes, true);
		if (!result.isSuccessful()) {
			actionResult.setResultCode(ErrorCode.ERROR_NETWORK_ISSUE);
			return actionResult;
		}

		actionResult.setOutputObject(result.getFeedbackPacket());
		return actionResult;
	}

	private void onAddNetseedDevice(SocketPacket packet) {
		ActionResult result = addNetseedDevice(packet);
		sendPacketWithoutBody(packet, packet.getControllerId(), packet.getExtensionId(),
				packet.getCommandId(), result.getResultCode());
	}
	
	private ActionResult addNetseedDevice(SocketPacket packet){
		
		ActionResult result = new ActionResult();

		JSONObject jsonBody = packet.getJsonBody();

		String controllerId = packet.getControllerId();
		String subcontrollerId = packet.getExtensionIdString();
		String deviceId = jsonBody.getString("DeviceId");
		SubController subcontroller = DeviceManager.getSubcontroller(controllerId, subcontrollerId);
		if (subcontroller==null){			
			result.setResultCode(ErrorCode.ERROR_INVALID_SUBCONTROLLERID);
			return result;
		}
		Device device = getDevice(controllerId, subcontrollerId, deviceId);	
		int resultCode = 0;
		if (device != null) {
			resultCode = ErrorCode.ERROR_INVALID_DEVICEID;
			result.setResultCode(resultCode);
			return result;
		}
		
		String deviceName = jsonBody.getString("DeviceName");
		int deviceType = jsonBody.getInt("Type");
		int deviceIndex = jsonBody.getInt("DeviceIndex");
		int circuitCount = jsonBody.getInt("CircuitCount");
		String brandCode = jsonBody.getString("BrandCode");
		
		
		device = new Device(subcontroller);

		device.setControllerId(controllerId);
		device.setSubcontrollerId(subcontrollerId);
		device.setDeviceId(deviceId);
		device.setDeviceName(deviceName);
		device.setDeviceType(deviceType);
		device.setDeviceIndex(deviceIndex);
		device.setBrandCode(brandCode);
		device.setIRCodeIndex(-1);
		device.setLearnedIRCode(false);
		device.setCircuitCount(circuitCount);
		ServerForm
		.showLog(String.format("Will create a device %s for controller: %s / %s.", deviceId, controllerId, subcontrollerId));
		
		boolean addResult = mDAL.addDevice(device);
		if (!addResult) {
			ServerForm.errorMsg("Failed to create device.");
			resultCode = ErrorCode.ERROR_FAILED_CREATE_DEVICE;
			result.setResultCode(resultCode);
			return result;
		}
		// cache the device
		subcontroller.addDevice(device.getDeviceId(), device);
		return result;		
	}

	private void onDisableTimer(SocketPacket packet) {
		ActionResult result = disableTimer(packet);
		
		Map<String, Object> replyBody = new HashMap<>();		

		sendPacket(packet, packet.getControllerId(), packet.getExtensionId(),
				packet.getCommandId(), result.getResultCode(), "1.0", replyBody);
	}

private ActionResult disableTimer(SocketPacket packet){
		
		ActionResult result = new ActionResult();
		JSONObject body = packet.getJsonBody();

		String Id = body.getString("Id");
		int nEnabled = body.getInt("Enabled");
		boolean enabled = (nEnabled == 1);
		
		logger.info("disableTimer: " + Id + ", Enabled: " + enabled);
		
		ControllerTimer timer = DeviceManager.getTimer(Id);
		if (timer ==null){
			result.setResultCode(ErrorCode.ERROR_INVALID_TIMERID);
			return result;
		}	
				
		boolean	b = mDAL.disableTimer(timer, enabled);
	
		if (!b){
			result.setResultCode(ErrorCode.ERROR_FAILED_UPDATEDB);
			return result;
		}		
		
		timer.setEnabled(enabled);
		DeviceManager.addTimer(timer);
		
		result.setOutputObject(timer);
		return result;		
	}

	private void onGetMaxKeyIndex(SocketPacket packet) {
		ActionResult result = getMaxKeyIndex(packet);
		Map<String, Object> replyBody = new HashMap<>();
		if(result.isSuccessful()){
			replyBody.put("MaxKeyIndex", result.getOutputObject());
		}		

		sendPacket(packet, packet.getControllerId(), packet.getExtensionId(),
				packet.getCommandId(), result.getResultCode(), "1.0", replyBody);		
	}

	private ActionResult getMaxKeyIndex(SocketPacket packet) {
		ActionResult result = new ActionResult();
		String controllerId = packet.getControllerId();
		String subcontrollerId = packet.getExtensionIdString();
				
		JSONObject body = packet.getJsonBody();
		String deviceId = body.getString("DeviceId");
		SubController  subctrl = DeviceManager.getSubcontroller(controllerId, subcontrollerId);
		if(subctrl ==null){
			result.setResultCode(ErrorCode.ERROR_INVALID_SUBCONTROLLERID);
			return result;
		}
		Device device = subctrl.getDevice(deviceId);
		if (device == null){
			result.setResultCode(ErrorCode.ERROR_INVALID_DEVICEID);
			return result;
		}
		
		Hashtable<Integer, IRCode> codes = device.getLearnedIRCodes();
		Enumeration<IRCode> en = codes.elements();

		int maxKeyIndex = -1;
		while (en.hasMoreElements()) {
			IRCode code = en.nextElement();
			if (code.getKeyIndex() > maxKeyIndex){
				maxKeyIndex = code.getKeyIndex();
			}
		}
		result.setOutputObject(maxKeyIndex);
		return result;
	}

	private ActionResult createTimer(SocketPacket packet){
		
		ActionResult result = new ActionResult();
		JSONObject body = packet.getJsonBody();
		String name = body.getString("Name");
		int weekdays = body.getInt("Weekdays");
		String time = body.getString("Time");
		
		String Id = "";
		if(body.containsKey("Id")){
			Id = body.getString("Id");
		}
		
		boolean isNewTimer = ControllerTimer.isNewTimer(Id);
		
		if(!isNewTimer){
			ControllerTimer timer = DeviceManager.getTimer(Id);
			if (timer ==null){
				result.setResultCode(ErrorCode.ERROR_INVALID_TIMERID);
				return result;
			}
		}
		
		ControllerTimer timer = new ControllerTimer();
		timer.setTimerId(Id);
		timer.setName(name);
		timer.setWeekDays(weekdays);
		timer.setTime(time);
		
		JSONArray steps = body.getJSONArray("Steps");
		int seqNo = 10;
		for (int i = 0; i < steps.size(); i++) {
				JSONObject step = (JSONObject) steps.get(i);
				String stepName = step.getString("Name");
				String controllerId = step.getString("ControllerId");
				String subcontrollerId = step.getString("ExtId");
				String deviceId = step.getString("DeviceId");
				SubController sub = DeviceManager.getSubcontroller(controllerId, subcontrollerId);
				if (sub == null){
					result.setResultCode(ErrorCode.ERROR_INVALID_CONTROLLERID);
					return result;
				}
				Device device = sub.getDevice(deviceId);
				if (device ==null){
					result.setResultCode(ErrorCode.ERROR_INVALID_DEVICEID);
					return result;
				}
				
				int keyIndex = step.getInt("Key"	);
				int delay = step.getInt("Delay");
				
				int value = 0;
				if(step.containsKey("Value")){
					value = step.getInt("Value");
				}
				
				
				TimerStep timerStep = new TimerStep();
				timerStep.setName(stepName);
				timerStep.setControllerId(controllerId);
				timerStep.setSubcontrollerId(subcontrollerId);
				timerStep.setDeviceId(deviceId);
				timerStep.setValue(value);
				
				timerStep.setKeyIndex(keyIndex);
				timerStep.setDelay(delay);
				timerStep.setSeqNo(seqNo);
				seqNo += 10;
				
				timer.addStep(timerStep);
		}
		
		boolean b = false;
		if (isNewTimer){
			b = mDAL.createTimer(timer);
		}else{
			b = mDAL.updateTimer(timer);
		}
		
		if (!b){
			result.setResultCode(ErrorCode.ERROR_FAILED_UPDATEDB);
			return result;
		}		
			
		DeviceManager.addTimer(timer);
		
		result.setOutputObject(timer);
		return result;		
	}
	
	
private ActionResult removeTimer(SocketPacket packet){
		
		ActionResult result = new ActionResult();
		JSONObject body = packet.getJsonBody();
		String timerId = body.getString("TimerId");
		
		ControllerTimer timer = DeviceManager.getTimers().get(timerId);
		if (timer == null){
			result.setResultCode(ErrorCode.ERROR_INVALID_TIMERID);
			return result;
		}

		boolean b = mDAL.removeTimer(timer);
		if (!b){
			result.setResultCode(ErrorCode.ERROR_FAILED_UPDATEDB);
			return result;
		}
		timer.setDeleted(true);
		return result;
		
	}

	private void onRemoveTimer(SocketPacket packet){
		ActionResult result = removeTimer(packet);
		sendPacketWithoutBody(packet, packet.getControllerId(), packet.getExtensionId(),
				packet.getCommandId(), result.getResultCode());
	}
	
	private void onCreateTimer(SocketPacket packet){
		ActionResult result = createTimer(packet);
				
		Map<String, Object> replyBody = new HashMap<>();
		if(result.isSuccessful()){
			replyBody.put("TimerId", ((ControllerTimer)result.getOutputObject()).getTimerId());
		}		

		sendPacket(packet, packet.getControllerId(), packet.getExtensionId(),
				packet.getCommandId(), result.getResultCode(), "1.0", replyBody);
	}
	
	private void onGetBrandCode(SocketPacket packet) {
		int type = packet.getJsonBody().getInt("Type");

		DeviceTypeDef def = DeviceManager.getDeviceTypeDef(type);
		List<Map<String, Object>> replyBody = new ArrayList<>();
		int resultCode = 0;
		if (def == null) {
			resultCode = ErrorCode.ERROR_INVALID_DEVICETYPE;
		} else {
			for (Brand brand : def.getBrands()) {
				HashMap<String, Object> typeJsonMap = new HashMap<>();
				typeJsonMap.put("BrandCode", brand.getBrandCode());
				typeJsonMap.put("Name", brand.getBrandName());
				typeJsonMap.put("LOGO", brand.getLogoUrl());
				replyBody.add(typeJsonMap);
			}
		}

		sendPacket(packet, packet.getControllerId(), packet.getExtensionId(),
				SocketPacket.COMMAND_ID_GET_BRANDCODE, resultCode, "1.0",
				replyBody);
	}

	public void onGetDeviceType(SocketPacket packet) {
		Hashtable<Integer, DeviceTypeDef> list = DeviceManager
				.getDeviceTypeList();
		Enumeration<DeviceTypeDef> en = list.elements();

		int resultCode = 0;

		List<Map<String, Object>> replyBody = new ArrayList<>();
		while (en.hasMoreElements()) {
			DeviceTypeDef def = en.nextElement();
			HashMap<String, Object> typeJsonMap = new HashMap<>();
			typeJsonMap.put("Type", def.getDeviceType());
			typeJsonMap.put("Name", def.getName());
			typeJsonMap.put("LOGO", def.getLogoUrl());
			replyBody.add(typeJsonMap);
		}

		sendPacket(packet, packet.getControllerId(), packet.getExtensionId(),
				SocketPacket.COMMAND_ID_GET_DEVICETYPE, resultCode, "1.0",
				replyBody);
	}

	private void onControllerReplied(SocketPacket packet) {
		ServerForm.debugMsg("Save replied packet." + packet.getCommandId());
		saveRepliedPacket(packet);
	}

	private void onControlDeviceACK(SocketPacket packet) {
		String controllerId = packet.getControllerId();
		String subcontrollerId = packet.getExtensionIdString();
		SubController subController = DeviceManager.getSubcontroller(
				controllerId, subcontrollerId);
		if (subController == null) {
			ServerForm.showLog(MsgType.Warn, String.format(
					"Invalid controller %s or subcontroller %s.", controllerId,
					subcontrollerId));
			return;
		}
		if (subController.getParent() != null) {
			subController.getParent().setLastHeartbeatDT(
					System.currentTimeMillis());
		}

		onControllerReplied(packet);

		byte[] body = packet.getCommandData();
		int deviceType = body[0] & 0xff;
		
		int keyIndex = 0;
		int deviceIndex = 0;
		if (packet.getCommandId() == SocketPacket.COMMAND_ID_SC_CONTROL_RFDEVICE){
			keyIndex = body[1];
			deviceIndex = body[2];
		}else{
			byte[] keyIndexBytes = new byte[2];
			keyIndexBytes[0] = body[1];
			keyIndexBytes[1] = body[2];
			keyIndex = FormatTransfer.lBytesToShort(keyIndexBytes);
		}

		Device device = subController.getDeviceByType(deviceType, deviceIndex);
		
		if (device == null) {
			ServerForm.showLog(MsgType.Warn, String.format(
					"Could not find device by type %s.", deviceType));
			return;
		}

		/*
		 * 设备类型  6，7，8，9 : 只有 默认的按键 需要更新状态， （开，关，停），  
		 *  其余的 自定义按键 （索引大于20） 的只要知道 执行了没 就行了，
		 *  没有 开，关，停 的状态
		 */
		if (device.getDeviceType()  >= Device.DEVICE_TYPE_SOCKET && device.getDeviceType()  <= Device.DEVICE_TYPE_CURTAIN){
			if (keyIndex > 20){
				return;
			}
		}	
		
		if (device.getDeviceType() == Device.DEVICE_TYPE_AC){
			// 空调的不保存  1000 以上的
			if (keyIndex > 1000){
				return;
			}
		}

		boolean result = mDAL.updateDeviceLastPressKey(device, keyIndex);
		ServerForm.debugMsg(String.format(
				"Update last pressed key for device: %s. %s.",
				device.getDeviceId(), result));

	}

	private void onGetFileContent(SocketPacket packet) {
		String data = packet.getCommandDataString();

		JSONObject jsonObject = JSONObject.fromObject(data);
		JSONObject jsonBody = jsonObject.getJSONObject("Body");

		int resultCode;
		String fileName = jsonBody.getString("FileName");
		fileName = "files\\" + fileName;
		File file = new File(fileName);
		byte[] bytesFile = null;
		if (!file.exists()) {
			resultCode = ErrorCode.ERROR_COULDNOT_FIND_FILE;
			bytesFile = FormatTransfer.toLH(resultCode);
		} else {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);

				// 4 bytes for result code.
				int len = fis.available() + 4;
				bytesFile = new byte[len];
				int index = 4;
				int bufferSize = 512;
				while (fis.available() > 0) {
					if (len - index < 512) {
						bufferSize = len - index;
					}
					int readLen = fis.read(bytesFile, index, bufferSize);
					if (readLen == 0) {
						break;
					}
					index += readLen;
				}
			} catch (IOException e) {
				resultCode = ErrorCode.ERROR_READ_FILE;
				bytesFile = FormatTransfer.toLH(resultCode);
				ServerForm.showLog(e);
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					fis = null;
				}
			}
		}

		sendPacket(packet, packet.getControllerId(), packet.getExtensionId(),
				SocketPacket.COMMAND_ID_GET_FILE_CONTENT, bytesFile);
	}

	private void onRequestSensorData(SocketPacket packet) {
		String controllerId = packet.getControllerId();
		byte subcontrollerId = packet.getExtensionId();

		ActionResult result = requestSensorData(controllerId, subcontrollerId,
				packet);
		int resultCode = result.getResultCode();
		SensorData sensorData = (SensorData) result.getOutputObject();
		Map<String, Object> replyBody = new HashMap<>();
		if (sensorData != null) {
			replyBody.put("Temperature", sensorData.getTemperature());
			replyBody.put("Humidity", sensorData.getHumidity());
			replyBody.put("LastEditDT", sensorData.getLastEditDT());
		}
		sendPacket(packet, controllerId, subcontrollerId,
				SocketPacket.COMMAND_ID_REQUEST_SENSOR_DATA, resultCode, "1.0",
				replyBody);
	}

	private ActionResult requestSensorData(String controllerId,
			byte subcontrollerId, SocketPacket packet) {
		ActionResult result = new ActionResult();
		SubController subcontroller = DeviceManager.getSubcontroller(
				controllerId, subcontrollerId);
		int resultCode;
		if (subcontroller == null) {
			resultCode = ErrorCode.ERROR_INVALID_SUBCONTROLLERID;
			result.setResultCode(resultCode);
			ServerForm.warnMsg(String.format(
					"Invalid controller or subcontroller %s, %s.",
					controllerId, String.valueOf(subcontrollerId & 0xff)));
			return result;
		}
		SocketPacket conn = connect2Controller(controllerId);
		if (conn == null) {
			resultCode = ErrorCode.ERROR_COULDNOT_CONNECT_CTRL;
			result.setResultCode(resultCode);
			return result;
		}

		ServerForm
				.showLog(MsgType.Debug,
						"Sending the request to sensor and waiting for the feedback...");
		PacketSendResult sendResult = sendPacket(conn, controllerId,
				subcontrollerId,
				SocketPacket.COMMAND_ID_SC_REQUEST_SENSOR_DATA, null, true);
		// check the ack from controller
		SocketPacket feedbackPacket = sendResult.getFeedbackPacket();
		if (!sendResult.isSuccessful()
				|| sendResult.getFeedbackPacket() == null) {
			resultCode = ErrorCode.ERROR_NETWORK_ISSUE;
			result.setResultCode(resultCode);
			return result;
		}

		if (feedbackPacket.getCommandId() != SocketPacket.COMMAND_ID_SC_REQUEST_SENSOR_DATA) {
			resultCode = ErrorCode.ERROR_NETWORK_ISSUE;
			result.setResultCode(resultCode);
			return result;
		}

		SensorData sensorData = null;
		try {
			sensorData = SensorData.parse(feedbackPacket.getCommandData());
			result.setOutputObject(sensorData);
		} catch (Exception e) {
			resultCode = ErrorCode.ERROR_READ_SENSOR_DATA;
			result.setResultCode(resultCode);
			return result;
		}

		return result;
	}

	private void onCheckAppVersion(SocketPacket packet) {
		String data = packet.getCommandDataString();

		JSONObject jsonObject = JSONObject.fromObject(data);
		JSONObject jsonBody = jsonObject.getJSONObject("Body");

		String platform = jsonBody.getString("Platform");
		String curVersion = jsonBody.getString("CurrentVersion");
		String appName = "NetSeed";

		int resultCode = 0;

		AppVersion appVersion = AppManager.getAppInfo(appName, platform);
		boolean hasNewVersion = false;
		String releaseNotes = "";
		String latestVersion = curVersion;
		if (appVersion == null) {
			resultCode = ErrorCode.ERROR_INVALID_APP_NAME;
		} else {
			try {
				if (Utils.compareVersion(appVersion.getLatestVersion(),
						curVersion) > 0) {
					hasNewVersion = true;
					releaseNotes = appVersion.getReleaseNotes();
					latestVersion = appVersion.getLatestVersion();
				}
			} catch (NumberFormatException e) {
				resultCode = ErrorCode.ERROR_INVALID_VERSION_FORMAT;
			}
		}

		Map<String, Object> replyBody = new HashMap<>();
		replyBody.put("hasNewVersion", hasNewVersion);
		replyBody.put("Version", latestVersion);
		replyBody.put("ReleaseNotes", releaseNotes);

		sendPacket(packet, packet.getControllerId(), packet.getExtensionId(),
				packet.getCommandId(), resultCode, "1.0", replyBody);
	}

	private void onRemoveSubcontroller(SocketPacket packet) {
		String controllerId = packet.getControllerId();
		String subcontrollerId = packet.getExtensionIdString();

		int resultCode = removeSubcontroller(controllerId, subcontrollerId,
				packet);

		sendPacketWithoutBody(packet, controllerId, packet.getExtensionId(),
				SocketPacket.COMMAND_ID_AS_REMOVE_DEVICE, resultCode);
	}

	private int removeSubcontroller(String controllerId,
			String subcontrollerId, SocketPacket packet) {
		SubController subcontroller = DeviceManager.getSubcontroller(
				controllerId, subcontrollerId);
		ServerForm.showLog(MsgType.Warn, String.format(
				"Will remove this subcontroller Id: %s of controller: %s.",
				subcontrollerId, controllerId));

		int resultCode;
		if (subcontroller == null) {
			resultCode = ErrorCode.ERROR_INVALID_SUBCONTROLLERID;
			return resultCode;
		}

		if (subcontroller.isDefaultSubController()) {
			return ErrorCode.ERROR_REMOVE_DEFAULT_SUBCONTROLLER_PROHIBITED;
		}

		SocketPacket conn = connect2Controller(controllerId);
		if (conn == null) {
			resultCode = ErrorCode.ERROR_COULDNOT_CONNECT_CTRL;
			return resultCode;
		}

		ServerForm
				.showLog(MsgType.Debug,
						"Sending the remove request to controller and waiting for the feedback...");
		PacketSendResult sendResult = sendPacket(conn, controllerId,
				packet.getExtensionId(),
				SocketPacket.COMMAND_ID_SC_REMOVE_SUBCONTROLLER, null, true);
		// check the ack from controller
		SocketPacket feedbackPacket = sendResult.getFeedbackPacket();
		if (!sendResult.isSuccessful()
				|| sendResult.getFeedbackPacket() == null) {
			resultCode = ErrorCode.ERROR_NETWORK_ISSUE;
			return resultCode;
		}

		if (feedbackPacket.getCommandId() != SocketPacket.COMMAND_ID_SC_REMOVE_SUBCONTROLLER) {
			resultCode = ErrorCode.ERROR_NETWORK_ISSUE;
			return resultCode;
		}

		boolean updateResult = mDAL.deleteSubController(subcontroller);
		ServerForm.showLog(MsgType.Info, "Delete subcontroller result: "
				+ updateResult);
		if (!updateResult) {
			return ErrorCode.ERROR_FAILED_UPDATEDB;
		}

		subcontroller.getParent().removeSubController(subcontroller);
		subcontroller = null;

		return 0;
	}

	/**
	 * Remove the specified device in database.
	 * 
	 * @param packet
	 */
	private void onRemoveDevice(SocketPacket packet) {
		String data = packet.getCommandDataString();

		JSONObject jsonObject = JSONObject.fromObject(data);
		JSONObject jsonBody = jsonObject.getJSONObject("Body");

		String controllerId = packet.getControllerId();
		String subcontrollerId = packet.getExtensionIdString();
		String deviceId = jsonBody.getString("DeviceId");
		Device device = getDevice(controllerId, subcontrollerId, deviceId);
		ServerForm
				.showLog(
						MsgType.Warn,
						String.format(
								"Will remove this device: %s of subcontroller Id: %s of controller: %s.",
								deviceId, subcontrollerId, controllerId));
		int resultCode = 0;
		if (device == null) {
			resultCode = ErrorCode.ERROR_INVALID_DEVICEID;
		} else {
			if (device.isSensor()) {
				resultCode = ErrorCode.ERROR_REMOVE_SENSON_IS_PROHIBITED;
			} else {
				boolean updateResult = mDAL.deleteDevice(device);
				ServerForm.showLog(MsgType.Warn, "Delete device result: "
						+ updateResult);
				if (updateResult) {
					device.getOwner().removeDevice(device);
					device = null;
				} else {
					resultCode = ErrorCode.ERROR_FAILED_UPDATEDB;
				}
			}
		}

		sendPacketWithoutBody(packet, controllerId, packet.getExtensionId(),
				SocketPacket.COMMAND_ID_AS_REMOVE_DEVICE, resultCode);
	}

	private void onConfigSubcontroller(SocketPacket packet) {
		String controllerId = packet.getControllerId();
		byte subcontrollerId = packet.getExtensionId();

		ConfigSubcontrollerResult result = configSubcontroller(controllerId,
				packet);
		int resultCode = result.getResultCode();
		String strSubcontrollerId = result.getSubcontrollerId();
		Map<String, Object> replyBody = new HashMap<>();
		replyBody.put("ExtId", strSubcontrollerId);
		sendPacket(packet, controllerId, subcontrollerId,
				SocketPacket.COMMAND_ID_MATCH_IRCODE, resultCode, "1.0",
				replyBody);

	}

	private ConfigSubcontrollerResult configSubcontroller(String controllerId,
			SocketPacket packet) {
		ConfigSubcontrollerResult result = new ConfigSubcontrollerResult();
		Controller controller = DeviceManager.getController(controllerId);
		int resultCode;
		if (controller == null) {
			resultCode = ErrorCode.ERROR_INVALID_CONTROLLERID;
			result.setResultCode(resultCode);
			ServerForm.warnMsg(String.format("Invalid controller %s.",
					controllerId));
			return result;
		}
		SocketPacket conn = connect2Controller(controllerId);
		if (conn == null) {
			resultCode = ErrorCode.ERROR_COULDNOT_CONNECT_CTRL;
			result.setResultCode(resultCode);
			return result;
		}

		ServerForm
				.showLog(MsgType.Debug,
						"Sending the config request to controller and waiting for the feedback...");
		PacketSendResult sendResult = sendPacket(conn, controllerId, (byte) 0,
				SocketPacket.COMMAND_ID_SC_CONFIG_SUBCONTROLLER, null, true);
		// check the ack from controller
		SocketPacket feedbackPacket = sendResult.getFeedbackPacket();
		if (!sendResult.isSuccessful()
				|| sendResult.getFeedbackPacket() == null) {
			resultCode = ErrorCode.ERROR_NETWORK_ISSUE;
			result.setResultCode(resultCode);
			return result;
		}

		if (feedbackPacket.getCommandId() != SocketPacket.COMMAND_ID_SC_CONFIG_SUBCONTROLLER) {
			resultCode = ErrorCode.ERROR_NETWORK_ISSUE;
			result.setResultCode(resultCode);
			return result;
		}

		String newSubcontrollerId = feedbackPacket.getExtensionIdString();
		ServerForm
				.showLog(
						MsgType.Debug,
						"Got feedback. Will add a subcontroller to database. The new subcontroller Id is "
								+ newSubcontrollerId);

		SubController checkSubController = DeviceManager.getSubcontroller(
				controllerId, newSubcontrollerId);
		if (checkSubController != null) {
			ServerForm.warnMsg("The subcontroller Id is already exists."
					+ newSubcontrollerId);

			result.setResultCode(ErrorCode.ERROR_SUBCONTROLLERID_ALREADY_EXISTS);
			return result;
		}
		SubController newSubcontroller = new SubController(controller);
		newSubcontroller.setControllerId(newSubcontrollerId);
		newSubcontroller.setControllerName(newSubcontrollerId);
		// add sub-controller to db.
		boolean bUpdateDB = mDAL.addSubcontroller(newSubcontroller);
		if (!bUpdateDB) {
			result.setResultCode(ErrorCode.ERROR_FAILED_UPDATEDB);
			return result;
		}

		controller.addSubController(newSubcontrollerId, newSubcontroller);

		result.setSubcontrollerId(newSubcontrollerId);
		return result;
	}

	private void onGetDevices(SocketPacket packet) {
		String controllerId = packet.getControllerId();
		byte subcontrollerId = packet.getExtensionId();
		String strSubcontrollerId = String.valueOf(subcontrollerId & 0xff);
		boolean isAllSubcontrollers = (subcontrollerId == (byte) 0xff);

		Controller controller = DeviceManager.getController(controllerId);
		List<Map<String, Object>> replyBody = new ArrayList<>();

		String data = packet.getCommandDataString();

		JSONObject jsonObject = JSONObject.fromObject(data);
		JSONObject jsonBody = jsonObject.getJSONObject("Body");
		long lastUpdateDT = jsonBody.getLong("LatestIRCodeDT");

		int resultCode = 0;
		if (controller != null) {
			boolean validSubcontrollerId = isAllSubcontrollers;
			Hashtable<String, SubController> subcontrollers = controller
					.getSubControllers();
			Enumeration<SubController> en = subcontrollers.elements();
			while (en.hasMoreElements()) {
				SubController subcontroller = en.nextElement();
				if (isAllSubcontrollers
						|| subcontroller.getControllerId().equals(
								strSubcontrollerId)) {
					if (!isAllSubcontrollers
							&& subcontroller.getControllerId().equals(
									strSubcontrollerId)) {
						validSubcontrollerId = true;
					}
					Enumeration<Device> enDevice = subcontroller.getDevices()
							.elements();
					while (enDevice.hasMoreElements()) {
						Device device = enDevice.nextElement();
						Map<String, Object> replyBodyMap = new HashMap<>();
						replyBodyMap.put("ExtId",
								subcontroller.getControllerId());
						int deviceType = device.getDeviceType();
						String brandCode = device.getBrandCode();
						DeviceTypeDef def = DeviceManager
								.getDeviceTypeDef(deviceType);
						String deviceTypeName = "UNKNOWN";
						String brandName = "UNKNOWN";
						if (def != null) {
							deviceTypeName = def.getName();
							Brand brand = def.getBrand(brandCode);
							if (brand != null) {
								brandName = brand.getBrandName();
							}
						}
						replyBodyMap.put("DeviceId", device.getDeviceId());
						replyBodyMap.put("DeviceName", device.getDeviceName());
						replyBodyMap.put("DeviceType", deviceType);
						replyBodyMap.put("DeviceIndex", device.getDeviceIndex());
						replyBodyMap.put("DeviceTypeName", deviceTypeName);
						replyBodyMap.put("BrandCode", brandCode);
						replyBodyMap.put("BrandName", brandName);
						replyBodyMap.put("CircuitCount", device.getCircuitCount());

						// replyBodyMap.put("IsLearnedIRCode",
						// device.isLearnedIRCode());
						replyBodyMap
								.put("IRCodeIndex", device.getIRCodeIndex());
						String standardIRCode = "";
						boolean isIRCodeUpdated = false;
						if (device.getStandardIRCode() != null
								&& device.getLastEditDT() > lastUpdateDT) {
							isIRCodeUpdated = true;

							IRCode irCode = device.getStandardIRCode();
							byte[] codeData = null;
							if (irCode == null) {
								codeData = new byte[0];
								logger.warn("NULL IRCODE");
							} else {
								codeData = irCode.getIRCodeData();
							}

							standardIRCode = MyBase64.encode(codeData);
							replyBodyMap.put("StandardIRCodeUpdateDT",
									device.getLastEditDT());
						}
						replyBodyMap.put("StandardIRCode", standardIRCode);

						List<Map<String, Object>> IRCodeNodeList = new ArrayList<>();
						if (device.getDeviceType() != 0) {
							// if (!device.isLearnedIRCode()) {
							// if (device.getLastEditDT() > lastUpdateDT) {
							// isIRCodeUpdated = true;
							// Map<String, Object> IRCodeMap = new HashMap<>();
							//
							// IRCodeMap.put("KeyIndex",
							// device.getIRCodeIndex());
							//
							// IRCode irCode =
							// DeviceManager.getStandardIRCode(device.getDeviceType(),
							// device.getBrandCode(),
							// device.getIRCodeIndex());
							//
							// byte[] codeData = null;
							// if (irCode == null) {
							// codeData = new byte[0];
							// logger.warn("NULL IRCODE");
							// } else {
							// codeData = irCode.getIRCodeData();
							// }
							//
							// IRCodeMap.put("IRCode",
							// MyBase64.encode(codeData));
							//
							// IRCodeMap.put("LastEditDT",
							// device.getLastEditDT());
							// IRCodeNodeList.add(IRCodeMap);
							// }
							// } else {
							Enumeration<IRCode> enIRCode = device
									.getLearnedIRCodes().elements();
							while (enIRCode.hasMoreElements()) {
								IRCode irCode = enIRCode.nextElement();
								if (irCode.getLastEditDT() > lastUpdateDT) {
									isIRCodeUpdated = true;
									Map<String, Object> IRCodeMap = new HashMap<>();
									IRCodeMap.put("LastEditDT",
											irCode.getLastEditDT());
									IRCodeMap.put("KeyIndex",
											irCode.getKeyIndex());
									IRCodeMap.put("IRCode", MyBase64
											.encode(irCode.getIRCodeData()));
									IRCodeMap.put("CodeType", irCode.getCodeType());
									IRCodeNodeList.add(IRCodeMap);
								}
							}
							// }
						}

						replyBodyMap.put("IRCode", IRCodeNodeList);

						replyBodyMap.put("IRCodeUpdated", isIRCodeUpdated);
						replyBody.add(replyBodyMap);
					}
				}
			}

			if (!validSubcontrollerId) {
				resultCode = ErrorCode.ERROR_INVALID_SUBCONTROLLERID;
			}
		} else {
			resultCode = ErrorCode.ERROR_INVALID_CONTROLLERID;
		}

		sendPacket(packet, controllerId, subcontrollerId,
				SocketPacket.COMMAND_ID_GET_DEVICES, resultCode, "1.0",
				replyBody);
	}

	private LearnIRCodeResult learnIRCode(String controllerId,
			String subcontrollerId, JSONObject jsonBody, SocketPacket packet) {
		String deviceId = jsonBody.getString("DeviceId");
		String deviceName = jsonBody.getString("DeviceName");
		int deviceType = jsonBody.getInt("Type");
		byte byteDeviceType = (byte) deviceType;
		short keyIndex = (short) jsonBody.getInt("KeyIndex");

		String brandCode = "OTHERS";
		int resultCode = 0;

		LearnIRCodeResult learnResult = new LearnIRCodeResult();
		learnResult.setDeviceId(deviceId);

		SubController subcontroller = DeviceManager.getSubcontroller(
				controllerId, subcontrollerId);
		if (subcontroller == null) {
			resultCode = ErrorCode.ERROR_INVALID_CONTROLLERID;
			ServerForm.warnMsg(String.format(
					"Invalid controller %s or subcontroller %s.", controllerId,
					subcontrollerId));
			learnResult.setResultCode(resultCode);
			return learnResult;
		}
		SocketPacket conn = connect2Controller(controllerId);
		if (conn == null) {
			resultCode = ErrorCode.ERROR_COULDNOT_CONNECT_CTRL;
			learnResult.setResultCode(resultCode);
			return learnResult;
		}
		int len = 1 + 2;
		byte[] cmdData = new byte[len];

		cmdData[0] = byteDeviceType;
		byte[] indexBytes = FormatTransfer.toLH(keyIndex);
		cmdData[1] = indexBytes[0];
		cmdData[2] = indexBytes[1];

		ServerForm.showLog(MsgType.Debug,
				"Sending the learn request to controller, waiting for ACK...");

		PacketSendResult result = sendPacket(conn, controllerId,
				packet.getExtensionId(),
				SocketPacket.COMMAND_ID_SC_LEARN_IRCODE, cmdData, true);
		// check the ack from controller
		SocketPacket feedbackPacket = result.getFeedbackPacket();
		if (!result.isSuccessful() || result.getFeedbackPacket() == null) {
			resultCode = ErrorCode.ERROR_NETWORK_ISSUE;
			learnResult.setResultCode(resultCode);
			return learnResult;
		}
		if (feedbackPacket.getCommandId() != SocketPacket.COMMAND_ID_SC_LEARN_IRCODE) {
			resultCode = ErrorCode.ERROR_NETWORK_ISSUE;
			learnResult.setResultCode(resultCode);
			return learnResult;
		}

		removeRepliedPacket(controllerId, subcontrollerId,
				SocketPacket.COMMAND_ID_SC_LEARN_IRCODE);

		ServerForm.showLog(MsgType.Debug, "Sending ACK to app...");
		PacketSendResult sendResult = sendPacketWithoutBody(packet,
				controllerId, packet.getExtensionId(),
				SocketPacket.COMMAND_ID_SA_LEARN_IRCODE_ACK, 0);
		if (sendResult == null || !sendResult.isSuccessful()) {
			resultCode = ErrorCode.ERROR_NETWORK_ISSUE;
			learnResult.setResultCode(resultCode);
			return learnResult;
		}

		try {
			ServerForm.showLog(MsgType.Debug,
					"Waiting for the IRCode from controller.");
			// read learned IR code from controller.
			SocketPacket packetLearned = waitRepliedPacket(controllerId,
					subcontrollerId, SocketPacket.COMMAND_ID_SC_LEARN_IRCODE);
			if (packetLearned.getCommandId() != SocketPacket.COMMAND_ID_SC_LEARN_IRCODE) {
				resultCode = ErrorCode.ERROR_READ_LEARNED_IRCODE;
				learnResult.setResultCode(resultCode);
				return learnResult;
			}

			LearnIRCode learnIRCode = LearnIRCode.parse(packetLearned
					.getCommandData());
			if (learnIRCode.getKeyIndex() != keyIndex) {
				resultCode = ErrorCode.ERROR_LEARNED_IRCODE_KEYINDEX_MISMATCH;
				learnResult.setResultCode(resultCode);
				return learnResult;
			}
			Device device;
			boolean isNewDevice = Device.isNewDevice(deviceId);
			if (isNewDevice) {
				// Auto create a device;
				device = new Device(subcontroller);

				device.setControllerId(controllerId);
				device.setSubcontrollerId(subcontrollerId);
				device.setDeviceName(deviceName);
				device.setDeviceType(deviceType);
				device.setBrandCode(brandCode);
				device.setIRCodeIndex(-1);
				device.setLearnedIRCode(true);

				if (deviceType == Device.DEVICE_TYPE_AC) {
					if (subcontroller.existsDevice(Device.DEVICE_TYPE_AC)) {
						ServerForm
								.warnMsg("The controller has an AC device already: "
										+ controllerId + "-" + subcontrollerId);
						resultCode = ErrorCode.ERROR_AC_ALREADY_EXISTS;
						learnResult.setResultCode(resultCode);
						return learnResult;
					}
				}

				ServerForm
						.showLog("Will create a device automatic for controller: "
								+ controllerId + "-" + subcontrollerId);
				boolean addResult = mDAL.addDevice(device);
				if (!addResult) {
					ServerForm.errorMsg("Failed to create device.");
					resultCode = ErrorCode.ERROR_FAILED_CREATE_DEVICE;
					learnResult.setResultCode(resultCode);
					return learnResult;
				}

				// cache the device
				subcontroller.addDevice(device.getDeviceId(), device);
				learnResult.setDeviceId(device.getDeviceId());
				ServerForm.showLog("The new device Id is: "
						+ device.getDeviceId());

			} else {
				device = getDevice(controllerId, subcontrollerId, deviceId);
				if (device == null) {
					resultCode = ErrorCode.ERROR_INVALID_DEVICEID;
					learnResult.setResultCode(resultCode);
					return learnResult;
				}
			}

			boolean exists = device.existsIRCode(keyIndex);
			boolean updateResult;
			if (!exists) {
				updateResult = mDAL.addLearnedIRCode(device, learnIRCode);
			} else {
				updateResult = mDAL.updateLearnedIRCode(device, learnIRCode);
			}
			if (!updateResult) {
				resultCode = ErrorCode.ERROR_FAILED_UPDATEDB;
				learnResult.setResultCode(resultCode);
				return learnResult;
			}
			// update cached IR code.
			device.putIRCode(keyIndex, learnIRCode);
		} catch (Exception e) {
			ServerForm.showLog(e);
			resultCode = ErrorCode.ERROR_READ_LEARNED_IRCODE;
		}

		learnResult.setResultCode(resultCode);
		return learnResult;
	}

	private LearnIRCodeResult learnRFCode(String controllerId,
			String subcontrollerId, JSONObject jsonBody, SocketPacket packet) {
		
		LearnIRCodeResult learnResult = new LearnIRCodeResult();		
		
		if (!getPacketAPIVersion(packet).equals("1.1")) {			
			learnResult.setResultCode(ErrorCode.ERROR_NEWVERSION_APP_REQUIRED);
			return learnResult;
		}
		
		String deviceId = jsonBody.getString("DeviceId");
		String deviceName = jsonBody.getString("DeviceName");
		int deviceType = jsonBody.getInt("Type");
		byte byteDeviceType = (byte) deviceType;
		byte keyIndex = (byte) jsonBody.getInt("KeyIndex");
		byte deviceIndex = (byte) jsonBody.getInt("DeviceIndex");
		byte codeType = (byte) jsonBody.getInt("CodeType");

		String brandCode = "OTHERS";
		int resultCode = 0;		

		SubController subcontroller = DeviceManager.getSubcontroller(
				controllerId, subcontrollerId);
		if (subcontroller == null) {
			resultCode = ErrorCode.ERROR_INVALID_CONTROLLERID;
			ServerForm.warnMsg(String.format(
					"Invalid controller %s or subcontroller %s.", controllerId,
					subcontrollerId));
			learnResult.setResultCode(resultCode);
			return learnResult;
		}
		SocketPacket conn = connect2Controller(controllerId);
		if (conn == null) {
			resultCode = ErrorCode.ERROR_COULDNOT_CONNECT_CTRL;
			learnResult.setResultCode(resultCode);
			return learnResult;
		}
		int len = 1 + 2 + 1;
		byte[] cmdData = new byte[len];

		cmdData[0] = byteDeviceType;
		//byte[] indexBytes = FormatTransfer.toLH(keyIndex);
		cmdData[1] =keyIndex;
		cmdData[2] = deviceIndex;
		cmdData[3] = codeType;

		ServerForm.showLog(MsgType.Debug,
				"Sending the learn request to controller, waiting for ACK...");

		PacketSendResult result = sendPacket(conn, controllerId,
				packet.getExtensionId(),
				SocketPacket.COMMAND_ID_SC_LEARN_RFCODE, cmdData, true);
		// check the ack from controller
		SocketPacket feedbackPacket = result.getFeedbackPacket();
		if (!result.isSuccessful() || result.getFeedbackPacket() == null) {
			resultCode = ErrorCode.ERROR_NETWORK_ISSUE;
			learnResult.setResultCode(resultCode);
			return learnResult;
		}
		if (feedbackPacket.getCommandId() != SocketPacket.COMMAND_ID_SC_LEARN_RFCODE) {
			resultCode = ErrorCode.ERROR_NETWORK_ISSUE;
			learnResult.setResultCode(resultCode);
			return learnResult;
		}

		removeRepliedPacket(controllerId, subcontrollerId,
				SocketPacket.COMMAND_ID_SC_LEARN_RFCODE);

		ServerForm.showLog(MsgType.Debug, "Sending ACK to app...");
		PacketSendResult sendResult = sendPacketWithoutBody(packet,
				controllerId, packet.getExtensionId(),
				SocketPacket.COMMAND_ID_SA_LEARN_RFCODE_ACK, 0);
		if (sendResult == null || !sendResult.isSuccessful()) {
			resultCode = ErrorCode.ERROR_NETWORK_ISSUE;
			learnResult.setResultCode(resultCode);
			return learnResult;
		}

		try {
			ServerForm.showLog(MsgType.Debug,
					"Waiting for the RFCode from controller.");
			// read learned IR code from controller.
			SocketPacket packetLearned = waitRepliedPacket(controllerId,
					subcontrollerId, SocketPacket.COMMAND_ID_SC_LEARN_RFCODE);
			if (packetLearned.getCommandId() != SocketPacket.COMMAND_ID_SC_LEARN_RFCODE) {
				resultCode = ErrorCode.ERROR_READ_LEARNED_IRCODE;
				learnResult.setResultCode(resultCode);
				return learnResult;
			}

			LearnIRCode learnIRCode = LearnRFCode.parse(packetLearned
					.getCommandData());
			if (learnIRCode.getKeyIndex() != keyIndex) {
				resultCode = ErrorCode.ERROR_LEARNED_IRCODE_KEYINDEX_MISMATCH;
				learnResult.setResultCode(resultCode);
				return learnResult;
			}
			learnIRCode.setCodeType(codeType);
			Device device;
			boolean isNewDevice = Device.isNewDevice(deviceId);
			if (isNewDevice) {
				// Auto create a device;
				device = new Device(subcontroller);

				device.setControllerId(controllerId);
				device.setSubcontrollerId(subcontrollerId);
				device.setDeviceName(deviceName);
				device.setDeviceType(deviceType);
				device.setDeviceIndex(deviceIndex & 0xff);
				device.setBrandCode(brandCode);
				device.setIRCodeIndex(-1);
				device.setLearnedIRCode(true);

				if (deviceType == Device.DEVICE_TYPE_AC) {
					if (subcontroller.existsDevice(Device.DEVICE_TYPE_AC)) {
						ServerForm
								.warnMsg("The controller has an AC device already: "
										+ controllerId + "-" + subcontrollerId);
						resultCode = ErrorCode.ERROR_AC_ALREADY_EXISTS;
						learnResult.setResultCode(resultCode);
						return learnResult;
					}
				}

				ServerForm
						.showLog("Will create a device automatic for controller: "
								+ controllerId + "-" + subcontrollerId);
				boolean addResult = mDAL.addDevice(device);
				if (!addResult) {
					ServerForm.errorMsg("Failed to create device.");
					resultCode = ErrorCode.ERROR_FAILED_CREATE_DEVICE;
					learnResult.setResultCode(resultCode);
					return learnResult;
				}

				// cache the device
				subcontroller.addDevice(device.getDeviceId(), device);
				learnResult.setDeviceId(device.getDeviceId());
				ServerForm.showLog("The new device Id is: "
						+ device.getDeviceId());

			} else {
				device = getDevice(controllerId, subcontrollerId, deviceId);
				if (device == null) {
					resultCode = ErrorCode.ERROR_INVALID_DEVICEID;
					learnResult.setResultCode(resultCode);
					return learnResult;
				}
			}

			boolean exists = device.existsIRCode(keyIndex);
			boolean updateResult;
			if (!exists) {
				updateResult = mDAL.addLearnedIRCode(device, learnIRCode);
			} else {
				updateResult = mDAL.updateLearnedIRCode(device, learnIRCode);
			}
			if (!updateResult) {
				resultCode = ErrorCode.ERROR_FAILED_UPDATEDB;
				learnResult.setResultCode(resultCode);
				return learnResult;
			}
			// update cached IR code.
			device.putIRCode(keyIndex, learnIRCode);
		} catch (Exception e) {
			ServerForm.showLog(e);
			resultCode = ErrorCode.ERROR_READ_LEARNED_IRCODE;
		}

		learnResult.setResultCode(resultCode);
		return learnResult;
	}
	
	private void onLearnIRCode(SocketPacket packet) {
		String data = packet.getCommandDataString();

		JSONObject jsonObject = JSONObject.fromObject(data);
		JSONObject jsonBody = jsonObject.getJSONObject("Body");
		String controllerId = packet.getControllerId();
		byte subcontrollerId = packet.getExtensionId();
		String strSubcontrollerId = String.valueOf(subcontrollerId & 0xff);

		LearnIRCodeResult result = learnIRCode(controllerId,
				strSubcontrollerId, jsonBody, packet);
		int resultCode = result.getResultCode();
		String deviceId = result.getDeviceId();
		Map<String, Object> replyBody = new HashMap<>();
		replyBody.put("DeviceId", deviceId);
		sendPacket(packet, controllerId, subcontrollerId,
				SocketPacket.COMMAND_ID_AS_LEARN_IRCODE, resultCode, "1.0",
				replyBody);

	}

	private void onLearnRFCode(SocketPacket packet) {
		String data = packet.getCommandDataString();

		JSONObject jsonObject = JSONObject.fromObject(data);
		JSONObject jsonBody = jsonObject.getJSONObject("Body");
		String controllerId = packet.getControllerId();
		byte subcontrollerId = packet.getExtensionId();		
		String strSubcontrollerId = packet.getExtensionIdString();

		LearnIRCodeResult result = learnRFCode(controllerId,
				strSubcontrollerId, jsonBody, packet);
		int resultCode = result.getResultCode();
		String deviceId = result.getDeviceId();
		Map<String, Object> replyBody = new HashMap<>();
		replyBody.put("DeviceId", deviceId);
		sendPacket(packet, controllerId, subcontrollerId,
				SocketPacket.COMMAND_ID_AS_LEARN_RFCODE, resultCode, "1.0",
				replyBody);

	}

	
	private Device getDevice(String controllerId, String subcontrollerId,
			String deviceId) {
		SubController subController = DeviceManager.getSubcontroller(
				controllerId, subcontrollerId);
		if (subController == null) {
			return null;
		}

		return subController.getDevice(deviceId);
	}

	private void onSubmitIRCodeMatch(SocketPacket packet) {
		ActionResult result = submitIRCodeMatch(packet);
		int resultCode = result.getResultCode();
		Map<String, Object> replyBody = new HashMap<>();
		String deviceId = "";
		String strIRCode = "";
		if (result.getOutputObject() != null) {
			Device device = (Device) result.getOutputObject();
			deviceId = device.getDeviceId();
			if (device.getStandardIRCode() !=null){
				strIRCode = MyBase64.encode(device.getStandardIRCode().getIRCodeData());
			}
		}
		replyBody.put("DeviceId", deviceId);
		replyBody.put("IRCode", strIRCode);

		sendPacket(packet, packet.getControllerId(), packet.getExtensionId(),
				SocketPacket.COMMAND_ID_SUBMIT_MATCH_IRCODE, resultCode, "1.0",
				replyBody);
	}

	/**
	 * The device is matched.
	 * 
	 * @param packet
	 */
	private ActionResult submitIRCodeMatch(SocketPacket packet) {
		ActionResult result = new ActionResult();
		String data = packet.getCommandDataString();

		JSONObject jsonObject = JSONObject.fromObject(data);
		JSONObject jsonBody = jsonObject.getJSONObject("Body");
		String deviceId = jsonBody.getString("DeviceId");
		String deviceName = jsonBody.getString("Name");
		short codeIndex = (short) jsonBody.getInt("IRCodeIndex");
		int deviceType = jsonBody.getInt("Type");
		String brandCode = jsonBody.getString("BrandCode");
		String extId = packet.getExtensionIdString();
		boolean isNewDevice = deviceId.equalsIgnoreCase(Device.DEVICE_ID_NEW);
		SubController subcontroller = DeviceManager.getSubcontroller(
				packet.getControllerId(), extId);
		int resultCode = ErrorCode.NO_ERROR;

		if (subcontroller == null) {
			resultCode = ErrorCode.ERROR_INVALID_CONTROLLERID;
			result.setResultCode(resultCode);
			return result;
		}

		Device device = null;

		boolean updateResult = false;
		if (isNewDevice) {

			if (deviceType == Device.DEVICE_TYPE_AC) {
				if (subcontroller.existsDevice(Device.DEVICE_TYPE_AC)) {
					ServerForm
							.warnMsg("The controller has an AC device already: "
									+ packet.getControllerId() + "-" + extId);
					resultCode = ErrorCode.ERROR_AC_ALREADY_EXISTS;
					result.setResultCode(resultCode);
					return result;
				}
			}

			ServerForm
					.showLog("Will create a device automatic for controller: "
							+ packet.getControllerId() + "-" + subcontroller);
			device = new Device(subcontroller);
			device.setControllerId(packet.getControllerId());
			device.setSubcontrollerId(extId);
			device.setDeviceName(deviceName);
			device.setDeviceType(deviceType);
			device.setBrandCode(brandCode);
			device.setIRCodeIndex(codeIndex);
			device.setLearnedIRCode(false);

			updateResult = mDAL.addDevice(device);
			if (updateResult) {
				deviceId = device.getDeviceId();
				ServerForm.showLog(MsgType.Info, "The new device Id: "
						+ deviceId);
				subcontroller.addDevice(deviceId, device);
			}
		} else {
			device = subcontroller.getDevice(deviceId);
			if (device == null) {
				resultCode = ErrorCode.ERROR_INVALID_DEVICEID;
				result.setResultCode(resultCode);
				return result;
			}

			ServerForm.showLog("Update device: " + device);

			device.setControllerId(packet.getControllerId());
			device.setSubcontrollerId(extId);
			device.setDeviceName(deviceName);
			device.setDeviceType(deviceType);
			device.setBrandCode(brandCode);
			device.setIRCodeIndex(codeIndex);
			device.setLearnedIRCode(false);
			updateResult = mDAL.updateDevice(device);
		}
		if (!updateResult) {
			resultCode = ErrorCode.ERROR_FAILED_UPDATEDB;
			ServerForm.errorMsg("Failed to add device.");
			result.setResultCode(resultCode);
			return result;
		}

		result.setResultCode(resultCode);
		result.setOutputObject(device);
		return result;

	}

	/**
	 * The device is matching IR code.
	 * 
	 * @param packet
	 */
	private void onMatchIRCode(SocketPacket packet) {
		String data = packet.getCommandDataString();

		JSONObject jsonObject = JSONObject.fromObject(data);
		JSONObject jsonBody = jsonObject.getJSONObject("Body");
		String deviceType = jsonBody.getString("Type");
		String brandCode = jsonBody.getString("BrandCode");
		// String deviceId = jsonBody.getString("DeviceId");
		int keyIndex = jsonBody.getInt("KeyIndex");
		short codeIndex = (short) jsonBody.getInt("IRCodeIndex");
		String key = (deviceType + "_" + brandCode).toUpperCase();
		StandardIRCode standardIRCode = DeviceManager.getStandardIRCode(key);
		String controllerId = packet.getControllerId();
		byte subcontrollerId = packet.getExtensionId();
		IRCode ircode = null;
		int resultCode = 0;
		if (standardIRCode == null) {
			resultCode = ErrorCode.ERROR_NO_IRCODE;
		} else {
			ircode = standardIRCode.getIRCode(codeIndex);
			if (ircode == null) {
				resultCode = ErrorCode.ERROR_NO_IRCODE;
			} else {
				SocketPacket conn = connect2Controller(controllerId);
				if (conn == null) {
					resultCode = ErrorCode.ERROR_COULDNOT_CONNECT_CTRL;
				} else {
					int len = 1 + 2 + ircode.getIRCodeData().length;
					byte[] cmdData = new byte[len];
					byte byteDeviceType = Byte.valueOf(deviceType);
					cmdData[0] = byteDeviceType;
					byte[] indexBytes = FormatTransfer.toLH(keyIndex);
					cmdData[1] = indexBytes[0];
					cmdData[2] = indexBytes[1];
					for (int i = 0; i < ircode.getIRCodeData().length; i++) {
						cmdData[3 + i] = ircode.getIRCodeData()[i];
					}

					PacketSendResult result = sendPacket(conn, controllerId,
							subcontrollerId,
							SocketPacket.COMMAND_ID_SC_MATCH_IRCODE, cmdData);
					if (!result.isSuccessful()) {
						ServerForm
								.showLog(MsgType.Warn,
										"Error occurs while communicating to controller.");
						resultCode = ErrorCode.ERROR_NETWORK_ISSUE;
					}
				}
			}
		}

		Map<String, Object> replyMap = new HashMap<>();

		String nextIndex = "-1";
		if (ircode != null) {
			if (ircode.getNext() != null) {
				nextIndex = String.valueOf(ircode.getNext().getKeyIndex());
			}
		}
		replyMap.put("NextIRCodeIndex", nextIndex);
		sendPacket(packet, controllerId, subcontrollerId,
				SocketPacket.COMMAND_ID_MATCH_IRCODE, resultCode, "1.0",
				replyMap);
	}

	/**
	 * Get sub-controllers by controller Id
	 * 
	 * @param packet
	 */
	private void onGetSubcontrollers(SocketPacket packet) {
		String controllerId = packet.getControllerId();
		Controller controller = DeviceManager.getController(controllerId);
		List<Map<String, Object>> replyBody = new ArrayList<>();
		int resultCode = 0;
		if (controller != null) {
			Hashtable<String, SubController> subcontrollers = controller
					.getSubControllers();
			Enumeration<SubController> en = subcontrollers.elements();
			while (en.hasMoreElements()) {
				SubController subcontroller = en.nextElement();
				Map<String, Object> replyBodyMap = new HashMap<>();
				replyBodyMap.put("ExtId", subcontroller.getControllerId());
				replyBodyMap.put("Name", subcontroller.getControllerName());
				replyBody.add(replyBodyMap);
			}
		} else {
			resultCode = ErrorCode.ERROR_INVALID_CONTROLLERID;
		}

		sendPacket(packet, controllerId, (byte) 0,
				SocketPacket.COMMAND_ID_GET_SUBCONTROLLERS, resultCode, "1.0",
				replyBody);

	}

	protected PacketSendResult sendPacket(SocketPacket sendTo,
			String controllerId, byte subcontrollerId, short commandId,
			int resultCode, String apiVersion, Map<String, Object> replyBody) {
		return sendPacket(sendTo, controllerId, subcontrollerId, commandId,
				resultCode, apiVersion, replyBody, false);
	}

	protected PacketSendResult sendPacket(SocketPacket sendTo,
			String controllerId, byte subcontrollerId, short commandId,
			int resultCode, String apiVersion, Map<String, Object> replyBody,
			boolean readFeedback) {

		return _sendPacket(sendTo, controllerId, subcontrollerId, commandId,
				resultCode, apiVersion, replyBody, null, readFeedback);
	}

	protected PacketSendResult sendPacket(SocketPacket sendTo,
			String controllerId, byte subcontrollerId, short commandId,
			int resultCode, String apiVersion,
			List<Map<String, Object>> replyBody) {
		return sendPacket(sendTo, controllerId, subcontrollerId, commandId,
				resultCode, apiVersion, replyBody, false);
	}

	protected PacketSendResult sendPacket(SocketPacket sendTo,
			String controllerId, byte subcontrollerId, short commandId,
			int resultCode, String apiVersion,
			List<Map<String, Object>> replyBody, boolean readFeedback) {

		return _sendPacket(sendTo, controllerId, subcontrollerId, commandId,
				resultCode, apiVersion, null, replyBody, readFeedback);
	}

	protected PacketSendResult sendPacketWithoutBody(SocketPacket sendTo,
			String controllerId, byte subcontrollerId, short commandId,
			int resultCode) {
		return sendPacketWithoutBody(sendTo, controllerId, subcontrollerId,
				commandId, resultCode, false);
	}

	protected PacketSendResult sendPacketWithoutBody(SocketPacket sendTo,
			String controllerId, byte subcontrollerId, short commandId,
			int resultCode, boolean readFeedback) {

		return _sendPacket(sendTo, controllerId, subcontrollerId, commandId,
				resultCode, "1.0", null, null, readFeedback);
	}

	protected PacketSendResult _sendPacket(SocketPacket sendTo,
			String controllerId, byte subcontrollerId, short commandId,
			int resultCode, String apiVersion, Map<String, Object> replyBody,
			List<Map<String, Object>> replyBodyList, boolean readFeedback) {

		Map<String, Object> replyMap = new HashMap<String, Object>();

		replyMap.put("OptCode", commandId);
		replyMap.put("APIVer", apiVersion);
		replyMap.put("ResultCode", resultCode);
		if (replyBody != null) {
			replyMap.put("Body", replyBody);
		} else if (replyBodyList != null) {
			replyMap.put("Body", replyBodyList);
		}
		JSONObject jsonReply = new JSONObject();
		jsonReply.putAll(replyMap);

		String strReply = jsonReply.toString();
		byte[] bytes;
		try {
			bytes = strReply.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			ServerForm.showLog(e);
			return new PacketSendResult();
		}
		return sendPacket(sendTo, controllerId, subcontrollerId, commandId,
				bytes, readFeedback);
	}

	private PacketSendResult sendPacket(SocketPacket sendTo,
			String controllerId, byte subcontrollerId, short commandId,
			byte[] cmdData) {
		return sendPacket(sendTo, controllerId, subcontrollerId, commandId,
				cmdData, false);
	}

	private static PacketSendResult sendPacket(SocketPacket sendTo,
			String controllerId, byte subcontrollerId, short commandId,
			byte[] cmdData, boolean readFeedback) {

		// data
		int cmdDataLen = 0;
		if (cmdData != null) {
			cmdDataLen = cmdData.length;
		}

		// the len of whole packet
		int len = SocketPacket.PACKET_LEN_WITHOUTDATA + cmdDataLen;
		boolean send2MID = commandId >= SocketPacket.COMMAND_ID_QUERY_CTR;
		if (send2MID) {
			len += 2;
		}
		byte[] bytes = new byte[len];

		// start flag
		bytes[0] = (byte) 0xAA;
		bytes[1] = (byte) 0xAA;

		// the len of packet without header.
		int lenWithoutHeader = len - 2;
		byte[] lenWithoutHeaderbytes = FormatTransfer.toLH(lenWithoutHeader);
		int controllerIDStartIndex = 0;
		if (send2MID) {
			bytes[2] = lenWithoutHeaderbytes[0];
			bytes[3] = lenWithoutHeaderbytes[1];
			bytes[4] = lenWithoutHeaderbytes[2];
			bytes[5] = lenWithoutHeaderbytes[3];
			controllerIDStartIndex = 6;
		} else {
			bytes[2] = lenWithoutHeaderbytes[0];
			bytes[3] = lenWithoutHeaderbytes[1];
			controllerIDStartIndex = 4;
		}
		// controller Id
		byte[] controllerIdBytes = Controller
				.convertControllerId2Bytes(controllerId);
		bytes[controllerIDStartIndex + 0] = controllerIdBytes[0];
		bytes[controllerIDStartIndex + 1] = controllerIdBytes[1];
		bytes[controllerIDStartIndex + 2] = controllerIdBytes[2];
		bytes[controllerIDStartIndex + 3] = controllerIdBytes[3];
		bytes[controllerIDStartIndex + 4] = controllerIdBytes[4];
		bytes[controllerIDStartIndex + 5] = controllerIdBytes[5];

		// sub controller Id
		bytes[controllerIDStartIndex + 6] = subcontrollerId;

		// command Id
		byte[] cmdIdBytes = FormatTransfer.toLH(commandId);
		bytes[controllerIDStartIndex + 7] = cmdIdBytes[0];
		bytes[controllerIDStartIndex + 8] = cmdIdBytes[1];

		for (int i = 0; i < cmdDataLen; i++) {
			bytes[controllerIDStartIndex + 9 + i] = cmdData[i];
		}

		byte[] crc = CRC16.calcCRC(bytes, 2, bytes.length - 2 - 1);

		int crcStartIndex = controllerIDStartIndex + 9 + cmdDataLen;
		bytes[crcStartIndex] = crc[0];
		bytes[crcStartIndex + 1] = crc[1];

		PacketSendResult result = new PacketSendResult();
		try {
			IoSession session = sendTo.getFromClient();
			if (readFeedback) {
				removeRepliedPacket(controllerId,
						String.valueOf(subcontrollerId), commandId);
			}
			synchronized (session) {

				ServerForm.debugMsg("Sending msg ["
						+ Utils.bytes2HexString(bytes) + "] to " + session);
				if (cmdData != null
						&& commandId >= SocketPacket.COMMAND_ID_QUERY_CTR) {
					ServerForm.debugMsg("Command data: "
							+ new String(cmdData, "utf-8"));
				}
				// msg += "\r\n";
				WriteFuture writeFuture = session.write(bytes);
				boolean finished = writeFuture
						.awaitUninterruptibly(READ_TIMEOUT);
				if (!finished) {

					ServerForm.warnMsg("Write timeout.");

					result.setResultCode(ErrorCode.ERROR_NETWORK_ISSUE);
					return result;
				}
			}

			ServerForm.debugMsg("Msg has been sent");
			result.setResultCode(0);
			if (readFeedback) {
				SocketPacket feedback = waitRepliedPacket(controllerId,
						String.valueOf(subcontrollerId), commandId);
				result.setFeedbackPacket(feedback);
			}
			return result;
		} catch (Exception e) {
			result.setResultCode(ErrorCode.ERROR_NETWORK_ISSUE);
			ServerForm.showLog(e);
			return result;
		}
	}

	/**
	 * Query the status for controller by Id
	 * 
	 * @param packet
	 */
	private void onQueryController(SocketPacket packet) {

		String data = packet.getCommandDataString();

		JSONObject jsonObject = JSONObject.fromObject(data);
		JSONArray bodyArray = jsonObject.getJSONArray("Body");

		List<Map<String, Object>> replyBody = new ArrayList<>();
		for (int i = 0; i < bodyArray.size(); i++) {
			JSONObject body = (JSONObject) bodyArray.get(i);
			String controllerId = body.get("ControllerID").toString();
			System.out.println(controllerId);

			Controller controller = DeviceManager.getController(controllerId);

			Map<String, Object> replyBodyMap = new HashMap<>();
			String status = "00";
			String macAddress = "";
			if (controller == null) {
				status = "02";
			} else {
				status = controller.isOnline() ? "00" : "01";
				macAddress = controller.getMacAddress();
			}

			replyBodyMap.put("ControllerId", controllerId);
			replyBodyMap.put("MacAddress", macAddress);

			// if (controllerId.equals("DE070506FFFF")) {
			// status = "00"; // TODO: remove this hard code.
			// }

			replyBodyMap.put("Status", status);
			replyBody.add(replyBodyMap);
		}

		sendPacket(packet, "", (byte) 0, SocketPacket.COMMAND_ID_QUERY_CTR, 0,
				"1.0", replyBody);
	}

	/**
	 * The controller is testing the network.
	 * 
	 * @param packet
	 */
	private void onControllerTestingNetwork(SocketPacket packet) {
		String Id = packet.getControllerId();
		ServerForm.showLog(MsgType.Info, "Controller: " + Id
				+ " is registering.");

		if (!registerController(packet)) {
			return;
		}

		sendPacket(packet, packet.getControllerId(), packet.getExtensionId(),
				SocketPacket.COMMAND_ID_CS_TESTING_NETWORK, null);
	}

	private boolean registerController(SocketPacket packet) {

		// check whether registered
		String Id = packet.getControllerId();
		Controller controller = DeviceManager.getController(Id);
		String macAddress = Utils.bytes2HexString2(packet.getCommandData());
		boolean isExists = (controller != null);
		boolean result = mDAL.registerController(Id, Id, macAddress, isExists);

		if (result) {
			if (!isExists) {
				controller = new Controller();
				controller.setControllerId(Id);
				controller.setControllerName(String.valueOf(Id));
				SubController subController = new SubController(controller);
				subController
						.setControllerId(Controller.DEFAULT_SUB_CONTROLLER_ID);
				subController.setControllerName(Id);
				controller.addSubController(
						Controller.DEFAULT_SUB_CONTROLLER_ID, subController);
				DeviceManager.addController(controller);
			}
			controller.setLastHeartbeatDT(System.currentTimeMillis());
			controller.setMacAddress(macAddress);

			SessionManager.keepConnection(Id, packet);
		}

		if (!result) {
			ServerForm.errorMsg("Failed to register the controller.");
		}
		return result;

	}

	public static int controlDevice(String controllerId,
			String subcontrollerId, String deviceId, int keyIndex) {
		SubController subcontroller = DeviceManager.getSubcontroller(
				controllerId, subcontrollerId);

		if (subcontroller == null) {
			return ErrorCode.ERROR_INVALID_CONTROLLERID;
		}
		Device device = subcontroller.getDevice(deviceId);
		if (device == null) {
			return ErrorCode.ERROR_INVALID_DEVICEID;
		}

		int deviceType = device.getDeviceType();
		// boolean islearnedIRCode = device.isLearnedIRCode();

		// IRCode irCode;
		// if (!islearnedIRCode) {
		// String brandCode = device.getBrandCode();
		// StandardIRCode standardIRCode = DeviceManager.getStandardIRCode(
		// deviceType, brandCode);
		// if (standardIRCode == null) {
		// return ErrorCode.ERROR_COULDNOT_FIND_IRCODE;
		// }
		// int codeIndex = device.getIRCodeIndex();
		// irCode = standardIRCode.getIRCode(codeIndex);
		// } else {
		// irCode = device.getLearnedIRCode(keyIndex);
		// }
		//
		IRCode irCode = device.getLearnedIRCode(keyIndex);
		if (irCode == null && device.getStandardIRCode() != null) {
			irCode = device.getStandardIRCode();
		}
		if (irCode == null) {
			return ErrorCode.ERROR_COULDNOT_FIND_IRCODE;
		}

		byte[] codeData = irCode.getIRCodeData();
		int len = 1 + 2 + codeData.length;
		byte[] cmdDataBytes = new byte[len];
		cmdDataBytes[0] = (byte) deviceType;
		byte[] keyIndexBytes = FormatTransfer.toLH((short) keyIndex);
		cmdDataBytes[1] = keyIndexBytes[0];
		cmdDataBytes[2] = keyIndexBytes[1];
		for (int i = 0; i < codeData.length; i++) {
			cmdDataBytes[3 + i] = codeData[i];
		}

		/*
		 * APP向主控器发出一个控制设备某个功能的操作操作码：0x0007 假设控制从属设备为01，控制空调关机功能（数据为学习得到的数据）
		 * AA,AA, ??,??, DE,07,03,04,01,00， 01， 07,00，01(空调ID)，01,00（空调功能键编号）
		 * 01,02,03…（学习数据（40-500字节），？？，？？（校验和） 主控器收到数据无误后，回应APP AA,AA, 10,00,
		 * DE,07,03,04,01,00， 01， 07,00，01(空调ID)，01,00（空调功能键编号）？？，？？（校验和）
		 * 
		 * 数据库的关联关系： 学习码数据库 → 设备（空调、电视、机顶盒、DVD、其它）→ 品牌（格力、美的…） →
		 * 学习码组编号(0001、0002、0003…) → 每一组学习码功能键编号（0001、0002、0003…）→
		 * 学习数据（40-500BYTE）。 固定码数据库 → 设备（空调、电视、机顶盒、DVD、其它）→ 品牌（格力、美的…）→
		 * 固定码组编号（0001、0002、0003…）→ 固定码数据（40-500BYTE）"
		 */

		SocketPacket conn = connect2Controller(controllerId);
		if (conn == null) {
			ServerForm.showLog(MsgType.Debug,
					"Couldn't connect to controller: " + controllerId);
			return ErrorCode.ERROR_COULDNOT_CONNECT_CTRL;
		}
		
		//int codeType = irCode.getCodeType();
		short commandId = 0;
		if (irCode.getCodeType() == IRCode.CODE_TYPE_IR){
			commandId = SocketPacket.COMMAND_ID_SC_CONTROL_DEVICE;
		} else{
			commandId = SocketPacket.COMMAND_ID_SC_CONTROL_RFDEVICE;
			
			cmdDataBytes[1] = (byte)keyIndex;
			cmdDataBytes[2] = (byte)device.getDeviceIndex();
		}

		PacketSendResult result = sendPacket(conn, controllerId,
				Byte.valueOf(subcontrollerId), commandId, cmdDataBytes, true);
		if (!result.isSuccessful()) {
			return ErrorCode.ERROR_NETWORK_ISSUE;
		}

		return ErrorCode.NO_ERROR;

	}

	/**
	 * Remote control the device
	 * 
	 * @param packet
	 */
	private void onControlDevice(SocketPacket packet) {
		String controllerId = packet.getControllerId();
		ServerForm.showLog(MsgType.Debug,
				"Try to control device of the contoller " + controllerId);

		String subcontrollerId = packet.getExtensionIdString();
		JSONObject jsonBody = packet.getJsonBody();
		String deviceId = jsonBody.getString("DeviceId");
		int keyIndex = jsonBody.getInt("KeyIndex");

		int resultCode = controlDevice(controllerId, subcontrollerId,
				deviceId, keyIndex);
		ServerForm.showLog(MsgType.Debug, "Result code: " + resultCode);
		sendPacketWithoutBody(packet, controllerId, packet.getExtensionId(),
				SocketPacket.COMMAND_ID_CONTROL_DEVICE, resultCode);
	}

	private static SocketPacket connect2Controller(String controllerId) {
		ServerForm.showLog(MsgType.Debug, "Connect 2 controller "
				+ controllerId);
		SocketPacket conn = null;
		long startTime = System.currentTimeMillis();
		Controller controller = DeviceManager.getController(controllerId);
		if (controller == null) {
			ServerForm.showLog(MsgType.Warn, "Invalid controller Id!");
			return null;
		}
		if (!controller.isOnline()) {
			ServerForm.showLog(MsgType.Warn, "The controller is offline!");
			return null;
		}
		while (conn == null) {

			if (System.currentTimeMillis() - startTime >= CONNECT_TIMEOUT) {

				ServerForm.showLog(MsgType.Warn, "Connect timeout!");
				break;
			}

			conn = SessionManager.getConnection(controllerId);

			if (conn != null) {
				if (!conn.getFromClient().isConnected()
						|| conn.getFromClient().isClosing()) {
					ServerForm
							.showLog(MsgType.Info,
									"The connection has expire. Waiting for a new connection.");
					SessionManager.removeConnection(controllerId);
					conn = null;
				}
			}

			if (conn == null) {
				ServerForm.showLog(MsgType.Debug, "Waiting for connection "
						+ controllerId + ". Will try again later.");
				// SessionManager.requestConnection(controllerId);

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}

		// SessionManager.removeConnectionRequest(controllerId);

		return conn;
	}

	private String getPacketAPIVersion(SocketPacket packet) {
		String data = packet.getCommandDataString();

		JSONObject jsonObject = JSONObject.fromObject(data);
		return jsonObject.get("APIVer").toString();
	}

	private void connect2Controller(SocketPacket packet) {
		String controllerId = packet.getControllerId();
		String subcontrollerId = packet.getExtensionIdString();

		SubController subcontroller = DeviceManager.getSubcontroller(
				controllerId, subcontrollerId);
		int resultCode = 0;

		Map<String, Object> replyBody = new HashMap<>();
		if (!getPacketAPIVersion(packet).equals("1.1")) {
			resultCode = ErrorCode.ERROR_NEWVERSION_APP_REQUIRED;
		} else {

			if (subcontroller == null) {
				resultCode = ErrorCode.ERROR_INVALID_SUBCONTROLLERID;
			} else {
				SocketPacket conn = connect2Controller(controllerId);

				boolean connected = (conn != null);
				long lastPressedKeyIndex = -1;
				// String status = "";
				// String description = "";
				if (connected) {
					JSONObject bodyJsonObject = packet.getJsonBody();
					int deviceType = bodyJsonObject.getInt("DeviceType");
					int deviceIndex = 0;
					if (bodyJsonObject.containsKey("DeviceIndex")){
						deviceIndex = bodyJsonObject.getInt("DeviceIndex");
					}
					Device device = subcontroller.getDeviceByType(deviceType, deviceIndex);
					if (device != null) {
						lastPressedKeyIndex = device.getLastPressedKey();
					}

					// status = "00";
					ServerForm.showLog(MsgType.Debug,
							"Connected to controller " + controllerId);
				} else {
					resultCode = ErrorCode.ERROR_COULDNOT_CONNECT_CTRL;
					// status = "01";
					ServerForm.showLog(MsgType.Warn,
							"Couldn't connect to controller: " + controllerId);
				}

				// replyBody.put("ControllerId", controllerId);
				// replyBody.put("Status", status);
				// replyBody.put("Description", description);
				replyBody.put("LastPressedKeyIndex", lastPressedKeyIndex);
			}
		}
		sendPacket(packet, controllerId, (byte) 0,
				SocketPacket.COMMAND_ID_CONNECT_CTR, resultCode, "1.0",
				replyBody);
	}

	// long warnTimeSpent = -1;
	private void onHeartbeat(SocketPacket packet) {

		String controllerId = packet.getControllerId();
		ServerForm.showLog(MsgType.Debug, "Received Heartbeat of "
				+ controllerId);

		// byte[] timestampBytes = packet.getCommandData();
		// long sendTimestamp = FormatTransfer.lBytesToLong(timestampBytes);
		// long spent = packet.getReceiveTime() - sendTimestamp;
		// if (warnTimeSpent==-1){
		// warnTimeSpent = Long.parseLong(Settings.getSetting("warnTimeSpent"));
		// }
		// if (spent >= warnTimeSpent){
		// ServerForm.warnMsg("Spent " + spent);
		// }
		//
		Controller controller = DeviceManager.getController(controllerId);
		if (controller != null) {
			ServerForm.showLog(MsgType.Debug, "Update heartbeat time for "
					+ controllerId);
			controller.setLastHeartbeatDT(System.currentTimeMillis());
		}

		ServerForm.showLog(MsgType.Debug,
				"Save the connection." + packet.getFromClient());
		SessionManager.keepConnection(controllerId, packet);

	}
}