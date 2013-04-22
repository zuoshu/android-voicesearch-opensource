package com.google.android.voicesearch.speechservice;

import java.util.Iterator;
import java.util.Locale;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.voicesearch.LanguagePrefManagerImpl;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageLite;
import com.google.protos.multimodal.RecognitionResultSet;
import com.google.protos.speech.apps.intentapi.IntentApi;
import com.google.protos.speech.common.RecognitionContextProto;
import com.google.protos.speech.service.ClientReportProto;
import com.google.protos.speech.service.SpeechService;
import com.google.protos.speech.service.SpeechService.AudioParameters;
import com.google.protos.speech.service.SpeechService.CreateSessionRequest;
import com.google.protos.speech.service.SpeechService.Encoding;
import com.google.protos.speech.service.SpeechService.Protocol;
import com.google.protos.speech.service.SpeechService.ProtocolFeatures;
import com.google.protos.speech.service.SpeechService.RecognizeRequest;
import com.google.protos.speech.voicesearch.GoogleSearchRequest;
import com.google.protos.wireless.voicesearch.VoiceSearch;

public class ProtoBufUtils {
	static final String RC_ENABLED_LANGUAGES = "enabledLanguages";
	private static final String RC_FIELD_ID = "fieldId";
	private static final String RC_FIELD_NAME = "fieldName";
	private static final String RC_HINT = "hint";
	private static final String RC_IME_OPTIONS = "imeOptions";
	private static final String RC_INPUT_TYPE = "inputType";
	private static final String RC_LABEL = "label";
	private static final String RC_PACKAGE_NAME = "packageName";
	private static final String RC_SELECTED_LANGUAGE = "selectedLanguage";

	public static SpeechService.RequestMessage makeCreateSessionRequest(
			RecognitionParameters params, boolean useTcp) {
		AudioParameters.Builder audioParamsBuilder = AudioParameters
				.newBuilder();
		audioParamsBuilder.setEncoding(Encoding.AMR_NB);
		audioParamsBuilder.setSampleRate(params.getAudioSampleRate());
		audioParamsBuilder.setEndpointerEnabled(true);
		audioParamsBuilder.setNoiseCancelerEnabled(false);
		CreateSessionRequest.Builder createSessionRequestbuilder = CreateSessionRequest
				.newBuilder();
		if (useTcp) {
			createSessionRequestbuilder.setProtocol(Protocol.TCP_STUN);
		} else {
			createSessionRequestbuilder.setProtocol(Protocol.HTTP);
		}
		createSessionRequestbuilder.setClientId(params.getClientId());
		createSessionRequestbuilder.setInputAudioParameters(audioParamsBuilder);
		createSessionRequestbuilder.setClientExperimentConfigHash((int) params
				.getExperimentHash());
		String cookie = params.getCookie();
		if (!TextUtils.isEmpty(cookie)) {
			createSessionRequestbuilder.setCookie(cookie);
		}
		createSessionRequestbuilder.setLocale(params.getLocale());
		ProtocolFeatures.Builder protocolFeaturesBuilder = ProtocolFeatures
				.newBuilder();
		protocolFeaturesBuilder.setEnableAck(true);
		protocolFeaturesBuilder.setEnableInProgressResponse(false);
		if (useTcp) {
			protocolFeaturesBuilder.setEnablePartialResults(params
					.isPartialTranscriptsEnabled());
		}
		protocolFeaturesBuilder.setEnableRecognitionAlternates(params
				.isAlternatesEnabled());
		createSessionRequestbuilder
				.setProtocolFeatures(protocolFeaturesBuilder);
		return makeRequestMessage(params,
				CreateSessionRequest.createSessionRequest,
				createSessionRequestbuilder.build(), false);
	}

	private static <T> SpeechService.RequestMessage makeRequestMessage(
			RecognitionParameters params,
			GeneratedMessageLite.GeneratedExtension<SpeechService.RequestMessage, T> extension,
			T paramT, boolean useEmptyRequestId) {
		SpeechService.RequestMessage.Builder localBuilder = SpeechService.RequestMessage
				.newBuilder();
		localBuilder.setHeader(makeMessageHeader(params, useEmptyRequestId));
		localBuilder.setExtension(extension, paramT);
		return localBuilder.build();
	}

