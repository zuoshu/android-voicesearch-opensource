package com.oneguy.googlevoicehack;

import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

import com.google.android.voicesearch.endpointer.FileLoopInputStream;
import com.google.android.voicesearch.speechservice.Utils;

public class FileConvert {
	private static final String TAG = "FileConvert";
	static{
		Utils.loadClasses();
	}

	public static void pcmToArm(String filename) {
		try {
			long start = System.currentTimeMillis();
			FileLoopInputStream flis = new FileLoopInputStream(filename, 8000);
			InputStream arm = Utils.createAmrInputStream(flis);
			int read = 0;
			int size = flis.available();
			byte[] buffer = new byte[1024];
			while ((read = arm.read(buffer)) != -1) {
				Log.d(TAG, "read:" + read);
			}
			arm.close();
			flis.close();
			Log.d(TAG,
					"convert size:" + size + " time:"
							+ (System.currentTimeMillis() - start));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
