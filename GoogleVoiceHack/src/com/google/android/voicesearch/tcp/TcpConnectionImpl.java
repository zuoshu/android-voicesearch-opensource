package com.google.android.voicesearch.tcp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.os.SystemClock;
import android.util.Log;

import com.cyberobject.inject.InjectUtil;
import com.example.googlevoicehack.BuildConfig;
import com.google.android.voicesearch.speechservice.ConnectionCallback;
import com.google.android.voicesearch.speechservice.ConnectionException;
import com.google.android.voicesearch.watchdog.TimeoutWatchdog;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protos.speech.service.SpeechService;
import com.google.protos.speech.service.SpeechService.ResponseMessage;
import com.google.protos.wireless.voicesearch.VoiceSearch;

public class TcpConnectionImpl implements Runnable {
	private static final boolean DBG = false;
	private static final int KEEP_ALIVE_TIMEOUT = 120000;
	private static final int MAX_PACKET = 65535;
	private static final int STUN_TIMEOUT_MILLIS = 3000;
	private static final String TAG = TcpConnectionImpl.class.getSimpleName();
	private ConnectionCallback mCallback;
	private ExtensionRegistryLite mExtensionRegistry;
	private final DataInputStream mInput;
	private volatile boolean mIsRunning = false;
	private final DataOutputStream mOutput;
	private final Socket mSocket;
	private final String mStunId;
	private Thread mThread;
	private TimeoutWatchdog mTimeoutWatchdog;
	private final CountDownLatch writableLatch;

	public TcpConnectionImpl(String ip, int port, String stunId, int timeout)
			throws ConnectionException {
		if (stunId == null)
			throw new NullPointerException("stunId");
		try {
			this.mStunId = stunId;
			this.mSocket = new Socket();
			this.mSocket.setSoTimeout(STUN_TIMEOUT_MILLIS);
			this.mSocket.setSendBufferSize(8 * 1024);
			this.mSocket.setReceiveBufferSize(8 * 1024);
			this.mSocket.bind(null);
			InetSocketAddress localInetSocketAddress = new InetSocketAddress(
					ip, port);
			this.mSocket.connect(localInetSocketAddress, timeout);
			this.mOutput = new DataOutputStream(new BufferedOutputStream(
					this.mSocket.getOutputStream(), 4 * 1024));
			this.mInput = new DataInputStream(new BufferedInputStream(
					this.mSocket.getInputStream(), 8 * 1024));
			this.writableLatch = new CountDownLatch(1);
			this.mExtensionRegistry = ExtensionRegistryLite.newInstance();
			SpeechService.registerAllExtensions(this.mExtensionRegistry);
			VoiceSearch.registerAllExtensions(this.mExtensionRegistry);
			this.mTimeoutWatchdog = new TimeoutWatchdog(KEEP_ALIVE_TIMEOUT,
					new Runnable() {
						public void run() {
							close();
						}
					});
			return;
		} catch (IOException localIOException) {
			throw new ConnectionException("Failed to establish connection",
					localIOException);
		}
	}

	private static byte[] createStunBindingRequest(String stunId)
			throws UnsupportedEncodingException {
		StunPacket packet = new StunPacket(StunMessageType.STUN_BINDING_REQUEST);
		StunAttribute stunAttribute = new StunAttribute(
				StunAttributeType.STUN_ATTR_USERNAME);
		stunAttribute.setData(new StunAttribute.Username(stunId));
		packet.addAttribute(stunAttribute);
		return packet.asByteArray();
	}

	private void handleStun(StunPacket paramStunPacket) throws IOException {
		if ((!this.mIsRunning)
				|| (paramStunPacket.getType() != StunMessageType.STUN_BINDING_REQUEST)) {
			Log.w(TAG, "unexpected stun packet:" + paramStunPacket);
			return;
		}
		StunPacket localStunPacket = new StunPacket(
				StunMessageType.STUN_BINDING_RESPONSE);
		localStunPacket.addAttribute(paramStunPacket
				.getAttribute(StunAttributeType.STUN_ATTR_USERNAME));
		localStunPacket.setTransactionIDForResponse(paramStunPacket);
		sendRequest(localStunPacket.asByteArray());
		writableLatch.countDown();
		mCallback.onConnectionAlive();
	}

	private byte[] readPacket() throws IOException {
		int length = this.mInput.readUnsignedShort();
		byte[] data = new byte[length];
		this.mInput.readFully(data);
		InjectUtil.logReceivePacket(data);
		return data;
	}

