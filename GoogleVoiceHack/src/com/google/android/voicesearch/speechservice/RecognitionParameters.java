package com.google.android.voicesearch.speechservice;

import java.util.List;

import android.os.Bundle;
import android.text.TextUtils;

public class RecognitionParameters {
	public static final String API_APPLICATION_ID = "intent-speech-api";
	public static final String VOICE_SEARCH_APPLICATION_ID = "voice-search";
	private boolean mAlternatesEnabled = true;
	private boolean mApiMode = false;
	private int mAudioEncoding;
	private int mAudioSampleRate;
	private boolean mCarDock = false;
	private String mClientApplicationId = null;
	private String mLanguageModel = null;
	private String mLanguageOverride;
	private int mMaxResults = -1;
	private String mMultislotActionSelectedSlot = null;
	private int mMultislotActionType = -1;
	private int mNetworkType = -1;
	private float mNoiseLevel = -1.0F;
	private boolean mPartialTranscriptsEnabled = false;
	private Bundle mRecognitionContextBundle = null;
	private int mRequestId = -1;
	private String mSessionId = null;
	private float mSnr = -1.0F;
	private String mSpeechServerUrlOverride;
	private boolean mUseContactAuth = false;
	private boolean mUseLocation = false;
	// hard-coding
	private String mCookie;
	private String mClientId;
	private String mDefaultLanguage;
	private String mLocale;
	private String mMofeHttpUrl;
	private String mMofeProtoUrl;
	private String mRevClientId;
	private int mSafeSearchSetting;
	private String mAuthToken;
	private String mUserAgent;
	private List<Integer> mSupportedActionInterpretations;
	private long mExperimentHash;
	private String language;

	public void clearSession() {
		this.mSessionId = null;
		this.mRequestId = -1;
	}

	public String getApplicationId() {
		if (this.mApiMode)
			return "intent-speech-api";
		return "voice-search";
	}

	public int getAudioEncoding() {
		return this.mAudioEncoding;
	}

	public int getAudioSampleRate() {
		return this.mAudioSampleRate;
	}

	public String getClientApplicationId() {
		return this.mClientApplicationId;
	}

	// TODO update this hard coding
	public String getClientId() {
		return mClientId;
		// return Utils.buildClientId(this.mContext);
	}

	public String[] getContactAuthTokens() {
		return null;
	}

	// TODO update this hard coding
	public String getCookie() {
		// long l = this.mCookieStore.getCookie();
		// if (l == -1L)
		// return null;
		// return String.valueOf(218498135489741l);
		return mCookie;
	}

	public String getDefaultLanguage() {
		return mDefaultLanguage;
	}

	// TODO update this hard coding
	public long getExperimentHash() {
		return mExperimentHash;
	}

	// TODO update this hard coding
	public String getGeoPosition() {
		return null;
	}

	// TODO update this hard coding
	public String getLanguage() {
		return TextUtils.isEmpty(language)?"en-US":language;
	}

	public String getLanguageModel() {
		return this.mLanguageModel;
	}

	// TODO update this hard coding
	public String getLocale() {
		return "en-US";
	}

	public int getMaxResults() {
		return this.mMaxResults;
	}

	public String getMofeHttpUrl() {
		return mMofeHttpUrl;
	}

	public String getMofeProtoUrl() {
		return mMofeProtoUrl;
	}

	public String getMultislotActionSelectedSlot() {
		return this.mMultislotActionSelectedSlot;
	}

	public int getMultislotActionType() {
		return this.mMultislotActionType;
	}

	public int getNetworkType() {
		return this.mNetworkType;
	}

	public float getNoiseLevel() {
		return this.mNoiseLevel;
	}

	public Bundle getRecognitionContextBundle() {
		return this.mRecognitionContextBundle;
	}

	public int getRequestId() {
		return this.mRequestId;
	}

	// TODO update this hard coding
	public String getRevClientId() {
		return mRevClientId;
	}

	// TODO update this hard coding
	public int getSafeSearchSetting() {
		return mSafeSearchSetting;
	}

	public String getSessionId() {
		return this.mSessionId;
	}

	public float getSnr() {
		return this.mSnr;
	}

	// TODO update this hard coding
	public String getSpeechPersonalizationServiceAuthToken() {
		return mAuthToken;
	}

	// TODO update this hard coding
	public String getSpeechServerUrl() {
		return mSpeechServerUrlOverride;
	}

	// TODO update this hard coding
	public List<Integer> getSupportedActionInterpretations() {
		// if (!this.mGservicesHelper.getAdvancedFeaturesEnabled())
		// return VoiceActionsFactory.getBasicActions();
		// return VoiceActionsFactory.getAllActionTypes(this.mContext);
		return mSupportedActionInterpretations;
	}

	// TODO update this hard coding
	public String getUserAgent() {
		return mUserAgent;
	}

