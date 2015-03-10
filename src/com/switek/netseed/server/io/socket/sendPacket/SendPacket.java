package com.switek.netseed.server.io.socket.sendPacket;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;

import com.switek.netseed.server.DeviceManager;
import com.switek.netseed.server.SessionManager;
import com.switek.netseed.server.Utils;
import com.switek.netseed.server.bean.Controller;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.PacketSendResult;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.dal.IDAL;
import com.switek.netseed.server.dal.MySQLDAL;
import com.switek.netseed.server.io.socket.strategy.CommStrategy;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;
import com.switek.netseed.util.CRC16;
import com.switek.netseed.util.FormatTransfer;

public class SendPacket {
	
	public  final int READ_TIMEOUT = 1 * 20 * 1000;
	
	public final static Hashtable<String, SocketPacket> mRepliedPackets = new Hashtable<>();
	private  Logger logger = Logger.getLogger(SendPacket.class);


	public  PacketSendResult sendPacket(SocketPacket sendTo,
			String controllerId, byte subcontrollerId, short commandId,
			byte[] cmdData) {
		return sendPacket(sendTo, controllerId, subcontrollerId, commandId,
				cmdData, false);
	}
	
	
	public  PacketSendResult sendPacket(SocketPacket sendTo,
			String controllerId, byte subcontrollerId, short commandId,
			int resultCode, String apiVersion,
			List<Map<String, Object>> replyBody) {
		return sendPacket(sendTo, controllerId, subcontrollerId, commandId,
				resultCode, apiVersion, replyBody, false);
	}
	
	public  PacketSendResult sendPacket(SocketPacket sendTo,
			String controllerId, byte subcontrollerId, short commandId,
			int resultCode, String apiVersion, Map<String, Object> replyBody) {
		return sendPacket(sendTo, controllerId, subcontrollerId, commandId,
				resultCode, apiVersion, replyBody, false);
	}
	
	
	public  PacketSendResult sendPacket(SocketPacket sendTo,
			String controllerId, byte subcontrollerId, short commandId,
			int resultCode, String apiVersion, Map<String, Object> replyBody,
			boolean readFeedback) {

		return _sendPacket(sendTo, controllerId, subcontrollerId, commandId,
				resultCode, apiVersion, replyBody, null, readFeedback);
	}
	
	public  PacketSendResult sendPacket(SocketPacket sendTo,
			String controllerId, byte subcontrollerId, short commandId,
			int resultCode, String apiVersion,
			List<Map<String, Object>> replyBody, boolean readFeedback) {

		return _sendPacket(sendTo, controllerId, subcontrollerId, commandId,
				resultCode, apiVersion, null, replyBody, readFeedback);
	}
	
	public  PacketSendResult sendPacket(SocketPacket sendTo,
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
	
	
	public  PacketSendResult _sendPacket(SocketPacket sendTo,
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
	
	public  PacketSendResult sendPacketWithoutBody(SocketPacket sendTo,
			String controllerId, byte subcontrollerId, short commandId,
			int resultCode) {
		return sendPacketWithoutBody(sendTo, controllerId, subcontrollerId,
				commandId, resultCode, false);
	}
	
	public  PacketSendResult sendPacketWithoutBody(SocketPacket sendTo,
			String controllerId, byte subcontrollerId, short commandId,
			int resultCode, boolean readFeedback) {

		return _sendPacket(sendTo, controllerId, subcontrollerId, commandId,
				resultCode, "1.0", null, null, readFeedback);
	}
	
	public  void removeRepliedPacket(String controllerId,
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
	
	public  SocketPacket waitRepliedPacket(String controllerId,
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
	
	
	
	

	
	
}
