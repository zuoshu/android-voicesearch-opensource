package com.google.android.voicesearch.speechservice;

import android.os.SystemClock;

public class TimeoutTimer {
	private long mUntil;

	TimeoutTimer() {
		this(0L);
	}

	TimeoutTimer(long paramLong) {
		this.mUntil = (paramLong + SystemClock.elapsedRealtime());
	}

	public void extend(long paramLong) {
		synchronized (this) {

			this.mUntil = (paramLong + this.mUntil);
		}
	}

	public long remaining() {
		synchronized (this) {
			long l1 = this.mUntil;
			long l2 = SystemClock.elapsedRealtime();
			long l3 = l1 - l2;
			return l3;
		}
	}

	public void set(long paramLong) {
		synchronized (this) {
			this.mUntil = (paramLong + SystemClock.elapsedRealtime());
		}
	}
}