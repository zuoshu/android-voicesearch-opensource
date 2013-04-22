package com.google.android.voicesearch.endpointer;

import java.io.IOException;
import java.io.InputStream;

public final class ResampleInputStream extends InputStream {
	private static final int mFirLength = 47;
	private byte[] mBuf;
	private int mBufCount;
	private InputStream mInputStream;
	private final int mRateIn;
	private final int mRateOut;

	public ResampleInputStream(InputStream paramInputStream, int paramInt1,
			int paramInt2) {
		if (paramInt1 != paramInt2 * 2)
			throw new IllegalArgumentException("only support 2:1 at the moment");
		this.mInputStream = paramInputStream;
		this.mRateIn = 2;
		this.mRateOut = 1;
	}

	private static native void fir21(byte[] paramArrayOfByte1, int paramInt1,
			byte[] paramArrayOfByte2, int paramInt2, int paramInt3);

	public void close() throws IOException {
		try {
			if (this.mInputStream != null)
				this.mInputStream.close();
			return;
		} finally {
			this.mInputStream = null;
		}
	}

	protected void finalize() throws Throwable {
		if (this.mInputStream == null)
			return;
		throw new IllegalStateException(
				"someone forgot to close ResampleInputStream");
	}

	public int read() {
		throw new UnsupportedOperationException(
				"Single-byte read not supported");
	}

	public int read(byte[] paramArrayOfByte) throws IOException {
		return read(paramArrayOfByte, 0, paramArrayOfByte.length);
	}

	public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
			throws IOException {
		if (this.mInputStream == null)
			throw new IllegalStateException("not open");
		int i = 2 * (47 + paramInt2 / 2 * this.mRateIn / this.mRateOut);
		if (this.mBuf == null)
			this.mBuf = new byte[i];
		while (true) {
			// label52:
			int j = 2 * ((this.mBufCount / 2 - 47) * this.mRateOut / this.mRateIn);
			if (j > 0) {
				if (j < paramInt2) {

				}
				fir21(this.mBuf, 0, paramArrayOfByte, paramInt1, j / 2);
				int l = j * this.mRateIn / this.mRateOut;
				this.mBufCount -= l;
				if (this.mBufCount > 0) {

					System.arraycopy(this.mBuf, l, this.mBuf, 0, this.mBufCount);
					return j;
				}
				if (i > this.mBuf.length) {

					byte[] arrayOfByte = new byte[i];
					System.arraycopy(this.mBuf, 0, arrayOfByte, 0,
							this.mBufCount);
					this.mBuf = arrayOfByte;
				} else {
					j = 2 * (paramInt2 / 2);
				}
				// break label52:
			}
			int k = this.mInputStream.read(this.mBuf, this.mBufCount,
					this.mBuf.length - this.mBufCount);
			if (k == -1)
				return -1;
			this.mBufCount = (k + this.mBufCount);
		}
	}
}