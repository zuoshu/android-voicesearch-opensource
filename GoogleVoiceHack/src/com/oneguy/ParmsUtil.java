package com.oneguy;

import java.util.ArrayList;
import java.util.List;

import com.google.android.voicesearch.speechservice.RecognitionParameters;

public class ParmsUtil {
	public static RecognitionParameters makeTcpSessionParms(
			RecognitionParameters input) {
		input.clearSession();
		input.setSessionId(null);
		input.setCookie("0");
		// AMR_NB
		input.setAudioEncoding(4);
		input.setAudioSampleRate(8000);
		input.setClientApplicationId(null);
		input.setClientId("VS 2.1.4 os=[Android 4.0.4 MI/1S]");
		input.setLanguage("en-us");
		input.setExperimentHash(-1646703526);
		input.setLanguageModel(null);
		input.setMaxResults(5);
		input.setMofeHttpUrl(null);
		input.setMultislotActionSelectedSlot(null);
		input.setMultislotActionType(-1);
		// wifi
		input.setNetworkType(2);
		input.setNoiseLevel(-1);
		input.incrementRequestId();
		input.setRevClientId("unknown");
		input.setSafeSearchSetting(1);
		input.setSnr(-1);
		input.setSpeechPersonaliztionServiceAuthToken("DQAAAMMAAABpDX4BQpkwp79Wz4aUWcncmgRs9wsb6tJIsZ5wUBTF3EXC0C64oeCPYvEUbIh0sTS0iQC9aygPg2FcnOImkuJLia3reYtGECuAiuC-OlM3wS4XcCsXJmymllqtJYVwwmTGIKFhPjI7O0NboPIYATXfuijfY1uqdlTtz0oI54g-4IIoYOkKXMzc1EePMcgjIHz6Mktk2KTOtWKh7fnvIQSg2Yiliiv3NNPdB2x_HVGUNRxseOKedC2cUtRvnCtZmqe5dRPfZZVdNYFV31IUoZ7e");
		input.setSpeechServerUrlOverride("http://www.google.com/m/voice-search");
		input.setUserAgent("Mozilla/5.0 (Linux; U; Android 4.0.4; zh-cn; mione_plus) AppleWebKit/525.10+ (KHTML, like Gecko) Version/3.0.4 Mobile Safari/523.12.2");
		List<Integer> action = new ArrayList<Integer>();
		action.add(1);
		action.add(10);
		action.add(12);
		action.add(3);
		action.add(4);
		action.add(2);
		action.add(11);
		action.add(-1);
		action.add(8);
		action.add(13);
		action.add(14);
		action.add(6);
		action.add(15);
		action.add(17);
		action.add(18);
		input.setSupportedActionInterpretations(action);
		return input;
	}

	public static RecognitionParameters makeTcpSessionParms() {
		RecognitionParameters parms = new RecognitionParameters();
		return makeTcpSessionParms(parms);
	}

}
