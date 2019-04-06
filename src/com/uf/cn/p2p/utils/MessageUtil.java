package com.uf.cn.p2p.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.CopyOnWriteArrayList;

import com.uf.cn.p2p.model.CommonConfigModel;
import com.uf.cn.p2p.model.Message;
import com.uf.cn.p2p.model.MessageType;
import com.uf.cn.p2p.model.Peer;
import com.uf.cn.p2p.services.BroadcastHave;
import com.uf.cn.p2p.services.RequestMessageTimerService;

public class MessageUtil {
	private static RequestMessageTimerService reqTimer;

	public static MessageType getMessageType(byte[] message) {
		return MessageType.getType(message[4]);
	}

	public static void bitFieldRcvd(Message message, Peer host, Peer remote) throws IOException {

		// If the payLoad is null, we are not interested
		if (message.getMessagePayLoad() == null) {
			// Not interested
			Message notInterestedMsg = new Message(null, MessageType.notInterested);
			// notInterestedMsg.sendMessage(outStream);
			remote.getService().sendMessage(notInterestedMsg);
			remote.setBitField(new BitSet(CommonConfigModel.getNumOfPieces()));
			return;
		}
		// Check if the remote peer has any of the pieces needed
		BitSet remoteBitSet = BitSet.valueOf(message.getMessagePayLoad());
		remote.setBitField(remoteBitSet);
		BitSet tempSet = (BitSet) host.getBitField().clone();
		tempSet.flip(0, remoteBitSet.length());
		if (tempSet.intersects(remoteBitSet)) {
			// Have something that I am interested in, create an interered message and send
			Message interestedMsg = new Message(null, MessageType.interested);
			// interestedMsg.sendMessage(outStream);
			// MessagingService.sendMessage(outStream, interestedMsg);
			remote.getService().sendMessage(interestedMsg);
		} else {
			// Not interested
			Message notInterestedMsg = new Message(null, MessageType.notInterested);
			// notInterestedMsg.sendMessage(outStream);
			// MessagingService.sendMessage(outStream, notInterestedMsg);
			remote.getService().sendMessage(notInterestedMsg);
		}

	}

	public static void interestedRcvd(Peer remotePeer) {

		// For now, I think it is sufficient to set the var. Let's see if it adds
		// functionality
		remotePeer.setInterested(true);

	}

	public static void sendRequestMessage(Peer hostPeer, Peer remotePeer) throws IOException {
		// Find next piece that is needed.
		Integer nextPieceNeeded = findNextPieceNeeded(hostPeer, remotePeer);
		LogUtil.logInfo("Requesting  " + remotePeer.getPeerId() + " piece " + nextPieceNeeded);
		if (nextPieceNeeded != null) {
			// Send the request message for the next piece needed
			Message requestMsg = new Message(ByteBuffer.allocate(4).putInt(nextPieceNeeded).array(),
					MessageType.request);

			// requestMsg.sendMessage(outStream);
			// MessagingService.sendMessage(remotePeer.getOut(), requestMsg);
			remotePeer.getService().sendMessage(requestMsg);
			hostPeer.setRequestedBitSetVal(nextPieceNeeded, true);
			// Setting the timer for 3 seconds. If the host bitset field is not set in 3
			// seconds,
			// we unset the requestField
			reqTimer = new RequestMessageTimerService(nextPieceNeeded, hostPeer);
			reqTimer.start();
		}

	}

	private static Integer findNextPieceNeeded(Peer hostPeer, Peer remotePeer) {
		for (int i = 0; i < CommonConfigModel.getNumOfPieces(); i++) {
			if (!hostPeer.getBitFieldValue(i) && !hostPeer.getRequestedBitSetVal(i) && remotePeer.getBitFieldValue(i)) {
				return i;
			}
		}

		return null;

	}

