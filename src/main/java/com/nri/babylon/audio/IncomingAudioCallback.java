package com.nri.babylon.audio;

public interface IncomingAudioCallback {
    void onIncomingAudio(String fileLocation, String roomName, String userName);
}
