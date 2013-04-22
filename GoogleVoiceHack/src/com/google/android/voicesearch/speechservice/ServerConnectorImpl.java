package com.google.android.voicesearch.speechservice;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.voicesearch.Experiments;
import com.google.android.voicesearch.tcp.TcpConnectionImpl;
import com.google.protos.speech.service.ClientParametersProto.ClientExperimentConfig;
import com.google.protos.speech.service.ClientReportProto.ClientReport.ClientPerceivedRequestStatus;
import com.google.protos.speech.service.SpeechService;
import com.google.protos.speech.service.SpeechService.CreateSessionResponse;
import com.google.protos.speech.service.SpeechService.MessageHeader;
import com.google.protos.speech.service.SpeechService.RecognizeRequest;
import com.google.protos.speech.service.SpeechService.RecognizeResponse;
import com.google.protos.speech.service.SpeechService.RequestMessage;
import com.google.protos.speech.service.SpeechService.ResponseMessage;
import com.google.protos.speech.service.SpeechService.SocketAddress;
import com.google.protos.speech.service.SpeechService.Status;

public class ServerConnectorImpl implements ServerConnector, ConnectionCallback {
	private static final String TAG = "ServerConnectorImpl";
	private RecognitionParameters mParams = null;
	private SpeechServiceHttpClient mHttpClient;
	private Context mContext;
	boolean mUseTcp = true;
	private ClientReportBuilder mClientReportBuilder;
	private ServerConnectorCallback mCallback;
	private TcpConnectionImpl mTcpConnection;
	private int mNetworkConnectionTimeoutMillis;
	private List<SpeechService.RequestMessage> mPendingMessages = null;

	public ServerConnectorImpl() {
		mParams = new RecognitionParameters();
		mHttpClient = new SpeechServiceHttpClient();
		mNetworkConnectionTimeoutMillis = 10 * 1000;
	}

	private void sendRequest(RequestMessage requestMessage)
			throws ConnectionException {
		if (hasTcpSession()) {
			this.mTcpConnection.sendRequest(requestMessage);
			return;
		}
		addPendingMessage(requestMessage);
	}

	private void addPendingMessage(RequestMessage requestMessage) {
		if (mPendingMessages == null) {
			mPendingMessages = new ArrayList<RequestMessage>();
		}
		mPendingMessages.add(requestMessage);
	}

	private void closeTcpSession() {
		if (this.mTcpConnection == null) {
			return;
		}
		destroySession();
		this.mTcpConnection.close();
		this.mTcpConnection = null;
	}

	private void destroySession() {
		try {
			if (this.mParams.getSessionId() != null) {
				this.mParams.incrementRequestId();
				sendRequest(ProtoBufUtils
						.makeDestroySessionRequest(this.mParams));
			}
			return;
		} catch (ConnectionException localConnectionException) {
			Log.e("ServerConnectorImpl", "Destroying session failed",
					localConnectionException);
		}
	}

	private void flushMessages() throws ConnectionException {
		if ((this.mPendingMessages == null)
				|| (this.mPendingMessages.isEmpty()))
			return;
		List<RequestMessage> localList = this.mPendingMessages;
		mPendingMessages = null;
		Iterator<ResponseMessage> localIterator = mHttpClient.post(
				this.mParams, localList).iterator();
		while (localIterator.hasNext()) {
			onResponseAvailable((SpeechService.ResponseMessage) localIterator
					.next());
		}
	}

	private boolean hasTcpSession() {
		return (this.mParams.getSessionId() != null)
				&& (this.mTcpConnection != null)
				&& (this.mTcpConnection.isConnected());
	}

	private byte[] copyAudioData(ByteBuffer buffer) {
		buffer.mark();
		byte[] arrayOfByte = new byte[buffer.remaining()];
		buffer.get(arrayOfByte);
		buffer.reset();
		return arrayOfByte;
	}

	private void createTcpSession() {
		RequestMessage requestMessage = ProtoBufUtils.makeCreateSessionRequest(
				mParams, true);
		ResponseMessage responseMessage;
		try {
			responseMessage = mHttpClient.post(mParams, requestMessage);
			onResponseAvailable(responseMessage);
			createTcpConnection((SpeechService.CreateSessionResponse) responseMessage
					.getExtension(SpeechService.CreateSessionResponse.createSessionResponse));
		} catch (ConnectionException e) {
			Log.e("ServerConnectorImpl", "Failed to create session", e);
			setRequestStatus(ClientPerceivedRequestStatus.CREATE_CONNECTION_FAILURE_VALUE);
			this.mCallback.onError(RecognitionController.ERROR_NETWORK);
		}
	}

