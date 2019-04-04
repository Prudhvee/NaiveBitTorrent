package com.uf.cn.p2p.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;

import com.uf.cn.p2p.model.RemotePeerInfo;

public class startRemoteProcess {
	public Vector<RemotePeerInfo> peerInfoVector;
	
	public void getConfiguration()
	{
		String st;
		int i1;
		peerInfoVector = new Vector<RemotePeerInfo>();
		try {
			BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
			int index = 0;
			while((st = in.readLine()) != null) {
				
				 String[] tokens = st.split("\\s+");
		    	 //System.out.println("tokens begin ----");
			     //for (int x=0; x<tokens.length; x++) {
			     //    System.out.println(tokens[x]);
			     //}
		         //System.out.println("tokens end ----");
			    
			     peerInfoVector.addElement(new RemotePeerInfo(index++, tokens[0], tokens[1], tokens[2], tokens[3].equals("1")));
			
			}
			
			in.close();
		}
		catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			startRemoteProcess myStart = new startRemoteProcess();
			myStart.getConfiguration();
					
			// get current path
			String path = System.getProperty("user.dir");
			
			// start clients at remote hosts
			for (int i = 0; i < myStart.peerInfoVector.size(); i++) {
				RemotePeerInfo pInfo = (RemotePeerInfo) myStart.peerInfoVector.elementAt(i);
				
				System.out.println("Start remote peer " + pInfo.peerId +  " at " + pInfo.peerAddress );
				
				// *********************** IMPORTANT *************************** //
				// If your program is JAVA, use this line.
				Runtime.getRuntime().exec("ssh " + pInfo.peerAddress + " cd " + path + "; java peerProcess " + pInfo.peerId);
				
				// If your program is C/C++, use this line instead of the above line. 
				//Runtime.getRuntime().exec("ssh " + pInfo.peerAddress + " cd " + path + "; ./peerProcess " + pInfo.peerId);
			}		
			System.out.println("Starting all remote peers has done." );

		}
		catch (Exception ex) {
			System.out.println(ex);
		}
	}

	
}
