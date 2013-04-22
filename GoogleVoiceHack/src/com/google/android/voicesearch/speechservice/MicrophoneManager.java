package com.google.android.voicesearch.speechservice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.google.android.voicesearch.endpointer.EndpointerInputStream;

public interface MicrophoneManager {
	public void close();

	public int getEncoding();

	public int getSamplingRate();

	public void pauseStream();

	public void restartStream();

	public void setSpeechInputCompleteSilenceLengthMillis(long paramLong);

	public void setSpeechInputMinimumLengthMillis(long paramLong);

	public void setSpeechInputPossiblyCompleteSilenceLengthMillis(long paramLong);

	public AudioBuffer setupMicrophone(
			EndpointerInputStream.Listener paramListener, int paramInt,
			boolean paramBoolean,
			ByteArrayOutputStream paramByteArrayOutputStream)
			throws IOException;

	public void stopListening();
}