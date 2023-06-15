package com.nri.babylon.audio;

public interface OutgoingAudioCallback {
    void onOutgoingAudio(String fileLocation, String roomName, String userName);
}
