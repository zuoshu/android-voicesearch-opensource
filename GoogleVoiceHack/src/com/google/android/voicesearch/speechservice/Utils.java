package com.google.android.voicesearch.speechservice;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.voicesearch.endpointer.EndpointerInputStream;
import com.google.android.voicesearch.endpointer.MicrophoneInputStream;
import com.google.protos.speech.common.Alternates;
import com.google.protos.speech.service.SpeechService.Encoding;

public class Utils {
	// TODO we need to pretend to be google voice
	public static final String buildClientId(Context paramContext) {
		String packageName = paramContext.getPackageName();
		String versionName;
		try {
			versionName = paramContext.getPackageManager().getPackageInfo(
					packageName, 0).versionName;
			StringBuilder localStringBuilder = new StringBuilder("VS ");
			localStringBuilder.append(versionName);
			localStringBuilder.append(" os=[Android ");
			localStringBuilder.append(Build.VERSION.RELEASE);
			localStringBuilder.append(" ");
			localStringBuilder.append(Build.MODEL.replace(' ', '/'));
			localStringBuilder.append("]");
			return localStringBuilder.toString();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static final String buildClientIdPretend(Context paramContext) {
		String packageName = paramContext.getPackageName();
		String versionName;
		try {
			versionName = paramContext.getPackageManager().getPackageInfo(
					packageName, 0).versionName;
			StringBuilder localStringBuilder = new StringBuilder("VS ");
			localStringBuilder.append(versionName);
			localStringBuilder.append(" os=[Android ");
			localStringBuilder.append(Build.VERSION.RELEASE);
			localStringBuilder.append(" ");
			localStringBuilder.append(Build.MODEL.replace(' ', '/'));
			localStringBuilder.append("]");
			return localStringBuilder.toString();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static InputStream createAmrInputStream(InputStream paramInputStream) {
		try {
			InputStream localInputStream = (InputStream) getAmrInputStreamClass()
					.getConstructor(new Class[] { InputStream.class })
					.newInstance(new Object[] { paramInputStream });
			return localInputStream;
		} catch (ClassNotFoundException localClassNotFoundException) {
			Log.e("Utils", "Exception while instantiating AmrInputStream: "
					+ localClassNotFoundException);
			throw new RuntimeException(
					"Exception while instantiating AmrInputStream",
					localClassNotFoundException);
		} catch (SecurityException localSecurityException) {
			Log.e("Utils", "Exception while instantiating AmrInputStream: "
					+ localSecurityException);
			throw new RuntimeException(
					"Exception while instantiating AmrInputStream",
					localSecurityException);
		} catch (NoSuchMethodException localNoSuchMethodException) {
			Log.e("Utils", "Exception while instantiating AmrInputStream: "
					+ localNoSuchMethodException);
			throw new RuntimeException(
					"Exception while instantiating AmrInputStream",
					localNoSuchMethodException);
		} catch (IllegalArgumentException localIllegalArgumentException) {
			Log.e("Utils", "Exception while instantiating AmrInputStream: "
					+ localIllegalArgumentException);
			throw new RuntimeException(
					"Exception while instantiating AmrInputStream",
					localIllegalArgumentException);
		} catch (InstantiationException localInstantiationException) {
			Log.e("Utils", "Exception while instantiating AmrInputStream: "
					+ localInstantiationException);
			throw new RuntimeException(
					"Exception while instantiating AmrInputStream",
					localInstantiationException);
		} catch (IllegalAccessException localIllegalAccessException) {
			Log.e("Utils", "Exception while instantiating AmrInputStream: "
					+ localIllegalAccessException);
			throw new RuntimeException(
					"Exception while instantiating AmrInputStream",
					localIllegalAccessException);
		} catch (InvocationTargetException localInvocationTargetException) {
			Log.e("Utils", "Exception while instantiating AmrInputStream: "
					+ localInvocationTargetException);
			throw new RuntimeException(
					"Exception while instantiating AmrInputStream",
					localInvocationTargetException);
		}
	}

	public static Bundle convertAlternatesProtoToBundle(
			Alternates.RecognitionClientAlternates alternates) {
		Bundle bundle = new Bundle();
		bundle.putInt("max_span_length", alternates.getMaxSpanLength());
		Bundle spans = new Bundle();
		for (int i = 0; i < alternates.getSpanCount(); ++i) {
			Alternates.AlternateSpan span = alternates.getSpan(i);
			Bundle alternatesWrap = new Bundle();
			int start = span.getStart();
			int length = span.getLength();
			alternatesWrap.putInt("start", start);
			alternatesWrap.putInt("length", length);
			if (span.hasConfidence())
				alternatesWrap.putFloat("confidence", span.getConfidence());
			int alterCount = span.getAlternatesCount();
			Parcelable[] arrayOfParcelable = new Parcelable[alterCount];
			for (int j = 0; j < alterCount; ++j) {
				Alternates.Alternate localAlternate = span.getAlternates(j);
				Bundle alternateBundle = new Bundle();
				alternateBundle.putString("text", localAlternate.getText());
				if (localAlternate.hasConfidence())
					alternateBundle.putFloat("confidence",
							localAlternate.getConfidence());
				arrayOfParcelable[j] = alternateBundle;
			}
			if (alterCount > 0) {
				alternatesWrap.putParcelableArray("alternates",
						arrayOfParcelable);
			}
			spans.putParcelable(start + ":" + length, alternatesWrap);
		}
		bundle.putBundle("spans", spans);
		return bundle;
	}

	private static Class<? extends InputStream> getAmrInputStreamClass()
			throws ClassNotFoundException {
		return (Class<? extends InputStream>) Class
				.forName("android.media.AmrInputStream");
	}

	public static void loadClasses() {
		MicrophoneInputStream.class.getName();
		EndpointerInputStream.class.getName();
		try {
			getAmrInputStreamClass().getName();
			AudioBuffer.class.getName();
			return;
		} catch (ClassNotFoundException localClassNotFoundException) {
			Log.e("Utils",
					"AmrInputStream class not found. AMR will not be supported");
		}
	}

	public static InputStream getEncodingInputStream(
			InputStream paramInputStream, int paramInt) {
		return createAmrInputStream(paramInputStream);
	}

	public static int getAudioPacketSize(int encoding) {
		switch (encoding) {
		case Encoding.AMR_NB_VALUE:
			return 448;
		default:
			throw new IllegalArgumentException("unsupported encoding: "
					+ encoding);
		}
	}
}
