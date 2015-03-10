package com.switek.netseed.server.bean;


public class PacketSendResult extends ActionResult {

	public PacketSendResult() {
		// TODO Auto-generated constructor stub
	}

	
	SocketPacket feedbackPacket = null;
	
	/**
	 * @return the feedbackPacket
	 */
	public SocketPacket getFeedbackPacket() {
		return feedbackPacket;
	}
	/**
	 * @param feedbackPacket the feedbackPacket to set
	 */
	public void setFeedbackPacket(SocketPacket feedbackPacket) {
		this.feedbackPacket = feedbackPacket;
	}
	
}
