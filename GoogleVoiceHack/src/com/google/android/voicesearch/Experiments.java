package com.google.android.voicesearch;

import android.content.Context;
import android.util.Log;

import com.google.protos.speech.service.ClientParametersProto.ClientParameters;

public class Experiments {

	private static final String TAG = "Experiments";
	public static long getExperimentHash(Context mContext) {
		Log.w(TAG,"getExperimentHash TODO");
		return 0;
	}

	public static void updateExperimentHash(Context mContext, long remoteHash) {
		Log.w(TAG,"updateExperimentHash TODO");
		//TODO
	}

	public static void setExperimentParameters(Context mContext,
			ClientParameters clientParameters) {
		Log.w(TAG,"setExperimentParameters TODO");
		// TODO
	}

}
