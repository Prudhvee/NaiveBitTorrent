package com.uf.cn.p2p.services;

import java.util.BitSet;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import com.uf.cn.p2p.model.CommonConfigModel;
import com.uf.cn.p2p.model.Peer;
import com.uf.cn.p2p.utils.ConfigParseUtil;
import com.uf.cn.p2p.utils.LogUtil;

public class peerProcess {
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.out.println("peerId is expected as arguement. Ex: peerProcess [peerId]");
			return;
		}
		Integer peerId = Integer.parseInt(args[0]);
		Vector<Peer> remotePeers = new Vector<>();
		// First parse the config files

		// INitialize the Logger for the peer
		LogUtil.createLogger(peerId);

		ConfigParseUtil.parseCommonConfig();
		remotePeers = ConfigParseUtil.parsePeerConfig();
		System.out.println("remote peers length" + remotePeers.size());
		Peer currPeer = getCurrPeerDetails(remotePeers, peerId);
		// If the current peer has file, initialize the file bits to the peer and
		// populate the pieces.
		BitSet requested = new BitSet(CommonConfigModel.getNumOfPieces());
		BitSet bitField = new BitSet(CommonConfigModel.getNumOfPieces());
		if (currPeer.hasFile()) {
			bitField.set(0, CommonConfigModel.getNumOfPieces(), true);
			FileParsingThread pthread = new FileParsingThread(peerId);
			pthread.setPriority(10);
			pthread.start();

		}
		requested.set(0, CommonConfigModel.getNumOfPieces(), false);
		currPeer.setRequestedBitSet(requested);
		currPeer.setBitField(bitField);
		// currPeer.setFilePieces(FileUtil.breakIntoPieces(FileUtil.fileToByteStream(peerId+"/"+CommonConfigModel.getFileName())));

		// Vector common between server and listener to store the connected peers
		CopyOnWriteArrayList<Peer> connectedPeers = new CopyOnWriteArrayList<Peer>();

		// IF the index is zero, then the node just accepts the requests
		// else, it listens for the requests and sends the request to the previous peers
		if (currPeer.getIndex() != 0) {
			// Initiate the client and the handshaking requests to the peers above
			PeerClientService client = new PeerClientService(currPeer, remotePeers, connectedPeers);
			client.start();
		}

		// Initiate the Listener
		PeerListenerService peerListener = new PeerListenerService(currPeer, remotePeers, connectedPeers);
		peerListener.start();
		// Call the service to listen for the requests
	}

	private static Peer getCurrPeerDetails(Vector<Peer> remotePeers, Integer peerId) {
		for (Peer peer : remotePeers) {
			System.out.println(peer.getPeerId());
			if (peer.getPeerId().equals(peerId))
				return peer;
		}
		return null;
	}
}
