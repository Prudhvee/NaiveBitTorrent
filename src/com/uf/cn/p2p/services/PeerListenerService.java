package com.uf.cn.p2p.services;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import com.uf.cn.p2p.model.CommonConfigModel;
import com.uf.cn.p2p.model.Handshake;
import com.uf.cn.p2p.model.Peer;
import com.uf.cn.p2p.utils.FileUtil;
import com.uf.cn.p2p.utils.LogUtil;
import com.uf.cn.p2p.utils.MessageUtil;

public class PeerListenerService extends Thread {

	private Peer hostPeer;
	private CopyOnWriteArrayList<Peer> neighbors;
	private Vector<Peer> allPeers;
	static ServerSocket listener;
	static Timer timer;

	PeerListenerService(Peer host, Vector<Peer> remotePeers, CopyOnWriteArrayList<Peer> connectedPeers)
			throws IOException {
		hostPeer = host;
		allPeers = remotePeers;
		neighbors = connectedPeers;
		listener = new ServerSocket(hostPeer.getPort());
	}

	@Override
	public void run() {

		try {
			// Start the schedulers.
			timer = new Timer();
			startSchedulers(timer);
			while (true) {
				new Handler(listener.accept()).start();
			}
		} catch (IOException ex) {
			System.out.println("Server Socket closed");
		} finally {
			try {
				closeServer(hostPeer.getPeerId());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void startSchedulers(Timer timer) {
		// // Start the scheduler to select the K random neighbors
		TimerTask schedService = new UnchokeScheduler(neighbors, hostPeer,
				CommonConfigModel.getNumOfPrefNeighbors());
		timer.scheduleAtFixedRate(schedService, 0, CommonConfigModel.getUnchokingInterval() * 1000);

		// Scheduler to optimistically unchoke a neighbor
		TimerTask optimisticSched = new OptmisticUnchokeScheduler(neighbors, hostPeer.getPeerId());
		// Timer Optimistictimer = new Timer();
		timer.scheduleAtFixedRate(optimisticSched, 0, CommonConfigModel.getOptismisticUnchokingInterval() * 1000);

	}

	/**
	 * A handler thread class. Handlers are spawned from the listening loop and are
	 * responsible for dealing with a single client's requests.
	 */
	private class Handler extends Thread {
		private Socket connection;
		private Peer remote;
		private DataInputStream in; // stream read from the socket
		private DataOutputStream out; // stream write to the socket

		public Handler(Socket connection) {
			this.connection = connection;
		}

		public void run() {
			try {

				// initialize Input and Output streams
				out = new DataOutputStream(connection.getOutputStream());
				// out.flush();
				in = new DataInputStream(connection.getInputStream());
				// wait for handshake
				Integer remotePeerId = waitForHandshakeAndSendReponse(in, out, hostPeer.getPeerId());

				// Constructing the remote peer.
				for (Peer remote : allPeers) {
					if (remotePeerId.equals(remote.getPeerId())) {
						this.remote = remote;
						break;
					}
				}
				LogUtil.receivedTCP(hostPeer.getPeerId(), remote.getPeerId());
				remote.setOut(out);
				remote.setIn(in);
				remote.setSoc(connection);
				// MessageUtil.sendBitField(out, hostPeer.getBitField());
				neighbors.add(remote);
				MessagingService msgService = new MessagingService(hostPeer, remote, neighbors);
				remote.setService(msgService);
				remote.getService().sendBitField(hostPeer.getBitField());
				remote.getService().startService();

			} catch (IOException ioException) {
				System.out.println("Disconnect with Client ");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				// Close connections
				try {
					LogUtil.logInfo("Removing from the neighbors " + remote.getPeerId());
					allPeers.remove(remote);
					neighbors.remove(remote);
					if (neighbors.size() == 0)
						closeServer(hostPeer.getPeerId());
					// in.close();
					// out.close();
					LogUtil.receivedClose(hostPeer.getPeerId(), remote.getPeerId());
					// connection.close();
				} catch (Exception ioException) {
					ioException.printStackTrace();
					System.out.println("Disconnect with Client ");
				}
			}
		}
		/*
		 * // send a message to the output stream public void sendMessage(String msg) {
		 * try { // out.write(msg); out.flush(); System.out.println("Send message: " +
		 * msg); } catch (IOException ioException) { ioException.printStackTrace(); } }
		 */

		private Integer waitForHandshakeAndSendReponse(DataInputStream in, DataOutputStream out, Integer peerId)
				throws IOException, InterruptedException {
			Handshake hnd = new Handshake();
			while (true) {
				if (hnd.readAndValidateHandshakeByteMessage(in, null))
					break;
			}
			System.out.println("received handshake and sending one" + peerId);
			Handshake outMsg = new Handshake(peerId);
			outMsg.sendHandshake(out);
			return hnd.getPeerId();
		}

	}

	synchronized public static void closeServer(Integer peerId) throws IOException {

		if (!listener.isClosed()) {
			FileUtil.deleteParts(peerId);
			listener.close();
			timer.cancel();
		}

	}

}