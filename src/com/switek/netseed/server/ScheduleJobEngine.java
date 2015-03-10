package com.switek.netseed.server;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.corba.se.spi.orbutil.fsm.Action;
import com.switek.netseed.server.bean.ActionResult;
import com.switek.netseed.server.bean.ControllerTimer;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.ErrorCode;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.bean.TimerStep;
import com.switek.netseed.server.dal.IDAL;
import com.switek.netseed.server.dal.MySQLDAL;
import com.switek.netseed.server.io.socket.AsyncSocketMsgHandler;
import com.switek.netseed.server.io.socket.strategy.CommControlNetseedDevice;
import com.switek.netseed.server.io.socket.strategy.ControlDevice;
import com.switek.netseed.server.ui.ServerForm;

public class ScheduleJobEngine {

	static ScheduleJobEngine engine = null;

	public static synchronized ScheduleJobEngine newInstance() {
		if (engine == null) {
			engine = new ScheduleJobEngine();
		}
		return engine;
	}

	ExecutorService mThreadPool = Executors.newFixedThreadPool(1000);

	Timer timer = null;

	public void start() {		
		timer = new Timer();
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				ServerForm.debugMsg("Starting schedule job engine...");
				Hashtable<String, ControllerTimer> timers = DeviceManager
						.getTimers();
				
				Enumeration<String> en = timers.keys();

				while (en.hasMoreElements()) {
					String key = en.nextElement();					
					ControllerTimer timer = timers.get(key);
					if (timer.isDeleted() || !timer.isEnabled()) {
//						ServerForm.showLog("Timer is disabled. "
//								+ timer.getTimerId() + ". " + timer.getName());
						continue;
					}
					if (timer.isRunning()) {
						ServerForm.debugMsg("Timer is running. "
								+ timer.getTimerId() + ". " + timer.getName());
						continue;
					}

					boolean isTime2Run = false;
					int currentWeekday = Utils.getWeekDay();
					int timerWeekdays = timer.getWeekDays();

					if (timerWeekdays < 0
							|| (timerWeekdays & (int)Math.pow(2, currentWeekday)) != 0) {
						String dateFormat = "";
						if (timerWeekdays >= 0) {
							dateFormat = "HH:mm";
						} else {
							dateFormat = "yyyy/MM/dd HH:mm";
						}
						String currentTime = Utils.getCurrentTime(dateFormat);
						if (timer.getTime().equals(currentTime)) {
							if (System.currentTimeMillis()
									- timer.getLastRunTime() > 60 * 1000) {
								isTime2Run = true;
							}
						}
					}

					if (isTime2Run) {
						TimerHandler handler = new TimerHandler(timer);
						mThreadPool.execute(handler);
					}
				}
			}
		};

		timer.schedule(task, 60 * 1000, 30 * 1000);
	}

	public void stop() {
		if (timer != null) {
			timer.cancel();
		}

		mThreadPool.shutdownNow();
	}

	final static IDAL mDAL = MySQLDAL.newInstance();

	class TimerHandler extends Thread {

		private ControllerTimer timer;

		public TimerHandler(ControllerTimer timer) {
			this.timer = timer;
			timer.setRunning(true);
		}

		@Override
		public void run() {

			ServerForm.showLog("Start to run the timer: " + timer.getTimerId()
					+ " / " + timer.getName());
			for (int i = 0; i < timer.getSteps().size(); i++) {
				int resultCode = 0;
				String msg = "";
				final TimerStep step = timer.getSteps().get(i);
				int seqNo = step.getSeqNo();
				ServerForm
						.showLog("Step " + timer.getTimerId() + " / " + seqNo);
				try {
					String controllerId = step.getControllerId();
					String subcontrollerId = step.getSubcontrollerId();
					String deviceId = step.getDeviceId();
					int keyIndex = step.getKeyIndex();
					SubController sc = DeviceManager.getSubcontroller(controllerId, subcontrollerId);
					if (sc == null) {
						resultCode = ErrorCode.ERROR_INVALID_SUBCONTROLLERID;
						msg = "Invalid controller or ext Id.";
					} else {
						Device device = sc.getDevice(deviceId);
						if (device == null) {
							resultCode = ErrorCode.ERROR_INVALID_DEVICEID;
							msg = "Invalid device Id.";
						} else {
							if (device.getDeviceType() >= 0x0B &&  device.getDeviceType() <= 0x0E){
								int value = step.getValue();
								ActionResult actionResult= new CommControlNetseedDevice()
										.controlNetseedDevice(controllerId,
												subcontrollerId, deviceId,
												keyIndex, value);
								resultCode =actionResult.getResultCode();
							}else{
								resultCode = new ControlDevice().controlDevice(
										controllerId, subcontrollerId, deviceId,
										keyIndex);
							}
							Thread.sleep(step.getDelay());
						}
					}
				} catch (Exception e) {
					resultCode = ErrorCode.ERROR_UNKNOWN_EXCEPTION;
					msg = e.getLocalizedMessage();
				}
				ServerForm.showLog(String.format(
						"Result of step %s - %s:  %s. Message: %s. ",
						timer.getTimerId(), seqNo, resultCode, msg));

				// log history
				final ActionResult actionResult = new ActionResult();
				actionResult.setResultCode(resultCode);
				actionResult.setMessage(msg);

				Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {
						mDAL.logTimerHistory(timer, step, actionResult);
					}
				});
				thread.start();
			}

			ServerForm.showLog("Complted to run the timer: "
					+ timer.getTimerId() + " / " + timer.getName());
			timer.setLastRunTime(System.currentTimeMillis());
			timer.setRunning(false);

		}
	}
}
