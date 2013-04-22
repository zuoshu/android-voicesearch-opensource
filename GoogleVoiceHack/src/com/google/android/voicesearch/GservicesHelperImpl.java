package com.google.android.voicesearch;

import java.util.Map;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.Log;

import com.google.protos.Gservices;

public class GservicesHelperImpl implements GservicesHelper {
	private static final boolean DEFAULT_ADVANCED_FEATURES_ENABLED = false;
	private static final String DEFAULT_ALTERNATE_BACKOFF_LANGUAGES = "zh-CN:cmn-Hans-CN zh-TW:cmn-Hant-TW zh-HK:yue-Hant-HK zh-SG:cmn-Hans-CN zh:cmn-Hans-CN ja:ja-JP de-CH:de-DE de-AT:de-DE de-LI:de-DE de:de-DE es-AR:es-ES es-BO:es-ES es-CL:es-ES es-CR:es-ES es-CO:es-ES es-DO:es-ES es-EC:es-ES es-GT:es-ES es-HN:es-ES es-NI:es-ES es-MX:es-ES es-PA:es-ES es-PE:es-ES es-PR:es-ES es-PY:es-ES es-SV:es-ES es-US:es-ES es-UY:es-ES es-VE:es-ES es:es-ES fr-BE:fr-FR fr-CH:fr-FR fr:fr-FR fr-CA:fr-FR it-CH:it-IT it:it-IT ko:ko-KR pl:pl-PL cs:cs-CZ ru:ru-RU tr:tr-TR pt-PT:pt-BR pt:pt-BR pt-AO:pt-BR nl-BE:nl-NL";
	private static final int DEFAULT_CONNECTION_RETRIES = 1;
	private static final String DEFAULT_ENCODING_THREE_G = "AMR_NB";
	private static final String DEFAULT_ENCODING_WIFI = "AMR_NB";
	private static final long DEFAULT_ENDPOINTER_COMPLETE_SILENCE_MS = 750L;
	private static final long DEFAULT_ENDPOINTER_POSSIBLY_COMPLETE_SILENCE_MS = -1L;
	private static final int DEFAULT_END_RESULT_TIMEOUT_MS = 13000;
	private static final int DEFAULT_EXTRA_TOTAL_RESULT_TIMEOUT_MS = 2000;
	private static final int DEFAULT_HELP_HINT_BUBBLE_MAX_APP_START_COUNT = 15;
	private static final int DEFAULT_HELP_HINT_BUBBLE_MAX_HELP_COUNT = 3;
	private static final String DEFAULT_HELP_VIDEO_URL = "";
	private static final int DEFAULT_HINT_DISPLAY_THRESHOLD = 2;
	private static final String DEFAULT_LANGUAGE_OVERRIDE;
	private static final String DEFAULT_MOBILE_PRIVACY_URL = "http://www.google.com/mobile/privacy.html";
	private static final String DEFAULT_MOFE_HTTP_URL = null;
	private static final String DEFAULT_MOFE_PROTO_URL = null;
	private static final int DEFAULT_NAVIGATION_ENABLED = 0;
	private static final int DEFAULT_NETWORK_CONNECTION_TIMEOUT_MILLIS = 10000;
	private static final String DEFAULT_PERSONALIZATION_COUNTRIES = "";
	private static final String DEFAULT_PERSONALIZATION_DASHBOARD_URL = "https://www.google.com/dashboard/";
	private static final String DEFAULT_PERSONALIZATION_MORE_INFO_URL = "http://www.google.com/support/mobile/bin/answer.py?answer=186263";
	private static final String DEFAULT_SEARCH_URL_PREFIX = "http://www.google.com/m/search?v=UTF-8&source=mobilesearchapp-vs&channel=iss&uipref=6";
	private static final int DEFAULT_SPEECH_TIMEOUT_MILLIS = 10000;
	private static final String DEFAULT_SSFE_URL = "http://www.google.com/m/voice-search";
	private static final String DEFAULT_SUPPORTED_ACTIONS = "en-US:14,18,2,12,13,15,4,17,1,6,3 en-CA:2,3,4,1 en-GB:2,3,4,1 en-AU:2,3,4,1 en-NZ:2,3,4,1 en-IN:2,3,4,1 en-001:2,3,4,1 cmn-Hans-CN: cmn-Hant-TW: cmn-Hans-SG: ja-JP: de-DE: es-ES: fr-FR: it-IT: ko-KR: pl-PL: cs-CZ: ru-RU: tr-TR: pt-BR: nl-NL: af-ZA: en-ZA: zu-ZA: yue-Hant-HK:";
	private static final String DEFAULT_SUPPORTED_LANGUAGES = "af-ZA cmn-Hans-CN cmn-Hans-HK cmn-Hant-TW yue-Hant-HK cs-CZ nl-NL en-AU en-CA en-IN en-NZ en-ZA en-GB en-US en-001 fr-FR de-DE zu-ZA it-IT ja-JP ko-KR pl-PL pt-BR ru-RU es-ES tr-TR";
	private static final int DEFAULT_TCP_ATTEMPTS = 1;
	private static final float DEFAULT_UTTERANCE_LENGTH_TIMEOUT_FACTOR = 0.5F;
	private static final String DEFAULT_WEB_VIEW_BASE_URL = "http://www.google.com/";
	private static final String DEFAULT_WEB_VIEW_WHITELIST = "http://www.google.com/m/ http://www.google.com/m? http://www.google.com/accounts https://www.google.com/accounts http://m.google.com/app/updates https://m.google.com/app/updates about:blank";
	private static final Pattern FALSE_PATTERN;
	private static final String GSERVICES_KEY_ADVANCED_FEATURES_ENABLED = "voice_search:advanced_features_enabled";
	private static final String GSERVICES_KEY_ALTERNATE_BACKOFF_LANGUAGES = "voice_search:alternate_backoff_languages";
	private static final String GSERVICES_KEY_CONNECTION_RETRIES = "voice_search:connection_tries";
	private static final String GSERVICES_KEY_ENCODING_THREE_G = "voice_search:encoding_three_g";
	private static final String GSERVICES_KEY_ENCODING_WIFI = "voice_search:encoding_wifi";
	private static final String GSERVICES_KEY_ENDPOINTER_COMPLETE_SILENCE_MS = "voice_search:endpointer_complete_silence_ms";
	private static final String GSERVICES_KEY_ENDPOINTER_POSSIBLY_COMPLETE_SILENCE_MS = "voice_search:endpointer_possibly_complete_silence_ms";
	private static final String GSERVICES_KEY_END_RESULT_TIMEOUT_MS = "voice_search:end_result_timeout_ms";
	private static final String GSERVICES_KEY_EXTRA_TOTAL_RESULT_TIMEOUT_MS = "voice_search:extra_total_result_timeout_ms";
	private static final String GSERVICES_KEY_HELP_HINT_BUBBLE_MAX_APP_START_COUNT = "voice_search:help_hint_bubble_max_app_start_count";
	private static final String GSERVICES_KEY_HELP_HINT_BUBBLE_MAX_HELP_COUNT = "voice_search:help_hint_bubble_max_help_count";
	private static final String GSERVICES_KEY_HELP_VIDEO_URL = "voice_search:help_video_url";
	private static final String GSERVICES_KEY_HINT_DISPLAY_THRESHOLD = "voice_search:hint_display_threshold";
	private static final String GSERVICES_KEY_LANGUAGE_OVERRIDE = "voice_search:language_override";
	private static final String GSERVICES_KEY_MOBILE_PRIVACY_URL = "voice_search:mobile_privacy_url";
	private static final String GSERVICES_KEY_MOFE_HTTP_URL = "voice_search:mofe_http_url";
	private static final String GSERVICES_KEY_MOFE_PROTO_URL = "voice_search:mofe_proto_url";
	private static final String GSERVICES_KEY_NAVIGATION_ENABLED = "maps_enable_navigation";
	private static final String GSERVICES_KEY_NETWORK_TIMEOUT_MS = "voice_search:network_timeout_ms";
	private static final String GSERVICES_KEY_PERSONALIZATION_COUNTRIES = "voice_search:personalization_v2_countries";
	private static final String GSERVICES_KEY_PERSONALIZATION_DASHBOARD_URL = "voice_search:personalization_dashboard_url";
	private static final String GSERVICES_KEY_PERSONALIZATION_MORE_INFO_URL = "voice_search:personalization_more_info_url";
	private static final String GSERVICES_KEY_SEARCH_URL_PREFIX = "voice_search:search_url_prefix";
	private static final String GSERVICES_KEY_SPEECH_TIMEOUT_MS = "voice_search:speech_timeout_ms";
	private static final String GSERVICES_KEY_SSFE_URL = "voice_search:url";
	private static final String GSERVICES_KEY_SUPPORTED_ACTIONS = "voice_search:supported_actions_new_numbering_scheme";
	private static final String GSERVICES_KEY_SUPPORTED_LANGUAGES = "voice_search:supported_languages";
	private static final String GSERVICES_KEY_TCP_ATTEMPTS = "voice_search:tcp_attempts";
	private static final String GSERVICES_KEY_UTTERANCE_LENGTH_TIMEOUT_FACTOR = "voice_search:utterance_length_timeout_factor";
	private static final String GSERVICES_KEY_WEB_VIEW_BASE_URL = "voice_search:web_view_base_url";
	private static final String GSERVICES_KEY_WEB_VIEW_WHITELIST = "voice_search:web_view_whitelist";
	private static final String GSERVICES_STRING_OVERRIDE_KEY_PREFIX = "voice_search:";
	private static final String SEPARATOR = " ";
	private static final String TAG = "GservicesHelper";
	private static final Pattern TRUE_PATTERN;
	private Context mContext;
	protected Map<String, String> mValues;

