package com.google.android.voicesearch;

import java.util.HashMap;
import java.util.List;

import android.content.Context;

public interface LanguagePrefManager {
	public void acknowledgeUnsupportedDeviceLanguage();

	public boolean deviceLanguageIsSupported();

	public String getDeviceDefaultLanguageName();

	public String getDeviceLanguageCode();

	public String[] getLanguageChoices();

	public String getLanguageName(String paramString);

	public String[] getLanguageNames(String[] paramArrayOfString);

	public String getLanguageSetting();

	public HashMap<String, int[]> getSupportedActions();

	public List<String> getSupportedLanguages();

	public void handleDeviceLanguageChange();

	public void handleGservicesChange(Context paramContext);

	public boolean hasAcknowledgedUnsupportedDeviceLanguage();

	public boolean languageSettingHasVoiceActions();

	public boolean languageSettingIsDefault();

	public void resetDefaultLanguageChange();

	public String updateLanguageSetting(String paramString);
}