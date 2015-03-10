package com.switek.netseed.server.io.socket.strategy;

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
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;
import com.switek.netseed.util.CRC16;
import com.switek.netseed.util.FormatTransfer;


/**
 * 使用策略模式重构 com.switek.netseed.server.io.socket.AsyncSocketMsgHandlerBAK
 * 所有的功能封装在 com.switek.netseed.server.io.socket.strategy包内
 * @author 林进铨  2014年12月13日
 *
 */
public abstract class  CommStrategy {

	static IDAL mDAL = MySQLDAL.newInstance();
	private static long CONNECT_TIMEOUT = 1 * 60 * 1000;
	protected  Logger logger = Logger.getLogger(CommStrategy.class);
	
	public abstract void analysisPacket(SocketPacket packet);

	
	protected static SocketPacket connect2Controller(String controllerId) {
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
	
	protected String getPacketAPIVersion(SocketPacket packet) {
		String data = packet.getCommandDataString();

		JSONObject jsonObject = JSONObject.fromObject(data);
		return jsonObject.get("APIVer").toString();
	}
	
	protected Device getDevice(String controllerId, String subcontrollerId,
			String deviceId) {
		SubController subController = DeviceManager.getSubcontroller(
				controllerId, subcontrollerId);
		if (subController == null) {
			return null;
		}

		return subController.getDevice(deviceId);
	}
	
	
	
	
	
}
