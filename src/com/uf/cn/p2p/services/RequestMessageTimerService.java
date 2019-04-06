package com.uf.cn.p2p.services;

import com.uf.cn.p2p.model.Peer;
import com.uf.cn.p2p.utils.LogUtil;

public class RequestMessageTimerService extends Thread {
	Integer index;
	Peer hostPeer;
	Peer remote;

	public RequestMessageTimerService(Integer requestedPieceIndex, Peer hostPeer) {
		this.index = requestedPieceIndex;
		this.hostPeer = hostPeer;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1000);
			LogUtil.logInfo(" Peer " + hostPeer.getPeerId() + " waiting for the requested piece " + index + "_-"
					+ hostPeer.getBitField().nextSetBit(index) + "===="
					+ hostPeer.getRequestedBitSet().nextSetBit(index));
			if (!hostPeer.getBitFieldValue(index)) {
				hostPeer.setRequestedBitSetVal(index, false);
				LogUtil.logInfo("Peer " + hostPeer.getPeerId()
				+ " did not not receive the requested piece. Restting the bit field " + hostPeer.getRequestedBitSet().nextClearBit(index));
			}

			return;

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
