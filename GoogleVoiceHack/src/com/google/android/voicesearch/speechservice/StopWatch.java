package com.google.android.voicesearch.speechservice;

import android.os.SystemClock;

class StopWatch
{
  private long mStart = -1L;

  public int getElapsedTime()
  {
    return (int)(SystemClock.elapsedRealtime() - this.mStart);
  }

  public boolean isStarted()
  {
    return this.mStart > 0L;
  }

  public void reset()
  {
    this.mStart = -1L;
  }

  public void start()
  {
    this.mStart = SystemClock.elapsedRealtime();
  }
}