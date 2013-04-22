package com.google.android.voicesearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

public class LanguagePrefManagerImpl implements LanguagePrefManager {
	static final int DEFAULT_CHANGE_REASON_DEVICE_LOCALE_CHANGED = 1;
	static final int DEFAULT_CHANGE_REASON_NEW_ALTERNATE_BACKOFF_LANGUAGE = 2;
	static final int DEFAULT_CHANGE_REASON_NEW_SUPPORTED_LANGUAGE = 0;
	static final int DEFAULT_CHANGE_REASON_NONE = -1;
	static final String DEFAULT_LANGUAGE_CODE = "default";
	static final String LAST_RESORT_DEFAULT_LANGUAGE = "en-001";
	static final String PREF_KEY_ACKNOWLEDGED_UNSUPPORTED_DEVICE_LANGUAGE = "acknowledged_unsupported_device_language";
	static final String PREF_KEY_ACTUAL_LANGUAGE_SETTING = "actual_language_setting";
	static final String PREF_KEY_ALTERNATE_BACKOFF_LANGUAGES = "alternate_backoff_languages";
	static final String PREF_KEY_DEFAULT_LANGUAGE_CHANGED = "default_language_changed";
	static final String PREF_KEY_DEVICE_LANGUAGE_NEWLY_SUPPORTED = "device_language_newly_supported";
	static final String PREF_KEY_LAST_KNOWN_DEVICE_LANGUAGE = "last_known_device_language";
	static final String PREF_KEY_SUPPORTED_LANGUAGE_CODES = "supported_languages";
	private static final String TAG = "LanguagePrefManager";
	private final Context mContext;
	private GservicesHelper mGservicesHelper;
	private HashMap<String, String> mLanguageToNameMap;
	private SharedPreferences mSharedPrefs;
	private HashMap<String, List<String>> mStringToListCache;
	private HashMap<String, int[]> mSupportedActions;

	public LanguagePrefManagerImpl(Context paramContext,
			GservicesHelper paramGservicesHelper) {
		this.mContext = paramContext;
		this.mGservicesHelper = paramGservicesHelper;
		this.mSharedPrefs = this.mContext.getSharedPreferences(
				"VoiceSearchPreferences", 0);
		this.mLanguageToNameMap = buildLanguageCodeToNameMap();
		this.mStringToListCache = new HashMap();
		normalizeLanguageSetting("language");
		normalizeLanguageSetting("actual_language_setting");
	}

	private HashMap<String, String> buildLanguageCodeToNameMap() {
		HashMap localHashMap = new HashMap();
		String[] arrayOfString1 = this.mContext.getResources().getStringArray(
				2131361792);
		String[] arrayOfString2 = this.mContext.getResources().getStringArray(
				2131361793);
		if (arrayOfString1.length != arrayOfString2.length)
			throw new RuntimeException(
					"count of languageCodes and languageNames does not match");
		for (int i = 0; i < arrayOfString1.length; ++i)
			localHashMap.put(arrayOfString1[i], arrayOfString2[i]);
		return localHashMap;
	}

	public static String getHlParameter() {
		Locale localLocale = Locale.getDefault();
		String str1 = localLocale.getLanguage().toLowerCase();
		String str2 = localLocale.getCountry();
		if ((str1.equals("zh")) || (str1.equals("pt")))
			return str1 + "-" + str2;
		return str1;
	}

	private String getStoredAlternateBackoffLanguageCodes() {
		String str = this.mSharedPrefs.getString("alternate_backoff_languages",
				null);
		if (str == null) {
			str = this.mGservicesHelper.getAlternateBackoffLanguages();
			SharedPreferences.Editor localEditor = this.mSharedPrefs.edit();
			localEditor.putString("alternate_backoff_languages", str);
			SharedPreferencesCompat.apply(localEditor);
		}
		return str;
	}

	private String getStoredSupportedLanguageCodes() {
		String str = this.mSharedPrefs.getString("supported_languages", null);
		if (str == null) {
			str = this.mGservicesHelper.getSupportedLanguages();
			SharedPreferences.Editor localEditor = this.mSharedPrefs.edit();
			localEditor.putString("supported_languages", str);
			SharedPreferencesCompat.apply(localEditor);
		}
		return str;
	}

	private HashMap<String, String> languageMappingStringAsMap(
			String paramString) {
		HashMap localHashMap = new HashMap();
		Iterator localIterator = languagesStringAsList(paramString).iterator();
		while (localIterator.hasNext()) {
			String[] arrayOfString = ((String) localIterator.next()).split(":");
			if (arrayOfString.length != 2)
				throw new IllegalArgumentException(
						"malformed language mapping string");
			localHashMap.put(arrayOfString[0], arrayOfString[1]);
		}
		return localHashMap;
	}

	private List<String> languagesStringAsList(String languages) {
		if (languages == null) {
			return new ArrayList<String>();
		}
		List<String> languageList = Arrays.asList(languages.split("\\s+"));
		mStringToListCache.put(languages, languageList);
		return languageList;
	}

