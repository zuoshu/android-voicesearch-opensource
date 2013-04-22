package com.google.android.voicesearch.speechservice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;

import com.oneguy.googlevoicehack.AudioListener;

public class AudioBuffer {
	private static final boolean DBG = false;
	private static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer
			.wrap(new byte[0]);
	private static final String TAG = "AudioBuffer";
	private final Lock lock = new ReentrantLock();
	private Exception mAudioException = null;
	private final int mAudioPacketSize;
	private final ArrayList<ByteBuffer> mBuffer = new ArrayList();
	private final Thread mCaptureThread;
	private final InputStream mIn;
	private final boolean mStopAfterEndpointing;
	// private volatile boolean mStopRecording = false;
	private volatile boolean mStopped = false;
	private final Condition notEmpty = lock.newCondition();
	private int readPosition = 0;
	private AudioListener mAudioListener;

	public AudioBuffer(int packageSize, InputStream paramInputStream,
			boolean stopAfterEndpoint) {
		mIn = paramInputStream;
		mAudioPacketSize = packageSize;
		mStopAfterEndpointing = stopAfterEndpoint;
		mCaptureThread = new Thread() {
			public void run() {
				captureLoop();
			}
		};
		mCaptureThread.start();
	}

	public void setAudioListener(AudioListener listener) {
		mAudioListener = listener;
	}

	private void addPacket(ByteBuffer paramByteBuffer) {
		// InjectUtil.logPacket(paramByteBuffer);
		lock.lock();
		try {
			mBuffer.add(paramByteBuffer);
			if (mAudioListener != null) {
				mAudioListener.onAudioData(paramByteBuffer);
			}
			notEmpty.signal();
			return;
		} finally {
			lock.unlock();
		}
	}

	private void captureLoop() {
		Log.d(TAG, "captureLoop start:" + Thread.currentThread().toString());
		byte[] arrayOfByte = null;
		int totalRead = 0;
		int read = 0;
		try {
			while ((!Thread.interrupted()) && (!mStopped)) {
				arrayOfByte = new byte[mAudioPacketSize];
				totalRead = 0;
				read = 0;
				while (totalRead != mAudioPacketSize) {
					read = mIn.read(arrayOfByte, totalRead, mAudioPacketSize
							- totalRead);
					if (read == -1 || mStopped) {
						mStopped = true;
						break;
					}
					totalRead += read;
				}
				addPacket(ByteBuffer.wrap(arrayOfByte, 0, totalRead));
			}
			addPacket(EMPTY_BYTE_BUFFER);
		} catch (IOException e) {
			Log.e("AudioBuffer", "error read audio inputstream", e);
		} finally {
			mStopped = true;
			try {
				mIn.close();
			} catch (IOException e) {
				Log.e("AudioBuffer", "Closing input stream failed", e);
			}
		}
	}

	private void setAudioException(Exception paramException) {
		lock.lock();
		try {
			mAudioException = paramException;
			notEmpty.signal();
			return;
		} finally {
			lock.unlock();
		}
	}

	public byte[] getAudio() {
		lock.lock();
		ByteArrayOutputStream outputStream;
		ByteBuffer byteBuffer;
		try {
			int size = mBuffer.size();
			if (size < 2) {
				return null;
			}
			outputStream = new ByteArrayOutputStream();
			Iterator<ByteBuffer> iterator = mBuffer.iterator();
			while (iterator.hasNext()) {
				byteBuffer = (ByteBuffer) iterator.next();
				outputStream.write(byteBuffer.array(), 0, byteBuffer.limit());
			}
			return outputStream.toByteArray();
		} finally {
			lock.unlock();
		}
	}

	public ByteBuffer getByteBuffer() throws AudioBuffer.AudioException,
			InterruptedException {
		Log.d(TAG, "getByteBuffer readPosition:" + readPosition
				+ " mBuffer.size:" + mBuffer.size() + " mAudioException:"
				+ mAudioException);
		lock.lock();
		try {
			if ((readPosition > mBuffer.size()) || (mAudioException != null)) {
				throw new AudioException("Audio capture threw exception",
						mAudioException);
			}
			ArrayList<ByteBuffer> localArrayList = mBuffer;
			ByteBuffer buffer = (ByteBuffer) localArrayList.get(readPosition++);
			return buffer;
		} finally {
			lock.unlock();
		}
	}

	public boolean isStopped() {
		return mStopped;
	}

	public void restart() {
		lock.lock();
		try {
			mBuffer.clear();
			readPosition = 0;
			return;
		} finally {
			lock.unlock();
		}
	}

	public void restartBuffersToBackup() {
		lock.lock();
		try {
			readPosition = 0;
			return;
		} finally {
			lock.unlock();
		}
	}

	public void stop() {
		mCaptureThread.interrupt();
		mStopped = true;
	}

	public static class AudioException extends Exception {
		public AudioException() {
		}

		public AudioException(String paramString) {
			super(paramString);
		}

		public AudioException(String paramString, Throwable paramThrowable) {
			super(paramString, paramThrowable);
		}

		public AudioException(Throwable paramThrowable) {
			super(paramThrowable);
		}
	}
}