package com.uf.cn.p2p.model;

import java.io.DataOutputStream;
import java.io.IOException;

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
	
	public void sendMessage(DataOutputStream out) throws IOException
	{
		//messageLength = messagePayLoad.length + 1;
		out.writeInt(messagePayLoad == null ? 1 : messagePayLoad.length + 1);
		System.out.println(messageType.getType());
		out.writeByte(messageType.getType());
		if(messagePayLoad != null && messagePayLoad.length > 0)
			out.write(messagePayLoad);
		out.flush();
	}
	

	/*public Integer getMessageLength() {
		return messageLength;
	}

	public void setMessageLength(Integer messageLength) {
		this.messageLength = messageLength;
	}
*/
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
