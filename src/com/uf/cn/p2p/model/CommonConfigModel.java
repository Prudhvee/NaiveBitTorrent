package com.uf.cn.p2p.model;

/*
 * Class to store the details from the common.cfg 
 */
public class CommonConfigModel {
	private static Integer NumOfPrefNeighbors;
	private static Integer unchokingInterval;
	private static Integer optismisticUnchokingInterval;
	private static String fileName;
	private static Long fileSize;
	private static Integer numOfPieces;

	public static Integer getNumOfPrefNeighbors() {
		return NumOfPrefNeighbors;
	}

	public static void setNumOfPrefNeighbors(Integer numOfPrefNeighbors) {
		NumOfPrefNeighbors = numOfPrefNeighbors;
	}

	public static Integer getUnchokingInterval() {
		return unchokingInterval;
	}

	public static void setUnchokingInterval(Integer unchokingInterval) {
		CommonConfigModel.unchokingInterval = unchokingInterval;
	}

	public static Integer getOptismisticUnchokingInterval() {
		return optismisticUnchokingInterval;
	}

	public static void setOptismisticUnchokingInterval(Integer optismisticUnchokingInterval) {
		CommonConfigModel.optismisticUnchokingInterval = optismisticUnchokingInterval;
	}

	public static String getFileName() {
		return fileName;
	}

	public static void setFileName(String fileName) {
		CommonConfigModel.fileName = fileName;
	}

	public static Long getFileSize() {
		return fileSize;
	}

	public static void setFileSize(Long fileSize) {
		CommonConfigModel.fileSize = fileSize;
	}

	public static Integer getPieceSize() {
		return pieceSize;
	}

	public static void setPieceSize(Integer pieceSize) {
		CommonConfigModel.pieceSize = pieceSize;
	}

	private static Integer pieceSize;

	public static Integer getNumOfPieces() {
		return numOfPieces;
	}

	public static void setNumOfPieces(Integer numOfPieces) {
		CommonConfigModel.numOfPieces = numOfPieces;
	}
}
