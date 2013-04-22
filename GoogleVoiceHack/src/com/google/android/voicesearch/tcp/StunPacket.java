package com.google.android.voicesearch.tcp;

import java.io.IOException;
import java.util.Random;
import java.util.Vector;

public class StunPacket {
	private static final int HEADER_SIZE = 20;
	private Vector<StunAttribute> messageAttributes = new Vector();
	private int messageLength;
	private StunMessageType messageType;
	private byte[] transactionId;

	private StunPacket() {
		this.transactionId = new byte[16];
	}

	public StunPacket(StunMessageType paramStunMessageType) {
		this.messageType = paramStunMessageType;
		this.transactionId = new byte[16];
		Random localRandom = new Random();
		for (int i = 0; i < 16; ++i)
			this.transactionId[i] = (byte) localRandom.nextInt();
		this.messageLength = 0;
	}

	private boolean equalsStunPacket(StunPacket paramStunPacket) {
		if (paramStunPacket.messageType != this.messageType)
			return false;
		if (paramStunPacket.getLength() != getLength())
			return false;
		if (paramStunPacket.messageAttributes.size() != this.messageAttributes
				.size())
			return false;
		for (int i = 0; i < 16; ++i)
			if (paramStunPacket.transactionId[i] != this.transactionId[i])
				return false;
		for (int j = 0; j < this.messageAttributes.size(); ++j)
			if (!((StunAttribute) paramStunPacket.messageAttributes
					.elementAt(j)).equals(this.messageAttributes.elementAt(j)))
				return false;
		return true;
	}

	public static StunPacket fromByteArray(byte[] paramArrayOfByte)
			throws IOException {
		StunPacket localStunPacket = new StunPacket();
		if (!localStunPacket.readHeader(paramArrayOfByte))
			throw new IOException("could not read stun header");
		localStunPacket.readBody(paramArrayOfByte);
		return localStunPacket;
	}

	public static StunPacket headerFromByteArray(byte[] paramArrayOfByte) {
		StunPacket localStunPacket = new StunPacket();
		if (localStunPacket.readHeader(paramArrayOfByte))
			return localStunPacket;
		return null;
	}

	private boolean readHeader(byte[] paramArrayOfByte) {
		if (paramArrayOfByte.length < 20)
			return false;
		this.messageType = StunMessageType
				.fromWireValue(((0xFF & paramArrayOfByte[0]) << 8)
						+ (0xFF & paramArrayOfByte[1]));
		this.messageLength = (((0xFF & paramArrayOfByte[2]) << 8) + (0xFF & paramArrayOfByte[3]));
		System.arraycopy(paramArrayOfByte, 4, this.transactionId, 0, 16);
		if (this.messageLength != paramArrayOfByte.length - 20)
			return false;
		return this.messageType != null;
	}

	public void addAttribute(StunAttribute paramStunAttribute) {
		this.messageAttributes.addElement(paramStunAttribute);
	}

	public byte[] asByteArray() {
		byte[] arrayOfByte = new byte[getLength()];
		writeHeader(arrayOfByte);
		int i = 0;
		int k;
		for (int j = 20; i < this.messageAttributes.size(); j = k) {
			((StunAttribute) this.messageAttributes.elementAt(i))
					.writeIntoArray(arrayOfByte, j);
			k = j
					+ ((StunAttribute) this.messageAttributes.elementAt(i))
							.getLength();
			++i;
		}
		return arrayOfByte;
	}

	public boolean equals(Object paramObject) {
		if (paramObject instanceof StunPacket)
			return equalsStunPacket((StunPacket) paramObject);
		return false;
	}

	public StunAttribute getAttribute(StunAttributeType paramStunAttributeType) {
		for (int i = 0; i < this.messageAttributes.size(); ++i) {
			StunAttribute localStunAttribute = (StunAttribute) this.messageAttributes
					.elementAt(i);
			if (localStunAttribute.getType() == paramStunAttributeType)
				return localStunAttribute;
		}
		return null;
	}

	public int getLength() {
		int i = 0;
		int k;
		int j;
		for (j = 20; i < this.messageAttributes.size(); j = k) {
			k = j
					+ ((StunAttribute) this.messageAttributes.elementAt(i))
							.getLength();
			++i;
		}
		return j;
	}

	public int getMessageLength() {
		return this.messageLength;
	}

	public String getTransactionId() {
		String str = "";
		for (int i = 0; i < this.transactionId.length; ++i)
			str = str + Integer.toString(0xFF & this.transactionId[i], 16);
		return str;
	}

	public StunMessageType getType() {
		return this.messageType;
	}

	public void readBody(byte[] paramArrayOfByte) throws IOException {
		int i = 20;
		while (i < 20 + this.messageLength) {
			StunAttribute localStunAttribute = StunAttribute.fromByteArray(
					paramArrayOfByte, i);
			i += localStunAttribute.getLength();
			this.messageAttributes.addElement(localStunAttribute);
		}
		this.messageLength = (getLength() - 20);
	}

	public void setTransactionIDForResponse(StunPacket paramStunPacket) {
		System.arraycopy(paramStunPacket.transactionId, 0, this.transactionId,
				0, paramStunPacket.transactionId.length);
	}

	public String toString() {
		String str = "Stun Packet:\ntype= " + this.messageType.toString()
				+ "\ntransactionId: " + getTransactionId() + "\n";
		for (int i = 0; i < this.messageAttributes.size(); ++i)
			str = str
					+ ((StunAttribute) this.messageAttributes.elementAt(i))
							.toString() + "\n";
		return str;
	}

	protected void writeHeader(byte[] paramArrayOfByte) {
		paramArrayOfByte[0] = (byte) (this.messageType.getWireValue() >>> 8);
		paramArrayOfByte[1] = (byte) this.messageType.getWireValue();
		int i = getLength() - 20;
		paramArrayOfByte[2] = (byte) (i >>> 8);
		paramArrayOfByte[3] = (byte) i;
		System.arraycopy(this.transactionId, 0, paramArrayOfByte, 4, 16);
	}
}