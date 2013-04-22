package com.google.android.voicesearch.tcp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class StunAttribute {
	private StunAttributeData data;
	private StunAttributeType type;

	private StunAttribute() {
	}

	public StunAttribute(StunAttributeType paramStunAttributeType) {
		this.type = paramStunAttributeType;
	}

	private boolean equalsSTUNAttribute(StunAttribute paramStunAttribute) {
		if (paramStunAttribute.type != this.type)
			return false;
		if ((paramStunAttribute.data == null) || (this.data == null))
			return paramStunAttribute.data == this.data;
		if (paramStunAttribute.data.asByteArray().length != this.data
				.asByteArray().length)
			return false;
		for (int i = 0; i < this.data.asByteArray().length; ++i)
			if (paramStunAttribute.data.asByteArray()[i] != this.data
					.asByteArray()[i])
				return false;
		return true;
	}

	public static StunAttribute fromByteArray(byte[] paramArrayOfByte,
			int paramInt) throws IOException {
		StunAttribute localStunAttribute = new StunAttribute();
		int i = (0xFF & paramArrayOfByte[(paramInt + 0)]) << 8
				| paramArrayOfByte[(paramInt + 1)];
		localStunAttribute.type = StunAttributeType.fromWireValue(i);
		int j = (0xFF & paramArrayOfByte[(paramInt + 2)]) << 8 | 0xFF
				& paramArrayOfByte[(paramInt + 3)];
		if ((localStunAttribute.type == StunAttributeType.STUN_ATTR_MAPPED_ADDRESS)
				|| (localStunAttribute.type == StunAttributeType.STUN_ATTR_SOURCE_ADDRESS)
				|| (localStunAttribute.type == StunAttributeType.STUN_ATTR_CHANGED_ADDRESS)) {
			localStunAttribute.data = Address.fromByteArray(paramArrayOfByte,
					paramInt + 4);
			return localStunAttribute;
		}
		if (localStunAttribute.type == StunAttributeType.STUN_ATTR_ERROR_CODE) {
			localStunAttribute.data = ErrorCode.fromByteArray(paramArrayOfByte,
					paramInt + 4, j);
			return localStunAttribute;
		}
		if (localStunAttribute.type == StunAttributeType.STUN_ATTR_USERNAME) {
			localStunAttribute.data = Username.fromByteArray(paramArrayOfByte,
					paramInt + 4, j);
			return localStunAttribute;
		}
		if ((0xFFFF & i) <= 32767)
			throw new IOException("Mandatory STUN attribute type " + i
					+ " not supported.");
		localStunAttribute.type = StunAttributeType.STUN_ATTR_UNKNOWN;
		localStunAttribute.data = new UnknownAttribute(j);
		return localStunAttribute;
	}

	public static StunAttributeData newAddress(String paramString) {
		return new Address(paramString);
	}

	public boolean equals(Object paramObject) {
		if (paramObject instanceof StunAttribute)
			return equalsSTUNAttribute((StunAttribute) paramObject);
		return false;
	}

	public StunAttributeData getData() {
		return this.data;
	}

	public int getLength() {
		return 4 + this.data.getLength();
	}

	public StunAttributeType getType() {
		return this.type;
	}

	public int hashCode() {
		int i = 629 + this.type.getWireValue();
		for (int j = 0; j < this.data.asByteArray().length; ++j)
			i = i * 37 + this.data.asByteArray()[j];
		return i;
	}

	public void setData(StunAttributeData paramStunAttributeData) {
		this.data = paramStunAttributeData;
	}

	public String toString() {
		return this.type.toString() + ": " + this.data.toString();
	}

	public void writeIntoArray(byte[] paramArrayOfByte, int paramInt) {
		paramArrayOfByte[(paramInt + 0)] = (byte) (0xFF & this.type
				.getWireValue() >>> 8);
		paramArrayOfByte[(paramInt + 1)] = (byte) (0xFF & this.type
				.getWireValue());
		paramArrayOfByte[(paramInt + 2)] = (byte) (0xFF & this.data.getLength() >>> 8);
		paramArrayOfByte[(paramInt + 3)] = (byte) (0xFF & this.data.getLength());
		System.arraycopy(this.data.asByteArray(), 0, paramArrayOfByte,
				paramInt + 4, this.data.getLength());
	}

	public static class Address implements StunAttributeData {
		private byte[] ipAddr = new byte[4];
		private int port;

		private Address() {
		}

		public Address(String paramString) {
			if (paramString.indexOf(':') == -1)
				throw new IllegalArgumentException(
						"Address not of form ip_address:port_number");
			this.port = Integer.valueOf(
					paramString.substring(1 + paramString.indexOf(':')))
					.intValue();
			int i = 0;
			int j = 0;
			while (i < 4) {
				int k = paramString.indexOf('.', j);
				if (k < 0)
					k = paramString.indexOf(':', j);
				if (k < 0)
					throw new IllegalArgumentException(
							"Address not of form x.x.x.x:port");
				this.ipAddr[i] = Integer.valueOf(paramString.substring(j, k))
						.byteValue();
				j = k + 1;
				++i;
			}
		}

		private boolean equalsAddress(Address paramAddress) {
			return paramAddress.toString().equals(toString());
		}

		public static Address fromByteArray(byte[] paramArrayOfByte,
				int paramInt) {
			Address localAddress = new Address();
			localAddress.port = ((0xFF & paramArrayOfByte[(paramInt + 2)]) << 8 | 0xFF & paramArrayOfByte[(paramInt + 3)]);
			localAddress.ipAddr[0] = paramArrayOfByte[(paramInt + 4)];
			localAddress.ipAddr[1] = paramArrayOfByte[(paramInt + 5)];
			localAddress.ipAddr[2] = paramArrayOfByte[(paramInt + 6)];
			localAddress.ipAddr[3] = paramArrayOfByte[(paramInt + 7)];
			return localAddress;
		}

		public byte[] asByteArray() {
			byte[] arrayOfByte = new byte[8];
			arrayOfByte[0] = -1;
			arrayOfByte[1] = 1;
			arrayOfByte[2] = (byte) (this.port >>> 8);
			arrayOfByte[3] = (byte) this.port;
			arrayOfByte[4] = this.ipAddr[0];
			arrayOfByte[5] = this.ipAddr[1];
			arrayOfByte[6] = this.ipAddr[2];
			arrayOfByte[7] = this.ipAddr[3];
			return arrayOfByte;
		}

		public boolean equals(Object paramObject) {
			if (paramObject instanceof Address)
				return equalsAddress((Address) paramObject);
			return false;
		}

		public int getLength() {
			return 8;
		}

		public int hashCode() {
			return toString().hashCode();
		}

		public String toString() {
			return String.valueOf(0xFF & this.ipAddr[0]) + "."
					+ String.valueOf(0xFF & this.ipAddr[1]) + "."
					+ String.valueOf(0xFF & this.ipAddr[2]) + "."
					+ String.valueOf(0xFF & this.ipAddr[3]) + ":"
					+ String.valueOf(this.port);
		}
	}

	public static class ErrorCode implements StunAttributeData {
		private int errorCode = 0;
		private String errorReason = null;

		private ErrorCode() {
		}

		public ErrorCode(int paramInt, String paramString) {
			this.errorCode = paramInt;
			this.errorReason = paramString;
		}

		private boolean equalsErrorCode(ErrorCode paramErrorCode) {
			return paramErrorCode.errorCode == this.errorCode;
		}

		public static ErrorCode fromByteArray(byte[] paramArrayOfByte,
				int paramInt1, int paramInt2) {
			ErrorCode localErrorCode = new ErrorCode();
			localErrorCode.errorCode = (100 * (0xFF & paramArrayOfByte[(paramInt1 + 2)]) + (0xFF & paramArrayOfByte[(paramInt1 + 3)]));
			localErrorCode.errorReason = "";
			for (int i = paramInt1 + 4; i < paramInt1 + paramInt2; ++i)
				localErrorCode.errorReason += (char) paramArrayOfByte[i];
			return localErrorCode;
		}

		public byte[] asByteArray() {
			byte[] arrayOfByte1 = new byte[getLength()];
			arrayOfByte1[0] = 0;
			arrayOfByte1[1] = 0;
			arrayOfByte1[2] = (byte) (this.errorCode / 100);
			arrayOfByte1[3] = (byte) (this.errorCode % 100);
			byte[] arrayOfByte2;
			try {
				byte[] arrayOfByte3 = this.errorReason.getBytes("UTF-8");
				arrayOfByte2 = arrayOfByte3;
			} catch (UnsupportedEncodingException localUnsupportedEncodingException) {
				arrayOfByte2 = new byte[0];
			}
			System.arraycopy(arrayOfByte2, 0, arrayOfByte1, 4,
					arrayOfByte2.length);
			return arrayOfByte1;
		}

		public StunError asSTUNError() {
			return StunError.fromErrorCode(this.errorCode);
		}

		public boolean equals(Object paramObject) {
			if (paramObject instanceof ErrorCode)
				return equalsErrorCode((ErrorCode) paramObject);
			return false;
		}

		public int getLength() {
			try {
				byte[] arrayOfByte = this.errorReason.getBytes("UTF-8");
				if (arrayOfByte.length % 4 != 0)
					this.errorReason += "   ".substring(0,
							4 - arrayOfByte.length % 4);
				int i = this.errorReason.getBytes("UTF-8").length;
				return i + 4;
			} catch (UnsupportedEncodingException localUnsupportedEncodingException) {
				this.errorReason = "";
			}
			return 4;
		}

		public int hashCode() {
			return toString().hashCode();
		}

		public String toString() {
			return this.errorCode + " " + this.errorReason;
		}
	}

	public static class STUNUint32 implements StunAttributeData {
		private int data = 0;

		private STUNUint32() {
		}

		public STUNUint32(int paramInt) {
			this.data = paramInt;
		}

		public static STUNUint32 fromByteArray(byte[] paramArrayOfByte,
				int paramInt) {
			STUNUint32 localSTUNUint32 = new STUNUint32();
			localSTUNUint32.data = (((0xFF & paramArrayOfByte[(paramInt + 4)]) << 24)
					+ ((0xFF & paramArrayOfByte[(paramInt + 4)]) << 16)
					+ ((0xFF & paramArrayOfByte[(paramInt + 4)]) << 8) + (0xFF & paramArrayOfByte[(paramInt + 4)]));
			return localSTUNUint32;
		}

		public byte[] asByteArray() {
			byte[] arrayOfByte = new byte[4];
			arrayOfByte[0] = (byte) (this.data >>> 24);
			arrayOfByte[1] = (byte) (this.data >>> 16);
			arrayOfByte[2] = (byte) (this.data >>> 8);
			arrayOfByte[3] = (byte) this.data;
			return arrayOfByte;
		}

		public int getLength() {
			return 4;
		}

		public String toString() {
			return Integer.toHexString(this.data);
		}
	}

	public static class UnknownAttribute implements StunAttributeData {
		private int dataLength;

		public UnknownAttribute(int paramInt) {
			this.dataLength = paramInt;
		}

		public byte[] asByteArray() {
			return null;
		}

		public int getLength() {
			return this.dataLength;
		}
	}

	public static class Username implements StunAttributeData {
		private String data;

		private Username() {
		}

		public Username(String paramString) {
			this.data = paramString;
		}

		public static Username fromByteArray(byte[] paramArrayOfByte,
				int paramInt1, int paramInt2) {
			Username localUsername = new Username();
			try {
				localUsername.data = new String(paramArrayOfByte, paramInt1,
						paramInt2, "UTF-8");
				return localUsername;
			} catch (UnsupportedEncodingException localUnsupportedEncodingException) {
				throw new RuntimeException("UTF-8 Encoding not supported");
			}
		}

		public byte[] asByteArray() {
			try {
				byte[] arrayOfByte = this.data.getBytes("UTF-8");
				return arrayOfByte;
			} catch (UnsupportedEncodingException localUnsupportedEncodingException) {
				throw new RuntimeException("UTF-8 Encoding not supported");
			}
		}

		public int getLength() {
			try {
				int i = this.data.getBytes("UTF-8").length;
				return i;
			} catch (UnsupportedEncodingException localUnsupportedEncodingException) {
				throw new RuntimeException("UTF-8 Encoding not supported");
			}
		}

		public String toString() {
			return this.data;
		}
	}
}