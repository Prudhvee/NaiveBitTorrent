package com.uf.cn.p2p.services;

import java.io.IOException;

import com.uf.cn.p2p.utils.FileUtil;

public class FileParsingThread extends Thread {

	Integer peerId;

	public FileParsingThread(Integer peerId) {
		this.peerId = peerId;
	}
	
	@Override
	public void run()
	{
		try {
			FileUtil.breakIntoPieces(peerId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
