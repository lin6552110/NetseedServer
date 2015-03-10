package com.switek.netseed.server.bean;

public class IRCode {

	public static final int CODE_TYPE_IR = 0;
	public static final int CODE_TYPE_ASK315M = 1;
	public static final int CODE_TYPE_ASK433M = 2;
	public static final int CODE_TYPE_LIWO = 3;	
	
	public IRCode() {
		// TODO Auto-generated constructor stub
	}
	
	int keyIndex;
	byte[] IRCodeData;
	
	int codeType = 0;
	
	public int getCodeType() {
		return codeType;
	}
	public void setCodeType(int codeType) {
		this.codeType = codeType;
	}

	IRCode previous;
	IRCode next;
	/**
	 * @return the previous
	 */
	public IRCode getPrevious() {
		return previous;
	}
	/**
	 * @param previous the previous to set
	 */
	public void setPrevious(IRCode previous) {
		this.previous = previous;
	}
	/**
	 * @return the next
	 */
	public IRCode getNext() {
		return next;
	}
	/**
	 * @param next the next to set
	 */
	public void setNext(IRCode next) {
		this.next = next;
	}
	/**
	 * @return the keyIndex
	 */
	public int getKeyIndex() {
		return keyIndex;
	}
	/**
	 * @param keyIndex the keyIndex to set
	 */
	public void setKeyIndex(int keyIndex) {
		this.keyIndex = keyIndex;
	}
	/**
	 * @return the iRCodeData
	 */
	public byte[] getIRCodeData() {
		return IRCodeData;
	}
	/**
	 * @param iRCodeData the iRCodeData to set
	 */
	public void setIRCodeData(byte[] iRCodeData) {
		IRCodeData = iRCodeData;
	}	
	
	long lastEditDT = 0;
	/**
	 * @return the lastEditDT
	 */
	public long getLastEditDT() {
		return lastEditDT;
	}

	/**
	 * @param lastEditDT the lastEditDT to set
	 */
	public void setLastEditDT(long lastEditDT) {
		this.lastEditDT = lastEditDT;
	}
	
	private String jsonData="";

	public String getJsonData() {
		return jsonData;
	}
	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
	}
	
	

	

}
