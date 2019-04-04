package com.uf.cn.p2p.services;

import com.uf.cn.p2p.model.Peer;

public class RequestMessageTimerService extends Thread {
	Integer index;
	Peer hostPeer;
	Peer remote;
	
	public RequestMessageTimerService(Integer requestedPieceIndex, Peer hostPeer)
	{
		this.index = requestedPieceIndex;
		this.hostPeer = hostPeer;
	}
	
	@Override
	public void run()
	{
		try {
			Thread.sleep(3000);
			if(!hostPeer.getBitFieldValue(index))
			{
				hostPeer.setRequestedBitSetVal(index, false);
			}
			
			return;
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
