package com.google.android.voicesearch.endpointer;

import java.io.IOException;
import java.io.InputStream;

import android.media.AudioRecord;

public final class MicrophoneInputStream extends InputStream {
	private static final boolean DBG = false;
	private static final String TAG = "MicrophoneInputStream";
	private AudioRecord mAudioRecord;
	private boolean mStarted = false;

	public MicrophoneInputStream(int sampleRateInHz, int bufferSizeInBytes)
			throws IOException {
		this.mAudioRecord = new AudioRecord(6, sampleRateInHz, 16, 2, Math.max(
				AudioRecord.getMinBufferSize(sampleRateInHz, 16, 2),
				bufferSizeInBytes));
		if (this.mAudioRecord.getState() == 1)
			return;
		this.mAudioRecord.release();
		this.mAudioRecord = null;
		throw new IOException("not open");
	}

	private void ensureStarted() throws IOException {
		if (this.mStarted)
			return;
		if (this.mAudioRecord == null)
			throw new IOException("not open");
		this.mAudioRecord.startRecording();
		if (this.mAudioRecord.getRecordingState() != 3)
			throw new IOException("couldn't start recording");
		this.mStarted = true;
	}

	public void close() {
		if (this.mAudioRecord != null)
			;
		try {
			this.mAudioRecord.stop();
			try {
				this.mAudioRecord.release();
				return;
			} finally {
				this.mAudioRecord = null;
			}
		} finally {
		}
	}

	protected void finalize() throws Throwable {
		if (this.mAudioRecord == null)
			return;
		throw new IllegalStateException("MicrophoneInputStream leaked");
	}

	public int read() {
		throw new UnsupportedOperationException(
				"Single-byte read not supported");
	}

	public int read(byte[] paramArrayOfByte) throws IOException {
		return read(paramArrayOfByte, 0, paramArrayOfByte.length);
	}

	public int read(byte[] paramArrayOfByte, int offset, int size)
			throws IOException {
		ensureStarted();
		int i = this.mAudioRecord.read(paramArrayOfByte, offset, size);
		if (i == -3)
			throw new IOException("not open");
		if (i == -2)
			throw new IOException("Bad offset/length arguments for buffer");
		return i;
	}
}