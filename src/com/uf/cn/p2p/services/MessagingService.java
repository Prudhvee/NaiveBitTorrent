package com.uf.cn.p2p.services;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Vector;

import com.uf.cn.p2p.model.Message;
import com.uf.cn.p2p.model.MessageType;
import com.uf.cn.p2p.model.Peer;
import com.uf.cn.p2p.utils.MessageUtil;

//Service for communication between the peers
public class MessagingService {

	Peer hostPeer;
	Peer remotePeer;
	Vector<Peer> neighbors;
	DataInputStream inStream;
	DataOutputStream outStream;

	public MessagingService(Peer hostPeer, Peer remote, Vector<Peer> neighbors, DataInputStream inStream,
			DataOutputStream outStream) {
		this.hostPeer = hostPeer;
		this.remotePeer = remote;
		this.neighbors = neighbors;
		this.inStream = inStream;
		this.outStream = outStream;
	}

	// Start listening to the messages and respond accordingly
	public void startService() {
		Message message;

		while (true) {
			try {
				message = readMessage(inStream);

				switch (message.getMessageType()) {
				case choke:
					System.out.println("Choke recieved");
					remotePeer.setChoked(true);
					// TODO Log choked
					break;
				case unchoke:
					System.out.println("unchoke recieved");
					remotePeer.setChoked(false);
					// Send Reqeusts if any filePieces are needed
					MessageUtil.sendRequestMessage(hostPeer, remotePeer, outStream);
					break;
				case interested:
					MessageUtil.interestedRcvd(remotePeer);
					break;
				case notInterested:
					System.out.println("Not interested received");
					remotePeer.setInterested(false);
					break;
				case have:
					MessageUtil.haveRcvd(message, hostPeer, outStream);
					break;
				case bitField:
					remotePeer.setBitField(MessageUtil.bitFieldRcvd(message, hostPeer.getBitField(), outStream));
					break;
				case request:
					System.out.println("Request received");
					MessageUtil.requestRcvd(hostPeer, remotePeer,
							ByteBuffer.wrap(message.getMessagePayLoad()).order(ByteOrder.BIG_ENDIAN).getInt(),
							outStream);
					break;
				case piece:
					Integer pieceRcvd = MessageUtil.pieceRcvd(message, hostPeer);
					MessageUtil.sendRequestMessage(hostPeer, remotePeer, outStream);
					MessageUtil.broadcastHave(neighbors, pieceRcvd);
					break;
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private Message readMessage(DataInputStream inStream) throws IOException {
		Integer messageLength = inStream.readInt();
		MessageType type_ = MessageType.getType(inStream.readByte());
		byte[] message = null;
		// Initialize the array if the length > 0
		if (messageLength > 1) {
			// Read the payload fromthe stream.
			message = new byte[messageLength - 1];
			
			long start = System.nanoTime();
			inStream.readFully(message);
			long end = System.nanoTime();
			
			if(type_ == MessageType.piece)
			{
				remotePeer.setDownLoadSpeed(messageLength/(end - start));
			}

			
		}
		return new Message(message, type_);
	}
}