	static {
		DEFAULT_LANGUAGE_OVERRIDE = null;
		TRUE_PATTERN = Pattern.compile("^(1|true|t|on|yes|y)$", 2);
		FALSE_PATTERN = Pattern.compile("^(0|false|f|off|no|n)$", 2);
	}

	public GservicesHelperImpl(Context paramContext) {
		this.mContext = paramContext;
		refreshGservicesValues();
	}

	private boolean getBoolean(String paramString, boolean paramBoolean) {
		String str = (String) this.mValues.get(paramString);
		if ((str == null) || (str.equals("")))
			return paramBoolean;
		if (TRUE_PATTERN.matcher(str).matches())
			return true;
		if (FALSE_PATTERN.matcher(str).matches())
			return false;
		Log.w("GservicesHelper", "attempt to read gservices key " + paramString
				+ " (value \"" + str + "\") as boolean");
		return paramBoolean;
	}

	private float getFloat(String paramString, float paramFloat) {
		String str = (String) this.mValues.get(paramString);
		if (str == null)
			return paramFloat;
		try {
			float f = Float.parseFloat(str);
			return f;
		} catch (NumberFormatException localNumberFormatException) {
			Log.e("GservicesHelper", "Gservices value is not a long",
					localNumberFormatException);
		}
		return paramFloat;
	}

