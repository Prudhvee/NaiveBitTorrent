package com.uf.cn.p2p.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.uf.cn.p2p.model.Message;
import com.uf.cn.p2p.model.MessageType;
import com.uf.cn.p2p.model.Peer;
import com.uf.cn.p2p.utils.DownLoadSpeedComparator;
import com.uf.cn.p2p.utils.LogUtil;

/*
 * Service to periodically update the preferred neighbors
 */
public class UnchokeScheduler extends TimerTask {

	CopyOnWriteArrayList<Peer> neighbors;
	Peer hostPeer;
	Integer numOfPreferredNeighbors;

	public UnchokeScheduler(CopyOnWriteArrayList<Peer> neighbors2, Peer hostPeer, Integer k) {
		this.neighbors = neighbors2;
		this.hostPeer = hostPeer;
		this.numOfPreferredNeighbors = k;
	}

	@Override
	public void run() {

		if (neighbors.size() == 0) {
			return;
		}
		List<Peer> list = new ArrayList<>();
		for (Peer peer : neighbors) {
			LogUtil.logInfo("HostPeer " + hostPeer.getPeerId() + " has file " + hostPeer.hasFile() + ". Remote Peer "
					+ peer.getPeerId() + " has file " + peer.hasFile());
			if (peer.hasFile() && hostPeer.hasFile()) {
				neighbors.remove(peer);
				try {
					peer.getSoc().close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			} else {
				list.add(peer);
			}
		}

		// Get the first K neighbors
		list.sort(new DownLoadSpeedComparator());
		int index = 0;

		for (Peer peer : list) {
			peer.setDownLoadSpeed(new Long(0));
			if (peer.getPeerId() == hostPeer.getPeerId())
				continue;
			// Check if the peer is interersted
			if (peer.isInterested()) {
				if (index++ < numOfPreferredNeighbors && peer.isChoked() && !peer.isOptimisticallyUnchoked()) {
					peer.setChoked(false);
					// unchokeQueue.add(peer.getPeerId());
					Message msg = new Message(null, MessageType.unchoke);
					try {
						peer.getService().sendMessage(msg);
						// msg.sendMessage(peer.getOut());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					if (!peer.isChoked()) {
						peer.setChoked(true);
						Message msg = new Message(null, MessageType.choke);
						try {
							peer.getService().sendMessage(msg);
							// msg.sendMessage(peer.getOut());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			} else {
				if (!peer.isChoked()) {
					peer.setChoked(true);
					Message msg = new Message(null, MessageType.choke);
					try {
						// msg.sendMessage(peer.getOut());
						peer.getService().sendMessage(msg);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		LogUtil.changeOfPreferredNeighbors(hostPeer.getPeerId(),
				list.stream().filter(p -> p.isChoked() == false).collect(Collectors.toList()));

	}

}
