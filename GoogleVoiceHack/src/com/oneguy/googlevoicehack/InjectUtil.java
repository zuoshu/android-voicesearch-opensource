package com.oneguy.googlevoicehack;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.google.android.voicesearch.speechservice.RecognitionController;
import com.google.android.voicesearch.speechservice.RecognitionParameters;
import com.google.protos.speech.service.ClientReportProto.ClientReport.ClientPerceivedRequestStatus;
import com.google.protos.speech.service.ClientReportProto.MobileInfo.NetworkType;
import com.google.protos.speech.service.SpeechService.DebugEvent;
import com.google.protos.speech.service.SpeechService.MessageHeader;
import com.google.protos.speech.service.SpeechService.RecognitionResult;
import com.google.protos.speech.service.SpeechService.RecognizeResponse;
import com.google.protos.speech.service.SpeechService.RequestMessage;
import com.google.protos.speech.service.SpeechService.ResponseMessage;
import com.google.protos.wireless.voicesearch.VoiceSearch.ActionRequest;

public class InjectUtil {
	public static void logParmsToString(Object o) {
		Log.w("zuoshu", "parms:" + o.toString());
	}

	public static void logString(String str) {
		Log.w("zuoshu", str);
	}

	public static void logControlImpl(String str) {
		Log.w("zuoshu-c", str);
	}

	static List<byte[]> soundData = new LinkedList<byte[]>();
	static int length = 0;

	public static void logPacket(ByteBuffer buffer) {
		byte[] data = new byte[buffer.limit()];
		byte[] original = buffer.array();
		System.arraycopy(original, 0, data, 0, data.length);
		if (data == null) {
			Log.w("zuoshu-packet", "null");
		} else if (data.length == 0) {
			Log.w("zuoshu-packet", "length 0");
		} else {
			Log.w("zuoshu-packet", getByteString(data));
		}
		soundData.add(data);
		length += data.length;
		if (data.length == 0) {
			saveAudioFile();
		}
	}

	private static void saveAudioFile() {
		byte[] sound = combineCacheData();
		saveFile(sound);
		// Log.d("saveAudioFile", getByteString(sound));
	}

	public static byte[] combineCacheData() {
		byte[] result = new byte[length];
		int writePosition = 0;
		for (byte[] data : soundData) {
			System.arraycopy(data, 0, result, writePosition, data.length);
			writePosition += data.length;
		}
		return result;
	}

