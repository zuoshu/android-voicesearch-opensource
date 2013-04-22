package com.cyberobject;

import java.nio.ByteBuffer;

public interface AudioListener {
	public void onAudioData(ByteBuffer buffer);
}
