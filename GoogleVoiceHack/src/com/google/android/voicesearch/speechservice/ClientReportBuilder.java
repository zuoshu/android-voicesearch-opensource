package com.google.android.voicesearch.speechservice;

import com.cyberobject.inject.InjectUtil;
import com.google.protos.speech.service.ClientReportProto;
import com.google.protos.speech.service.SpeechService;

public class ClientReportBuilder {
	private int mClientPerceivedRequestStatus = -1;
	private int mClientSideError = -1;
	private int mEndpointTriggerType = -1;
	private int mNetworkType = -1;
	private int mRequestAckLatencyMs = -1;
	private int mTotalLatencyMs = -1;
	private final StopWatch mTotalLatencyWatch = new StopWatch();
	private int mUserPerceivedLatencyMs = -1;
	private final StopWatch mUserPerceivedLatencyWatch = new StopWatch();
	private static ClientReportBuilder instance = null;

	public static synchronized ClientReportBuilder getInstance() {
		if (instance == null) {
			instance = new ClientReportBuilder();
		}
		return instance;
	}

	private ClientReportBuilder() {
	}

	private void appendField(StringBuilder sb, String paramString, int paramInt) {
		if (paramInt == -1)
			return;
		sb.append(paramString).append('=').append(paramInt).append(',');
	}

	private void appendField(StringBuilder paramStringBuilder,
			String paramString1, String paramString2) {
		if (paramString2 == null)
			return;
		paramStringBuilder.append(paramString1).append('=')
				.append(paramString2).append(',');
	}

	public void ackReceived() {
		this.mRequestAckLatencyMs = this.mTotalLatencyWatch.getElapsedTime();
	}

	public SpeechService.RequestMessage createClientReportRequest(
			RecognitionParameters paramRecognitionParameters) {
		ClientReportProto.ClientReport.Builder localBuilder = ClientReportProto.ClientReport
				.newBuilder();
		if (this.mClientPerceivedRequestStatus != -1)
			localBuilder
					.setClientPerceivedRequestStatus(ClientReportProto.ClientReport.ClientPerceivedRequestStatus
							.valueOf(this.mClientPerceivedRequestStatus));
		if (this.mRequestAckLatencyMs != -1)
			localBuilder.setRequestAckLatencyMs(this.mRequestAckLatencyMs);
		if (this.mTotalLatencyMs != -1)
			localBuilder.setTotalLatencyMs(this.mTotalLatencyMs);
		if (this.mUserPerceivedLatencyMs != -1)
			localBuilder
					.setUserPerceivedLatencyMs(this.mUserPerceivedLatencyMs);
		if (this.mNetworkType != -1) {
			ClientReportProto.MobileInfo.Builder localBuilder2 = ClientReportProto.MobileInfo
					.newBuilder();
			localBuilder2
					.setNetworkType(ClientReportProto.MobileInfo.NetworkType
							.valueOf(this.mNetworkType));
			localBuilder.setExtension(ClientReportProto.MobileInfo.mobileInfo,
					localBuilder2.build());
		}
		if (this.mEndpointTriggerType != -1) {
			ClientReportProto.AudioInputInfo.Builder localBuilder1 = ClientReportProto.AudioInputInfo
					.newBuilder();
			localBuilder1
					.setEndpointTriggerType(ClientReportProto.AudioInputInfo.EndpointTriggerType
							.valueOf(this.mEndpointTriggerType));
			localBuilder.setExtension(
					ClientReportProto.AudioInputInfo.audioInputInfo,
					localBuilder1.build());
		}
		if (this.mClientSideError != -1)
			localBuilder.setClientSideError(this.mClientSideError);
		SpeechService.RequestMessage requestMessage = ProtoBufUtils
				.makeClientReportRequest(paramRecognitionParameters,
						localBuilder.build());
		return requestMessage;
	}

	public void endOfSpeech() {
		this.mUserPerceivedLatencyWatch.start();
	}

	public void responseReceived() {
		this.mTotalLatencyMs = this.mTotalLatencyWatch.getElapsedTime();
		if (this.mUserPerceivedLatencyWatch.isStarted()) {
			this.mUserPerceivedLatencyMs = this.mUserPerceivedLatencyWatch
					.getElapsedTime();
		}
	}

	public void setClientSideError(int paramInt) {
		this.mClientSideError = paramInt;
	}

	public void setEndpointTriggerType(int paramInt) {
		this.mEndpointTriggerType = paramInt;
	}

	public void setNetworkType(int paramInt) {
		this.mNetworkType = paramInt;
	}

	public void setRequestStatus(int status) {
		this.mClientPerceivedRequestStatus = status;
	}

	public void startRequest() {
		this.mTotalLatencyWatch.start();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("");
		sb.append("user_perceived_latency_ms:").append(mUserPerceivedLatencyMs)
				.append("\n");
		sb.append("client_perceived_request_status:")
				.append(InjectUtil
						.getClientRequestStatus(mClientPerceivedRequestStatus))
				.append("\n");
		sb.append("request_ack_latency_ms:").append(mRequestAckLatencyMs)
				.append("\n");
		sb.append("total_latency_ms:").append(mTotalLatencyMs).append("\n");
		sb.append("network_type:")
				.append(InjectUtil.getNetworkTypeString(mNetworkType))
				.append("\n");
		return sb.toString();
	}

	public String toString(RecognitionParameters paramRecognitionParameters) {
		StringBuilder sb = new StringBuilder("ClientReport{");
		appendField(sb, "session_id", paramRecognitionParameters.getSessionId());
		appendField(sb, "request_id", paramRecognitionParameters.getRequestId());
		appendField(sb, "application_id",
				paramRecognitionParameters.getApplicationId());
		appendField(sb, "client_perceived_request_status",
				this.mClientPerceivedRequestStatus);
		appendField(sb, "request_ack_latency_ms", this.mRequestAckLatencyMs);
		appendField(sb, "total_latency_ms", this.mTotalLatencyMs);
		appendField(sb, "user_perceived_latency_ms",
				this.mUserPerceivedLatencyMs);
		appendField(sb, "network_type", this.mNetworkType);
		appendField(sb, "endpoint_trigger_type", this.mEndpointTriggerType);
		appendField(sb, "client_side_error", this.mClientSideError);
		return sb.append("}").toString();
	}
}