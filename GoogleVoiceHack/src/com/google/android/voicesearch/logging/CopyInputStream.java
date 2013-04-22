package com.google.android.voicesearch.logging;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CopyInputStream extends FilterInputStream {
	private static final String TAG = CopyInputStream.class.getSimpleName();
	private OutputStream mOut;

	public CopyInputStream(InputStream paramInputStream,
			OutputStream paramOutputStream) {
		super(paramInputStream);
		this.mOut = paramOutputStream;
	}

	public void close() throws IOException {
		super.close();
		this.mOut.close();
	}

	public int read() throws IOException {
		int i = this.in.read();
		if (i == -1) {
			this.mOut.close();
			return i;
		}
		this.mOut.write(i);
		return i;
	}

	public int read(byte[] paramArrayOfByte) throws IOException {
		return read(paramArrayOfByte, 0, paramArrayOfByte.length);
	}

	public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
			throws IOException {
		int i = this.in.read(paramArrayOfByte, paramInt1, paramInt2);
		if (i == -1) {
			this.mOut.close();
			return i;
		}
		this.mOut.write(paramArrayOfByte, paramInt1, i);
		return i;
	}
}