package com.switek.netseed.server.dal;

import java.util.List;

import net.sf.json.JSONArray;

import com.switek.netseed.server.bean.ActionResult;
import com.switek.netseed.server.bean.ControllerTimer;
import com.switek.netseed.server.bean.Device;
import com.switek.netseed.server.bean.LearnIRCode;
import com.switek.netseed.server.bean.SubController;
import com.switek.netseed.server.bean.TimerStep;

public interface IDAL {
	boolean addDevice(Device device);
	boolean addSubcontroller(SubController subcontroller);
	boolean createTimer(ControllerTimer timer);
	boolean removeTimer(ControllerTimer timer);
	boolean updateTimer(ControllerTimer timer);
	boolean updateDevice(Device device);
	boolean updateDeviceLastPressKey(Device device, long lastPressedKey);
	boolean deleteDevice(Device device);
	
	boolean deleteSubController(SubController subcontroller);
	boolean registerController(String id, String name, String macAddress, boolean isReregister);
	boolean addLearnedIRCode(Device device, LearnIRCode learnIRCode);
	boolean updateLearnedIRCode(Device device, LearnIRCode learnIRCode);
	boolean logTimerHistory(ControllerTimer timer, TimerStep step,  ActionResult result);
	boolean disableTimer(ControllerTimer timer, boolean enabled);
	//List<String> getTimerId(List<String> controllerId);
} 
