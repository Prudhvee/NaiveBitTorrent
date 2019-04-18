package com.uf.cn.p2p.services;

import java.io.IOException;
import java.net.SocketException;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import com.uf.cn.p2p.model.Message;
import com.uf.cn.p2p.model.Peer;

public class BroadcastHave extends Thread {

	CopyOnWriteArrayList<Peer> neighbors;
	Message hvMsg;

	public BroadcastHave(CopyOnWriteArrayList<Peer> neighbors, Message hvMsg) {
		this.neighbors = neighbors;
		this.hvMsg = hvMsg;
	}

	@Override
	public void run() {
		Iterator<Peer> iter = neighbors.iterator();
		while (iter.hasNext()) {
			Peer peer = (Peer) iter.next();
			try {
				peer.getService().sendMessage(hvMsg);
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
