package com.uf.cn.p2p.utils;

import java.io.IOException;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.uf.cn.p2p.model.Peer;
import com.uf.cn.p2p.services.peerProcess;

public class LogUtil {

	static {
		System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT]: %5$s %n");
		logger = Logger.getLogger(peerProcess.class.getName());
	}

	private static FileHandler fh;
	private static Logger logger;

	public static void createLogger(Integer peerId) throws SecurityException, IOException {
		fh = new FileHandler(FileUtil.path + "log_peer_" + peerId + ".log", false);
		logger.setUseParentHandlers(false);
		logger.addHandler(fh);
		SimpleFormatter formatter = new SimpleFormatter();
		fh.setFormatter(formatter);
	}

	public static void logInfo(String logMsg) {
		logger.info(logMsg);
	}

	public static void logMakeTCP(Integer peer1, Integer peer2) {
		logger.info("Peer " + peer1 + " makes a connection to Peer " + peer2);
	}

	public static void closingTCP(Integer peer1, Integer peer2)
	{
		logger.info("Peer " + peer1 + " is closing the TCP connection with " + peer2);
	}
	
	public static void receivedClose(Integer peer1, Integer peer2)
	{
		logger.info("Peer " + peer1 + " received close TCP from " + peer2);
	}
	
	public static void receivedTCP(Integer peer1, Integer peer2) {
		logger.info("Peer " + peer1 + " is connected from Peer " + peer2);
	}

	public static void changeOfPreferredNeighbors(Integer hostPeerId, List<Peer> list) {
		logger.info("Peer " + hostPeerId + " has the preferred neighbors " + list.toString());
	}

	public static void changeOfOptimisticNeighbor(Integer peerId, Integer neighborId) {
		logger.info("Peer " + peerId + " has the optimistically unchoked neighbor " + neighborId);
	}

	public static void logchokeunchoke(Integer peerId1, Integer peerId2, String type) {
		logger.info("Peer " + peerId1 + " is " + type + " by " + peerId2);
	}

	public static void logHave(Integer peer1, Integer peer2, Integer index) {
		logger.info("Peer " + peer1 + " received the 'have' message from Peer " + peer2 + " for the piece " + index);
	}

	public static void logInterested(Integer peer1, Integer peer2, boolean isInterested) {
		String interested;
		if (isInterested)
			interested = "'interested'";
		else
			interested = "'not interested'";

		logger.info("Peer " + peer1 + " received the " + interested + " message from Peer " + peer2);
	}

	public static void logPieceDowloaded(Integer peer1, Integer peer2, Integer pieceIndex, Integer numOfPieces) {
		logger.info("Peer " + peer1 + " has downloaded the piece " + pieceIndex + " from Peer " + peer2
				+ ".Now the number of pieces it has is " + numOfPieces);
	}

	public static void logDownloaded(Integer peerid) {
		logger.info("Peer " + peerid + " has downloaded the complete file");
	}

	public static void logRequetRcvd(Integer peer1, Integer peer2, Integer index) {
		logger.info("Peer " + peer1 + " received 'request' message for the index " + index + " from Peer " + peer2);
	}
}
