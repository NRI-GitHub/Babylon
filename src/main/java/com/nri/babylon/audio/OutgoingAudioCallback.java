package com.nri.babylon.audio;

import org.kurento.tutorial.groupcall.UserSession;

public interface OutgoingAudioCallback {
    void onOutgoingAudio(String fileLocation, UserSession sender);
}
