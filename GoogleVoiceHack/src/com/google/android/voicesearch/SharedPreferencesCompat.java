package com.google.android.voicesearch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.SharedPreferences;

public class SharedPreferencesCompat {
	private static final Method sApplyMethod = findApplyMethod();

	public static void apply(SharedPreferences.Editor paramEditor) {
		if (sApplyMethod != null) {
			try {
				sApplyMethod.invoke(paramEditor, new Object[0]);
				return;
			} catch (IllegalAccessException localIllegalAccessException) {
				paramEditor.commit();
				return;
			} catch (InvocationTargetException localInvocationTargetException) {
			}
		}
	}

	private static Method findApplyMethod() {
		try {
			Method localMethod = SharedPreferences.Editor.class.getMethod(
					"apply", new Class[0]);
			return localMethod;
		} catch (NoSuchMethodException localNoSuchMethodException) {
		}
		return null;
	}
}