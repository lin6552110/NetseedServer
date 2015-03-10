package com.switek.netseed.server.bean;



public class SensorData extends IRCode {

	public SensorData() {
	}
	
	int temperature = 0;
	int humidity = 0;
	/**
	 * @return the temperature
	 */
	public int getTemperature() {
		return temperature;
	}


	/**
	 * @param temperature the temperature to set
	 */
	public void setTemperature(int temperature) {
		this.temperature = temperature;
	}


	/**
	 * @return the humidity
	 */
	public int getHumidity() {
		return humidity;
	}


	/**
	 * @param humidity the humidity to set
	 */
	public void setHumidity(int humidity) {
		this.humidity = humidity;
	}


	/**
	 * @return the lastEditDT
	 */
	@Override
	public long getLastEditDT() {
		return lastEditDT;
	}


	/**
	 * @param lastEditDT the lastEditDT to set
	 */
	@Override
	public void setLastEditDT(long lastEditDT) {
		this.lastEditDT = lastEditDT;
	}

	long lastEditDT = 0;

	
	public static SensorData parse(byte[] rawdata) throws IllegalArgumentException  {
		if (rawdata.length != 2){
			throw new IllegalArgumentException("The len of data must be 2 bytes.");
		}
		SensorData sensordata = new SensorData();
		byte temperatureByte = rawdata[0];
		byte humidityByte = rawdata[1];
		
		sensordata.setTemperature(temperatureByte & 0xff);
		sensordata.setHumidity(humidityByte & 0xff);
		sensordata.setLastEditDT(System.currentTimeMillis());
		
		
		return sensordata;
		
	}
}
