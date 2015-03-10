package com.switek.netseed.server.bean;

import java.util.ArrayList;


public class ControllerTimer {

	public ControllerTimer(){
		
	}
	String timerId="";	
	String name = "";
	long lastRunTime = 0;
	int weekDays = -1;
	String time = "";
	boolean deleted = false;
	boolean enabled = true;
	boolean isRunning = false;
	
	ArrayList<TimerStep> steps = new ArrayList<>(); 
	
	public static boolean isNewTimer(String Id){
		return "{NEW_TIMER}".equalsIgnoreCase(Id);
	}
	
	public ArrayList<TimerStep> getSteps() {
		return steps;
	}
	public void setSteps(ArrayList<TimerStep> steps) {
		this.steps = steps;
	}
	
	public long getLastRunTime() {
		return lastRunTime;
	}
	public void setLastRunTime(long lastRunTime) {
		this.lastRunTime = lastRunTime;
	}
	public boolean isRunning() {
		return isRunning;
	}
	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getTimerId() {
		return timerId;
	}
	public void setTimerId(String timerId) {
		this.timerId = timerId;
	}
	
	public void addStep(TimerStep step){
		getSteps().add(step);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getWeekDays() {
		return weekDays;
	}
	public void setWeekDays(int weekDays) {
		this.weekDays = weekDays;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	
	
}
