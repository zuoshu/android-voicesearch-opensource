package com.google;

import android.util.Log;

public class TestPlatformLog {
	private static final String EVENT_ERROR_PREFIX = "ERROR: ";
	private static final String EVENT_LOGGING_AUDIO_PREFIX = "LOGGING_AUDIO: ";
	private static final String EVENT_PREFIX = "TEST_PLATFORM: ";
	private static final String EVENT_RESULTS_PREFIX = "RESULTS: ";
	public static final String EVENT_SPEAK_NOW = "SPEAK_NOW";
	public static final String EVENT_VOICE_SEARCH_COMPLETE = "VOICE_SEARCH_COMPLETE";
	public static final String EVENT_WORKING = "WORKING";
	private static final String NON_WEB_SEARCH_RESULT = "result:NON_WEB_SEARCH_RESULT,";
	private static final String RESULT_ITEM_FORMAT = "result:\"%s\",";
	private static final String TAG = "TestPlatformLog";
	private static boolean sEnableTestPlatformLogging = false;

	public static void log(String paramString) {
		if (!sEnableTestPlatformLogging)
			return;
		Log.i("TestPlatformLog", "TEST_PLATFORM: " + paramString);
	}

	public static void logAudioPath(String paramString) {
		log("LOGGING_AUDIO: " + paramString);
	}

	public static void logError(String paramString) {
		log("ERROR: " + paramString);
	}

	public static void setEnabled(boolean paramBoolean) {
		sEnableTestPlatformLogging = paramBoolean;
	}
}