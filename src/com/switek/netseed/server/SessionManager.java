package com.switek.netseed.server;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.mina.core.session.IoSession;

import com.switek.netseed.server.bean.SocketPacket;
import com.switek.netseed.server.ui.ServerForm;
import com.switek.netseed.server.ui.ServerForm.MsgType;

public class SessionManager {

	public SessionManager() {

	}

	/**
	 * Key: Controller Id
	 */
	private static final List<String> connect2ControllerRequestList = new ArrayList<>();

	/**
	 * Key: ControllerId Value: SocketPacket
	 */
	private static final Hashtable<String, SocketPacket> controllerConnectionList = new Hashtable<>();

	public static synchronized boolean hasConnectRequest(String controllerId) {
		synchronized (connect2ControllerRequestList) {
			return connect2ControllerRequestList.contains(controllerId);
		}
	}

	public static synchronized void requestConnection(String controllerId) {
		synchronized (connect2ControllerRequestList) {
			if (!connect2ControllerRequestList.contains(controllerId)) {
				connect2ControllerRequestList.add(controllerId);
			}
		}
	}

	public static synchronized void removeConnectionRequest(String controllerId) {
		synchronized (connect2ControllerRequestList) {
			connect2ControllerRequestList.remove(controllerId);
		}
	}

	public static synchronized SocketPacket getConnection(String controllerId) {
		return controllerConnectionList.get(controllerId);
	}

	static ExecutorService mThreadPool = Executors.newFixedThreadPool(100);

	public static void keepConnection(String controllerId,
			final SocketPacket packet) {

		//close the old session if need.
		final SocketPacket oldPacket = controllerConnectionList
				.remove(controllerId);
		controllerConnectionList.put(controllerId, packet);
		if (oldPacket != null) {
			final IoSession oldSession = oldPacket.getFromClient();
			if (oldSession.getId() != packet.getFromClient().getId()) {
				Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							
							ServerForm.showLog(String
									.format("The old session of %s will be closed. %s ==> %s.",
											oldPacket.getControllerId(),
											oldSession.toString(), packet
											.getFromClient()
											.toString()));
							synchronized (oldSession) {
								oldSession.close(false);
							}
							
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				});
				mThreadPool.execute(thread);
			}
		}
	}

	public static void removeConnection(String controllerId) {
		SocketPacket conn = controllerConnectionList.remove(controllerId);
		if (conn == null) {
			return;
		}

		synchronized (conn.getFromClient()) {
			ServerForm.showLog(
					MsgType.Info,
					"Will remove and close the connection "
							+ conn.getFromClient());
			conn.getFromClient().close(false);
			conn = null;
		}
	}

	void startHouseKeeper() {
		Timer timerHouseKeeper = new Timer();
		timerHouseKeeper.schedule(new TimerTask() {

			@Override
			public void run() {

			}
		}, 5 * 60 * 1000, 5 * 60 * 1000);
	}

}
