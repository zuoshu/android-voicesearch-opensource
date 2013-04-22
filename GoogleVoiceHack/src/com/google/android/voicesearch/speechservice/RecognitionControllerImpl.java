package com.google.android.voicesearch.speechservice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.speech.RecognitionListener;
import android.util.Log;

import com.cyberobject.AudioListener;
import com.cyberobject.recognize.ParmsUtil;
import com.google.android.voicesearch.endpointer.EndpointerInputStream;
import com.google.android.voicesearch.endpointer.MicrophoneInputStream;
import com.google.protos.speech.service.ClientReportProto.ClientReport.ClientPerceivedRequestStatus;
import com.google.protos.speech.service.PartialResult.PartialRecognitionResult;
import com.google.protos.speech.service.SpeechService;
import com.google.protos.speech.service.SpeechService.Encoding;
import com.google.protos.speech.service.SpeechService.RecognizeResponse;
import com.google.protos.speech.service.SpeechService.ResponseMessage;

public class RecognitionControllerImpl implements RecognitionController,
		ServerConnectorCallback {
	private static final String ALTERNATES_BUNDLE = "alternates_bundle";
	private static final boolean DBG = false;
	private static final String EXTRA_ALTERNATES = "android.speech.extra.ALTERNATES";
	private static final String EXTRA_CAR_DOCK = "car_dock";
	private static final String EXTRA_SERVER_URL = "android.speech.extras.SERVER_URL";
	private static final int MSG_START_LISTENING = 1;
	private static final int SLEEP_BETWEEN_RETRIES_MILLIS = 1000;
	private static final String TAG = "RecognitionControllerImpl";
	private static final int START_LISTENING = 1;

	private static enum State {
		STARTING, RECOGNIZING, CANCELED, PAUSED, ERROR, RECOGNIZED
	}

	private boolean mAddFullRecognitionResult = false;
	private AudioBuffer mAudioBuffer = null;
	private final AudioManager mAudioManager;
	private final int mConnectionRetries;
	private final Context mContext;
	private final int mDefaultSpeechTimeoutMillis;
	private final EndpointerInputStreamListener mEndpointerListener = new EndpointerInputStreamListener();
	private int mError = ERROR_NONE;
	private final int mExtraTotalResultTimeoutMillis;
	private final Handler mHandler;
	private HandlerThread mHandlerThread;
	private boolean mIsFollowUpRecognition = false;
	private boolean mIsSpeechDetected = false;
	private final Lock mLock = new ReentrantLock();
	// private final MicrophoneManager mMicrophoneManager;
	private int mNetworkType;
	private float mNoiseLevel = -1.0F;
	private RecognitionParameters mParams = null;
	private ByteArrayOutputStream mRawAudio;
	private RecognitionListener mRecognitionListener;
	private ResponseMessage mResponse;
	private final ServerConnector mServerConnector;
	private float mSnr = -1.0F;
	private long mSpeechBeginTimeMillis;
	private int mSpeechTimeoutMillis;
	private State mState = State.STARTING;
	private final Condition mStateChanged = this.mLock.newCondition();
	private TimeoutTimer mWaitingForResultsTimer;
	private TimeoutTimer mSpeechRecordingTimer;
	private RecordAndSendManager mRecordAndSendManager;

	public RecognitionControllerImpl(Context context,
			ServerConnector paramServerConnector,
			MicrophoneManager paramMicrophoneManager) {
		mContext = context;
		mDefaultSpeechTimeoutMillis = 10 * 1000;
		mExtraTotalResultTimeoutMillis = 2000;
		this.mSpeechTimeoutMillis = mDefaultSpeechTimeoutMillis;
		this.mConnectionRetries = 2;
		this.mAudioManager = ((AudioManager) this.mContext
				.getSystemService("audio"));
		if (this.mAudioManager == null) {
			throw new RuntimeException("Audio manager not found");
		}
		this.mServerConnector = paramServerConnector;
		this.mServerConnector.setCallback(this);
		this.mSpeechRecordingTimer = new TimeoutTimer();
		this.mWaitingForResultsTimer = new TimeoutTimer();
		Utils.loadClasses();
		this.mHandlerThread = new HandlerThread("RecognitionControllerThread");
		this.mHandlerThread.start();
		this.mHandler = new Handler(this.mHandlerThread.getLooper()) {
			public void handleMessage(Message paramMessage) {
				switch (paramMessage.what) {
				case START_LISTENING:
					startRecognition((Intent) paramMessage.obj);
					break;
				}
			}
		};
	}

	private void changeState(State paramState) {
		this.mLock.lock();
		try {
			changeStateInternal(paramState);
			return;
		} finally {
			this.mLock.unlock();
		}
	}

	private boolean changeStateIfOneOf(State paramState,
			State[] paramArrayOfState) {
		this.mLock.lock();
		try {
			int i = paramArrayOfState.length;
			for (int j = 0; j < i; ++j) {
				State localState = paramArrayOfState[j];
				if (this.mState != localState)
					continue;
				changeStateInternal(paramState);
				return true;
			}
			return false;
		} finally {
			this.mLock.unlock();
		}
	}

	private void changeStateInternal(State paramState) {
		Log.i("RecognitionControllerImpl", "State change: " + this.mState
				+ " -> " + paramState);
		this.mState = paramState;
		this.mStateChanged.signalAll();
	}

	private boolean stateIs(State paramState) {
		return getState() == paramState;
	}

	public boolean isApiMode() {
		return this.mParams.isApiMode();
	}

	private void resetRequest() {
		this.mResponse = null;
		this.mIsSpeechDetected = false;
	}

	protected void startRecognition(Intent intent) {
		changeState(State.STARTING);
		mParams = ParmsUtil.makeTcpSessionParms();
		runRecognitionMainLoop();
	}

	private void runRecognitionMainLoop() {
		try {
			resetRequest();
			updateNetwork();
			mParams.setNetworkType(mNetworkType);
			mServerConnector.setUseTcp(true);
			if (!changeStateIfOneOf(State.RECOGNIZING,
					new State[] { State.STARTING })) {
				Log.i(TAG, "not in starting state!");
			}
			mServerConnector.createSession(mParams);
			if (!stateIs(State.RECOGNIZING)) {
				onError(ERROR_NETWORK);
				fireFailure(ERROR_NETWORK);
				return;
			}
			if (mRecognitionListener != null) {
				mRecognitionListener.onReadyForSpeech(null);
			}
			// InjectUtil.logTime("session created");
			mParams.setSnr(mSnr);
			mParams.setNoiseLevel(mNoiseLevel);
			mServerConnector.startRecognize();
			// InjectUtil.logTime("recognize started");
			startRecord();
			waitForFinalResult();
			Log.i(TAG, "Final state:" + mState);
			switch (getState()) {
			case RECOGNIZED:
				processResponse();
				mServerConnector.setRequestStatus(mState.ordinal());
				break;
			case CANCELED:
			case PAUSED:
				mServerConnector.setRequestStatus(mState.ordinal());
				mServerConnector.cancelRecognition();
				break;
			case ERROR:
				// TODO if retry times not exceed mRetryTimes, retry
				fireFailure(mError);
				break;
			case STARTING:
			case RECOGNIZING:
				Log.w(TAG, "unexpect state:" + mState);
				mServerConnector.setRequestStatus(14);
				break;
			default:
				Log.e(TAG, "an unexpected exception occurred");
				onError(ERROR_CLIENT);
				fireFailure(ERROR_CLIENT);
				break;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void startRecord() {
		mSpeechRecordingTimer.set(mDefaultSpeechTimeoutMillis);
		// TODO we should make it configurable
		mRecordAndSendManager = new RecordAndSendManager(mServerConnector, 0,
				750, -1L);
		mRecordAndSendManager.start();
	}

	private boolean updateNetwork() {
		NetworkInfo localNetworkInfo = ((ConnectivityManager) mContext
				.getSystemService("connectivity")).getActiveNetworkInfo();
		if ((localNetworkInfo == null) || (!localNetworkInfo.isConnected()))
			return false;
		this.mNetworkType = localNetworkInfo.getType();
		return true;
	}

	private void clearVariables() {
		this.mAudioManager.abandonAudioFocus(null);
		if (this.mAudioBuffer == null)
			return;
		this.mAudioBuffer.stop();
	}

	private void fireFailure(int errorCode) {
		State localState = getState();
		if ((localState == State.CANCELED) || (localState == State.PAUSED))
			return;
		if (this.mRecognitionListener != null) {
			this.mRecognitionListener.onError(errorCode);
		}
		switch (errorCode) {
		case ERROR_SPEECH_TIMEOUT:
			Log.e("RecognitionControllerImpl", "ERROR_SPEECH_TIMEOUT");
			this.mServerConnector.setEndpointTriggerType(1);
			this.mServerConnector
					.setRequestStatus(ClientPerceivedRequestStatus.REQUEST_CANCELED_VALUE);
			break;
		case ERROR_NETWORK_TIMEOUT:
			Log.e("RecognitionControllerImpl", "ERROR_NETWORK_TIMEOUT");
			this.mServerConnector
					.setRequestStatus(ClientPerceivedRequestStatus.REQUEST_TIMEOUT_VALUE);
			break;
		case ERROR_AUDIO:
			Log.e("RecognitionControllerImpl", "ERROR_AUDIO");
			this.mServerConnector
					.setRequestStatus(ClientPerceivedRequestStatus.CLIENT_SIDE_ERROR_VALUE);
			break;
		case ERROR_CLIENT:
			Log.e("RecognitionControllerImpl", "ERROR_CLIENT");
			this.mServerConnector
					.setRequestStatus(ClientPerceivedRequestStatus.CLIENT_SIDE_ERROR_VALUE);
			break;
		case ERROR_NETWORK:
			Log.e("RecognitionControllerImpl", "ERROR_NETWORK");
			this.mServerConnector
					.setRequestStatus(ClientPerceivedRequestStatus.NETWORK_CONNECTIVITY_ERROR_VALUE);
			break;
		case ERROR_NO_MATCH:
			Log.e("RecognitionControllerImpl", "ERROR_NO_MATCH");
			break;
		case ERROR_SERVER:
			Log.e("RecognitionControllerImpl", "ERROR_SERVER");
			break;
		default:
			Log.e("RecognitionControllerImpl", "Unknown error: " + errorCode);
			break;
		}
	}

	private State getState() {
		this.mLock.lock();
		try {
			State localState = this.mState;
			return localState;
		} finally {
			this.mLock.unlock();
		}
	}

	private int processResponse() {
		SpeechService.Status localStatus = this.mResponse.getStatus();
		if (localStatus == SpeechService.Status.CANCELED) {
			Log.i("RecognitionControllerImpl", "Request canceled");
			fireFailure(ERROR_SERVER);
			return 2;
		}
		if (localStatus == SpeechService.Status.PREPROCESSOR_ERROR) {
			Log.w("RecognitionControllerImpl",
					"Server reported preprocessor error");
			fireFailure(ERROR_SERVER);
			return 1;
		}
		SpeechService.RecognizeResponse localRecognizeResponse = (SpeechService.RecognizeResponse) this.mResponse
				.getExtension(SpeechService.RecognizeResponse.recognizeResponse);
		if ((localStatus != SpeechService.Status.OK)
				|| (!localRecognizeResponse.hasRecognitionResult())) {
			Log.w("RecognitionControllerImpl", "server reported error status:"
					+ localStatus);
			fireFailure(ERROR_SERVER);
			return 1;
		}
		SpeechService.RecognitionResult result = localRecognizeResponse
				.getRecognitionResult();
		SpeechService.RecognitionStatus localRecognitionStatus = result
				.getStatus();
		if (localRecognitionStatus == SpeechService.RecognitionStatus.NO_MATCH) {
			Log.w("RecognitionControllerImpl", "no match found");
			fireFailure(ERROR_NO_MATCH);
			return 1;
		}
		if (localRecognitionStatus != SpeechService.RecognitionStatus.SUCCESS) {
			Log.w("RecognitionControllerImpl",
					"server reported error SpeechServiceMessageTypes."
							+ localRecognitionStatus);
			fireFailure(ERROR_SERVER);
			return 1;
		}
		// InjectUtil.logResult(result);
		mServerConnector.sendClientReports();
		if (mRecognitionListener != null) {
			Bundle bundle = new Bundle();
			bundle.putByteArray("results", result.toByteArray());
			mRecognitionListener.onResults(bundle);
		}
		onStop();
		return 0;
	}

	private void waitForFinalResult() throws InterruptedException {
		this.mWaitingForResultsTimer.set(30 * 1000);
		// this.mLock.lock();
		while (true) {
			long remaining;
			if (this.mState != State.RECOGNIZING) {
				return;
			}
			remaining = this.mWaitingForResultsTimer.remaining();
			if (remaining <= 0L) {
				Log.w("RecognitionControllerImpl",
						"Recognition request timed out");
				onError(ERROR_NETWORK_TIMEOUT);
				return;
			}
		}
	}

	@Override
	public void enterIntoPauseMode() {
		State localState = State.PAUSED;
		State[] arrayOfState = new State[4];
		arrayOfState[0] = State.RECOGNIZING;
		arrayOfState[1] = State.RECOGNIZED;
		arrayOfState[2] = State.STARTING;
		arrayOfState[3] = State.ERROR;
		if (changeStateIfOneOf(localState, arrayOfState)) {
			// TODO only pause audio here
			mRecordAndSendManager.stop();
			return;
		}
		Log.w("RecognitionControllerImpl", "onPause() called from illegal "
				+ this.mState + " state");
	}

	@Override
	public void onCancel(RecognitionListener paramRecognitionListener) {
		mRecognitionListener = paramRecognitionListener;
		mHandler.removeMessages(1);
		changeState(State.CANCELED);
	}

	@Override
	public void onDestroy() {
		mHandlerThread.quit();
	}

	@Override
	public void onPause() {
		mHandler.removeMessages(START_LISTENING);
		clearVariables();
	}

	@Override
	public void onStartListening(Intent intent,
			RecognitionListener paramRecognitionListener) {
		this.mRecognitionListener = paramRecognitionListener;
		if (mHandler.hasMessages(START_LISTENING)) {
			return;
		}
		Message.obtain(mHandler, START_LISTENING, intent).sendToTarget();
	}

	@Override
	public void onStop() {
		changeState(State.CANCELED);
		this.mHandler.post(new Runnable() {
			public void run() {
				mServerConnector.close();
			}
		});
	}

	@Override
	public void onStopListening(RecognitionListener paramRecognitionListener) {
		mRecognitionListener = paramRecognitionListener;
		mHandler.removeMessages(START_LISTENING);
		// mMicrophoneManager.stopListening();
		mRecordAndSendManager.stop();
	}

	@Override
	public void onError(int errorCode) {
		State localState = State.ERROR;
		State[] arrayOfState = new State[2];
		arrayOfState[0] = State.RECOGNIZING;
		arrayOfState[1] = State.RECOGNIZED;
		if (changeStateIfOneOf(localState, arrayOfState)) {
			this.mError = errorCode;
			return;
		}
		Log.e("RecognitionControllerImpl", "Ignoring error " + errorCode);
	}

	@Override
	public void onIsAlive() {
		mWaitingForResultsTimer.set(13 * 1000);
	}

	@Override
	public void onPartialResponse(RecognizeResponse result) {
		if (result == null) {
			return;
		}
		PartialRecognitionResult partialResult = result.getPartialResult();
		if (mRecognitionListener != null) {
			Bundle bundle = new Bundle();
			bundle.putByteArray("partialResults", partialResult.toByteArray());
			mRecognitionListener.onPartialResults(bundle);
		}
	}

	@Override
	public void onResponse(ResponseMessage response) {
		mResponse = response;
		State localState = State.RECOGNIZED;
		State[] arrayOfState = new State[1];
		arrayOfState[0] = State.RECOGNIZING;
		if (changeStateIfOneOf(localState, arrayOfState))
			return;
		Log.w("RecognitionControllerImpl", "Final response received in state:"
				+ getState());
		return;

	}

	void logState(State state) {
		Log.d("state", state.name());
	}

	private class EndpointerInputStreamListener
			implements
			com.google.android.voicesearch.endpointer.EndpointerInputStream.Listener {
		private EndpointerInputStreamListener() {
		}

		public void onBeginningOfSpeech() {
			Log.i("EndpointerInputStreamListener", "onBeginningOfSpeech");
			// mSpeechRecordingTimer.extend(mExtraTotalResultTimeoutMillis);
			if (mRecognitionListener != null) {
				mRecognitionListener.onBeginningOfSpeech();
			}
			mIsSpeechDetected = true;
		}

		public void onBufferReceived(byte[] buffer) {
			if (stateIs(State.RECOGNIZING) && mRecognitionListener != null) {
				mRecognitionListener.onBufferReceived(buffer);
			}
		}

		public void onEndOfSpeech() {
			Log.i("EndpointerInputStreamListener", "onEndOfSpeech");
			if (!stateIs(State.RECOGNIZING)) {
				return;
			}
			mAudioManager.abandonAudioFocus(null);
			mServerConnector.setEndOfSpeech();
			mRecordAndSendManager.stop();
			if (mRecognitionListener != null) {
				mRecognitionListener.onEndOfSpeech();
			}
		}

		public void onReadyForSpeech(float noiseLevel, float noiseRatio) {
			// Log.i("RecognitionControllerImpl",
			// "onReadyForSpeech, noise level:"
			// + noiseLevel + ", snr:" + noiseRatio);
			Bundle bundle = new Bundle();
			bundle.putFloat("NoiseLevel", noiseLevel);
			bundle.putFloat("SignalNoiseRatio", noiseRatio);
			if (mRecognitionListener != null) {
				mRecognitionListener.onReadyForSpeech(bundle);
			}
		}

		public void onRmsChanged(float rms) {
			if (stateIs(State.RECOGNIZING) && mRecognitionListener != null) {
				mRecognitionListener.onRmsChanged(rms);
			}
		}
	}

	class RecordAndSendManager implements AudioListener {

		private AudioBuffer mAudioBuffer;
		private ServerConnector mServerConnector;
		private InputStream mis = null;
		private EndpointerInputStream endpointInputStream = null;
		private InputStream amrIs = null;
		private String TAG = "RecordAndSendManager";
		private long mSpeechInputMinimumLengthMillis;
		private long mSpeechInputCompleteSilenceLengthMillis;
		private long mSpeechInputPossiblyCompleteSilenceLengthMillis;
		private static final int SAMPLE_RATE_8000 = 8000;
		private static final int BUFFER_SIZE = 10 * 1024;
		private boolean speechEnd = false;

		public RecordAndSendManager(ServerConnector serverConnector,
				long speechInputMinimumLengthMillis,
				long speechInputCompleteSilenceLengthMillis,
				long speechInputPossiblyCompleteSilenceLengthMillis) {
			mSpeechInputMinimumLengthMillis = speechInputMinimumLengthMillis;
			mSpeechInputCompleteSilenceLengthMillis = speechInputCompleteSilenceLengthMillis;
			mSpeechInputPossiblyCompleteSilenceLengthMillis = speechInputPossiblyCompleteSilenceLengthMillis;
			mServerConnector = serverConnector;
			try {
				mis = new MicrophoneInputStream(SAMPLE_RATE_8000, BUFFER_SIZE);
			} catch (IOException e) {
				e.printStackTrace();
			}
			endpointInputStream = new EndpointerInputStream(mis,
					EndpointerInputStream.DENOISE_MODE_NEVER,
					mSpeechInputMinimumLengthMillis,
					mSpeechInputCompleteSilenceLengthMillis,
					mSpeechInputPossiblyCompleteSilenceLengthMillis);
			endpointInputStream
					.setListener(new EndpointerInputStreamListener());
			amrIs = Utils.createAmrInputStream(endpointInputStream);
		}

		public void start() {
			mAudioBuffer = new AudioBuffer(
					Utils.getAudioPacketSize(Encoding.AMR_NB_VALUE), amrIs,
					false);
			mAudioBuffer.setAudioListener(this);
		}

		public void stop() {
			mAudioBuffer.stop();
		}

		@Override
		public void onAudioData(ByteBuffer buffer) {
			if ((stateIs(State.RECOGNIZING)) && (!speechEnd)
					&& mSpeechRecordingTimer.remaining() > 0) {
				if (buffer.remaining() == 0) {
					speechEnd = true;
					Log.d(TAG, "speechEnd->true");
				}
				mServerConnector.postAudioChunk(buffer, speechEnd);
				return;
			}
			if (!speechEnd) {
				Log.d(TAG, "speechEnd == false");
				mServerConnector.postAudioChunk(buffer, true);
				endpointInputStream.stopListening();
			}
		}
	}

}
