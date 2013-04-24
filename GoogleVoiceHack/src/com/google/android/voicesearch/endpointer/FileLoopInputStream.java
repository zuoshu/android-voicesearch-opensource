package com.google.android.voicesearch.endpointer;

import android.util.Log;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class FileLoopInputStream extends InputStream {
	private static final boolean DBG = false;
	private static final String TAG = "FileLoopInputStream";
	private String filename;
	private FileInputStream fis = null;
	private int sampleRate;

	public FileLoopInputStream(String filename, int sampleRate)
			throws IOException {
		this.filename = filename;
		this.sampleRate = sampleRate;
		this.fis = new FileInputStream(filename);
	}

	public void close() {
		if (fis != null) {
			try {
				fis.close();
			} catch (IOException e) {
			}
		}
	}

	public int read() {
		throw new UnsupportedOperationException(
				"Single-byte read not supported");
	}

	public int read(byte[] paramArrayOfByte) throws IOException {
		return read(paramArrayOfByte, 0, paramArrayOfByte.length);
	}

	public int read(byte[] buffer, int offset, int length) throws IOException {
		Log.d(TAG, "file read offset:" + offset + " length:" + length);
		try {
			int readSize = this.fis.read(buffer, offset, length);
			int readInRealTime = 1000000 / this.sampleRate * (readSize / 2);
			long timeoutRealTime = System.currentTimeMillis() + readInRealTime
					/ 1000;
			while (System.currentTimeMillis() < timeoutRealTime) {
				if (readSize == -1) {
					close();
					this.fis = new FileInputStream(this.filename);
					int read = this.fis.read(buffer, offset, length);
					readSize = read;
				}
			}
			return readSize;
		} catch (IOException localIOException) {
			Log.w("FileLoopInputStream", "I/O problem whilst streaming file");
			throw localIOException;
		}
	}
}