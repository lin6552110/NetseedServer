package com.switek.netseed.server.bean;

public class ActionResult {
	int resultCode;
	String message;

	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	/**
	 * @return the resultCode
	 */
	public int getResultCode() {
		return resultCode;
	}
	
	
	/**
	 * @param resultCode the resultCode to set
	 */
	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}
	
	boolean isSuccessful = false;
	/**
	 * @return the isSuccessful
	 */
	public boolean isSuccessful() {
		return getResultCode() == ErrorCode.NO_ERROR;
	}
	
	Object outputObject;

	/**
	 * @return the resultObject
	 */
	public Object getOutputObject() {
		return outputObject;
	}
	/**
	 * @param resultObject the resultObject to set
	 */
	public void setOutputObject(Object resultObject) {
		this.outputObject = resultObject;
	}

}
