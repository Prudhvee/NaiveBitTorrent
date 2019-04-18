import java.io.*;
import java.util.*;
import java.lang.Runtime;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class StartRemotePeers {

	private static final String scriptPrefix = "java -jar peerToPeer.jar ";

	public static class PeerInfo {

		private String peerID;
		private String hostName;

		public PeerInfo(String peerID, String hostName) {
			super();
			this.peerID = peerID;
			this.hostName = hostName;
		}

		public String getPeerID() {
			return peerID;
		}

		public void setPeerID(String peerID) {
			this.peerID = peerID;
		}

		public String getHostName() {
			return hostName;
		}

		public void setHostName(String hostName) {
			this.hostName = hostName;
		}

	}

	public static void main(String[] args) {

		String ciseUser = "rogue"; // change ciseUser to the correct account username
		String projectName = ""; 
		
		// Read from Common.cfg to find fileName
		String fileName = null;
		File file = null;
		Scanner sc = null;
		try {
			file = new File(System.getProperty("user.dir") + "/"  + "Common.cfg");
			sc = new Scanner(file);
			while (sc.hasNextLine()) {
				String[] line = sc.nextLine().split(" ");
				if (line[0].equals("FileName")) {
					fileName = line[1];
					break;
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println(e);
		}
		
		System.out.println("File Name: " + fileName);
		
		// Read from peerInfo file, make subdirectories and upload file to correct subdirectories
		ArrayList<PeerInfo> peerList = new ArrayList<>();
		try {
			file = new File(System.getProperty("user.dir") + "/"  + "/PeerInfo.cfg");
			sc = new Scanner(file);
			while (sc.hasNextLine()) {
				String[] line = sc.nextLine().split(" ");
				
				// Extract variables from line
				String peerID = line[0];
				String hostname = line[1];
				String hasFile = line[3];
				
				// Create peer directory, this change will be reflected on all CISE lin114 computers 
				Runtime.getRuntime().exec("mkdir /home/" + ciseUser + "/peer_" + peerID);
				System.out.println("Peer " + peerID + "'s subdirectory has been created!" + "/home/" + ciseUser + "/peer_" + peerID);
				
				// Copy file to appropriate peers' subdirectories
				if (hasFile.equals("1")) {
					Runtime.getRuntime().exec("cp /home/" + ciseUser +"/" + fileName + " /home/" + ciseUser + "/peer_" + peerID);
					System.out.println(fileName + " has been copied into Peer " + peerID + "'s subdirectory!");
				}
				
				// Add peer to peerList
				peerList.add(new PeerInfo(peerID, hostname));
			}
		} catch (FileNotFoundException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}

		for (PeerInfo remotePeer : peerList) {
		
			try {
			
				JSch jsch = new JSch();
				
				/*
				* Give the path to your private key. Make sure your public key
				* is already within your remote CISE machine to ssh into it
				* without a password. Or you can use the corressponding method
				* of JSch which accepts a password.
				*/
				jsch.addIdentity("/home/" + ciseUser + "/.ssh/id_rsa", "rogue");
				Session session = jsch.getSession(ciseUser, remotePeer.getHostName(), 22);
				Properties config = new Properties();
				config.put("StrictHostKeyChecking", "no");
				session.setConfig(config);

				session.connect();

				System.out.println("Session to Peer " + remotePeer.getPeerID() + " at " + remotePeer.getHostName());

				Channel channel = session.openChannel("exec");
				((ChannelExec) channel).setCommand(scriptPrefix + remotePeer.getPeerID());

				channel.setInputStream(null);
				((ChannelExec) channel).setErrStream(System.err);

				InputStream input = channel.getInputStream();
				channel.connect();

				System.out.println("Channel connected to Peer " + remotePeer.getPeerID() + " at "
				+ remotePeer.getHostName() + " server with commands");

				(new Thread() {
					@Override
					public void run() {

					InputStreamReader inputReader = new InputStreamReader(input);
					BufferedReader bufferedReader = new BufferedReader(inputReader);
					String line = null;

					try {

						while ((line = bufferedReader.readLine()) != null) {
							System.out.println(remotePeer.getPeerID() + ">:" + line);
						}
					
						bufferedReader.close();
						inputReader.close();
						
					} catch (Exception ex) {
						System.out.println(remotePeer.getPeerID() + " Exception >:");
						ex.printStackTrace();
					}

					channel.disconnect();
					session.disconnect();
					}
				}).start();

			} catch (JSchException e) {
				System.out.println(remotePeer.getPeerID() + " JSchException >:");
				e.printStackTrace();
			} catch (IOException ex) {
				System.out.println(remotePeer.getPeerID() + " Exception >:");
				ex.printStackTrace();
			}

		}
	}
}
