package com.uf.cn.p2p.model;

public class FilePiece {
	byte[] data;

	public FilePiece(byte[] data) {
		this.data = data;
	}

	public FilePiece() {
		this.data = null;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
