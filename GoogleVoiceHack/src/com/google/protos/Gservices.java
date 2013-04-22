package com.google.protos;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class Gservices {
	public static final String CHANGED_ACTION = "com.google.gservices.intent.action.GSERVICES_CHANGED";
	public static final Uri CONTENT_PREFIX_URI;
	public static final Uri CONTENT_URI = Uri
			.parse("content://com.google.android.gsf.gservices");
	private static final Pattern FALSE_PATTERN;
	public static final String OVERRIDE_ACTION = "com.google.gservices.intent.action.GSERVICES_OVERRIDE";
	public static final String TAG = "Gservices";
	private static final Pattern TRUE_PATTERN;
	private static HashMap<String, String> sCache;
	private static ContentResolver sResolver;
	private static Object sVersionToken;

	static {
		CONTENT_PREFIX_URI = Uri
				.parse("content://com.google.android.gsf.gservices/prefix");
		TRUE_PATTERN = Pattern.compile("^(1|true|t|on|yes|y)$", 2);
		FALSE_PATTERN = Pattern.compile("^(0|false|f|off|no|n)$", 2);
	}

	static class LockCacheThread extends Thread {
		ContentResolver contentResolver;

		public LockCacheThread(ContentResolver cr) {
			contentResolver = cr;
		}

		@Override
		public void run() {
			Looper.prepare();
			contentResolver.registerContentObserver(Gservices.CONTENT_URI,
					true, new ContentObserver(new Handler(Looper.myLooper())) {
						public void onChange(boolean paramBoolean) {
							Gservices.sCache.clear();
							// Gservices.access$102(new Object());
							return;
						}
					});
			Looper.loop();
		}
	}

	private static void ensureCacheInitializedLocked(
			ContentResolver paramContentResolver) {
		if (sCache != null)
			return;
		sCache = new HashMap();
		sVersionToken = new Object();
		sResolver = paramContentResolver;
		new LockCacheThread(paramContentResolver).start();
	}

	public static boolean getBoolean(ContentResolver paramContentResolver,
			String paramString, boolean paramBoolean) {
		String str = getString(paramContentResolver, paramString);
		if ((str == null) || (str.equals("")))
			return paramBoolean;
		if (TRUE_PATTERN.matcher(str).matches())
			return true;
		if (FALSE_PATTERN.matcher(str).matches())
			return false;
		Log.w("Gservices", "attempt to read gservices key " + paramString
				+ " (value \"" + str + "\") as boolean");
		return paramBoolean;
	}

	public static int getInt(ContentResolver paramContentResolver,
			String paramString, int paramInt) {
		String str = getString(paramContentResolver, paramString);
		if (str != null)
			;
		try {
			int i = Integer.parseInt(str);
			return i;
		} catch (NumberFormatException localNumberFormatException) {
		}
		return paramInt;
	}

	public static long getLong(ContentResolver paramContentResolver,
			String paramString, long paramLong) {
		String str = getString(paramContentResolver, paramString);
		if (str != null)
			;
		try {
			long l = Long.parseLong(str);
			return l;
		} catch (NumberFormatException localNumberFormatException) {
		}
		return paramLong;
	}

	public static String getString(ContentResolver paramContentResolver,
			String paramString) {
		return getString(paramContentResolver, paramString, null);
	}

	// TODO
	public static String getString(ContentResolver paramContentResolver,
			String paramString1, String paramString2) {
		return paramString2;
		// // monitorenter;
		// while (true)
		// {
		// Object localObject2;
		// Cursor localCursor;
		// try
		// {
		// ensureCacheInitializedLocked(paramContentResolver);
		// localObject2 = sVersionToken;
		// if (sCache.containsKey(paramString1))
		// {
		// String str2 = (String)sCache.get(paramString1);
		// if (str2 == null)
		// break label170;
		// String str3 = str2;
		// return str3;
		// }
		// // monitorexit;
		// localCursor = sResolver.query(CONTENT_URI, null, null, new String[] {
		// paramString1 }, null);
		// return paramString2;
		// }
		// finally
		// {
		// // monitorexit;
		// }
		// while (true)
		// {
		// try
		// {
		// localCursor.moveToFirst();
		// str1 = localCursor.getString(1);
		// try
		// {
		// if (localObject2 == sVersionToken)
		// sCache.put(paramString1, str1);
		// // monitorexit;
		// if (str1 == null)
		// break label164;
		// return str1;
		// }
		// finally
		// {
		// // monitorexit;
		// }
		// }
		// finally
		// {
		// localCursor.close();
		// }
		// label164: String str1 = paramString2;
		// }
		// label170: String str3 = paramString2;
		// }
	}

	public static Map<String, String> getStringsByPrefix(
			ContentResolver paramContentResolver, String[] paramArrayOfString) {
		Cursor localCursor = paramContentResolver.query(CONTENT_PREFIX_URI,
				null, null, paramArrayOfString, null);
		TreeMap localTreeMap = new TreeMap();
		if (localCursor == null) {
			return localTreeMap;
		}
		try {
			if (localCursor.moveToNext()) {
				localTreeMap.put(localCursor.getString(0),
						localCursor.getString(1));
			}
		} finally {
			localCursor.close();
		}
		return localTreeMap;
	}

	public static Object getVersionToken(ContentResolver paramContentResolver) {
		ensureCacheInitializedLocked(paramContentResolver);
		Object localObject2 = sVersionToken;
		return localObject2;
	}
}