	private static SpeechService.MessageHeader makeMessageHeader(
			RecognitionParameters params, boolean useEmptyRequestId) {
		SpeechService.MessageHeader.Builder localBuilder = SpeechService.MessageHeader
				.newBuilder();
		localBuilder.setApplicationId(params.getApplicationId());
		if ((!useEmptyRequestId) || (params.getRequestId() > 0)) {
			localBuilder.setRequestId(params.getRequestId());
		}
		if (params.getSessionId() != null) {
			localBuilder.setSessionId(params.getSessionId());
		}
		return localBuilder.build();
	}

	public static SpeechService.RequestMessage makeRecognizeRequest(
			RecognitionParameters params) {
		SpeechService.RecognizeRequest.Builder recognizeRequestBuilder = SpeechService.RecognizeRequest
				.newBuilder();
		if (params.isApiMode()) {
			IntentApi.IntentApiGrammar.Builder intentApiGrammarBuilder = IntentApi.IntentApiGrammar
					.newBuilder();
			if (params.getLanguageModel() != null)
				intentApiGrammarBuilder.setLanguageModel(params
						.getLanguageModel());
			intentApiGrammarBuilder.setMaxNbest(params.getMaxResults());
			Bundle bundle = params.getRecognitionContextBundle();
			if (bundle != null)
				intentApiGrammarBuilder
						.setRecognitionContext(makeRecognitionContext(bundle,
								params.getDefaultLanguage()));
			SpeechService.Grammar.Builder grammarBuilder = SpeechService.Grammar
					.newBuilder();
			grammarBuilder.setExtension(
					IntentApi.IntentApiGrammar.intentApiGrammar,
					intentApiGrammarBuilder.build());
			recognizeRequestBuilder.setGrammar(grammarBuilder);
		} else {
			VoiceSearch.ConfigurableGrammar.Builder confgGrammarBuilder = VoiceSearch.ConfigurableGrammar
					.newBuilder();
			String[] contactAuthTokens = params.getContactAuthTokens();
			if (contactAuthTokens != null) {
				confgGrammarBuilder
						.setUseSpeechpersonalizationGaiaAuthenticationTokens(true);
				int i = contactAuthTokens.length;
				for (int j = 0; j < i; ++j)
					confgGrammarBuilder
							.addGaiaAuthenticationToken(contactAuthTokens[j]);
			}
			SpeechService.Grammar.Builder localBuilder2 = SpeechService.Grammar
					.newBuilder();
			localBuilder2.setExtension(
					VoiceSearch.ConfigurableGrammar.configurableGrammar,
					confgGrammarBuilder.build());
			recognizeRequestBuilder.setGrammar(localBuilder2);
			recognizeRequestBuilder.setInputData(makeApplicationData(params));
		}

		String clientApplicationId = params.getClientApplicationId();
		if (clientApplicationId != null) {
			recognizeRequestBuilder.setClientApplicationId(clientApplicationId);

		}
		recognizeRequestBuilder.setLanguage(params.getLanguage());
		recognizeRequestBuilder.setEnableProfanityFilter(params
				.isProfanityFilterEnabled());
		setPersonalizationToken(params, recognizeRequestBuilder);
		recognizeRequestBuilder
				.setInputModality(SpeechService.InputModality.PUSH_TO_TALK);
		recognizeRequestBuilder
				.setInputDevice(SpeechService.InputDevice.EMBEDDED_MICROPHONE);
		if (!params.isCarDock()) {
			recognizeRequestBuilder
					.setInputEnvironment(SpeechService.InputEnvironment.MOBILE_UNDOCKED);

		} else {
			recognizeRequestBuilder
					.setInputEnvironment(SpeechService.InputEnvironment.MOBILE_CAR_DOCK);
		}

		if (params.hasNoiseEstmation()) {
			recognizeRequestBuilder.setSnr((int) (100.0F * params.getSnr()));
			recognizeRequestBuilder.setNoiseLevel((int) params.getNoiseLevel());
		}
		RecognizeRequest request = recognizeRequestBuilder.build();
		return makeRequestMessage(params,
				SpeechService.RecognizeRequest.recognizeRequest, request, false);
	}

	private static SpeechService.ApplicationData makeApplicationData(
			RecognitionParameters params) {
		SpeechService.ApplicationData.Builder appDataBuilder = SpeechService.ApplicationData
				.newBuilder();
		appDataBuilder.setExtension(VoiceSearch.ActionRequest.actionRequest,
				makeActionRequest(params));
		return appDataBuilder.build();
	}