	private void normalizeLanguageSetting(String language) {
		String setting = this.mSharedPrefs.getString(language, null);
		if (setting == null) {
			return;
		}

		if (("default".equals(setting))
				|| (this.mSharedPrefs.getString("supported_languages", "")
						.contains(setting))) {
			SharedPreferences.Editor localEditor = this.mSharedPrefs.edit();
			localEditor.putString(language, "default");
			SharedPreferencesCompat.apply(localEditor);
		}
	}

	public void acknowledgeUnsupportedDeviceLanguage() {
		SharedPreferences.Editor localEditor = this.mSharedPrefs.edit();
		localEditor
				.putBoolean("acknowledged_unsupported_device_language", true);
		SharedPreferencesCompat.apply(localEditor);
	}

	public boolean deviceLanguageIsSupported() {
		return languagesStringAsList(getStoredSupportedLanguageCodes())
				.contains(getDeviceLanguageCode());
	}

	String getDefaultLanguageCodeChoice(String paramString) {
		if (languagesStringAsList(getStoredSupportedLanguageCodes()).contains(
				paramString))
			return paramString;
		String str = (String) languageMappingStringAsMap(
				getStoredAlternateBackoffLanguageCodes()).get(paramString);
		if (str != null)
			return str;
		return "en-001";
	}

	public String getDeviceDefaultLanguageName() {
		return getLanguageName(getDefaultLanguageCodeChoice(getDeviceLanguageCode()));
	}

	public String getDeviceLanguageCode() {
		Locale localLocale = Locale.getDefault();
		String str1 = localLocale.getLanguage();
		String str2 = localLocale.getCountry();
		if ((str1 != null) && (str2 != null))
			return str1.toLowerCase() + "-" + str2.toUpperCase();
		return "en-001";
	}

	public String[] getLanguageChoices() {
		List localList = languagesStringAsList(getStoredSupportedLanguageCodes());
		String[] arrayOfString = new String[1 + localList.size()];
		arrayOfString[0] = "default";
		for (int i = 0; i < localList.size(); ++i)
			arrayOfString[(i + 1)] = ((String) localList.get(i));
		return arrayOfString;
	}

	public String getLanguageName(String language) {
		String deviceLanguageCode = getDeviceLanguageCode();
		String name = (String) this.mLanguageToNameMap.get(language);
		if (name == null) {
			if (!"default".equals(language)) {
				Log.w("LanguagePrefManager",
						"no display name available for supported voice search language: "
								+ language);
				return language;
			}
		}
		String str3 = getDefaultLanguageCodeChoice(deviceLanguageCode);
		String str4 = (String) this.mLanguageToNameMap.get(str3);
		if (str4 == null)
			str4 = str3;
		name = this.mContext.getResources().getString(2131298108,
				new Object[] { str4 });
		return name;

	}

	public String[] getLanguageNames(String[] paramArrayOfString) {
		String[] arrayOfString = new String[paramArrayOfString.length];
		for (int i = 0; i < paramArrayOfString.length; ++i)
			arrayOfString[i] = getLanguageName(paramArrayOfString[i]);
		return arrayOfString;
	}

	public String getLanguageSetting() {
		String str = this.mGservicesHelper.getLanguageOverride();
		if (str == null) {
			return str;
		}
		str = this.mSharedPrefs.getString("actual_language_setting", null);
		return updateLanguageSetting(null);
	}

	public HashMap<String, int[]> getSupportedActions() {
		if (this.mSupportedActions == null)
			updateSupportedActions(null);
		return this.mSupportedActions;
	}

	public List<String> getSupportedLanguages() {
		return languagesStringAsList(getStoredSupportedLanguageCodes());
	}

	public void handleDeviceLanguageChange() {
		SharedPreferences.Editor localEditor = this.mSharedPrefs.edit();
		String str1 = this.mSharedPrefs.getString("last_known_device_language",
				null);
		String str2 = getDeviceLanguageCode();
		if ((str1 != null)
				&& (!getDefaultLanguageCodeChoice(str2).equals(
						getDefaultLanguageCodeChoice(str1)))) {
			localEditor.putInt("default_language_changed", 1);
			updateLanguageSetting(localEditor, null);
		}
		localEditor.putString("last_known_device_language", str2);
		localEditor.putBoolean("acknowledged_unsupported_device_language",
				false);
		SharedPreferencesCompat.apply(localEditor);
		this.mLanguageToNameMap = buildLanguageCodeToNameMap();
	}

