package com.nri.babylon.audio;

import com.nri.babylon.kurento.UserSession;

public interface OutgoingAudioCallback {
    void onOutgoingAudio(String fileLocation, UserSession sender);
}
