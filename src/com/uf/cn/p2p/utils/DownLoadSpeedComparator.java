package com.uf.cn.p2p.utils;

import java.util.Comparator;

import com.uf.cn.p2p.model.Peer;

public class DownLoadSpeedComparator implements Comparator<Peer>{

	@Override
	public int compare(Peer o1, Peer o2) {
		return o1.getDownLoadSpeed().compareTo(o2.getDownLoadSpeed());
	}

}
