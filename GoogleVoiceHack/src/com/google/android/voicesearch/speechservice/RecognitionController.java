package com.google.android.voicesearch.speechservice;

import android.content.Intent;
import android.speech.RecognitionListener;

public interface RecognitionController {
	public static final String ACTION_ANALYZE_SPEECH = "android.speech.action.ANALYZE_SPEECH";
	public static final String ACTION_TEST_RESULTS = "com.google.android.voicesearch.TEST_RESULTS";
	public static final boolean DEBUG_ENABLE_TEST_AUTOMATION = false;
	public static final int EVENT_RETRY = 0;
	public static final String EXTRA_ACTION_CONTEXT_ACTION_TYPE = "android.speech.extras.ACTION_CONTEXT_ACTION_TYPE";
	public static final String EXTRA_ACTION_CONTEXT_SELECTED_SLOT = "android.speech.extras.ACTION_CONTEXT_SELECTED_SLOT";
	public static final String EXTRA_CONTACT_AUTH = "contact_auth";
	public static final String EXTRA_PLAYBACK_FILENAME = "android.speech.extras.PLAYBACK_FILENAME";
	public static final String EXTRA_RAW_AUDIO = "android.speech.extras.RAW_AUDIO";
	public static final String EXTRA_RECOGNITION_CONTEXT = "android.speech.extras.RECOGNITION_CONTEXT";
	public static final String EXTRA_RECORD_FILENAME = "android.speech.extras.RECORD_FILENAME";
	public static final String EXTRA_SPEECH_TIMEOUT_MILLIS = "android.speech.extras.SPEECH_TIMEOUT_MILLIS";
	public static final String FULL_RECOGNITION_RESULTS = "fullRecognitionResults";
	public static final String FULL_RECOGNITION_RESULTS_REQUEST = "fullRecognitionResultsRequest";
	public static final String NOISE_LEVEL = "NoiseLevel";
	public static final String SIGNAL_NOISE_RATIO = "SignalNoiseRatio";
	public static final String USE_LOCATION = "useLocation";
	public static final int ERROR_NONE = -1;
	public static final int ERROR_NETWORK_TIMEOUT = 1;
	public static final int ERROR_NETWORK = 2;
	public static final int ERROR_AUDIO = 3;
	public static final int ERROR_SERVER = 4;
	public static final int ERROR_CLIENT = 5;
	public static final int ERROR_SPEECH_TIMEOUT = 6;
	public static final int ERROR_NO_MATCH = 7;

	public void enterIntoPauseMode();

	public void onCancel(RecognitionListener paramRecognitionListener);

	public void onDestroy();

	public void onPause();

	public void onStartListening(Intent paramIntent,
			RecognitionListener paramRecognitionListener);

	public void onStop();

	public void onStopListening(RecognitionListener paramRecognitionListener);
}