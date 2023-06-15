package com.nri.babylon.audio;

import com.nri.library.stt.NRISpeechToText;
import com.nri.library.text_translation.NRITextTranslation;
import com.nri.library.tts.NRITextToSpeech;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;

public class NriAudioCodec implements OutgoingAudioCallback{
    HashMap<String, IncomingAudioCallback> listeners;
    @Autowired
    private NRISpeechToText nriSpeechToText;
    @Autowired
    private NRITextToSpeech nriTextToSpeech;
    @Autowired
    private NRITextTranslation nriTextTranslation;

    public NriAudioCodec(){
        listeners = new HashMap<>();
    }

    public void addListener(IncomingAudioCallback listener, String room, String user){
        listeners.put(room +"_"+ user, listener);
    }

    public void createAudioThread(String audioFile, String room, String user){
        AudioThread audioThread = new AudioThread(this, audioFile, room, user, nriSpeechToText, nriTextToSpeech, nriTextTranslation);
        audioThread.run();
    }

    @Override
    public void onOutgoingAudio(String fileLocation, String roomName, String userName) {
        IncomingAudioCallback callback = listeners.get(roomName +"_"+ userName);
        if(callback != null){
            callback.onIncomingAudio(fileLocation, roomName, userName);
            listeners.remove(roomName +"_"+ userName);
        }
    }
}
