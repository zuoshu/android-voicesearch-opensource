package com.google.android.voicesearch.speechservice;

import com.google.protos.speech.service.SpeechService;

public interface ServerConnectorCallback
{
  public void onError(int paramInt);

  public void onIsAlive();

  public void onPartialResponse(SpeechService.RecognizeResponse paramRecognizeResponse);

  public void onResponse(SpeechService.ResponseMessage paramResponseMessage);
}