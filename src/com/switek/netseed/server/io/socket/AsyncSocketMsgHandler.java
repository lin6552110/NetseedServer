package com.switek.netseed.server.io.socket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.switek.netseed.server.Utils;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.io.socket.sendPacket.SendPacket;
import com.switek.netseed.server.io.socket.strategy.CommContext;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;
import com.switek.netseed.util.CRC16;
	

public class AsyncSocketMsgHandler extends Thread {

	private static Logger logger = Logger
			.getLogger(AsyncSocketMsgHandlerBAK.class);
	SocketPacket triggerPacket;
	public AsyncSocketMsgHandler(SocketPacket packet) {
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
		SendPacket send=new SendPacket();
		

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
				CommContext comm=new CommContext(packet);
				comm.analysisPacket();
			
		} catch (Exception e) {
			ServerForm.showLog(e);
			try {
				if (packet.getCommandId() >= SocketPacket.COMMAND_ID_QUERY_CTR) {
					Map<String, Object> replyBody = new HashMap<>();
					replyBody.put("ErrorMsg", e.getLocalizedMessage());
					send.sendPacket(packet, packet.getControllerId(),
							packet.getExtensionId(), packet.getCommandId(),
							ErrorCode.ERROR_UNKNOWN_EXCEPTION, "1.0", replyBody);
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		} finally {
			try {
				if (packet.getCommandId() >= SocketPacket.COMMAND_ID_QUERY_CTR) {
					System.out.println(packet.getControllerId()+"======"+SocketPacket.COMMAND_ID_QUERY_CTR);
					System.out.println("关闭 session......");
					packet.getFromClient().close(true);
					
				}
			} catch (Exception e) {
				logger.error(e);
			}
		}

	}


}
