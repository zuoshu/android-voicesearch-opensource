package com.example.googlevoicehack;

import android.app.Activity;

public class MainActivity extends Activity {
//
//	ReadRunnable readRunnable;
//	List<byte[]> mSound;
//	String TAG = "googlevoicehack";
//	Object mutex = new Object();
//	MyAudioTrack mAudioTrack;
//	AudioBuffer mAudioBuffer;
//	AudioBuffer2 mAudioBuffer2;
//	InputStream mis = null;
//	EndpointerInputStream eis = null;
//	InputStream amrIs = null;
//	RecognitionControllerImplTest recognize;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//		// readRunnable = new ReadRunnable();
//		// new Thread(readRunnable).start();
//		// mAudioTrack = new MyAudioTrack(8000, AudioFormat.CHANNEL_OUT_MONO,
//		// AudioFormat.ENCODING_PCM_16BIT);
//		Button start = (Button) findViewById(R.id.startRec);
//		start.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				readRunnable.startRecord();
//			}
//		});
//
//		Button stop = (Button) findViewById(R.id.stopRec);
//		stop.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				readRunnable.stopRecord();
//			}
//		});
//
//		Button play = (Button) findViewById(R.id.play);
//		play.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				String mFileName = getFilesDir() + "/google-voice.pcm";
//				// String mFileName =
//				// "/data/data/com.google.android.voicesearch/files/mic-1365396625831.pcm";
//				play(mFileName);
//			}
//		});
//
//		Button stopPlay = (Button) findViewById(R.id.stopPlay);
//		stopPlay.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				stopPlay();
//			}
//		});
//
//		Button testConnect = (Button) findViewById(R.id.testServerConnectorImpl);
//		testConnect.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				new Thread(new Runnable() {
//					@Override
//					public void run() {
//						recognize = new RecognitionControllerImplTest();
//						InjectUtil.timerInit();
//						recognize.testStart(MainActivity.this);
//					}
//				}).start();
//			}
//		});
//
//		final EndpointerInputStream.Listener listener = new Listener() {
//
//			@Override
//			public void onRmsChanged(float paramFloat) {
//
//			}
//
//			@Override
//			public void onReadyForSpeech(float paramFloat1, float paramFloat2) {
//				// TODO Auto-generated method stub
//
//			}
//
//			@Override
//			public void onEndOfSpeech() {
//				// TODO Auto-generated method stub
//
//			}
//
//			@Override
//			public void onBufferReceived(byte[] paramArrayOfByte) {
//				// TODO Auto-generated method stub
//
//			}
//
//			@Override
//			public void onBeginningOfSpeech() {
//				// TODO Auto-generated method stub
//
//			}
//		};
//		Button testAudio = (Button) findViewById(R.id.testAudio);
//
//		testAudio.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				MicrophoneManagerImpl mic = new MicrophoneManagerImpl(
//						MainActivity.this);
//				ByteArrayOutputStream mRawAudio = new ByteArrayOutputStream();
//				try {
//					mAudioBuffer = mic.setupMicrophone(listener, 2, true,
//							mRawAudio);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				for (int i = 0; i < 10; i++) {
//					ByteBuffer buffer;
//					try {
//						buffer = mAudioBuffer.getByteBuffer();
//						Log.d(TAG, "data:" + buffer.capacity());
//					} catch (AudioException e1) {
//						e1.printStackTrace();
//					} catch (InterruptedException e1) {
//						e1.printStackTrace();
//					}
//				}
//			}
//		});
//
//		Button startAudioBuffer = (Button) findViewById(R.id.startAudioBuffer);
//		startAudioBuffer.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				new Thread(new Runnable() {
//					@Override
//					public void run() {
//						recognize = new RecognitionControllerImplTest();
//						InjectUtil.timerInit();
//						recognize.testStart(MainActivity.this);
//					}
//				}).start();
//			}
//		});
//
//		Button stopAudioBuffer = (Button) findViewById(R.id.stopAudioBuffer);
//		stopAudioBuffer.setOnClickListener(new OnClickListener() {
//
//			public void onClick(View v) {
//				new Thread(new Runnable() {
//					@Override
//					public void run() {
//						recognize.testStop();
//					}
//				}).start();
//			}
//		});
//		mSound = new LinkedList<byte[]>();
//	}
//
//	MyAudioListener audioListener = new MyAudioListener();
//
//	class MyAudioListener implements AudioListener {
//
//		@Override
//		public void onAudioData(ByteBuffer buffer) {
//			Log.d("readAudioBuffer", "length:" + buffer.limit());
//		}
//
//	}
//
//	protected void initAudioBuffer() {
//		if (mAudioBuffer2 == null) {
//			try {
//				mis = new MicrophoneInputStream(8000, 1024 * 10);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			eis = new EndpointerInputStream(mis, 2, 3000, 1000, 1000);
//			amrIs = Utils.createAmrInputStream(eis);
//			mAudioBuffer2 = new AudioBuffer2(448, amrIs, false);
//			mAudioBuffer2.setAudioListener(audioListener);
//		} else {
//			mAudioBuffer2.restart();
//		}
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
//
//	class ReadRunnable implements Runnable {
//
//		volatile boolean record = false;
//		boolean running = true;
//		InputStream mis = null;
//		EndpointerInputStream eis = null;
//		InputStream amrIs = null;
//		byte[] buffer = new byte[1024];
//		AudioBuffer ab = null;
//
//		public ReadRunnable() {
//			try {
//				mis = new MicrophoneInputStream(8000, 1024 * 10);
//				eis = new EndpointerInputStream(mis, 2, 3000, 1000, 1000);
//				amrIs = Utils.createAmrInputStream(eis);
//				// ab = new AudioBuffer(448, amrIs, true);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//
//		@Override
//		public void run() {
//			while (running) {
//				while (record) {
//					int read;
//					try {
//						// ByteBuffer bb = amrIs.;
//						read = amrIs.read(buffer);
//						// Log.d(TAG, "read:" + bb.limit());
//						copySound(buffer, read);
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//		}
//
//		public void startRecord() {
//			record = true;
//		}
//
//		public void stopRecord() {
//			record = false;
//			eis.requestClose();
//			final String mFileName = getFilesDir() + "/google-voice.pcm";
//			saveFile(mFileName);
//			Log.d(TAG, "save ok");
//			// new Thread(new Runnable() {
//			//
//			// @Override
//			// public void run() {
//			// getResponseFromServer(mFileName);
//			// }
//			// }).start();
//			// Log.d(TAG, "response ok");
//		}
//
//		public void stopRun() {
//			running = false;
//		}
//	}
//
//	void copySound(byte[] data, int read) {
//		if (read < 0) {
//			return;
//		}
//		byte[] cache = new byte[read];
//		System.arraycopy(data, 0, cache, 0, read);
//		mSound.add(cache);
//	}
//
//	private void saveFile(String mFileName) {
//		try {
//			RandomAccessFile randomAccessWriter = new RandomAccessFile(
//					mFileName, "rw");
//			randomAccessWriter.setLength(0);
//			int length = 0;
//			for (byte[] data : mSound) {
//				randomAccessWriter.write(data);
//				length += data.length;
//			}
//			randomAccessWriter.close();
//			Log.d(TAG, "wirte file done,length:" + length);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		if (BuildConfig.DEBUG) {
//			Log.d(TAG, "encoder->stop");
//		}
//	}
//
//	private static final int TIMEOUT = 15 * 1000;
//	private static final int TRY_STOP_TIME = 100;
//	private static final String ONE_SHOT_SPEECH_API_URL = "http://www.google.com/speech-api/v1/recognize?lang=en-us&maxresults=1&xjerr=1";
//	private static final String METHOD_POST = "POST";
//
//	private void getResponseFromServer(String flacFileName) {
//		HttpParams httpParameters = new BasicHttpParams();
//		HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
//		HttpConnectionParams.setSoTimeout(httpParameters, 5000);
//		HttpClient httpclient = new DefaultHttpClient(httpParameters);
//		HttpPost postMethod = new HttpPost(ONE_SHOT_SPEECH_API_URL);
//		File inputFile = new File(flacFileName);
//		FileEntity entity = new FileEntity(inputFile, "binary/octet-stream");
//		postMethod.setEntity(entity);
//		postMethod.setHeader("Content-Type", "audio/L16; rate=8000");
//		try {
//			HttpResponse response = httpclient.execute(postMethod);
//			int statusCode = response.getStatusLine().getStatusCode();
//			String reponseStr = EntityUtils.toString(response.getEntity());
//			if (statusCode == 200) {
//				Log.d(TAG, reponseStr);
//			} else {
//			}
//		} catch (ClientProtocolException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//	private void play(String fileName) {
//		FileInputStream fis = null;
//		try {
//			fis = new FileInputStream(new File(fileName));
//			byte[] sound = new byte[fis.available()];
//			int read = fis.read(sound);
//			if (read > 0) {
//				mAudioTrack.init();
//				mAudioTrack.playAudioTrack(sound, 0, read);
//				Log.d(TAG, "play:" + read);
//			}
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (fis != null) {
//				try {
//					fis.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}
//
//	protected void stopPlay() {
//		mAudioTrack.release();
//	}
}
