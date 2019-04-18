package com.uf.cn.p2p.services;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import com.uf.cn.p2p.model.Handshake;
import com.uf.cn.p2p.model.Message;
import com.uf.cn.p2p.model.MessageType;
import com.uf.cn.p2p.model.Peer;
import com.uf.cn.p2p.utils.FileUtil;
import com.uf.cn.p2p.utils.LogUtil;
import com.uf.cn.p2p.utils.MessageUtil;

public class PeerClientService extends Thread {
	Peer hostPeer;
	CopyOnWriteArrayList<Peer> neighbors;
	Vector<Peer> allPeers;

	PeerClientService(Peer host, Vector<Peer> neighbors, CopyOnWriteArrayList<Peer> connectedPeers) {
		this.hostPeer = host;
		this.allPeers = neighbors;
		this.neighbors = connectedPeers;
	}

	@Override
	public void run() {
		// Send handshake and bitfield requests to the peers above me
		for (Peer peer : allPeers) {
			if (peer.getIndex() < hostPeer.getIndex()) {
				// Creating a client handler
				LogUtil.logMakeTCP(hostPeer.getPeerId(), peer.getPeerId());
				new ClientHandler(peer).start();
			}
		}
	}

	private class ClientHandler extends Thread {
		DataInputStream inStream;
		DataOutputStream outStream;
		Socket soc;
		Peer remote;

		ClientHandler(Peer remotePeer) {
			this.remote = remotePeer;
		}

		@Override
		public void run() {
			try {
				soc = new Socket(remote.getHostName(), remote.getPort());
				inStream = new DataInputStream(soc.getInputStream());
				outStream = new DataOutputStream(soc.getOutputStream());
				remote.setOut(outStream);
				remote.setIn(inStream);
				remote.setSoc(soc);
				neighbors.add(remote);
				// Send and Receive handshake message, validate it and then send the bitField
				sendInitialMessages();

				// Initalphase done, now the client waits for the messages
				MessagingService msgService = new MessagingService(hostPeer, remote, neighbors);
				remote.setService(msgService);
				remote.getService().sendBitField(hostPeer.getBitField());
				remote.getService().startService();

			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					LogUtil.logInfo("Removing from the neighbors " + remote.getPeerId());
					allPeers.remove(remote);
					neighbors.remove(remote);
					if (neighbors.size() == 0)
						PeerListenerService.closeServer(hostPeer.getPeerId());
					// inStream.close();
					// outStream.close();
					LogUtil.closingTCP(hostPeer.getPeerId(), remote.getPeerId());
					// soc.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		private void sendInitialMessages() throws ClassNotFoundException, IOException, InterruptedException {
			// SendHandshake message
			Handshake handshakeMsg = new Handshake(hostPeer.getPeerId());
			handshakeMsg.sendHandshake(outStream);
			Handshake rcvdHandshake = new Handshake();
			while (true) {
				if (rcvdHandshake.readAndValidateHandshakeByteMessage(inStream, remote.getPeerId()))
					break;
				System.out.println("incorrect handshake received");
			}

			// Handshaking done, send the bitfield message
			System.out.println("Sending the bitFiled message" + hostPeer.getBitField().toString());
		}

	}

}
