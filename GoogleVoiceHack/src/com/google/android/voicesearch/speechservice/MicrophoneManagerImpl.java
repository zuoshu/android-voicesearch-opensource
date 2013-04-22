package com.google.android.voicesearch.speechservice;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.TestPlatformLog;
import com.google.android.voicesearch.endpointer.EndpointerInputStream;
import com.google.android.voicesearch.endpointer.MicrophoneInputStream;
import com.google.android.voicesearch.logging.CopyInputStream;
import com.google.protos.speech.service.SpeechService.Encoding;

public class MicrophoneManagerImpl implements MicrophoneManager {
	private static final boolean DBG = false;
	private static final String TAG = "MicrophoneManagerImpl";
	private boolean mAudioLoggingEnabled = false;
	private Context mContext;
	protected int mEncoding;
	protected int mEncodingThreeG;
	protected int mEncodingWifi;
	protected EndpointerInputStream mEndpointer = null;
	protected int mSamplingRate = 8000;
	protected long mSpeechInputCompleteSilenceLengthMillis = -1L;
	protected long mSpeechInputMinimumLengthMillis = -1L;
	protected long mSpeechInputPossiblyCompleteSilenceLengthMillis = -1L;

	public MicrophoneManagerImpl(Context paramContext) {
		this.mContext = paramContext;
		this.mContext.getContentResolver();
		this.mEncodingThreeG = getEncodingOf(Encoding.AMR_NB.name());
		this.mEncodingWifi = getEncodingOf(Encoding.AMR_NB.name());
		this.mAudioLoggingEnabled = PreferenceManager
				.getDefaultSharedPreferences(paramContext).getBoolean(
						"audioLoggingEnabled", false);
	}

	private static int getEncodingOf(String encoding) {
		Encoding enc = Encoding.valueOf(encoding);
		if(enc == null){
			return Encoding.AMR_NB_VALUE;
		}
		return enc.getNumber();
	}

	protected InputStream captureStream(InputStream paramInputStream,
			ByteArrayOutputStream paramByteArrayOutputStream) {
		if (paramByteArrayOutputStream == null)
			return paramInputStream;
		return new CopyInputStream(paramInputStream, paramByteArrayOutputStream);
	}

	public void close() {
		if (this.mEndpointer == null)
			return;
		this.mEndpointer.requestClose();
	}

	public int getEncoding() {
		return this.mEncoding;
	}

	protected AudioBuffer getMicInputStream(
			EndpointerInputStream.Listener paramListener, int paramInt,
			boolean paramBoolean,
			ByteArrayOutputStream paramByteArrayOutputStream)
			throws IOException {
		Object localObject = new MicrophoneInputStream(this.mSamplingRate,
				2 * this.mSamplingRate);
		this.mEncoding = Encoding.AMR_NB_VALUE;
		if (paramInt == 1)
			;
		for (this.mEncoding = this.mEncodingWifi;; this.mEncoding = this.mEncodingThreeG)
			do {
				this.mEndpointer = new EndpointerInputStream(
						new BufferedInputStream(captureStream(
								logStream((InputStream) localObject, "mic"),
								paramByteArrayOutputStream), 1600), 2,
						this.mSpeechInputMinimumLengthMillis,
						this.mSpeechInputCompleteSilenceLengthMillis,
						this.mSpeechInputPossiblyCompleteSilenceLengthMillis);
				this.mEndpointer.setListener(paramListener);
				InputStream localInputStream = Utils.getEncodingInputStream(
						mEndpointer, mEncoding);
				return new AudioBuffer(Utils.getAudioPacketSize(mEncoding),
						localInputStream, paramBoolean);
			} while (paramInt != 3);
	}

	public int getSamplingRate() {
		return this.mSamplingRate;
	}

	protected InputStream logStream(InputStream paramInputStream,
			String paramString) {
		if (!this.mAudioLoggingEnabled)
			return paramInputStream;
		String str = paramString + "-" + System.currentTimeMillis() + ".pcm";
		TestPlatformLog.logAudioPath(new File(mContext.getFilesDir(), str)
				.toString());
		try {
			CopyInputStream localCopyInputStream = new CopyInputStream(
					paramInputStream, this.mContext.openFileOutput(str, 0));
			return localCopyInputStream;
		} catch (FileNotFoundException localFileNotFoundException) {
			Log.e("MicrophoneManagerImpl", "Error opening audio log file.",
					localFileNotFoundException);
		}
		return paramInputStream;
	}

	public void pauseStream() {
		this.mEndpointer.pauseStream();
	}

	public void restartStream() {
		this.mEndpointer.restartStream();
	}

	public void setSpeechInputCompleteSilenceLengthMillis(long paramLong) {
		this.mSpeechInputCompleteSilenceLengthMillis = paramLong;
	}

	public void setSpeechInputMinimumLengthMillis(long paramLong) {
		this.mSpeechInputMinimumLengthMillis = paramLong;
	}

	public void setSpeechInputPossiblyCompleteSilenceLengthMillis(long paramLong) {
		this.mSpeechInputPossiblyCompleteSilenceLengthMillis = paramLong;
	}

	public AudioBuffer setupMicrophone(
			EndpointerInputStream.Listener paramListener, int paramInt,
			boolean paramBoolean,
			ByteArrayOutputStream paramByteArrayOutputStream)
			throws IOException {
		return getMicInputStream(paramListener, paramInt, paramBoolean,
				paramByteArrayOutputStream);
	}

	public void stopListening() {
		this.mEndpointer.stopListening();
	}
}