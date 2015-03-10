package com.switek.netseed.server.io.socket.strategy;

import java.util.HashMap;
import java.util.Map;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.ConfigSubcontrollerResult;
import com.switek.netseed.server.bean.Controller;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.PacketSendResult;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;

public class CommConfigSubcontroller extends CommStrategy{
	
	private SendPacket send=new SendPacket();
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		String controllerId = packet.getControllerId();
		byte subcontrollerId = packet.getExtensionId();

		ConfigSubcontrollerResult result = configSubcontroller(controllerId,
				packet);
		int resultCode = result.getResultCode();
		String strSubcontrollerId = result.getSubcontrollerId();
		Map<String, Object> replyBody = new HashMap<>();
		replyBody.put("ExtId", strSubcontrollerId);
		send.sendPacket(packet, controllerId, subcontrollerId,
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
		PacketSendResult sendResult = send.sendPacket(conn, controllerId, (byte) 0,
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

}
