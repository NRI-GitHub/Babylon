package com.nri.babylon.audio;

import java.io.File;

public interface OnAudioStreamInterceptorListener {
    void onIncomingAudio(Byte[] audio);
    void onIncomingAudio(File audioFile);
}
