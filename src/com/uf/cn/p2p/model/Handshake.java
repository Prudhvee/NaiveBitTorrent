package com.uf.cn.p2p.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Handshake implements Serializable{
	private byte[] handshakeHeader = "P2PFILESHARINGPROJ".getBytes();
	private byte[] zeroBits = new byte[10];
	private byte[] peerId = new byte[4];

	
	public Handshake(Integer peerId) {
		this.peerId = ByteBuffer.allocate(4).putInt(peerId).array();
		Arrays.fill(zeroBits, (byte)0);
	}


	public Handshake() {
	}


	public void sendHandshake(DataOutputStream out) throws IOException
	{
		out.write(handshakeHeader);
		out.write(zeroBits);
		out.write(peerId);
		
		out.flush();
	}
	

	public boolean readAndValidateHandshakeByteMessage(DataInputStream in, Integer expectedPeerId) throws IOException, InterruptedException {

		//read the handhake message and validate the message
		byte[] rcvhandshakeHeader = new byte[handshakeHeader.length];
		in.read(rcvhandshakeHeader, 0, handshakeHeader.length);
		in.read(zeroBits, 0, zeroBits.length);
		in.read(peerId, 0, peerId.length);
		
		String str = new String(rcvhandshakeHeader);
		Integer inPeerId = ByteBuffer.wrap(peerId).order(ByteOrder.BIG_ENDIAN).getInt();
		if(str.equals(new String(handshakeHeader)))
		{
			if(expectedPeerId == null || expectedPeerId.equals(inPeerId))
				return true;
		}
		
		//Thread.sleep(100000);
		return  false;
	}
	
	public Integer getPeerId() {
		return ByteBuffer.wrap(peerId).order(ByteOrder.BIG_ENDIAN).getInt();
	}


	public void setPeerId(byte[] peerId) {
		this.peerId = peerId;
	}


	public void setPeerId(Integer peerId)
	{
		this.peerId = (ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(peerId).array());
	}

}
