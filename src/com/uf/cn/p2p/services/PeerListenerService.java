package com.uf.cn.p2p.services;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.uf.cn.p2p.model.CommonConfigModel;
import com.uf.cn.p2p.model.Peer;
import com.uf.cn.p2p.utils.MessageUtil;

public class PeerListenerService extends Thread {

	private Peer hostPeer;
	private Vector<Peer> neighbors;
	private Vector<Peer> allPeers;

	PeerListenerService(Peer host, Vector<Peer> remotePeers, Vector<Peer> connectedPeers) throws IOException {
		hostPeer = host;
		allPeers = remotePeers;
		neighbors = connectedPeers;
	}

	@Override
	public void run() {

		ServerSocket listener = null;
		try {
			// Start the schedulers.
			startSchedulers();
			System.out.println("After the schedulers**************");
			System.out.println("Starting the server at " + hostPeer.getPort());
			listener = new ServerSocket(hostPeer.getPort());
			while (true) {
				new Handler(listener.accept()).start();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				listener.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void startSchedulers() {
		// // Start the scheduler to select the K random neighbors
		TimerTask schedService = new UnchokeScheduler(neighbors, hostPeer.getPeerId(),
				CommonConfigModel.getNumOfPrefNeighbors());
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(schedService, 0, CommonConfigModel.getUnchokingInterval() * 1000);

		// Scheduler to optimistically unchoke a neighbor
		TimerTask optimisticSched = new OptmisticUnchokeScheduler(neighbors, hostPeer.getPeerId());
		//Timer Optimistictimer = new Timer();
		timer.scheduleAtFixedRate(optimisticSched, 0,
				CommonConfigModel.getOptismisticUnchokingInterval() * 1000);

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
				Integer remotePeerId = MessageUtil.waitForHandshakeAndSendReponse(in, out, hostPeer.getPeerId());

				// Constructing the remote peer.
				for (Peer remote : allPeers) {
					if (remotePeerId.equals(remote.getPeerId())) {
						this.remote = remote;
						break;
					}
				}
				remote.setOut(out);
				remote.setSoc(connection);
				neighbors.addElement(remote);
				System.out.println("Sending the bitFiled message" + hostPeer.getBitField().toString());
				MessageUtil.sendBitField(out, hostPeer.getBitField());

				MessagingService msgService = new MessagingService(hostPeer, remote, neighbors, in, out);

				msgService.startService();

			} catch (IOException ioException) {
				System.out.println("Disconnect with Client ");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				// Close connections
				try {
					in.close();
					out.close();
					connection.close();
				} catch (IOException ioException) {
					ioException.printStackTrace();
					System.out.println("Disconnect with Client ");
				}
			}
		}

		// send a message to the output stream
		public void sendMessage(String msg) {
			try {
				// out.write(msg);
				out.flush();
				System.out.println("Send message: " + msg);
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}

	}

}