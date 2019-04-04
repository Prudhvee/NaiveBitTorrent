package com.uf.cn.p2p.services;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;

import com.uf.cn.p2p.model.Message;
import com.uf.cn.p2p.model.MessageType;
import com.uf.cn.p2p.model.Peer;

/*
 * Service to periodically update the preferred neighbors
 */
public class UnchokeScheduler extends TimerTask {

	Vector<Peer> neighbors;
	Integer hostPeerId;
	Integer numOfPreferredNeighbors;

	public UnchokeScheduler(Vector<Peer> neighbors, Integer hostPeerId, Integer k) {
		this.neighbors = neighbors;
		this.hostPeerId = hostPeerId;
		this.numOfPreferredNeighbors = k;
	}

	@Override
	public void run() {
		System.out.println("Unchoke sched");
		
		// First get the downloadSpeeds in a sortedMap
		SortedMap<Long, Peer> downSpeedMap = new TreeMap<>(Collections.reverseOrder());
		if(neighbors.size() == 0)
		{
			return;
		}
		for (Peer peer : neighbors) {
			downSpeedMap.put(peer.getDownLoadSpeed(), peer);
		}

		// Get the first K neighbors
		Set keySet = downSpeedMap.entrySet();
		Iterator i = keySet.iterator();
		int index = 0;

		while (i.hasNext()) {
			Map.Entry entry = (Map.Entry) i.next();
			Peer peer = (Peer) entry.getValue();
			if (peer.getPeerId() == hostPeerId)
				continue;

			// Check if the peer is interersted
			if (peer.isInterested()) {
				if (index++ < numOfPreferredNeighbors && peer.isChoked() && !peer.isOptimisticallyUnchoked()) {
					peer.setChoked(false);
					//unchokeQueue.add(peer.getPeerId());
					Message msg = new Message(null, MessageType.unchoke);
					try {
						msg.sendMessage(peer.getOut());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				if (!peer.isChoked()) {
					peer.setChoked(true);
					Message msg = new Message(null, MessageType.choke);
					try {
						msg.sendMessage(peer.getOut());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		downSpeedMap.clear();

	}

}