	public static void requestRcvd(Peer hostPeer, Peer remotePeer, Integer pieceRequired) throws IOException {
		LogUtil.logRequetRcvd(hostPeer.getPeerId(), remotePeer.getPeerId(), pieceRequired);
		// check if the peer is not choked
		LogUtil.logInfo(
				"Received request from " + remotePeer.getPeerId() + " The peer is choked?" + remotePeer.isChoked());
		if (!remotePeer.isChoked() || remotePeer.isOptimisticallyUnchoked()) {
			// Send the required piece.
			// Piece Message is 4byte index + the content
			byte[] pieceReqInBytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(pieceRequired).array();
			Path p = FileUtil.getFilePath(hostPeer.getPeerId(), pieceRequired);
			if (hostPeer.hasFile() && Files.notExists(p, LinkOption.NOFOLLOW_LINKS)) {
				remotePeer.setInterested(true);
				return;
			}
			byte[] payLoad = Files.readAllBytes(p);
			LogUtil.logInfo("Sending the piece " + pieceRequired + " to " + remotePeer.getPeerId());
			ByteBuffer reqMsgPayload = ByteBuffer.allocate(pieceReqInBytes.length + payLoad.length);
			reqMsgPayload.put(pieceReqInBytes, 0, pieceReqInBytes.length);
			reqMsgPayload.put(payLoad, 0, payLoad.length);
			Message pieceMsg = new Message(reqMsgPayload.array(), MessageType.piece);
			// pieceMsg.sendMessage(outStream);
			// MessagingService.sendMessage(outStream, pieceMsg);
			remotePeer.getService().sendMessage(pieceMsg);
		} else
			remotePeer.setInterested(true);

	}

	public static Integer pieceRcvd(Message message, Peer hostPeer, Peer remotePeer) throws IOException {
		// Store the piece
		byte[] msg = message.getMessagePayLoad();
		Integer pieceIndex = ByteBuffer.wrap(Arrays.copyOfRange(msg, 0, 4)).order(ByteOrder.BIG_ENDIAN).getInt();

		if (!hostPeer.getBitFieldValue(pieceIndex)) {
			// Check if getInt above might cause problems with the below instruction.
			hostPeer.setBitFieldValue(pieceIndex, true);
			FileUtil.createDirectoryIfNotExists(hostPeer.getPeerId());
			Files.write(FileUtil.getFilePath(hostPeer.getPeerId(), pieceIndex), Arrays.copyOfRange(msg, 4, msg.length),
					StandardOpenOption.CREATE);
			LogUtil.logPieceDowloaded(hostPeer.getPeerId(), remotePeer.getPeerId(), pieceIndex,
					hostPeer.getBitField().cardinality());
			// If all are received, merge the file.
			FileUtil.MergeTheFileIfWeCan(hostPeer);

			return pieceIndex;
		}

		return null;

	}

	public static void broadcastHave(CopyOnWriteArrayList<Peer> neighbors, Integer havePiece) throws IOException {

		LogUtil.logInfo("broadcasting " + havePiece);
		new BroadcastHave(neighbors, new Message(ByteBuffer.allocate(4).putInt(havePiece).array(), MessageType.have))
				.start();
	}

	public static void haveRcvd(Message message, Peer hostPeer, Peer remotePeer) throws IOException {
		// Get the piece index from the message
		Integer pieceIndex = ByteBuffer.wrap(message.getMessagePayLoad()).getInt();
		LogUtil.logHave(hostPeer.getPeerId(), remotePeer.getPeerId(), pieceIndex);
		remotePeer.setBitFieldValue(pieceIndex, true);
		if (remotePeer.getBitField().cardinality() == CommonConfigModel.getNumOfPieces()) {
			LogUtil.logInfo("Peer " + remotePeer.getPeerId() + " has the file");
			remotePeer.setHasFile(true);
		}
		if (!hostPeer.getBitFieldValue(pieceIndex)) {
			// send interested
			Message interestedMsg = new Message(null, MessageType.interested);
			// MessagingService.sendMessage(outStream, interestedMsg);
			remotePeer.getService().sendMessage(interestedMsg);
			// interestedMsg.sendMessage(outStream);
		}

	}

}