	private static VoiceSearch.ActionRequest makeActionRequest(
			RecognitionParameters params) {
		VoiceSearch.ActionRequest.Builder actionRequestBuilder = VoiceSearch.ActionRequest
				.newBuilder();
		Iterator<Integer> localIterator = params
				.getSupportedActionInterpretations().iterator();
		while (localIterator.hasNext()) {
			VoiceSearch.ActionInterpretation.Action action = VoiceSearch.ActionInterpretation.Action
					.valueOf(((Integer) localIterator.next()).intValue());
			if (action == null) {
				continue;
			}
			actionRequestBuilder.addSupportedAction(action);
		}
		actionRequestBuilder
				.setWebsearchRequestData(makeWebSearchRequestData(params));
		int multislotActionType = params.getMultislotActionType();
		if (multislotActionType != -1) {
			VoiceSearch.MultislotActionContext.Builder multislotActionContextBuilder = VoiceSearch.MultislotActionContext
					.newBuilder();
			multislotActionContextBuilder
					.setActionType(VoiceSearch.ActionInterpretation.Action
							.valueOf(multislotActionType));
			String selectedSlot = params.getMultislotActionSelectedSlot();
			if (selectedSlot != null)
				multislotActionContextBuilder.setSelectedSlot(selectedSlot);
			actionRequestBuilder
					.setMultislotActionContext(multislotActionContextBuilder);
		}
		return actionRequestBuilder.build();
	}

	private static RecognitionContextProto.RecognitionContext makeRecognitionContext(
			Bundle paramBundle, String paramString) {
		RecognitionContextProto.RecognitionContext.Builder localBuilder = RecognitionContextProto.RecognitionContext
				.newBuilder();
		String str1 = paramBundle.getString(RC_PACKAGE_NAME);
		if (str1 != null)
			localBuilder.setApplicationName(str1);
		String str2 = paramBundle.getString(RC_FIELD_NAME);
		if (str2 != null)
			localBuilder.setFieldName(str2);
		if (paramBundle.containsKey(RC_FIELD_ID))
			localBuilder.setFieldId(String.valueOf(paramBundle
					.getInt(RC_FIELD_ID)));
		String str3 = paramBundle.getString(RC_LABEL);
		if (str3 != null)
			localBuilder.setLabel(str3);
		String str4 = paramBundle.getString(RC_HINT);
		if (str4 != null)
			localBuilder.setHint(str4);
		if (paramBundle.containsKey(RC_INPUT_TYPE))
			localBuilder.setInputType(paramBundle.getInt(RC_INPUT_TYPE));
		if (paramBundle.containsKey(RC_IME_OPTIONS))
			localBuilder.setImeOptions(paramBundle.getInt(RC_IME_OPTIONS));
		String str5 = paramBundle.getString(RC_SELECTED_LANGUAGE);
		if (str5 != null)
			localBuilder.setSelectedKeyboardLanguage(str5);
		String[] arrayOfString = paramBundle
				.getStringArray(RC_ENABLED_LANGUAGES);
		if (arrayOfString != null) {
			int i = arrayOfString.length;
			for (int j = 0; j < i; ++j)
				localBuilder.addEnabledKeyboardLanguage(arrayOfString[j]);
		}
		localBuilder.setVoiceSearchLanguage(paramString);
		return localBuilder.build();
	}

