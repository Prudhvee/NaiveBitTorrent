package com.uf.cn.p2p.model;

public class Message {

	//COmmenting as of now, because I do not see any use by this var.
	//private Integer messageLength;
	private MessageType messageType;
	private byte[] messagePayLoad;
	
	public Message(byte[] payLoad, MessageType type)
	{
		messagePayLoad = payLoad;
		messageType = type;
	}
	
	public Integer getMessageLength() {
		return messagePayLoad == null ? 1 : messagePayLoad.length + 1;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public byte[] getMessagePayLoad() {
		return messagePayLoad;
	}

	public void setMessagePayLoad(byte[] messagePayLoad) {
		this.messagePayLoad = messagePayLoad;
	}

}
