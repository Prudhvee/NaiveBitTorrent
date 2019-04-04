package com.uf.cn.p2p.model;

public enum MessageType {
	choke((byte)0),
	unchoke((byte)1),
	interested((byte)2),
	notInterested((byte)3),
	have((byte)4),
	bitField((byte)5),
	request((byte)6),
	piece((byte)7);
	
	private byte _type;
	
	MessageType(byte inType)
	{
		_type = inType;
	}
	
	public static MessageType getType(byte b)
	{
		for (MessageType t : MessageType.values()) {
            if (t._type == b) {
                return t;
            }
        }
        throw new IllegalArgumentException();
	}
	
	public byte getType()
	{
		return _type;
	}
}