	private int getInt(String paramString, int paramInt) {
		String str = (String) this.mValues.get(paramString);
		if (str == null)
			return paramInt;
		try {
			int i = Integer.parseInt(str);
			return i;
		} catch (NumberFormatException localNumberFormatException) {
			Log.e("GservicesHelper", "Gservices value is not a long",
					localNumberFormatException);
		}
		return paramInt;
	}

	private long getLong(String paramString, long paramLong) {
		String str = (String) this.mValues.get(paramString);
		if (str == null)
			return paramLong;
		try {
			long l = Long.parseLong(str);
			return l;
		} catch (NumberFormatException localNumberFormatException) {
			Log.e("GservicesHelper", "Gservices value is not a long",
					localNumberFormatException);
		}
		return paramLong;
	}

	private String getString(String paramString1, String paramString2) {
		String str = (String) this.mValues.get(paramString1);
		if (str == null)
			return paramString2;
		return str;
	}

	public boolean getAdvancedFeaturesEnabled() {
		return getBoolean("voice_search:advanced_features_enabled", false);
	}

	public String getAlternateBackoffLanguages() {
		return getString(
				"voice_search:alternate_backoff_languages",
				"zh-CN:cmn-Hans-CN zh-TW:cmn-Hant-TW zh-HK:yue-Hant-HK zh-SG:cmn-Hans-CN zh:cmn-Hans-CN ja:ja-JP de-CH:de-DE de-AT:de-DE de-LI:de-DE de:de-DE es-AR:es-ES es-BO:es-ES es-CL:es-ES es-CR:es-ES es-CO:es-ES es-DO:es-ES es-EC:es-ES es-GT:es-ES es-HN:es-ES es-NI:es-ES es-MX:es-ES es-PA:es-ES es-PE:es-ES es-PR:es-ES es-PY:es-ES es-SV:es-ES es-US:es-ES es-UY:es-ES es-VE:es-ES es:es-ES fr-BE:fr-FR fr-CH:fr-FR fr:fr-FR fr-CA:fr-FR it-CH:it-IT it:it-IT ko:ko-KR pl:pl-PL cs:cs-CZ ru:ru-RU tr:tr-TR pt-PT:pt-BR pt:pt-BR pt-AO:pt-BR nl-BE:nl-NL");
	}

	public String getAudioEncoding3G() {
		return getString("voice_search:encoding_three_g", "AMR_NB");
	}

	public String getAudioEncodingWifi() {
		return getString("voice_search:encoding_wifi", "AMR_NB");
	}

	public int getConnectionRetries() {
		return getInt("voice_search:connection_tries", 1);
	}

	public int getEndResultTimout() {
		return getInt("voice_search:end_result_timeout_ms", 13000);
	}

	public long getEndpointerCompleteSilenceMillis() {
		return getLong("voice_search:endpointer_complete_silence_ms", 750L);
	}

