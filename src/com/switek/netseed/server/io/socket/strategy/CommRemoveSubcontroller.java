package com.switek.netseed.server.io.socket.strategy;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.PacketSendResult;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;

public class CommRemoveSubcontroller extends CommStrategy{
	
	private SendPacket send=new SendPacket();
	
	
	@Override
	public void analysisPacket(SocketPacket packet) {
		// TODO Auto-generated method stub
		String controllerId = packet.getControllerId();
		String subcontrollerId = packet.getExtensionIdString();

		int resultCode = removeSubcontroller(controllerId, subcontrollerId,
				packet);

		send.sendPacketWithoutBody(packet, controllerId, packet.getExtensionId(),
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
		PacketSendResult sendResult = send.sendPacket(conn, controllerId,
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

}