	public void handleGservicesChange(Context paramContext) {
		SharedPreferences localSharedPreferences = paramContext
				.getSharedPreferences("VoiceSearchPreferences", 0);
		String str1 = localSharedPreferences.getString("supported_languages",
				null);
		String str2 = this.mGservicesHelper.getSupportedLanguages();
		if (!str2.equals(str1))
			updateSupportedLanguages(str1, str2);
		String str3 = localSharedPreferences.getString(
				"alternate_backoff_languages", null);
		String str4 = this.mGservicesHelper.getAlternateBackoffLanguages();
		if (!str4.equals(str3))
			updateAlternateBackoffLanguages(str3, str4);
		updateSupportedActions(this.mGservicesHelper.getSupportedActions());
	}

	public boolean hasAcknowledgedUnsupportedDeviceLanguage() {
		return this.mSharedPrefs.getBoolean(
				"acknowledged_unsupported_device_language", false);
	}

	public boolean languageSettingHasVoiceActions() {
		String str = getLanguageSetting();
		HashMap localHashMap = getSupportedActions();
		if (!localHashMap.containsKey(str))
			return false;
		return (localHashMap.get(str) != null)
				&& (((int[]) localHashMap.get(str)).length != 0);
	}

	public boolean languageSettingIsDefault() {
		return this.mSharedPrefs.getString("language", "default").equals(
				"default");
	}

	public void resetDefaultLanguageChange() {
		SharedPreferences.Editor localEditor = this.mSharedPrefs.edit();
		localEditor.putInt("default_language_changed", -1);
		SharedPreferencesCompat.apply(localEditor);
	}

	void updateAlternateBackoffLanguages(String paramString1,
			String paramString2) {
		HashMap localHashMap1 = languageMappingStringAsMap(paramString1);
		HashMap localHashMap2 = languageMappingStringAsMap(paramString2);
		SharedPreferences.Editor localEditor = this.mSharedPrefs.edit();
		String str1 = getDeviceLanguageCode();
		if (!languagesStringAsList(getStoredSupportedLanguageCodes()).contains(
				str1)) {
			String str2 = (String) localHashMap1.get(str1);
			if (!TextUtils.equals((String) localHashMap2.get(str1), str2)) {
				localEditor.putInt("default_language_changed", 2);
				updateLanguageSetting(localEditor, null);
			}
		}
		localEditor.putString("alternate_backoff_languages", paramString2);
		SharedPreferencesCompat.apply(localEditor);
	}

	String updateLanguageSetting(SharedPreferences.Editor paramEditor,
			String paramString) {
		paramEditor.putString("last_known_device_language",
				getDeviceLanguageCode());
		if (paramString == null)
			;
		for (String str = this.mSharedPrefs.getString("language", null);; str = paramString) {
			if (str == null)
				str = "default";
			if ("default".equals(str))
				str = getDefaultLanguageCodeChoice(getDeviceLanguageCode());
			paramEditor.putString("actual_language_setting", str);
			return str;
		}
	}

	public String updateLanguageSetting(String paramString) {
		SharedPreferences.Editor localEditor = this.mSharedPrefs.edit();
		String str = updateLanguageSetting(localEditor, paramString);
		SharedPreferencesCompat.apply(localEditor);
		return str;
	}

	void updateSupportedActions(String paramString) {
		if (paramString == null)
			;
		for (String str = this.mGservicesHelper.getSupportedActions();; str = paramString) {
			this.mSupportedActions = new HashMap();
			String[] arrayOfString1 = str.split("\\s+");
			int i = arrayOfString1.length;
			int j = 0;
			while (true) {
				String[] arrayOfString2 = new String[] {};
				String[] arrayOfString3 = new String[] {};
				int[] arrayOfInt = new int[] {};
				int k = 0;
				if (j < i) {
					arrayOfString2 = arrayOfString1[j].split(":");
					if (arrayOfString2.length == 2) {
						arrayOfString3 = arrayOfString2[1].split(",");
						arrayOfInt = new int[arrayOfString3.length];
						k = 0;
					}
				}
				try {
					if (k < arrayOfString3.length) {
						arrayOfInt[k] = Integer.parseInt(arrayOfString3[k]);
						++k;
					}
					this.mSupportedActions.put(arrayOfString2[0], arrayOfInt);
					++j;
				} catch (NumberFormatException localNumberFormatException) {
					Log.e("LanguagePrefManager",
							"problem in supported actions list",
							localNumberFormatException);
					++j;
					return;
				}
			}
		}
	}

	void updateSupportedLanguages(String paramString1, String paramString2) {
		List localList1 = languagesStringAsList(paramString1);
		List localList2 = languagesStringAsList(paramString2);
		SharedPreferences.Editor localEditor = this.mSharedPrefs.edit();
		localEditor.putString("supported_languages", paramString2);
		SharedPreferencesCompat.apply(localEditor);
		String str = getDeviceLanguageCode();
		if ((localList1.isEmpty()) || (localList1.contains(str))
				|| (!localList2.contains(str)))
			return;
		localEditor.putInt("default_language_changed", 0);
		SharedPreferencesCompat.apply(localEditor);
		updateLanguageSetting(localEditor, null);
		SharedPreferencesCompat.apply(localEditor);
	}
}