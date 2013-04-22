package com.google.android.voicesearch.speechservice;

import com.google.protos.speech.service.SpeechService.ResponseMessage;

public interface ConnectionCallback {
	public void onConnectionAlive();

	public void onConnectionClosed();

	public void onException(Exception paramException);

	public void onResponseAvailable(
			ResponseMessage response);
}