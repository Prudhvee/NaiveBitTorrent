package com.uf.cn.p2p.services;

import java.util.concurrent.BlockingQueue;

public class chokeMessageService extends Thread {
	private BlockingQueue<Integer> chokeQueue;
	Integer remotePeerId;

	public chokeMessageService(BlockingQueue<Integer> chokeQueue, BlockingQueue<Integer> unchokeQueue,
			Integer peerId) {
		this.chokeQueue = chokeQueue;
		this.remotePeerId = peerId;
	}

	@Override
	public void run() {
		//Check if the
	}

}
