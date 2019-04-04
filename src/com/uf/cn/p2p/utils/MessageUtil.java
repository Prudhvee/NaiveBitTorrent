package com.uf.cn.p2p.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Vector;

import com.uf.cn.p2p.model.CommonConfigModel;
import com.uf.cn.p2p.model.Handshake;
import com.uf.cn.p2p.model.Message;
import com.uf.cn.p2p.model.MessageType;
import com.uf.cn.p2p.model.Peer;
import com.uf.cn.p2p.services.RequestMessageTimerService;

public class MessageUtil {

	public static MessageType getMessageType(byte[] message) {
		return MessageType.getType(message[4]);
	}

	public static Integer waitForHandshakeAndSendReponse(DataInputStream in, DataOutputStream out, Integer peerId)
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

	public static void sendBitField(DataOutputStream out, BitSet hostBitSet) throws IOException {
		System.out.println(hostBitSet.isEmpty());
		Message bitFieldMsg = new Message(hostBitSet.isEmpty() ? null : hostBitSet.toByteArray(), MessageType.bitField);
		if (!hostBitSet.isEmpty())
			System.out.println("Payload:" + hostBitSet.toString() + "Length:" + bitFieldMsg.getMessagePayLoad().length);
		bitFieldMsg.sendMessage(out);
	}

	public static BitSet bitFieldRcvd(Message message, BitSet hostBitSet, DataOutputStream outStream)
			throws IOException {

		// If the payLoad is null, we are not interested
		if (message.getMessagePayLoad() == null) {
			// Not interested
			Message notInterestedMsg = new Message(null, MessageType.notInterested);
			System.out.println("Not Interested");
			notInterestedMsg.sendMessage(outStream);
			return null;
		}
		// Check if the remote peer has any of the pieces needed
		BitSet remoteBitSet = BitSet.valueOf(message.getMessagePayLoad());
		hostBitSet.flip(0, remoteBitSet.length());
		if (hostBitSet.intersects(remoteBitSet)) {
			// Have something that I am interested in, create an interered message and send
			System.out.println("Interested");
			Message interestedMsg = new Message(null, MessageType.interested);
			interestedMsg.sendMessage(outStream);
		} else {
			// Not interested
			Message notInterestedMsg = new Message(null, MessageType.notInterested);
			System.out.println("Not Interested");
			notInterestedMsg.sendMessage(outStream);
		}

		hostBitSet.flip(0, remoteBitSet.length());

		return remoteBitSet;

	}

	public static void interestedRcvd(Peer remotePeer) {

		// For now, I think it is sufficient to set the var. Let's see if it adds
		// functionality
		System.out.println("Received interested");
		remotePeer.setInterested(true);

	}

	public static void sendRequestMessage(Peer hostPeer, Peer remotePeer, DataOutputStream outStream)
			throws IOException {
		// Find next piece that is needed.
		Integer nextPieceNeeded = findNextPieceNeeded(hostPeer);
		System.out.println("Requesting " + nextPieceNeeded);
		if (nextPieceNeeded != null) {
			// Send the request message for the next piece needed
			Message requestMsg = new Message(
					ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(nextPieceNeeded).array(),
					MessageType.request);

			requestMsg.sendMessage(outStream);
			hostPeer.setRequestedBitSetVal(nextPieceNeeded, true);
			// Setting the timer for 3 seconds. If the host bitset field is not set in 3
			// seconds,
			// we unset the requestField
			new RequestMessageTimerService(nextPieceNeeded, hostPeer).start();
		}

	}

	private static Integer findNextPieceNeeded(Peer hostPeer) {
		for (int i = 0; i < CommonConfigModel.getNumOfPieces(); i++) {
			if (!hostPeer.getBitFieldValue(i) && !hostPeer.getRequestedBitSetVal(i)) {
				System.out.println("-------------------" + hostPeer.getBitFieldValue(i));
				return i;
			}
		}

		return null;

	}

	public static void requestRcvd(Peer hostPeer, Peer remotePeer, Integer pieceRequired, DataOutputStream outStream)
			throws IOException {
		// check if the peer is not choked
		if (!remotePeer.isChoked() || remotePeer.isOptimisticallyUnchoked()) {
			// Send the required piece.
			// Piece Message is 4byte index + the content
			byte[] pieceReqInBytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(pieceRequired).array();
			byte[] payLoad = Files.readAllBytes(FileUtil.getFilePath(hostPeer.getPeerId(), pieceRequired));

			ByteBuffer reqMsgPayload = ByteBuffer.allocate(pieceReqInBytes.length + payLoad.length);
			reqMsgPayload.put(pieceReqInBytes, 0, pieceReqInBytes.length);
			reqMsgPayload.put(payLoad, 0, payLoad.length);
			Message pieceMsg = new Message(reqMsgPayload.array(), MessageType.piece);
			pieceMsg.sendMessage(outStream);
		}

	}

	public static Integer pieceRcvd(Message message, Peer hostPeer) throws IOException {
		// Store the piece
		byte[] msg = message.getMessagePayLoad();

		Integer pieceIndex = ByteBuffer.wrap(Arrays.copyOfRange(msg, 0, 4)).order(ByteOrder.BIG_ENDIAN).getInt();

		if (!hostPeer.getBitFieldValue(pieceIndex)) {
			// Check if getInt above might cause problems with the below instruction.

			FileUtil.createDirectoryIfNotExists(hostPeer.getPeerId());
			Files.write(FileUtil.getFilePath(hostPeer.getPeerId(), pieceIndex), Arrays.copyOfRange(msg, 4, msg.length),
					StandardOpenOption.CREATE);
			hostPeer.setBitFieldValue(pieceIndex);

			// If all are received, merge the file.
			FileUtil.MergeTheFileIfWeCan(hostPeer);

			return pieceIndex;
		}

		return null;

	}

	public static void broadcastHave(Vector<Peer> neighbors, Integer havePiece) throws IOException {
		Message haveMsg = new Message(ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(havePiece).array(),
				MessageType.have);

		for (Peer peer : neighbors) {
			haveMsg.sendMessage(peer.getOut());
		}
	}

	public static void haveRcvd(Message message, Peer hostPeer, DataOutputStream outStream) throws IOException {
		// Get the piece index from the message
		Integer pieceIndex = ByteBuffer.wrap(message.getMessagePayLoad()).order(ByteOrder.BIG_ENDIAN).getInt();

		if (!hostPeer.getBitFieldValue(pieceIndex)) {
			// send interested
			Message interestedMsg = new Message(null, MessageType.interested);
			interestedMsg.sendMessage(outStream);
		}

	}

}
