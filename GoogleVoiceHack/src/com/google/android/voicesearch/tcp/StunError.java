package com.google.android.voicesearch.tcp;

import java.util.Hashtable;

public class StunError {
	public static final StunError STUN_ERROR_BAD_REQUEST;
	public static final StunError STUN_ERROR_GLOBAL_FAILURE;
	public static final StunError STUN_ERROR_INTEGRITY_CHECK_FAILURE;
	public static final StunError STUN_ERROR_MISSING_USERNAME;
	public static final StunError STUN_ERROR_SERVER_ERROR;
	public static final StunError STUN_ERROR_STALE_CREDENTIALS;
	public static final StunError STUN_ERROR_UNAUTHORIZED;
	public static final StunError STUN_ERROR_UNKNOWN_ATTRIBUTE;
	public static final StunError STUN_ERROR_USE_TLS;
	private static Hashtable<Integer, StunError> errorCodeMappings = new Hashtable();
	private int errorCode;
	private String errorReason;

	static {
		STUN_ERROR_BAD_REQUEST = new StunError(400, "Bad Request");
		STUN_ERROR_UNAUTHORIZED = new StunError(401, "Unauthorized");
		STUN_ERROR_UNKNOWN_ATTRIBUTE = new StunError(420, "Unknown Attribute");
		STUN_ERROR_STALE_CREDENTIALS = new StunError(430, "Stale Credentials");
		STUN_ERROR_INTEGRITY_CHECK_FAILURE = new StunError(431,
				"Integrity Check Failure");
		STUN_ERROR_MISSING_USERNAME = new StunError(432, "Missing Username");
		STUN_ERROR_USE_TLS = new StunError(433, "Use TLS");
		STUN_ERROR_SERVER_ERROR = new StunError(500, "Server Error");
		STUN_ERROR_GLOBAL_FAILURE = new StunError(600, "Global Failure");
	}

	private StunError(int paramInt, String paramString) {
		this.errorCode = paramInt;
		this.errorReason = paramString;
		errorCodeMappings.put(Integer.valueOf(paramInt), this);
	}

	public static StunError fromErrorCode(int paramInt) {
		return (StunError) errorCodeMappings.get(new Integer(paramInt));
	}

	public String toString() {
		return this.errorCode + " " + this.errorReason;
	}
}