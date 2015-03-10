package com.switek.netseed.server.bean;

import java.util.List;

public class NetSeedTimer{
	public NetSeedTimer(){
		
	}	
	
	private String timerId;
	private String timerName;
	private String timerType;
	private int weekdays;
	private String TriggerTime;
	private boolean isEnabled;
	private boolean isDeleted;
	private List<TimerStep> timerStepList;
}