	private void createTcpConnection(CreateSessionResponse response) {
		if ((response == null) || (!response.hasServerAddress())) {
			setRequestStatus(ClientPerceivedRequestStatus.BAD_RESPONSE_VALUE);
			this.mCallback.onError(RecognitionController.ERROR_NETWORK);
			return;
		}
		SocketAddress socketAddress = response.getServerAddress();
		String ip = socketAddress.getHost();
		int port = socketAddress.getPort();
		String stunId = response.getStunId();
		Log.i("ServerConnectorImpl", "Creating TCP connection to " + ip + ":"
				+ port);
		try {
			SystemClock.elapsedRealtime();
			mTcpConnection = new TcpConnectionImpl(ip, port, stunId,
					mNetworkConnectionTimeoutMillis);
			this.mTcpConnection.start(this);
			return;
		} catch (ConnectionException localConnectionException) {
			Log.e("ServerConnectorImpl", "Failed to create TCP connection",
					localConnectionException);
			setRequestStatus(ClientPerceivedRequestStatus.TCP_CONNECTION_FAILURE_VALUE);
			this.mCallback.onError(RecognitionController.ERROR_NETWORK);
		}
	}

	@Override
	public void cancelRecognition() {
		if (mParams != null) {
			try {
				sendRequest(ProtoBufUtils.makeCancelRequest(mParams));
				return;
			} catch (ConnectionException localConnectionException) {
				Log.e("ServerConnectorImpl", "Sending cancel request failed ",
						localConnectionException);
			}
		}
	}

	@Override
	public void close() {
		if (this.mHttpClient != null) {
			this.mHttpClient.close();
			this.mHttpClient = null;
		}
		closeTcpSession();
	}

	@Override
	public void createClientReport() {
		// TODO Auto-generated method stub
	}

	@Override
	public void createSession(RecognitionParameters params) {
		mParams = params;
		if (this.mHttpClient == null) {
			mHttpClient = new SpeechServiceHttpClient();
		}
		mClientReportBuilder = ClientReportBuilder.getInstance();
		mClientReportBuilder.setNetworkType(mParams.getNetworkType());
		createTcpSession();
	}

	@Override
	public void postAudioChunk(ByteBuffer buffer, boolean speechEnd) {
		// InjectUtil.logPostChunk(buffer, speechEnd);
		SpeechService.RequestMessage requestMessage = ProtoBufUtils
				.makeMediaDataRequest(this.mParams, copyAudioData(buffer),
						speechEnd);
		try {
			sendRequest(requestMessage);
			if ((speechEnd) && (!this.mUseTcp)) {
				destroySession();
				flushMessages();
			}
			return;
		} catch (ConnectionException localConnectionException) {
			Log.e("ServerConnectorImpl", "Failed to send message",
					localConnectionException);
			setRequestStatus(ClientPerceivedRequestStatus.NETWORK_CONNECTIVITY_ERROR_VALUE);
			this.mCallback.onError(RecognitionController.ERROR_NETWORK);
		}
	}

	@Override
	public void sendClientReports() {
		// TODO use reporter sender to send report
	}

	@Override
	public void setCallback(ServerConnectorCallback callback) {
		mCallback = callback;
	}

	@Override
	public void setEndOfSpeech() {
		mClientReportBuilder.endOfSpeech();
	}

	@Override
	public void setEndpointTriggerType(int type) {
		if (mClientReportBuilder != null) {
			mClientReportBuilder.setEndpointTriggerType(type);
		}
	}

	@Override
	public void setRequestStatus(int status) {
		if (this.mClientReportBuilder != null) {
			mClientReportBuilder.setRequestStatus(status);
		}
	}

	@Override
	public void setUseTcp(boolean useTcp) {
		mUseTcp = useTcp;
		if (!mUseTcp) {
			closeTcpSession();
		}
	}

	@Override
	public void onConnectionAlive() {
		mCallback.onIsAlive();
	}

	@Override
	public void onConnectionClosed() {
		mTcpConnection = null;
	}