	private static VoiceSearch.WebSearchRequestData makeWebSearchRequestData(
			RecognitionParameters paramRecognitionParameters) {
		String str1 = paramRecognitionParameters.getSessionId() + "-"
				+ paramRecognitionParameters.getRequestId();
		GoogleSearchRequest.GoogleSearchRequestProto.Builder localBuilder = GoogleSearchRequest.GoogleSearchRequestProto
				.newBuilder();
		localBuilder.setApplicationId(paramRecognitionParameters
				.getApplicationId());
		localBuilder.setUtteranceId(str1);
		localBuilder
				.setSafeSearchFilter(GoogleSearchRequest.GoogleSearchRequestProto.SafeSearchFilter
						.valueOf(paramRecognitionParameters
								.getSafeSearchSetting()));
		localBuilder.setClientId(paramRecognitionParameters.getUserAgent());
		addSearchAttribute(localBuilder, "hl",
				LanguagePrefManagerImpl.getHlParameter());
		addSearchAttribute(localBuilder, "gl", Locale.getDefault().getCountry());
		addSearchAttribute(localBuilder, "ie", "");
		addSearchAttribute(localBuilder, "v", "");
		addSearchAttribute(localBuilder, "client",
				paramRecognitionParameters.getRevClientId());
		addSearchAttribute(localBuilder, "source", "mobilesearchapp-vs");
		addSearchAttribute(localBuilder, "channel", "iss");
		String str2 = paramRecognitionParameters.getGeoPosition();
		if (str2 != null)
			localBuilder.addHttpHeader(makeRequestAttribute("Geo-Position",
					str2));
		localBuilder
				.setRecognitionResultSet(RecognitionResultSet.RecognitionResultSetProto
						.newBuilder().build());
		VoiceSearch.WebSearchRequestData.Builder localBuilder1 = VoiceSearch.WebSearchRequestData
				.newBuilder();
		localBuilder1.setGoogleSearchRequestProto(localBuilder.build()
				.toByteString());
		String str3 = paramRecognitionParameters.getMofeHttpUrl();
		if (str3 != null)
			localBuilder1.setServerHttpUrl(str3);
		String str4 = paramRecognitionParameters.getMofeProtoUrl();
		if (str4 != null)
			localBuilder1.setServerProtoUrl(str4);
		localBuilder1
				.setResponseContentEncoding(VoiceSearch.MultiModalData.ContentEncoding.GZIP);
		Iterator localIterator = paramRecognitionParameters
				.getSupportedActionInterpretations().iterator();
		while (localIterator.hasNext()) {
			int i = ((Integer) localIterator.next()).intValue();
			if (i == 8)
				continue;
			VoiceSearch.ActionInterpretation.Action localAction = VoiceSearch.ActionInterpretation.Action
					.valueOf(i);
			if (localAction == null)
				continue;
			localBuilder1.addSkipWebsearchAction(localAction);
		}
		localBuilder1.setSkipWebsearchAlways(true);
		return localBuilder1.build();
	}

	public static SpeechService.RequestMessage makeDestroySessionRequest(
			RecognitionParameters paramRecognitionParameters) {
		return makeRequestMessage(paramRecognitionParameters,
				SpeechService.DestroySessionRequest.destroySessionRequest,
				SpeechService.DestroySessionRequest.newBuilder().build(), false);
	}

	public static SpeechService.RequestMessage makeMediaDataRequest(
			RecognitionParameters paramRecognitionParameters,
			byte[] paramArrayOfByte, boolean paramBoolean) {
		SpeechService.MediaData.Builder mediaDataBuilder = SpeechService.MediaData
				.newBuilder();
		mediaDataBuilder.setEndOfData(paramBoolean);
		mediaDataBuilder.setData(ByteString.copyFrom(paramArrayOfByte));
		return makeRequestMessage(paramRecognitionParameters,
				SpeechService.MediaData.mediaData, mediaDataBuilder.build(),
				false);
	}

	public static SpeechService.RequestMessage makeClientReportRequest(
			RecognitionParameters paramRecognitionParameters,
			ClientReportProto.ClientReport paramClientReport) {
		SpeechService.ClientReportRequest.Builder localBuilder = SpeechService.ClientReportRequest
				.newBuilder();
		localBuilder.setClientReport(paramClientReport);
		return makeRequestMessage(paramRecognitionParameters,
				SpeechService.ClientReportRequest.clientReportRequest,
				localBuilder.build(), true);
	}

	private static GoogleSearchRequest.RequestAttributeProto makeRequestAttribute(
			String paramString1, String paramString2) {
		GoogleSearchRequest.RequestAttributeProto.Builder localBuilder = GoogleSearchRequest.RequestAttributeProto
				.newBuilder();
		localBuilder.setName(paramString1);
		localBuilder.setValue(paramString2);
		return localBuilder.build();
	}

	public static SpeechService.RequestMessage makeCancelRequest(
			RecognitionParameters paramRecognitionParameters) {
		return makeRequestMessage(paramRecognitionParameters,
				SpeechService.CancelRequest.cancelRequest,
				SpeechService.CancelRequest.newBuilder().build(), false);
	}

	private static void addSearchAttribute(
			GoogleSearchRequest.GoogleSearchRequestProto.Builder paramBuilder,
			String paramString1, String paramString2) {
		paramBuilder.addAttributes(makeRequestAttribute(paramString1,
				paramString2));
	}

	private static void setPersonalizationToken(
			RecognitionParameters paramRecognitionParameters,
			SpeechService.RecognizeRequest.Builder paramBuilder) {
		Log.d("protoutil", "skip personalizatoin set!");
	}
}
