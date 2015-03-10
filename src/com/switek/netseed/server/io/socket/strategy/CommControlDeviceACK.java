package com.switek.netseed.server.io.socket.strategy;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;
import com.switek.netseed.util.FormatTransfer;

public class CommControlDeviceACK extends CommStrategy{
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

}