	private static void saveFile(byte[] data) {
		String fileName = "/data/data/com.google.android.voicesearch/files/save.packet";
		try {
			RandomAccessFile randomAccessWriter = new RandomAccessFile(
					fileName, "rw");
			randomAccessWriter.setLength(0);
			randomAccessWriter.write(data);
			randomAccessWriter.close();
			Log.d("saveWavFile", "wirte file done,length:" + data.length);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void test() {
		long l1 = 100;
		logLong(l1);
	}

	public static void logLong(long l){
		Log.d("zuoshu-long", "long value:"+l);
	}
	public void logBoolean(String tag, boolean value) {
		Log.d("zuoshu-bool", tag + ":" + value);
	}

	public static void logPostChunk(ByteBuffer buffer, boolean speechEnd) {
		Log.w("zuoshu-logPostChunk", "buffer:" + buffer.limit() + " speechEnd:"
				+ speechEnd);
	}

	public static void onPartialTranscript(String result) {
		Log.w("zuoshu", "onPartialTranscript:" + result);
	}

	public static void logMakeRecognizeRequest(RecognitionParameters parms) {
		StringBuilder sb = new StringBuilder("RecognitionParameters{");
		sb.append("isCarDock=").append(parms.isCarDock()).append(",");
		sb.append("session=").append(parms.getSessionId()).append(",");
		sb.append("cookie=").append(parms.getCookie()).append(",");
		sb.append("applicationId=").append(parms.getApplicationId())
				.append(",");
		sb.append("audioEncoding=").append(parms.getAudioEncoding())
				.append(",");
		sb.append("audioSampleRate=").append(parms.getAudioSampleRate())
				.append(",");
		sb.append("clientApplicationId=")
				.append(parms.getClientApplicationId()).append(",");
		sb.append("clientId=").append(parms.getClientId()).append(",");
		sb.append("cookie=").append(parms.getCookie()).append(",");
		sb.append("defaultLanguage=").append(parms.getDefaultLanguage())
				.append(",");
		sb.append("experimentHash=").append(parms.getExperimentHash())
				.append(",");
		sb.append("geoPosition=").append(parms.getGeoPosition()).append(",");
		sb.append("language=").append(parms.getLanguage()).append(",");
		sb.append("languageModel=").append(parms.getLanguageModel())
				.append(",");
		sb.append("locale=").append(parms.getLocale()).append(",");
		sb.append("maxResults=").append(parms.getMaxResults()).append(",");
		sb.append("mofeHttpUrl=").append(parms.getMofeHttpUrl()).append(",");
		sb.append("mofeProtoUrl=").append(parms.getMofeProtoUrl()).append(",");
		sb.append("multislotActionSelectedSlot=")
				.append(parms.getMultislotActionSelectedSlot()).append(",");
		sb.append("multislotActionType=")
				.append(parms.getMultislotActionType()).append(",");
		sb.append("networkType=").append(parms.getNetworkType()).append(",");
		sb.append("noiseLevel=").append(parms.getNoiseLevel()).append(",");
		sb.append("requestId=").append(parms.getRequestId()).append(",");
		sb.append("revClientId=").append(parms.getRevClientId()).append(",");
		sb.append("safeSearchSetting=").append(parms.getSafeSearchSetting())
				.append(",");
		sb.append("sessionId=").append(parms.getSessionId()).append(",");
		sb.append("snr=").append(parms.getSnr()).append(",");
		sb.append("speechPersonalizationServiceAuthToken=")
				.append(parms.getSpeechPersonalizationServiceAuthToken())
				.append(",");
		sb.append("speechServerUrl=").append(parms.getSpeechServerUrl())
				.append(",");
		sb.append("userAgent=").append(parms.getUserAgent()).append(",");
		List<Integer> actions = parms.getSupportedActionInterpretations();
		sb.append("supportedActionInterpretations=");
		for (Integer i : actions) {
			sb.append(i).append(":");
		}
		sb.append("}");
		Log.w("zuoshu-make-recognize-request", sb.toString());
	}

	public static void logActionRequest(ActionRequest request) {
		StringBuilder sb = new StringBuilder("logActionRequest{");
		sb.append("hasMapsRequestData=").append(request.hasMapsRequestData())
				.append(",");
		sb.append("hasMultislotActionContext=")
				.append(request.hasMultislotActionContext()).append(",");
		sb.append("hasWebsearchRequestData=")
				.append(request.hasWebsearchRequestData()).append("}");
		Log.w("zuoshu-logActionRequest", sb.toString());
	}

	public static void logRecognitionParameters(RecognitionParameters parms) {
		StringBuilder sb = new StringBuilder("RecognitionParameters{");
		sb.append("session=").append(parms.getSessionId()).append(",");
		sb.append("cookie=").append(parms.getCookie()).append(",");
		sb.append("applicationId=").append(parms.getApplicationId())
				.append(",");
		sb.append("audioEncoding=").append(parms.getAudioEncoding())
				.append(",");
		sb.append("audioSampleRate=").append(parms.getAudioSampleRate())
				.append(",");
		sb.append("clientApplicationId=")
				.append(parms.getClientApplicationId()).append(",");
		sb.append("clientId=").append(parms.getClientId()).append(",");
		sb.append("cookie=").append(parms.getCookie()).append(",");
		sb.append("defaultLanguage=").append(parms.getDefaultLanguage())
				.append(",");
		sb.append("experimentHash=").append(parms.getExperimentHash())
				.append(",");
		sb.append("geoPosition=").append(parms.getGeoPosition()).append(",");
		sb.append("language=").append(parms.getLanguage()).append(",");
		sb.append("languageModel=").append(parms.getLanguageModel())
				.append(",");
		sb.append("locale=").append(parms.getLocale()).append(",");
		sb.append("maxResults=").append(parms.getMaxResults()).append(",");
		sb.append("mofeHttpUrl=").append(parms.getMofeHttpUrl()).append(",");
		sb.append("mofeProtoUrl=").append(parms.getMofeProtoUrl()).append(",");
		sb.append("multislotActionSelectedSlot=")
				.append(parms.getMultislotActionSelectedSlot()).append(",");
		sb.append("multislotActionType=")
				.append(parms.getMultislotActionType()).append(",");
		sb.append("networkType=").append(parms.getNetworkType()).append(",");
		sb.append("noiseLevel=").append(parms.getNoiseLevel()).append(",");
		sb.append("requestId=").append(parms.getRequestId()).append(",");
		sb.append("revClientId=").append(parms.getRevClientId()).append(",");
		sb.append("safeSearchSetting=").append(parms.getSafeSearchSetting())
				.append(",");
		sb.append("sessionId=").append(parms.getSessionId()).append(",");
		sb.append("snr=").append(parms.getSnr()).append(",");
		sb.append("speechPersonalizationServiceAuthToken=")
				.append(parms.getSpeechPersonalizationServiceAuthToken())
				.append(",");
		sb.append("speechServerUrl=").append(parms.getSpeechServerUrl())
				.append(",");
		sb.append("userAgent=").append(parms.getUserAgent()).append(",");
		List<Integer> actions = parms.getSupportedActionInterpretations();
		sb.append("supportedActionInterpretations=");
		for (Integer i : actions) {
			sb.append(i).append(":");
		}
		sb.append("}");
		Log.w("zuoshu-rec-parms", sb.toString());
	}

	public static void logRequestMessage(RequestMessage msg) {
		StringBuilder sb = new StringBuilder();
		sb.append("RequestMessage{");
		MessageHeader h = msg.getHeader();
		sb.append("MessageHeader{");
		sb.append("applicationId=").append(h.getApplicationId()).append(",");
		sb.append("requestId=").append(h.getRequestId()).append(",");
		sb.append("serializedSize=")
				.append(Integer.valueOf(h.getSerializedSize())).append(",");
		sb.append("sessionId=").append(h.getSessionId()).append("}");
		sb.append("enableDebug=").append(msg.getEnableDebug()).append(",");
		sb.append("enableDebugPassword=").append(msg.getEnableDebugPassword())
				.append(",");
		sb.append("serializedSize=")
				.append(Integer.valueOf(msg.getSerializedSize())).append("}");
		sb.append("byte{").append(getByteString(msg.toByteArray())).append("}");
		Log.w("zuoshu-req-msg", sb.toString());
	}

	public static void logResponseMessage(ResponseMessage msg) {
		StringBuilder sb = new StringBuilder();
		sb.append("ResponseMessage{");
		// other
		sb.append("status=").append(msg.getStatus().name()).append(",");
		sb.append("errorDetails=").append(msg.getErrorDetail()).append(",");
		sb.append("serializedSize=")
				.append(Integer.valueOf(msg.getSerializedSize())).append("}");
		// MessageHeader
		MessageHeader h = msg.getHeader();
		sb.append("MessageHeader{");
		sb.append("applicationId=").append(h.getApplicationId()).append(",");
		sb.append("requestId=").append(h.getRequestId()).append(",");
		sb.append("serializedSize=")
				.append(Integer.valueOf(h.getSerializedSize())).append(",");
		sb.append("sessionId=").append(h.getSessionId()).append("}");
		// DebugEvent
		DebugEvent event = msg.getDebugEvent();
		sb.append("DebugEvent{");
		sb.append("startTimeMs=").append(event.getStartTimeMs() + "")
				.append(",");
		sb.append("durationMs=").append(event.getDurationMs() + "").append(",");
		sb.append("text=").append(event.getText()).append(",");
		sb.append("subevent=").append(event.getSubeventCount() + "")
				.append("}");
		sb.append("byte{").append(getByteString(msg.toByteArray())).append("}");
		if (msg.hasExtension(RecognizeResponse.recognizeResponse)) {
			RecognizeResponse recognizeResponse = msg
					.getExtension(RecognizeResponse.recognizeResponse);
			sb.append("response=").append(
					getByteString(recognizeResponse.toByteArray()));

		}
		Log.w("zuoshu-resp-msg", sb.toString());
	}

	public static String getByteString(byte[] data) {
		if (data==null || data.length==0) {
			return "empty";
		}
		StringBuilder sb = new StringBuilder();
		for (byte b : data) {
			sb.append(b & 0xff).append(" ");
			// sb.append((char)b).append(" ");
		}
		return sb.toString();
	}

	public static void logByteArray(String tag, byte[] bytes) {
		Log.d(tag, getByteString(bytes));
	}

	public static void logResult(RecognitionResult result) {
		String tag = "recognize-result";
		if (result == null) {
			Log.d(tag, "null");
			return;
		}
		int count = result.getHypothesisCount();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < count; i++) {
			sb.append(i + ":").append(result.getHypothesis(i).getSentence())
					.append("\n");
		}
		InjectUtil.logTime("response received");
		Log.d(tag, sb.toString());
	}

	static long time;
	final static String TIME_TAG = "timer";
	static Map<String, String> map = new HashMap<String, String>();

	public static void timerInit() {
		Log.d(TIME_TAG, "timer init");
		time = System.currentTimeMillis();
	}

	public static void logTime(String content) {
		Log.d(TIME_TAG, content + ":" + (System.currentTimeMillis() - time));
		time = System.currentTimeMillis();
	}

	public static void logReceivePacket(byte[] data) {
		Log.d("logPacket", "receive " +(data==null?0:data.length)+":"+ getByteString(data));
	}

	public static void logSendPacket(byte[] data) {
		Log.d("logPacket", "send " +(data==null?0:data.length)+":"+ getByteString(data));
	}

	public static void logReadPacketLength(int length) {
		Log.d("logReadPacketLength", length + "");
	}

	public static String getErrorCodeStr(int errorCode) {
		switch (errorCode) {
		case RecognitionController.ERROR_NONE:
			return "ERROR_NONE";
		case RecognitionController.ERROR_NETWORK_TIMEOUT:
			return "ERROR_NETWORK_TIMEOUT";
		case RecognitionController.ERROR_NETWORK:
			return "ERROR_NETWORK";
		case RecognitionController.ERROR_AUDIO:
			return "ERROR_AUDIO";
		case RecognitionController.ERROR_SERVER:
			return "ERROR_SERVER";
		case RecognitionController.ERROR_CLIENT:
			return "ERROR_CLIENT";
		case RecognitionController.ERROR_SPEECH_TIMEOUT:
			return "ERROR_SPEECH_TIMEOUT";
		case RecognitionController.ERROR_NO_MATCH:
			return "ERROR_NO_MATCH";
		default:
			return "UNKNOWN";
		}
	}

	public static String getClientRequestStatus(int status) {
		ClientPerceivedRequestStatus s = ClientPerceivedRequestStatus
				.valueOf(status);
		if (s == null) {
			return String.valueOf(status);
		} else {
			return s.name();
		}
	}

	public static String getNetworkTypeString(int networkType) {
		NetworkType type = NetworkType.valueOf(networkType);
		if (type == null) {
			return String.valueOf(networkType);
		} else {
			return type.name();
		}
	}
}
