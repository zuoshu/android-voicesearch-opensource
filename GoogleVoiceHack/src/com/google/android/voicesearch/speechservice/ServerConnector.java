package com.google.android.voicesearch.speechservice;

import java.nio.ByteBuffer;

public interface ServerConnector {
	public void cancelRecognition();

	public void close();

	public void createClientReport();

	public void createSession(RecognitionParameters paramRecognitionParameters);

	public void postAudioChunk(ByteBuffer paramByteBuffer, boolean paramBoolean);

	public void sendClientReports();

	public void setCallback(ServerConnectorCallback paramServerConnectorCallback);

	public void setEndOfSpeech();

	public void setEndpointTriggerType(int paramInt);

	public void setRequestStatus(int paramInt);

	public void setUseTcp(boolean paramBoolean);

	public void startRecognize();
}