package com.switek.netseed.server.io.socket.strategy;

import net.sf.json.JSONObject;

import com.switek.netseed.server.bean.ActionResult;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;

public class CommRemoveDevice extends CommStrategy{
	
	private SendPacket send=new SendPacket();
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		ActionResult result=new ActionResult();
		result=removeDevice(packet);
		int resultCode=result.getResultCode();
		send.sendPacketWithoutBody(packet, packet.getControllerId(), packet.getExtensionId(),
				SocketPacket.COMMAND_ID_AS_REMOVE_DEVICE, resultCode);
	}
	
	public ActionResult removeDevice(SocketPacket packet){
		ActionResult result=new ActionResult();
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
		result.setResultCode(resultCode);
		return result;
	}

}