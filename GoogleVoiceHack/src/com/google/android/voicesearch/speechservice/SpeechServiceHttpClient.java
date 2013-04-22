package com.google.android.voicesearch.speechservice;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.params.HttpConnectionParams;

import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.util.Log;

import com.google.protobuf.ExtensionRegistryLite;
import com.google.protos.speech.service.SpeechService;
import com.google.protos.speech.service.SpeechService.RequestMessage;
import com.google.protos.speech.service.SpeechService.ResponseMessage;
import com.google.protos.wireless.voicesearch.VoiceSearch;

public class SpeechServiceHttpClient {
	private static final boolean DBG = false;
	private int mActiveClients = 1;
	private AndroidHttpClient mHttpClient = null;

	private ExtensionRegistryLite mExtensionRegistry;
	private static final int TIMEOUT = 5 * 1000;
	private static final String TAG = "SpeechServiceHttpClient";

	public SpeechServiceHttpClient() {
		mHttpClient = AndroidHttpClient.newInstance("xiaomi");
		HttpConnectionParams
				.setSoTimeout(this.mHttpClient.getParams(), TIMEOUT);
		this.mExtensionRegistry = ExtensionRegistryLite.newInstance();
		SpeechService.registerAllExtensions(this.mExtensionRegistry);
		VoiceSearch.registerAllExtensions(this.mExtensionRegistry);
	}

	public ResponseMessage post(RecognitionParameters mParams,
			RequestMessage requestMessage) throws ConnectionException {
		ResponseMessage responseMessage = null;
		InputStream inputStream = null;
		try {
			inputStream = post(mParams.getSpeechServerUrl(),
					requestMessage.toByteArray());
			responseMessage = SpeechService.ResponseMessage.parseFrom(
					inputStream, this.mExtensionRegistry);
		} catch (IOException e) {
			Log.e("SpeechServiceHttpClient",
					"Exception occured while posting a message over HTTP", e);
			throw new ConnectionException(e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return responseMessage;
	}

	private InputStream post(String speechServerUrl, byte[] byteArray) {
		try {
			HttpPost localHttpPost = new HttpPost(speechServerUrl);
			ByteArrayEntity localByteArrayEntity = new ByteArrayEntity(
					byteArray);
			localByteArrayEntity.setContentType("application/octet-stream");
			localHttpPost.setEntity(localByteArrayEntity);
			HttpResponse localHttpResponse = this.mHttpClient
					.execute(localHttpPost);
			if (localHttpResponse.getStatusLine().getStatusCode() == 200) {
				return localHttpResponse.getEntity().getContent();
			} else {
				Log.e(TAG, "post error!");
			}
		} catch (IOException e) {
			Log.e(TAG, "post IO error!");
			e.printStackTrace();
		}
		return null;
	}

	public List<SpeechService.ResponseMessage> post(
			RecognitionParameters paramRecognitionParameters,
			List<SpeechService.RequestMessage> paramList)
			throws ConnectionException {
		if (paramList.isEmpty())
			throw new IllegalArgumentException("No messages");
		DataInputStream localDataInputStream;
		ArrayList localArrayList;
		try {
			ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
			DataOutputStream localDataOutputStream = new DataOutputStream(
					localByteArrayOutputStream);
			for (int i = 0; i < paramList.size(); ++i) {
				byte[] arrayOfByte = ((SpeechService.RequestMessage) paramList
						.get(i)).toByteArray();
				localDataOutputStream.writeShort(arrayOfByte.length);
				localDataOutputStream.write(arrayOfByte);
			}
			localDataInputStream = new DataInputStream(
					post(addMultiProto(paramRecognitionParameters
							.getSpeechServerUrl()), localByteArrayOutputStream
							.toByteArray()));
			SpeechService.ResponseMessage localResponseMessage;
			try {
				localArrayList = new ArrayList();
				localResponseMessage = readMessage(localDataInputStream);
				if (localResponseMessage == null) {
					localDataInputStream.close();
				}
			} finally {
				localDataInputStream.close();
			}
		} catch (IOException localIOException) {
			throw new ConnectionException("Error encoding message",
					localIOException);
		}
		return localArrayList;
	}

	private String addMultiProto(String paramString) {
		return Uri.parse(paramString).buildUpon()
				.appendQueryParameter("multiproto", "true").toString();
	}

	private SpeechService.ResponseMessage readMessage(
			DataInputStream paramDataInputStream) throws IOException {
		try {
			int i = paramDataInputStream.readUnsignedShort();
			byte[] arrayOfByte = new byte[i];
			paramDataInputStream.readFully(arrayOfByte);
			return SpeechService.ResponseMessage.parseFrom(arrayOfByte,
					this.mExtensionRegistry);
		} catch (EOFException localEOFException) {
		}
		return null;
	}

	public void close() {
		synchronized (this) {
			int i = this.mActiveClients - 1;
			this.mActiveClients = i;
			if (i > 0)
				return;
			if (this.mHttpClient == null)
				return;
			Log.i("SpeechServiceHttpClient", "Closing the HTTP client.");
			this.mHttpClient.close();
			this.mHttpClient.getConnectionManager().shutdown();
			return;
		}
	}
}
