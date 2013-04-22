package com.google.android.voicesearch;

import android.content.Context;

public  interface GservicesHelper
{
  public  boolean getAdvancedFeaturesEnabled();

  public  String getAlternateBackoffLanguages();

  public  String getAudioEncoding3G();

  public  String getAudioEncodingWifi();

  public  int getConnectionRetries();

  public  int getEndResultTimout();

  public  long getEndpointerCompleteSilenceMillis();

  public  long getEndpointerPossiblyCompleteSilenceMillis();

  public  int getExtraTotalResultTimeout();

  public  int getHelpHintBubbleMaxAppStartCount();

  public  int getHelpHintBubbleMaxHelpCount();

  public  String getHelpVideoUrl();

  public  int getHintDisplayThreshold();

  public  String getLanguageOverride();

  public  String getMobilePrivacyUrl();

  public  String getMofeHttpUrl();

  public  String getMofeProtoUrl();

  public  int getNavigationEnabled();

  public  int getNetworkTimeout();

  public  String getPersonalizationCountries();

  public  String getPersonalizationDashboardUrl();

  public  String getPersonalizationMoreInfoUrl();

  public  String getSearchUrlPrefix();

  public  int getSpeechTimeout();

  public  String getSsfeUrl();

  public  String getStringResourceOverride(String paramString);

  public  String getSupportedActions();

  public  String getSupportedLanguages();

  public  int getTcpAttempts();

  public  float getUtteranceLengthTimoutFactor();

  public  String getWebViewBaseUrl();

  public  String getWebViewWhitelist();

  public  void handleGservicesChange(Context paramContext);
}