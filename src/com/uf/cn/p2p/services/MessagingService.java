package com.uf.cn.p2p.services;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import com.uf.cn.p2p.model.Handshake;
import com.uf.cn.p2p.model.Message;
import com.uf.cn.p2p.model.MessageType;
import com.uf.cn.p2p.model.Peer;
import com.uf.cn.p2p.utils.LogUtil;
import com.uf.cn.p2p.utils.MessageUtil;

//Service for communication between the peers
public class MessagingService {

	Peer hostPeer;
	Peer remotePeer;
	CopyOnWriteArrayList<Peer> neighbors;
	//MessageUtil msgUtil;

	public MessagingService(Peer hostPeer, Peer remote, CopyOnWriteArrayList<Peer> neighbors2) {
		this.hostPeer = hostPeer;
		this.remotePeer = remote;
		this.neighbors = neighbors2;
		//msgUtil = new MessageUtil();
	}

	// Start listening to the messages and respond accordingly
	public void startService() {
		Message message;

		while (true) {
			try {
				try {
					LogUtil.logInfo(remotePeer.getIn().available() + " Waiting for the message at the peer "
							+ remotePeer.getPeerId());
					message = readMessage(remotePeer.getIn());
				} catch (EOFException e) {
					e.printStackTrace();
					LogUtil.logInfo("Socket Closed++++++++++++++++++++++++++" + e.getMessage());
					break;
				} catch (SocketException e) {
					e.printStackTrace();
					LogUtil.logInfo("Socket exception received+++++++++++++++++++++++++" + e.getMessage());
					break;
				}

				switch (message.getMessageType()) {
				case choke:
					LogUtil.logchokeunchoke(hostPeer.getPeerId(), remotePeer.getPeerId(), "choked");
					remotePeer.setChoked(true);
					break;
				case unchoke:
					LogUtil.logchokeunchoke(hostPeer.getPeerId(), remotePeer.getPeerId(), "unchoked");
					remotePeer.setChoked(false);
					// Send Reqeusts if any filePieces are needed
					MessageUtil.sendRequestMessage(hostPeer, remotePeer);
					break;
				case interested:
					LogUtil.logInterested(hostPeer.getPeerId(), remotePeer.getPeerId(), true);
					MessageUtil.interestedRcvd(remotePeer);
					break;
				case notInterested:
					LogUtil.logInterested(hostPeer.getPeerId(), remotePeer.getPeerId(), false);
					remotePeer.setInterested(false);
					break;
				case have:
					MessageUtil.haveRcvd(message, hostPeer, remotePeer);
					break;
				case bitField:
					MessageUtil.bitFieldRcvd(message, hostPeer, remotePeer);
					break;
				case request:
					MessageUtil.requestRcvd(hostPeer, remotePeer,
							ByteBuffer.wrap(message.getMessagePayLoad()).order(ByteOrder.BIG_ENDIAN).getInt());
					break;
				case piece:
					Integer pieceRcvd = MessageUtil.pieceRcvd(message, hostPeer, remotePeer);
					MessageUtil.sendRequestMessage(hostPeer, remotePeer);
					if (pieceRcvd != null)
						MessageUtil.broadcastHave(neighbors, pieceRcvd);
					break;
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		LogUtil.logInfo("Leaving the while loop " + remotePeer.getPeerId());
	}

	private Message readMessage(DataInputStream inStream) throws IOException, SocketException {
		LogUtil.logInfo("reading 4 bytes from " + remotePeer.getPeerId());
		Integer messageLength = inStream.readInt();
		LogUtil.logInfo("read 4 bytes from " + remotePeer.getPeerId() + "---" + messageLength);
		LogUtil.logInfo("reading a byte from the " + remotePeer.getPeerId());
		byte b = inStream.readByte();
		LogUtil.logInfo("read 1 byte from " + remotePeer.getPeerId() + "---" + b);
		MessageType type_ = MessageType.getType(b);
		byte[] message = null;
		// Initialize the array if the length > 0
		if (messageLength > 1) {
			// Read the payload fromthe stream.
			message = new byte[messageLength - 1];

			inStream.read(message);

			if (type_ == MessageType.piece) {
				remotePeer.setDownLoadSpeed(remotePeer.getDownLoadSpeed() + new Long(messageLength));
			}

		}
		return new Message(message, type_);
	}

	synchronized public void sendMessage(Message msg) throws IOException, SocketException {
		// messageLength = messagePayLoad.length + 1;
		remotePeer.getOut().writeInt(msg.getMessageLength());
		LogUtil.logInfo("Wriring the message " + msg.getMessageLength());
		remotePeer.getOut().writeByte(msg.getMessageType().getType());
		LogUtil.logInfo("Written the message of type" + msg.getMessageType().getType());
		if (msg.getMessagePayLoad() != null && msg.getMessagePayLoad().length > 0)
			remotePeer.getOut().write(msg.getMessagePayLoad());

		LogUtil.logInfo("Message payload--" + msg.getMessageLength());
		remotePeer.getOut().flush();
	}

	public void sendBitField(BitSet hostBitSet) throws IOException {
		Message bitFieldMsg = new Message(hostBitSet.isEmpty() ? null : hostBitSet.toByteArray(), MessageType.bitField);
		// bitFieldMsg.sendMessage(out);
		sendMessage(bitFieldMsg);
	}

}