	public long getEndpointerPossiblyCompleteSilenceMillis() {
		return getLong("voice_search:endpointer_possibly_complete_silence_ms",
				-1L);
	}

	public int getExtraTotalResultTimeout() {
		return getInt("voice_search:extra_total_result_timeout_ms", 2000);
	}

	public int getHelpHintBubbleMaxAppStartCount() {
		return getInt("voice_search:help_hint_bubble_max_app_start_count", 15);
	}

	public int getHelpHintBubbleMaxHelpCount() {
		return getInt("voice_search:help_hint_bubble_max_help_count", 3);
	}

	public String getHelpVideoUrl() {
		return getString("voice_search:help_video_url", "");
	}

	public int getHintDisplayThreshold() {
		return getInt("voice_search:hint_display_threshold", 2);
	}

	public String getLanguageOverride() {
		return getString("voice_search:language_override",
				DEFAULT_LANGUAGE_OVERRIDE);
	}

	public String getMobilePrivacyUrl() {
		return getString("voice_search:mobile_privacy_url",
				"http://www.google.com/mobile/privacy.html");
	}

	public String getMofeHttpUrl() {
		return getString("voice_search:mofe_http_url", DEFAULT_MOFE_HTTP_URL);
	}

	public String getMofeProtoUrl() {
		return getString("voice_search:mofe_proto_url", DEFAULT_MOFE_PROTO_URL);
	}

	public int getNavigationEnabled() {
		return Gservices.getInt(this.mContext.getContentResolver(),
				"maps_enable_navigation", 0);
	}

	public int getNetworkTimeout() {
		return getInt("voice_search:network_timeout_ms", 10000);
	}

	public String getPersonalizationCountries() {
		return getString("voice_search:personalization_v2_countries", "");
	}

	public String getPersonalizationDashboardUrl() {
		return getString("voice_search:personalization_dashboard_url",
				"https://www.google.com/dashboard/");
	}

	public String getPersonalizationMoreInfoUrl() {
		return getString("voice_search:personalization_more_info_url",
				"http://www.google.com/support/mobile/bin/answer.py?answer=186263");
	}

	public String getSearchUrlPrefix() {
		return getString(
				"voice_search:search_url_prefix",
				"http://www.google.com/m/search?v=UTF-8&source=mobilesearchapp-vs&channel=iss&uipref=6");
	}

	public int getSpeechTimeout() {
		return getInt("voice_search:speech_timeout_ms", 10000);
	}

	public String getSsfeUrl() {
		return getString("voice_search:url",
				"http://www.google.com/m/voice-search");
	}

	public String getStringResourceOverride(String paramString) {
		return getString("voice_search:" + paramString, null);
	}

	public String getSupportedActions() {
		return getString(
				"voice_search:supported_actions_new_numbering_scheme",
				"en-US:14,18,2,12,13,15,4,17,1,6,3 en-CA:2,3,4,1 en-GB:2,3,4,1 en-AU:2,3,4,1 en-NZ:2,3,4,1 en-IN:2,3,4,1 en-001:2,3,4,1 cmn-Hans-CN: cmn-Hant-TW: cmn-Hans-SG: ja-JP: de-DE: es-ES: fr-FR: it-IT: ko-KR: pl-PL: cs-CZ: ru-RU: tr-TR: pt-BR: nl-NL: af-ZA: en-ZA: zu-ZA: yue-Hant-HK:");
	}

	public String getSupportedLanguages() {
		return getString(
				"voice_search:supported_languages",
				"af-ZA cmn-Hans-CN cmn-Hans-HK cmn-Hant-TW yue-Hant-HK cs-CZ nl-NL en-AU en-CA en-IN en-NZ en-ZA en-GB en-US en-001 fr-FR de-DE zu-ZA it-IT ja-JP ko-KR pl-PL pt-BR ru-RU es-ES tr-TR");
	}

	public int getTcpAttempts() {
		return getInt("voice_search:tcp_attempts", 1);
	}

	public float getUtteranceLengthTimoutFactor() {
		return getFloat("voice_search:utterance_length_timeout_factor", 0.5F);
	}

	public String getWebViewBaseUrl() {
		return getString("voice_search:web_view_base_url",
				"http://www.google.com/");
	}

	public String getWebViewWhitelist() {
		return getString(
				"voice_search:web_view_whitelist",
				"http://www.google.com/m/ http://www.google.com/m? http://www.google.com/accounts https://www.google.com/accounts http://m.google.com/app/updates https://m.google.com/app/updates about:blank");
	}

	public void handleGservicesChange(Context paramContext) {
		refreshGservicesValues();
	}

	protected void refreshGservicesValues() {
		this.mValues = Gservices.getStringsByPrefix(
				this.mContext.getContentResolver(),
				new String[] { "voice_search:" });
	}
}