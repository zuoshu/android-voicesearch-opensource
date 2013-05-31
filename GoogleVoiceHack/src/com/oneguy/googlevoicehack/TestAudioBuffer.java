package com.oneguy.googlevoicehack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.googlevoicehack.R;
import com.google.android.voicesearch.endpointer.EndpointerInputStream;
import com.google.android.voicesearch.endpointer.EndpointerInputStream.Listener;
import com.google.android.voicesearch.speechservice.AudioBuffer;
import com.google.android.voicesearch.speechservice.AudioBuffer.AudioException;
import com.google.android.voicesearch.speechservice.MicrophoneManager;
import com.google.android.voicesearch.speechservice.MicrophoneManagerImpl;

public class TestAudioBuffer extends Activity {
	private static final String TAG = "TestAudioBuffer";

	private AudioBuffer mAudioBuffer;
	private MicrophoneManager mMicrophoneManager;
	private EndpointerInputStream.Listener mEndpointerListener = new Listener() {

		@Override
		public void onRmsChanged(float paramFloat) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReadyForSpeech(float noiseLevel, float snr) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onEndOfSpeech() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onBufferReceived(byte[] paramArrayOfByte) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onBeginningOfSpeech() {
			// TODO Auto-generated method stub

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_audio_buffer);
		Button start = (Button) findViewById(R.id.testAudio);
		start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						testAudio();
					}
				}).start();
			}
		});
	}

	private void testAudio() {
		mMicrophoneManager = new MicrophoneManagerImpl(this);
		ByteArrayOutputStream rawAudio = new ByteArrayOutputStream();
		try {
			mAudioBuffer = mMicrophoneManager.setupMicrophone(mEndpointerListener,
					2, false, rawAudio);
			Thread.sleep(1000);
			boolean speechend = false;
			while(!speechend){
				ByteBuffer buffer = mAudioBuffer.getByteBuffer();
				Log.d(TAG, "buffer:"+buffer.remaining());
				if(buffer.remaining() == 0){
					speechend = true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (AudioException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
