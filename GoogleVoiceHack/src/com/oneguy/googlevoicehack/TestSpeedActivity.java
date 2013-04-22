package com.oneguy.googlevoicehack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.googlevoicehack.R;
import com.google.android.voicesearch.speechservice.ClientReportBuilder;
import com.google.android.voicesearch.speechservice.MicrophoneManager;
import com.google.android.voicesearch.speechservice.MicrophoneManagerImpl;
import com.google.android.voicesearch.speechservice.RecognitionController;
import com.google.android.voicesearch.speechservice.RecognitionControllerImpl;
import com.google.android.voicesearch.speechservice.ServerConnector;
import com.google.android.voicesearch.speechservice.ServerConnectorImpl;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protos.speech.service.PartialResult.PartialRecognitionResult;
import com.google.protos.speech.service.SpeechService.RecognitionHypothesis;
import com.google.protos.speech.service.SpeechService.RecognitionResult;

public class TestSpeedActivity extends Activity implements OnTouchListener,
		RecognitionListener, OnClickListener {
	private RecognitionController engine;
	private TextView info;
	private TextView state;
	private static final String TAG = "TestSpeedActivity";
	private ScrollView mInfoScroll;
	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		long l1 = 100;
		InjectUtil.logLong(l1);
		ServerConnector connector = new ServerConnectorImpl();
		MicrophoneManager manager = new MicrophoneManagerImpl(this);
		engine = new RecognitionControllerImpl(this, connector, manager);
		setContentView(R.layout.test_speed);
		Button speak = (Button) findViewById(R.id.speak);
		speak.setOnClickListener(this);

		Button stop = (Button) findViewById(R.id.stop);
		stop.setOnClickListener(this);
		info = (TextView) findViewById(R.id.info);
		state = (TextView) findViewById(R.id.state);

		mInfoScroll = (ScrollView) findViewById(R.id.infoScroll);
		mHandler = new Handler();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction();
		if (action == MotionEvent.ACTION_DOWN) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					state.setText("wait init...");
				}
			});
			engine.onStartListening(new Intent(), this);
			return false;
		}
		if (action == MotionEvent.ACTION_UP) {
			engine.onStopListening(TestSpeedActivity.this);
			return false;
		}
		return false;
	}

	@Override
	public void onBeginningOfSpeech() {
		Log.d(TAG, "onBeginningOfSpeech");
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				state.setText("speech begin");
				state.invalidate();
			}
		});
	}

	@Override
	public void onBufferReceived(byte[] buffer) {
//		Log.d(TAG, "onBufferReceived");
	}

	@Override
	public void onEndOfSpeech() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				state.setText("speech end");
				state.invalidate();
			}
		});
	}

	@Override
	public void onError(final int error) {
		Log.d(TAG, "onError");
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				String text = "Error :" + InjectUtil.getErrorCodeStr(error)
						+ ", please retry later!";
				addText(info, text);
				info.invalidate();
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mInfoScroll.fullScroll(View.FOCUS_DOWN);
						mInfoScroll.invalidate();
					}
				});
			}
		});
	}

	@Override
	public void onEvent(int eventType, Bundle params) {
		Log.d(TAG, "onEvent:" + eventType);
	}

	@Override
	public void onPartialResults(Bundle partialResults) {
		final String resultStr = getPartialResult(partialResults,
				"partialResults");
		Log.d(TAG, resultStr);
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				addText(info, resultStr);
				info.invalidate();
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mInfoScroll.fullScroll(View.FOCUS_DOWN);
						mInfoScroll.invalidate();
					}
				});
			}
		});
	}

	@Override
	public void onReadyForSpeech(Bundle params) {
		Log.d(TAG, "onReadyForSpeech");
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				state.setText("please speak...");
			}
		});

	}

	@Override
	public void onResults(Bundle results) {
		Log.d(TAG, "onResults");
		final String resultStr = ClientReportBuilder.getInstance().toString()
				+ getResult(results, "results");
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				addText(info, resultStr);
				info.invalidate();
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mInfoScroll.fullScroll(View.FOCUS_DOWN);
						mInfoScroll.invalidate();
					}
				});
			}
		});

	}

	@Override
	public void onRmsChanged(final float rmsdB) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				state.setText("rms:"+rmsdB);
				state.invalidate();
			}
		});
	}

	private String getResult(Bundle results, String key) {
		byte[] data = results.getByteArray(key);
		try {
			RecognitionResult recognitionResult = RecognitionResult
					.parseFrom(data);

			int count = recognitionResult.getHypothesisCount();
			StringBuffer sb = new StringBuffer();
			sb.append("\n");
			sb.append("HYPOTHESIS:").append("\n");
			for (int i = 0; i < count; i++) {
				RecognitionHypothesis rh = recognitionResult.getHypothesis(i);
				sb.append(i + ":").append(rh.getSentence()).append("\n");
			}
			return sb.toString();
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		return "";
	}

	private void addText(TextView view, String text) {
		if (view == null) {
			return;
		}
		view.setText(view.getText() + "\n" + text);
	}

	private void addTextAtFront(TextView view, String text) {
		if (view == null) {
			return;
		}
		view.setText(text + "\n" + view.getText());
	}

	private void setTextIfEmpty(TextView view, String text) {
		if (view == null) {
			return;
		}
		if (TextUtils.isEmpty(view.getText())) {
			view.setText(text);
		} else {
			addText(view, text);
		}
	}

	private String getPartialResult(Bundle results, String key) {
		byte[] data = results.getByteArray(key);
		try {
			PartialRecognitionResult partialResult = PartialRecognitionResult
					.parseFrom(data);
			StringBuilder sb = new StringBuilder();
			if (partialResult.getPartCount() > 0) {
				sb.append(partialResult.getPart(0).getTranscript());
			}
			return sb.toString();
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.speak:
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					state.setText("wait init...");
					info.setText("");
				}
			});
			engine.onStartListening(new Intent(), this);
			break;
		case R.id.stop:
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					state.setText("wait result...");
				}
			});
			engine.onStopListening(TestSpeedActivity.this);
			break;
		}

	}
}
