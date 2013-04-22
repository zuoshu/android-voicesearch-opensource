package com.google.android.voicesearch.tcp;

import java.util.Hashtable;

public class StunAttributeType {
	public static final StunAttributeType STUN_ATTR_ALTERNATE_SERVER;
	public static final StunAttributeType STUN_ATTR_BANDWIDTH;
	public static final StunAttributeType STUN_ATTR_CHANGED_ADDRESS;
	public static final StunAttributeType STUN_ATTR_CHANGE_REQUEST;
	public static final StunAttributeType STUN_ATTR_DATA;
	public static final StunAttributeType STUN_ATTR_DESTINATION_ADDRESS;
	public static final StunAttributeType STUN_ATTR_ERROR_CODE;
	public static final StunAttributeType STUN_ATTR_LIFETIME;
	public static final StunAttributeType STUN_ATTR_MAGIC_COOKIE;
	public static final StunAttributeType STUN_ATTR_MAPPED_ADDRESS;
	public static final StunAttributeType STUN_ATTR_MESSAGE_INTEGRITY;
	public static final StunAttributeType STUN_ATTR_OPTIONS;
	public static final StunAttributeType STUN_ATTR_PASSWORD;
	public static final StunAttributeType STUN_ATTR_REFLECTED_FROM;
	public static final StunAttributeType STUN_ATTR_RESPONSE_ADDRESS;
	public static final StunAttributeType STUN_ATTR_SOURCE_ADDRESS;
	public static final StunAttributeType STUN_ATTR_SOURCE_ADDRESS2;
	public static final StunAttributeType STUN_ATTR_TRANSPORT_PREFERENCES;
	public static final StunAttributeType STUN_ATTR_UNKNOWN;
	public static final StunAttributeType STUN_ATTR_UNKNOWN_ATTRIBUTES;
	public static final StunAttributeType STUN_ATTR_USERNAME;
	private static final Hashtable<Integer, StunAttributeType> wireMappings = new Hashtable();
	private final String stringValue;
	private final int wireValue;

	static {
		STUN_ATTR_MAPPED_ADDRESS = new StunAttributeType(1,
				"STUN_ATTR_MAPPED_ADDRESS");
		STUN_ATTR_RESPONSE_ADDRESS = new StunAttributeType(2,
				"STUN_ATTR_RESPONSE_ADDRESS");
		STUN_ATTR_CHANGE_REQUEST = new StunAttributeType(3,
				"STUN_ATTR_CHANGE_REQUEST");
		STUN_ATTR_SOURCE_ADDRESS = new StunAttributeType(4,
				"STUN_ATTR_SOURCE_ADDRESS");
		STUN_ATTR_CHANGED_ADDRESS = new StunAttributeType(5,
				"STUN_ATTR_CHANGED_ADDRESS");
		STUN_ATTR_USERNAME = new StunAttributeType(6, "STUN_ATTR_USERNAME");
		STUN_ATTR_PASSWORD = new StunAttributeType(7, "STUN_ATTR_PASSWORD");
		STUN_ATTR_MESSAGE_INTEGRITY = new StunAttributeType(8,
				"STUN_ATTR_MESSAGE_INTEGRITY");
		STUN_ATTR_ERROR_CODE = new StunAttributeType(9, "STUN_ATTR_ERROR_CODE");
		STUN_ATTR_UNKNOWN_ATTRIBUTES = new StunAttributeType(10,
				"STUN_ATTR_UNKNOWN_ATTRIBUTES");
		STUN_ATTR_REFLECTED_FROM = new StunAttributeType(11,
				"STUN_ATTR_REFLECTED_FROM");
		STUN_ATTR_TRANSPORT_PREFERENCES = new StunAttributeType(12,
				"STUN_ATTR_TRANSPORT_PREFERENCES");
		STUN_ATTR_LIFETIME = new StunAttributeType(13, "STUN_ATTR_LIFETIME");
		STUN_ATTR_ALTERNATE_SERVER = new StunAttributeType(14,
				"STUN_ATTR_ALTERNATE_SERVER");
		STUN_ATTR_MAGIC_COOKIE = new StunAttributeType(15,
				"STUN_ATTR_MAGIC_COOKIE");
		STUN_ATTR_BANDWIDTH = new StunAttributeType(16, "STUN_ATTR_BANDWIDTH");
		STUN_ATTR_DESTINATION_ADDRESS = new StunAttributeType(17,
				"STUN_ATTR_DESTINATION_ADDRESS");
		STUN_ATTR_SOURCE_ADDRESS2 = new StunAttributeType(18,
				"STUN_ATTR_SOURCE_ADDRESS2");
		STUN_ATTR_DATA = new StunAttributeType(19, "STUN_ATTR_DATA");
		STUN_ATTR_OPTIONS = new StunAttributeType(32769, "STUN_ATTR_OPTIONS");
		STUN_ATTR_UNKNOWN = new StunAttributeType(0, "STUN_ATTR_UNKNOWN");
	}

	private StunAttributeType(int paramInt, String paramString) {
		this.wireValue = paramInt;
		this.stringValue = paramString;
		wireMappings.put(new Integer(paramInt), this);
	}

	public static StunAttributeType fromWireValue(int paramInt) {
		return (StunAttributeType) wireMappings.get(new Integer(paramInt));
	}

	public int getWireValue() {
		return this.wireValue;
	}

	public String toString() {
		return this.stringValue;
	}
}