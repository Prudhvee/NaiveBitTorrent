package com.uf.cn.p2p.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.BitSet;

import com.uf.cn.p2p.services.MessagingService;

public class Peer {
	private Integer peerId;
	private Integer index;
	private String hostName;
	private Integer port;
	private boolean hasFile;
	private BitSet bitField;
	private DataOutputStream out;
	private DataInputStream in;
	private Socket soc;

	private boolean isChoked = true;
	private boolean isOptimisticallyUnchoked = false;
	private boolean isInterested = false;
	private Long downLoadSpeed;
	private BitSet requestedBitSet;
	private MessagingService service;

	public Peer(Integer index, Integer peerId, String hostName, Integer port, boolean hasFile) {
		this.peerId = peerId;
		this.index = index;
		this.hostName = hostName;
		this.port = port;
		this.hasFile = hasFile;
		this.downLoadSpeed = new Long(0);
	}

	public Integer getPeerId() {
		return peerId;
	}

	public void setPeerId(Integer peerId) {
		this.peerId = peerId;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public boolean hasFile() {
		return hasFile;
	}

	public void setHasFile(boolean hasFile) {
		this.hasFile = hasFile;
	}

	synchronized public BitSet getBitField() {
		return bitField;
	}

	synchronized public void setBitField(BitSet bitField) {
		this.bitField = bitField;
	}

	public boolean isChoked() {
		return isChoked;
	}

	public synchronized void setChoked(boolean isChoked) {
		this.isChoked = isChoked;
	}

	public boolean isInterested() {
		return isInterested;
	}

	public void setInterested(boolean isInterested) {
		this.isInterested = isInterested;
	}

	public Long getDownLoadSpeed() {
		return downLoadSpeed;
	}

	public void setDownLoadSpeed(Long l) {
		this.downLoadSpeed = l;
	}

	public boolean isOptimisticallyUnchoked() {
		return isOptimisticallyUnchoked;
	}

	public void setOptimisticallyUnchoked(boolean isOptimisticallyUnchoked) {
		this.isOptimisticallyUnchoked = isOptimisticallyUnchoked;
	}

	synchronized public DataOutputStream getOut() {
		return out;
	}

	public void setOut(DataOutputStream out) {
		this.out = out;
	}

	synchronized public BitSet getRequestedBitSet() {
		return requestedBitSet;
	}

	synchronized public void setRequestedBitSet(BitSet requestedBitSet) {
		this.requestedBitSet = requestedBitSet;
	}

	public Socket getSoc() {
		return soc;
	}

	public void setSoc(Socket soc) {
		this.soc = soc;
	}

	synchronized public boolean getBitFieldValue(int index) {
		return bitField.get(index);
	}

	synchronized public void setBitFieldValue(int index, boolean val) {
		bitField.set(index, val);
	}

	synchronized public boolean getRequestedBitSetVal(int index) {
		return requestedBitSet.get(index);
	}

	synchronized public void setRequestedBitSetVal(int index, boolean val) {
		requestedBitSet.set(index, val);
	}

	@Override
	public String toString() {
		return this.peerId + "";
	}

	public MessagingService getService() {
		return service;
	}

	public void setService(MessagingService service) {
		this.service = service;
	}

	public DataInputStream getIn() {
		return in;
	}

	public void setIn(DataInputStream in) {
		this.in = in;
	}

}
