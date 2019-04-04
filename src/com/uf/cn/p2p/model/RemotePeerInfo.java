package com.uf.cn.p2p.model;

/*
 *                     CEN5501C Project2
 * This is the program starting remote processes.
 * This program was only tested on CISE SunOS environment.
 * If you use another environment, for example, linux environment in CISE 
 * or other environments not in CISE, it is not guaranteed to work properly.
 * It is your responsibility to adapt this program to your running environment.
 */

public class RemotePeerInfo {
	public Integer indexinConfig;
	public String peerId;
	public String peerAddress;
	public String peerPort;
	public boolean hasFile;

	@Override
	public String toString() {
		return "RemotePeerInfo [indexinConfig=" + indexinConfig + ", peerId=" + peerId + ", peerAddress=" + peerAddress
				+ ", peerPort=" + peerPort + ", hasFile=" + hasFile + "]";
	}

	public RemotePeerInfo(Integer index, String pId, String pAddress, String pPort, boolean hasFile) {
		indexinConfig = index;
		peerId = pId;
		peerAddress = pAddress;
		peerPort = pPort;
		this.hasFile = hasFile;
	}

}