	private StunPacket receiveStunResponsePacket() throws ConnectionException {
		try {
			StunPacket localStunPacket = StunPacket.fromByteArray(readPacket());
			if (localStunPacket.getType() == StunMessageType.STUN_BINDING_RESPONSE)
				return localStunPacket;
			throw new ConnectionException("Bad STUN response:"
					+ localStunPacket);
		} catch (EOFException localEOFException) {
			throw new ConnectionException("STUN connection closed",
					localEOFException);
		} catch (SocketTimeoutException localSocketTimeoutException) {
			throw new ConnectionException("STUN packet read timed out",
					localSocketTimeoutException);
		} catch (IOException localIOException) {
			throw new ConnectionException("STUN packet read error.",
					localIOException);
		}
	}

	private void sendRequest(byte[] data) throws IOException {
		InjectUtil.logSendPacket(data);
		if (data.length >= MAX_PACKET) {
			throw new IOException("packet too big:" + data.length);
		}
		this.mOutput.writeShort(data.length);
		this.mOutput.write(data);
		this.mOutput.flush();
	}

	private void setupStun() throws ConnectionException {
		try {
			byte[] arrayOfByte = createStunBindingRequest(this.mStunId);
			this.mSocket.setSoTimeout(STUN_TIMEOUT_MILLIS);
			sendRequest(arrayOfByte);
			receiveStunResponsePacket();
			this.mSocket.setSoTimeout(KEEP_ALIVE_TIMEOUT);
			return;
		} catch (IOException localIOException) {
			throw new ConnectionException(
					"Failed to establish stun connection", localIOException);
		}
	}

	public void close() {
		this.mIsRunning = false;
		if (this.mThread != null)
			this.mThread.interrupt();
		this.mTimeoutWatchdog.stop();
		try {
			try {
				this.mSocket.close();
				return;
			} finally {
			}
		} catch (IOException localIOException) {
			Log.e(TAG, "Failed to close the socket", localIOException);
		}
	}

	public boolean isConnected() {
		if ((this.mSocket != null) && (!this.mSocket.isClosed())) {
			return mSocket.isConnected();
		} else {
			return false;
		}
	}

	public void run() {
		long timeout = KEEP_ALIVE_TIMEOUT;
		long timeoutRealTime = SystemClock.elapsedRealtime() + timeout;
		while (mIsRunning) {
			if (SystemClock.elapsedRealtime() > timeoutRealTime) {
				break;
			}
			try {
				byte[] packet = readPacket();
				StunPacket stunPacket = StunPacket.headerFromByteArray(packet);
				if (stunPacket != null) {
					stunPacket.readBody(packet);
					handleStun(stunPacket);
				} else {
					ResponseMessage response = ResponseMessage.parseFrom(
							packet, mExtensionRegistry);
					mCallback.onResponseAvailable(response);
					mTimeoutWatchdog.extend();
				}
			} catch (IOException e) {
				if (mIsRunning) {
					Log.e(TAG, "TCP exception");
					mCallback.onException(new ConnectionException(e));
					break;
				}
			}
		}
		try {
			Log.d(TAG, "socket close");
			mSocket.close();
		} catch (IOException e) {
			Log.e(TAG, "Error closing TCP");
			mCallback.onException(new ConnectionException(e));
		}
	}

	public void sendRequest(SpeechService.RequestMessage paramRequestMessage)
			throws ConnectionException {
		try {
			this.mTimeoutWatchdog.extend();
			sendRequest(paramRequestMessage.toByteArray());
			return;
		} catch (IOException localIOException) {
			throw new ConnectionException("Failed to send request",
					localIOException);
		}
	}

	public void start(ConnectionCallback callback) throws ConnectionException {
		if (callback == null) {
			throw new NullPointerException("callback");
		}
		this.mCallback = callback;
		setupStun();
		this.mIsRunning = true;
		this.mThread = new Thread(this);
		this.mThread.setDaemon(true);
		this.mThread.start();
		try {
			if (!writableLatch
					.await(STUN_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
				Log.e(TAG, "Did not receive the expected stun packet");
				this.mIsRunning = false;
				throw new ConnectionException("Timeout");
			}
		} catch (InterruptedException localInterruptedException) {
			Log.e(TAG,
					"Got interrupted while waiting for the first stun message",
					localInterruptedException);
			this.mIsRunning = false;
			throw new ConnectionException("Interrupted");
		}
		this.mTimeoutWatchdog.start();
	}
}