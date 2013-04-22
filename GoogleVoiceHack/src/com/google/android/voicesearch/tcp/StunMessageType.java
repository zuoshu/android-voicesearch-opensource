package com.google.android.voicesearch.tcp;

import java.util.Hashtable;

public class StunMessageType {
	public static final StunMessageType STUN_ALLOCATE_ERROR_RESPONSE;
	public static final StunMessageType STUN_ALLOCATE_REQUEST;
	public static final StunMessageType STUN_ALLOCATE_RESPONSE;
	public static final StunMessageType STUN_BINDING_ERROR_RESPONSE;
	public static final StunMessageType STUN_BINDING_REQUEST;
	public static final StunMessageType STUN_BINDING_RESPONSE;
	public static final StunMessageType STUN_DATA_INDICATION;
	public static final StunMessageType STUN_SEND_ERROR_RESPONSE;
	public static final StunMessageType STUN_SEND_REQUEST;
	public static final StunMessageType STUN_SEND_RESPONSE;
	public static final StunMessageType STUN_SHARED_SECRET_ERROR_RESPONSE;
	public static final StunMessageType STUN_SHARED_SECRET_REQUEST;
	public static final StunMessageType STUN_SHARED_SECRET_RESPONSE;
	private static final Hashtable<Integer, StunMessageType> wireMappings = new Hashtable();
	private final String stringValue;
	private final int wireValue;

	static {
		STUN_BINDING_REQUEST = new StunMessageType(1, "STUN_BINDING_REQUEST");
		STUN_BINDING_RESPONSE = new StunMessageType(257,
				"STUN_BINDING_RESPONSE");
		STUN_BINDING_ERROR_RESPONSE = new StunMessageType(273,
				"STUN_BINDING_ERROR_RESPONSE");
		STUN_SHARED_SECRET_REQUEST = new StunMessageType(2,
				"STUN_SHARED_SECRET_REQUEST");
		STUN_SHARED_SECRET_RESPONSE = new StunMessageType(258,
				"STUN_SHARED_SECRET_RESPONSE");
		STUN_SHARED_SECRET_ERROR_RESPONSE = new StunMessageType(274,
				"STUN_SHARED_SECRET_ERROR_RESPONSE");
		STUN_ALLOCATE_REQUEST = new StunMessageType(3, "STUN_ALLOCATE_REQUEST");
		STUN_ALLOCATE_RESPONSE = new StunMessageType(259,
				"STUN_ALLOCATE_RESPONSE");
		STUN_ALLOCATE_ERROR_RESPONSE = new StunMessageType(275,
				"STUN_ALLOCATE_ERROR_RESPONSE");
		STUN_SEND_REQUEST = new StunMessageType(4, "STUN_SEND_REQUEST");
		STUN_SEND_RESPONSE = new StunMessageType(260, "STUN_SEND_RESPONSE");
		STUN_SEND_ERROR_RESPONSE = new StunMessageType(276,
				"STUN_SEND_ERROR_RESPONSE");
		STUN_DATA_INDICATION = new StunMessageType(277, "STUN_DATA_INDICATION");
	}

	private StunMessageType(int paramInt, String paramString) {
		this.wireValue = paramInt;
		this.stringValue = paramString;
		wireMappings.put(new Integer(paramInt), this);
	}

	public static StunMessageType fromWireValue(int paramInt) {
		return (StunMessageType) wireMappings.get(new Integer(paramInt));
	}

	public int getWireValue() {
		return this.wireValue;
	}

	public String toString() {
		return this.stringValue;
	}
}