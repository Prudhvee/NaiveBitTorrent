package com.uf.cn.p2p.model;

public class PeerConfigModel {
	private static Integer peerId;
	private static String hostName;
	private static Integer listeningPort;
	private static boolean hasFile;

	public static Integer getPeerId() {
		return peerId;
	}

	public static void setPeerId(Integer peerId) {
		PeerConfigModel.peerId = peerId;
	}

	public static String getHostName() {
		return hostName;
	}

	public static void setHostName(String hostName) {
		PeerConfigModel.hostName = hostName;
	}

	public static Integer getListeningPort() {
		return listeningPort;
	}

	public static void setListeningPort(Integer listeningPort) {
		PeerConfigModel.listeningPort = listeningPort;
	}

	public static boolean isHasFile() {
		return hasFile;
	}

	public static void setHasFile(boolean hasFile) {
		PeerConfigModel.hasFile = hasFile;
	}
}
