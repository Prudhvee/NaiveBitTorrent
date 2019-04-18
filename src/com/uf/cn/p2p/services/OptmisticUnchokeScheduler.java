package com.uf.cn.p2p.services;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import com.uf.cn.p2p.model.Message;
import com.uf.cn.p2p.model.MessageType;
import com.uf.cn.p2p.model.Peer;
import com.uf.cn.p2p.utils.LogUtil;

public class OptmisticUnchokeScheduler extends TimerTask {
	CopyOnWriteArrayList<Peer> neighbors;
	Integer hostPeerId;

	public OptmisticUnchokeScheduler(CopyOnWriteArrayList<Peer> neighbors2, Integer hostPeerId) {
		this.neighbors = neighbors2;
		this.hostPeerId = hostPeerId;
	}

	@Override
	public void run() {
		if (neighbors.size() == 0) {
			return;
		}

		// Check if all the peers are unchoked, then not needed
		// TODO
		List<Peer> list = new ArrayList<>();
		for (Peer peer : neighbors) {
			if (peer.isOptimisticallyUnchoked())
				peer.setOptimisticallyUnchoked(false);
			list.add(peer);
		}
		Random rand = new Random();
		while (true) {
			int p = rand.nextInt(list.size());
			Peer peer = list.get(p);
			if (peer.isChoked()) {
				LogUtil.changeOfOptimisticNeighbor(hostPeerId, peer.getPeerId());
				peer.setOptimisticallyUnchoked(true);
				peer.setDownLoadSpeed(new Long(0));
				Message msg = new Message(null, MessageType.unchoke);
				try {
					peer.getService().sendMessage(msg);
					//msg.sendMessage(peer.getOut());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}
				break;
			}
		}
	}

}
