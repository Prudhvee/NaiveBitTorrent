package com.uf.cn.p2p.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;

import com.uf.cn.p2p.model.CommonConfigModel;
import com.uf.cn.p2p.model.Peer;
import com.uf.cn.p2p.model.RemotePeerInfo;

public class ConfigParseUtil {
	static String path = System.getProperty("user.dir");

	public static void parseCommonConfig() {
		String str;
		try {

			BufferedReader in = new BufferedReader(new FileReader(path + "/" + "Common.cfg"));
			while ((str = in.readLine()) != null) {
				str = str.trim();
				if ((str.length() <= 0) || (str.startsWith("#"))) {
					continue;
				}
				String[] tokens = str.split("\\s+");
				switch (tokens[0]) {
				case "NumberOfPreferredNeighbors":
					CommonConfigModel.setNumOfPrefNeighbors(Integer.parseInt(tokens[1]));
					break;
				case "UnchokingInterval":
					CommonConfigModel.setUnchokingInterval(Integer.parseInt(tokens[1]));
					break;
				case "OptimisticUnchokingInterval":
					CommonConfigModel.setOptismisticUnchokingInterval(Integer.parseInt(tokens[1]));
					break;
				case "FileName":
					CommonConfigModel.setFileName(tokens[1]);
					break;
				case "FileSize":
					CommonConfigModel.setFileSize(Long.parseLong(tokens[1]));
					break;
				case "PieceSize":
					CommonConfigModel.setPieceSize(Integer.parseInt(tokens[1]));
				}
			}

			CommonConfigModel.setNumOfPieces(
					(int)Math.ceil((double) CommonConfigModel.getFileSize() / CommonConfigModel.getPieceSize()));
			in.close();
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}

	}

	public static Vector<Peer> parsePeerConfig() {
		Vector<Peer> vec = new Vector<>();
		String st;
		Integer index = 0;
		try {
			BufferedReader in = new BufferedReader(new FileReader(path + "/" + "PeerInfo.cfg"));
			while ((st = in.readLine()) != null) {
				String[] tokens = st.split("\\s+");
				vec.addElement(new Peer(index++, Integer.parseInt(tokens[0]), tokens[1], Integer.parseInt(tokens[2]),
						tokens[3].equals("1")));
			}
			in.close();
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
		return vec;
	}
}
