package com.uf.cn.p2p.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.uf.cn.p2p.model.CommonConfigModel;
import com.uf.cn.p2p.model.FilePiece;
import com.uf.cn.p2p.model.Peer;

public class FileUtil {

	public static String path = System.getProperty("user.dir") + "/src/";

	public static void MergeTheFileIfWeCan(Peer hostPeer) throws IOException {
		// If the number of bits set in bitfield is equal to the filePieces.length,
		// Then, I think we received all the pieces and ready to merge
		LogUtil.logInfo("Cardinality of the set " + hostPeer.getBitField().cardinality());
		LogUtil.logInfo("Number of pieces" + CommonConfigModel.getNumOfPieces());
		LogUtil.logInfo("BitSet" + hostPeer.getBitField().nextClearBit(0));
		if (hostPeer.getBitField().cardinality() == CommonConfigModel.getNumOfPieces()) {

			File outFile = new File(
					FileUtil.getDirectoryPath(hostPeer.getPeerId()) + "/" + CommonConfigModel.getFileName());
			FileOutputStream fos = new FileOutputStream(outFile);
			Path p;
			for (int i = 0; i < CommonConfigModel.getNumOfPieces(); i++) {
				p = FileUtil.getFilePath(hostPeer.getPeerId(), i);
				fos.write(Files.readAllBytes(p));

				// Delete ith Piece
				//Files.delete(p);
			}
			LogUtil.logDownloaded(hostPeer.getPeerId());
			hostPeer.setHasFile(true);
			fos.flush();
			fos.close();

		}

	}

	public static Integer breakIntoPieces(Integer peerId) throws IOException {

		// System.getProperty("user.dir") + "/src/";
		System.out.println("Breaking the file");
		File file = new File(path + "/" + peerId + "/" + CommonConfigModel.getFileName());

		System.out.println(file.toString());
		FileInputStream fis = new FileInputStream(file);
		FileOutputStream fos;
		byte[] pieceBytes;
		Integer fileLength = (int) file.length();
		int piece = 0;
		String pieceName;
		while (fileLength > 0) {
			// Read the bytes into the array
			pieceBytes = new byte[fileLength > CommonConfigModel.getPieceSize() ? CommonConfigModel.getPieceSize()
					: fileLength];
			int read = fis.read(pieceBytes);
			fileLength -= read;
			pieceName = path + "/" + peerId + "/" + "piece" + piece++;

			fos = new FileOutputStream(new File(pieceName));

			fos.write(pieceBytes);
			fos.flush();
			fos.close();

			pieceBytes = null;
			fos = null;
		}

		fis.close();
		return piece > 0 ? piece - 1 : piece;
	}

	public static Path getFilePath(Integer peerId, Integer piece) {
		return Paths.get(path + "/" + peerId + "/" + "piece" + piece);
	}

	public static String getDirectoryPath(Integer peerId) {
		return path + "/" + peerId;
	}

	public static void createDirectoryIfNotExists(Integer peerId) throws IOException {
		File file = new File(getDirectoryPath(peerId));
		if (!file.exists()) {
			file.mkdir();
		}

	}

	public static void deleteParts(Integer peerId) throws IOException {
		for(int i=0; i<CommonConfigModel.getNumOfPieces();i++)
			Files.delete(getFilePath(peerId, i));
		
	}

}
