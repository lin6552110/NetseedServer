package com.switek.netseed.server.bean;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import com.switek.netseed.server.dao.DeviceDao;


public class SubController extends Controller {

	Controller parent = null;
	Hashtable<String, Device> devices = new Hashtable<>();
	/**
	 * @return the devices
	 */
	public Hashtable<String, Device> getDevices() {
		return devices;
	}
	
	public Device getDevice(String deviceId){
		return devices.get(deviceId);
	}
	
	public Device getDeviceByType(int deviceType, int deviceIndex){
		Enumeration<Device> en = devices.elements();
		while (en.hasMoreElements()) {
			Device device = en.nextElement();
			if (device.getDeviceType()==deviceType && device.getDeviceIndex() == deviceIndex){
				return device;
			}
		}
		
		return null;
	}
	
	public boolean existsDevice(int deviceType){
		Enumeration<Device> en = devices.elements();
		while (en.hasMoreElements()) {
			Device device = en.nextElement();
			if (device.getDeviceType()==deviceType){
				return true;
			}
		}
		return false;
	}
	
	public boolean isDefaultSubController(){
		return getControllerId().equals("0");
	}
	/**
	 * @param devices the devices to set
	 */
	public void setDevices(Hashtable<String, Device> devices) {
		this.devices = devices;
	}
	
	public void addDevice(String id, Device device) {
		devices.put(id, device);
	}
	
	
	public void removeDevice(Device device){
		devices.remove(device.getDeviceId());
	}
	
	
	public SubController(Controller parent) {
		this.parent = parent;
	}
	/**
	 * @return the parent
	 */	
	public Controller getParent() {
		return parent;
	}
	
	
	

}