	@Override
	public void onException(Exception e) {
		this.mCallback.onError(RecognitionController.ERROR_NETWORK);
		Log.e("ServerConnectorImpl", "connection exception received", e);
		setRequestStatus(ClientPerceivedRequestStatus.NETWORK_CONNECTIVITY_ERROR
				.getNumber());
	}

	@Override
	public void onResponseAvailable(ResponseMessage responseMessage) {
		if (responseMessage == null || !responseMessage.hasHeader()) {
			Log.e(TAG, "No header in response：" + responseMessage);
			return;
		}
		MessageHeader header = responseMessage.getHeader();
		if (mUseTcp) {
			int id = header.getRequestId();
			if (id != mParams.getRequestId()) {
				Log.w("ServerConnectorImpl",
						"Discarding response with bad request id, current="
								+ mParams.getRequestId() + ", received=" + id);
				return;
			}
		}
		// CreateSessionResponse
		if (responseMessage
				.hasExtension(CreateSessionResponse.createSessionResponse)) {
			Log.d(TAG, "onResponseAvailable:CreateSessionResponse");
			CreateSessionResponse createSessionResponse;
			String sessionId = header.getSessionId();
			mParams.setSessionId(sessionId);
			Log.d("ServerConnectorImpl",
					"Created session " + mParams.getSessionId());
			createSessionResponse = (SpeechService.CreateSessionResponse) responseMessage
					.getExtension(SpeechService.CreateSessionResponse.createSessionResponse);
			if (createSessionResponse.hasClientExperimentConfigHash()) {
				long remoteHash = createSessionResponse
						.getClientExperimentConfigHash();
				long localHash = Experiments.getExperimentHash(mContext);
				if (remoteHash != localHash) {
					Experiments.updateExperimentHash(mContext, remoteHash);
				}
			}
			if (createSessionResponse.hasClientExperimentConfig()) {
				ClientExperimentConfig clientExperimentConfig = createSessionResponse
						.getClientExperimentConfig();
				if (clientExperimentConfig.hasClientParameters()) {
					Experiments.setExperimentParameters(mContext,
							clientExperimentConfig.getClientParameters());
				}
			}
		}
		// RecognizeAckResponse
		else if (responseMessage
				.hasExtension(SpeechService.RecognizeAck.recognizeAck)) {
			Log.d(TAG, "onResponseAvailable:RecognizeAck");
			if (mClientReportBuilder != null) {
				mClientReportBuilder.ackReceived();
			}
		}
		// RecognizeResponse
		else if (responseMessage
				.hasExtension(RecognizeResponse.recognizeResponse)) {
			Log.d(TAG, "onResponseAvailable:RecognizeResponse");
			RecognizeResponse recognizeResponse = (SpeechService.RecognizeResponse) responseMessage
					.getExtension(SpeechService.RecognizeResponse.recognizeResponse);
			if (recognizeResponse.hasGaiaResult()) {
				Log.i("ServerConnectorImpl", "Response: Gaia Result "
						+ recognizeResponse.getGaiaResult().getCode());
			}
			if (responseMessage.getStatus() == Status.IN_PROGRESS) {
				mCallback.onPartialResponse(recognizeResponse);
			} else {
				mCallback.onResponse(responseMessage);
				mClientReportBuilder.responseReceived();
				Log.d(TAG, mClientReportBuilder.toString(mParams));
			}
		} else {
			Log.w(TAG, "unknown response message!");
		}
	}

	public void startRecognize() {
		mParams.incrementRequestId();
		Log.i("ServerConnectorImpl", "startRecognize " + this.mParams);
		SpeechService.RequestMessage requestMessage = ProtoBufUtils
				.makeRecognizeRequest(this.mParams);
		RecognizeRequest recognizeRequest = ((SpeechService.RecognizeRequest) requestMessage
				.getExtension(SpeechService.RecognizeRequest.recognizeRequest));
		if (this.mClientReportBuilder != null)
			this.mClientReportBuilder.startRequest();
		try {
			sendRequest(requestMessage);
		} catch (ConnectionException localConnectionException) {
			Log.e("ServerConnectorImpl", "Failed to send recognition request",
					localConnectionException);
			setRequestStatus(ClientPerceivedRequestStatus.NETWORK_CONNECTIVITY_ERROR_VALUE);
			this.mCallback.onError(RecognitionController.ERROR_NETWORK);
		}
	};
}
