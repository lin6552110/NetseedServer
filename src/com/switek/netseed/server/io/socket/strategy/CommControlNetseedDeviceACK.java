package com.switek.netseed.server.io.socket.strategy;

import java.math.BigInteger;

import org.apache.commons.lang.StringUtils;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.Utils;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;

public class CommControlNetseedDeviceACK extends CommStrategy{
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
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

		new CommControllerReplied().analysisPacket(packet);

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
}