	public boolean hasNoiseEstmation() {
		return this.mSnr > 0.0F;
	}

	public void incrementRequestId() {
		this.mRequestId = (1 + this.mRequestId);
	}

	public boolean isAlternatesEnabled() {
		return this.mAlternatesEnabled;
	}

	public boolean isApiMode() {
		return this.mApiMode;
	}

	public boolean isCarDock() {
		return this.mCarDock;
	}

	public boolean isPartialTranscriptsEnabled() {
		return this.mPartialTranscriptsEnabled;
	}

	// TODO update this hard coding
	public boolean isPersonalizationEnabled() {
		return false;
	}

	// TODO update this hard coding
	public boolean isPersonalizationSet() {
		return false;
	}

	// TODO update this hard coding
	public boolean isProfanityFilterEnabled() {
		return false;
		// return this.mContext.getSharedPreferences("VoiceSearchPreferences",
		// 0)
		// .getBoolean("profanityFilter", true);
	}

	public void setAlternatesEnabled(boolean paramBoolean) {
		this.mAlternatesEnabled = paramBoolean;
	}

	public void setApiMode(boolean paramBoolean) {
		this.mApiMode = paramBoolean;
	}

	public void setAudioEncoding(int paramInt) {
		this.mAudioEncoding = paramInt;
	}

	public void setAudioSampleRate(int paramInt) {
		this.mAudioSampleRate = paramInt;
	}

	public void setCarDock(boolean paramBoolean) {
		this.mCarDock = paramBoolean;
	}

	public void setClientApplicationId(String paramString) {
		this.mClientApplicationId = paramString;
	}

	// TODO hard coding
	public void setClientId(String clientId) {
		mClientId = clientId;
	}

	public void setDefaultLanguage(String language) {
		mDefaultLanguage = language;
	}

	// TODO hard coding
	public void setExperimentHash(long hash){
		mExperimentHash = hash;
	}
	public void setLanguageModel(String paramString) {
		this.mLanguageModel = paramString;
	}
	
	public void setLanguage(String l){
		language = l;
	}

	public void setLanguageOverride(String paramString) {
		this.mLanguageOverride = paramString;
	}

	// TODO hard coding
	public void setLocale(String locale) {
		mLocale = locale;
	}

	public void setMaxResults(int paramInt) {
		this.mMaxResults = paramInt;
	}

	// TODO hard coding
	public void setMofeHttpUrl(String mofeHttpUrl) {
		mMofeHttpUrl = mofeHttpUrl;
	}

	// TODO hard coding
	public void setMofeProtoUrl(String mofeProtoUrl) {
		mMofeProtoUrl = mofeProtoUrl;
	}

	public void setMultislotActionSelectedSlot(String paramString) {
		this.mMultislotActionSelectedSlot = paramString;
	}

	public void setMultislotActionType(int paramInt) {
		this.mMultislotActionType = paramInt;
	}

	public void setNetworkType(int paramInt) {
		this.mNetworkType = paramInt;
	}

	public void setNoiseLevel(float paramFloat) {
		this.mNoiseLevel = paramFloat;
	}

	public void setPartialTranscriptsEnabled(boolean paramBoolean) {
		this.mPartialTranscriptsEnabled = paramBoolean;
	}

	public void setRecognitionContextBundle(Bundle paramBundle) {
		this.mRecognitionContextBundle = paramBundle;
	}

	// TODO hard coding
	public void setRevClientId(String id) {
		mRevClientId = id;
	}

	// TODO hard coding
	public void setSafeSearchSetting(int setting) {
		mSafeSearchSetting = setting;
	}

	public void setSessionId(String paramString) {
		this.mSessionId = paramString;
	}

	public void setSnr(float paramFloat) {
		this.mSnr = paramFloat;
	}

	// TODO hard coding
	public void setSpeechPersonaliztionServiceAuthToken(String authToken) {
		mAuthToken = authToken;
	}

	public void setSpeechServerUrlOverride(String paramString) {
		this.mSpeechServerUrlOverride = paramString;
	}

	// TODO hard coding
	public void setSupportedActionInterpretations(List<Integer> action) {
		mSupportedActionInterpretations = action;
	}

	// TODO hard coding
	public void setUserAgent(String agent) {
		mUserAgent = agent;
	}

	public void setUseContactAuth(boolean paramBoolean) {
		this.mUseContactAuth = paramBoolean;
	}

	public void setUseLocation(boolean paramBoolean) {
		this.mUseLocation = paramBoolean;
	}

	public void setCookie(String cookie) {
		mCookie = cookie;
	}

	public String toString() {
		StringBuilder localStringBuilder = new StringBuilder(
				"RecognitionParameters{");
		localStringBuilder.append("session=").append(this.mSessionId)
				.append(",");
		localStringBuilder.append("request=").append(this.mRequestId)
				.append("}");
		return localStringBuilder.toString();
	}
}