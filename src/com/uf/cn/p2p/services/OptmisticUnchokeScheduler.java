package com.uf.cn.p2p.services;

import java.io.IOException;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

import com.uf.cn.p2p.model.Message;
import com.uf.cn.p2p.model.MessageType;
import com.uf.cn.p2p.model.Peer;

public class OptmisticUnchokeScheduler extends TimerTask {
	Vector<Peer> neighbors;
	Integer hostPeerId;

	public OptmisticUnchokeScheduler(Vector<Peer> neighbors, Integer hostPeerId) {
		this.neighbors = neighbors;
		this.hostPeerId = hostPeerId;
	}

	@Override
	public void run() {
		System.out.println("optimistic");
		if(neighbors.size() == 0)
		{
			return;
		}
		SortedMap<Long, Peer> downSpeedMap = new TreeMap<>(Collections.reverseOrder());
		for (Peer peer : neighbors) {
			if(peer.isOptimisticallyUnchoked())
				peer.setOptimisticallyUnchoked(false);
			downSpeedMap.put(peer.getDownLoadSpeed(), peer);
		}

		Set keySet = downSpeedMap.entrySet();
		while(true)
		{
			Peer peer = downSpeedMap.get(ThreadLocalRandom.current().nextLong(downSpeedMap.size()));
			if(peer.isChoked())
			{
				peer.setOptimisticallyUnchoked(true);
				Message msg = new Message(null, MessageType.choke);
				try {
					System.out.println("Sending the unchoke to " + peer.getPeerId());
					msg.sendMessage(peer.getOut());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
	}

}
