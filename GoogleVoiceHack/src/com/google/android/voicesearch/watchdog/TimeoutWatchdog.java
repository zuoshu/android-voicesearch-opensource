package com.google.android.voicesearch.watchdog;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TimeoutWatchdog {
	private static final boolean DBG = false;
	private static final String TAG = "TimeoutWatchdog";
	private final Runnable mOnTimeoutRunnable;
	private ScheduledFuture<?> mOnTimeoutTask;
	private final ScheduledExecutorService mScheduler;
	private volatile long mTcpTimeoutTimestamp;
	private final int mTimeOutMillis;

	public TimeoutWatchdog(int timeout, Runnable onTimeoutRunnable) {
		this(timeout, Executors.newScheduledThreadPool(1), onTimeoutRunnable);
	}

	public TimeoutWatchdog(int timeout, ScheduledExecutorService sheduler,
			Runnable paramRunnable) {
		this.mOnTimeoutRunnable = paramRunnable;
		this.mTimeOutMillis = timeout;
		this.mScheduler = sheduler;
	}

	private void scheduleTask() {
		long delay = Math.max(1L,
				mTcpTimeoutTimestamp - System.currentTimeMillis());
		try {
			mOnTimeoutTask = mScheduler.schedule(new WatchdogTask(), delay,
					TimeUnit.MILLISECONDS);
			return;
		} catch (RejectedExecutionException e) {
		}
	}

	public void extend() {
		this.mTcpTimeoutTimestamp = (System.currentTimeMillis() + this.mTimeOutMillis);
	}

	public void start() {
		extend();
		scheduleTask();
	}

	public void stop() {
		if (this.mOnTimeoutTask != null)
			this.mOnTimeoutTask.cancel(true);
		this.mScheduler.shutdown();
	}

	private class WatchdogTask implements Runnable {
		private WatchdogTask() {
		}

		public void run() {
			if (System.currentTimeMillis() >= TimeoutWatchdog.this.mTcpTimeoutTimestamp) {
				TimeoutWatchdog.this.mOnTimeoutRunnable.run();
				return;
			}
			TimeoutWatchdog.this.scheduleTask();
		}
	}
}