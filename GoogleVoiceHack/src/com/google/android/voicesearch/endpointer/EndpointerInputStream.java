package com.google.android.voicesearch.endpointer;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

public class EndpointerInputStream extends InputStream implements
		AsyncClosableInputStream {
	private static final boolean DEBUG = false;
	public static final int DENOISE_MODE_ALWAYS = 1;
	public static final int DENOISE_MODE_JUDGING = 3;
	public static final int DENOISE_MODE_JUDGING_FOR_SERVER = 4;
	public static final int DENOISE_MODE_NEVER = 2;
	private static final int FRAME_LENGTH_MS = 20;
	private static final int SAMPLES_PER_FRAME = 160;
	private static final int SAMPLE_RATE_HZ = 8000;
	private static final int STATE_ENVIRONMENT_ESTIMATION = -2;
	private static final int STATE_NOISE_JUDGED_ABSENT = -4;
	private static final int STATE_NOISE_JUDGED_PRESENT = -5;
	private static final int STATE_NOISE_JUDGING = -6;
	private static final int STATE_NOISE_JUDGING_COMPLETE = -3;
	private static final int STATE_NOISE_JUDGING_FOR_SERVER = -7;
	private static final int STATE_POSSIBLE_OFFSET = 13;
	private static final int STATE_POSSIBLE_ONSET = 11;
	private static final int STATE_POST_SPEECH = 14;
	private static final int STATE_PRE_SPEECH = 10;
	private static final int STATE_SIGNAL_NOT_AVAILABLE = 15;
	private static final int STATE_SPEECH_COMPLETE = -1;
	private static final int STATE_SPEECH_PRESENT = 12;
	private static final String TAG = "EndpointerInputStream";
	private byte[] mBuf = new byte[320];
	private int mBufIn = 0;
	private int mBufOut = 0;
	private volatile boolean mCloseRequested = false;
	private volatile boolean mDiscardRequested = false;
	private volatile boolean mEndListening = false;
	private int mEnvironmentEstimationFramesRemaining;
	private InputStream mInputStream;
	private boolean mIsDiscarding = false;
	private boolean mIsSpeechDetected = false;
	private Listener mListener;
	private int mNativeHandle;
	private byte[] mNoiseJudgingBuffer;
	private int mNoiseJudgingBufferConsumed = 0;
	private byte[] mNoiseJudgingBufferDenoised;
	private int mProcessorNativeInstance;
	private volatile boolean mRestartRequested = false;
	private float[] mRms = new float[1];
	private int mState = 0;
	private boolean mUseDenoiser;

	static {
		try {
			System.loadLibrary("voicesearch");
		} catch (UnsatisfiedLinkError localUnsatisfiedLinkError) {
			System.loadLibrary("speech");
		}
	}

	public EndpointerInputStream(InputStream inputStream, int paramInt,
			long speechInputMinimumLengthMillis,
			long speechInputCompleteSilenceLengthMillis,
			long speechInputPossiblyCompleteSilenceLengthMillis) {
		this.mInputStream = inputStream;
		this.mNativeHandle = endpointerNew(speechInputMinimumLengthMillis,
				speechInputCompleteSilenceLengthMillis,
				speechInputPossiblyCompleteSilenceLengthMillis);
		switch (paramInt) {
		case DENOISE_MODE_NEVER:
			this.mUseDenoiser = false;
			this.mState = STATE_ENVIRONMENT_ESTIMATION;
			this.mEnvironmentEstimationFramesRemaining = 10;
			break;
		case DENOISE_MODE_ALWAYS:
			this.mUseDenoiser = true;
			this.mState = STATE_ENVIRONMENT_ESTIMATION;
			this.mEnvironmentEstimationFramesRemaining = 50;
			break;
		case DENOISE_MODE_JUDGING:
			this.mNoiseJudgingBuffer = new byte[3200];
			this.mNoiseJudgingBufferDenoised = new byte[3200];
			this.mState = STATE_NOISE_JUDGING;
			this.mEnvironmentEstimationFramesRemaining = 40;
			break;
		case DENOISE_MODE_JUDGING_FOR_SERVER:
			this.mNoiseJudgingBuffer = new byte[3200];
			this.mState = STATE_NOISE_JUDGING_FOR_SERVER;
			this.mEnvironmentEstimationFramesRemaining = 0;
			break;
		}
	}

	private native void endpointerDelete(int paramInt);

	private native int endpointerNew(long speechInputMinimumLengthMillis,
			long speechInputCompleteSilenceLengthMillis,
			long speechInputPossiblyCompleteSilenceLengthMillis);

	private native float getNoiseLevel(int handle);

	private int judgeNoise(byte[] data, int offset, int length)
			throws IOException {
		int bufferLength = this.mNoiseJudgingBuffer.length;
		float[] snrArray;
		float noiseLevel;
		int j;
		if ((this.mState == STATE_NOISE_JUDGING)
				|| (this.mState == STATE_NOISE_JUDGING_FOR_SERVER)) {
			new DataInputStream(this.mInputStream)
					.readFully(this.mNoiseJudgingBuffer);
			snrArray = new float[] { 0.0F };
			processAudio(this.mNativeHandle, this.mNoiseJudgingBuffer,
					snrArray, true, false);
			noiseLevel = getNoiseLevel(this.mNativeHandle);
			if (noiseLevel <= 150.0F) {
				j = 0;
			} else {
				j = 1;
			}
			if (j == 0) {
				this.mUseDenoiser = false;
				this.mState = STATE_NOISE_JUDGED_ABSENT;
				promptForSpeech(noiseLevel, snrArray[0]);
			} else {
				if (this.mState != STATE_NOISE_JUDGING) {
					this.mState = STATE_NOISE_JUDGED_ABSENT;
				} else {
					System.arraycopy(this.mNoiseJudgingBuffer, 0,
							this.mNoiseJudgingBufferDenoised, 0, bufferLength);
					processAudio(this.mNativeHandle,
							this.mNoiseJudgingBufferDenoised, null, false, true);
					this.mUseDenoiser = true;
					this.mState = STATE_NOISE_JUDGED_PRESENT;
				}
			}
		}
		byte[] bufferDenoised = null;
		int bufferConsumed = 0;
		if (this.mState == STATE_NOISE_JUDGED_ABSENT) {
			bufferDenoised = this.mNoiseJudgingBuffer;
		}
		if (this.mState == STATE_NOISE_JUDGED_PRESENT) {
			bufferDenoised = this.mNoiseJudgingBufferDenoised;
			bufferConsumed = this.mNoiseJudgingBufferConsumed;
			if (length <= bufferLength - bufferConsumed)
				throw new IllegalStateException("Bad state: " + this.mState);
		}
		int read = bufferLength - bufferConsumed;
		System.arraycopy(bufferDenoised, bufferConsumed, data, offset, read);
		this.mNoiseJudgingBufferConsumed = (read + this.mNoiseJudgingBufferConsumed);
		if (this.mNoiseJudgingBufferConsumed == bufferLength) {
			this.mState = STATE_NOISE_JUDGING_COMPLETE;
		}
		return read;
	}

	private native int processAudio(int nativeHandle,
			byte[] noiseJudgingBuffer, float[] rms, boolean paramBoolean1,
			boolean useDenoiser);

	private void promptForSpeech(float noiseLevel, float snr) {
		startUserInput(this.mNativeHandle);
		if (this.mListener != null) {
			this.mListener.onReadyForSpeech(noiseLevel, snr);
		}
	}

	private native void restart(int paramInt);

	private void restartStreamInternal() {
		restart(this.mNativeHandle);
		this.mIsSpeechDetected = false;
		this.mState = 0;
		this.mBufOut = 0;
		this.mBufIn = 0;
		this.mEnvironmentEstimationFramesRemaining = 1;
		this.mIsDiscarding = false;
		this.mDiscardRequested = false;
		this.mRestartRequested = false;
		this.mEndListening = false;
	}

	private native void startUserInput(int paramInt);

	private String stateString(int paramInt) {
		switch (paramInt) {
		case STATE_PRE_SPEECH:
			return "STATE_PRE_SPEECH";
		case STATE_POSSIBLE_ONSET:
			return "STATE_POSSIBLE_ONSET";
		case STATE_SPEECH_PRESENT:
			return "STATE_SPEECH_PRESENT";
		case STATE_POSSIBLE_OFFSET:
			return "STATE_POSSIBLE_OFFSET";
		case STATE_POST_SPEECH:
			return "STATE_POST_SPEECH";
		case STATE_SIGNAL_NOT_AVAILABLE:
			return "STATE_SIGNAL_NOT_AVAILABLE";
		default:
			return "UNKNOWN";
		}
	}

	public void close() {
		try {
			this.mInputStream.close();
			if (this.mNativeHandle != 0) {
				endpointerDelete(this.mNativeHandle);
				this.mNativeHandle = 0;
			}
			return;
		} catch (IOException localIOException) {
			Log.e("EndpointerInputStream", "close() failed", localIOException);
		}
	}

	protected void finalize() {
		if (this.mNativeHandle == 0)
			return;
		throw new IllegalStateException(
				"someone forgot to close EndpointerInputStream");
	}

	public boolean isSpeechDetected() {
		return this.mIsSpeechDetected;
	}

	public void pauseStream() {
		this.mDiscardRequested = true;
	}

	public int read() {
		throw new UnsupportedOperationException(
				"Single-byte read not supported");
	}

	public int read(byte[] paramArrayOfByte) throws IOException {
		return read(paramArrayOfByte, 0, paramArrayOfByte.length);
	}

	public int read(byte[] data, int offset, int length) throws IOException {
		if ((this.mNativeHandle == 0) || (this.mInputStream == null)) {
			Log.w("EndpointerInputStream",
					"Reading from a closed EndpointerInputStream");
			throw new EOFException(
					"The EndpointerInputStream is already closed.");
		}
		if (this.mCloseRequested) {
			close();
			return -1;
		}
		if (this.mDiscardRequested) {
			this.mDiscardRequested = false;
			this.mIsDiscarding = true;
			return -1;
		}
		if (this.mRestartRequested) {
			restartStreamInternal();
		}
		if ((this.mState == STATE_NOISE_JUDGING)
				|| (this.mState == STATE_NOISE_JUDGING_FOR_SERVER)
				|| (this.mState == STATE_NOISE_JUDGED_PRESENT)
				|| (this.mState == STATE_NOISE_JUDGED_ABSENT)) {
			return judgeNoise(data, offset, length);
		}
		if (this.mBufOut >= this.mBufIn) {
			int bufLength = this.mBuf.length;
			this.mBufOut = 0;
			this.mBufIn = bufLength;
			int totalRead = 0;
			while (totalRead < bufLength) {
				int read = this.mInputStream.read(this.mBuf, totalRead,
						bufLength - totalRead);
				if (this.mIsDiscarding) {
					this.mBufOut = this.mBufIn;
					return 0;
				}
				if ((read == -1) || (this.mEndListening)) {
					if (this.mListener != null)
						this.mListener.onEndOfSpeech();
					this.mIsDiscarding = true;
					return -1;
				}
				totalRead += read;
			}
		}
		if (this.mEnvironmentEstimationFramesRemaining == 0) {
			int state = processAudio(this.mNativeHandle, this.mBuf, this.mRms,
					true, this.mUseDenoiser);
//			Log.d(TAG, "mEnvironmentEstimationFramesRemaining state:" + state
//					+ " mState:" + mState + " mIsSpeechDetected:"
//					+ mIsSpeechDetected);
			if (state != this.mState) {
				if (state == STATE_SPEECH_COMPLETE) {
					if (this.mListener != null)
						this.mListener.onEndOfSpeech();
					this.mIsDiscarding = true;
					return -1;
				}
				if ((!this.mIsSpeechDetected)
						&& (state == STATE_SPEECH_PRESENT)
						&& (this.mListener != null)) {
					this.mIsSpeechDetected = true;
					this.mListener.onBeginningOfSpeech();
				}
				this.mState = state;
			}
			this.mListener.onBufferReceived(this.mBuf);
			this.mListener.onRmsChanged(this.mRms[0]);
			if (this.mIsDiscarding) {
				this.mBufOut = this.mBufIn;
				return 0;
			}
		} else {
			float[] snrArray = { 0.0F };
			processAudio(this.mNativeHandle, this.mBuf, snrArray, true,
					this.mUseDenoiser);
			mEnvironmentEstimationFramesRemaining--;
			promptForSpeech(getNoiseLevel(this.mNativeHandle), snrArray[0]);
			this.mBufOut = this.mBufIn;
			return 0;
		}
		int outLength = this.mBufIn - this.mBufOut;
		System.arraycopy(this.mBuf, this.mBufOut, data, offset, outLength);
		this.mBufOut = (outLength + this.mBufOut);
		return outLength;
	}

	public void requestClose() {
		this.mCloseRequested = true;
	}

	public void restartStream() {
		this.mRestartRequested = true;
	}

	public void setListener(Listener paramListener) {
		this.mListener = paramListener;
	}

	public void stopListening() {
		this.mEndListening = true;
	}

	public static interface Listener {
		public void onBeginningOfSpeech();

		public void onBufferReceived(byte[] paramArrayOfByte);

		public void onEndOfSpeech();

		public void onReadyForSpeech(float noiseLevel, float snr);

		public void onRmsChanged(float paramFloat);
	}
}