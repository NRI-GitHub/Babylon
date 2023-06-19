package com.nri.babylon.audio;

import com.nri.library.stt.NRISpeechToText;
import com.nri.library.text_translation.NRITextTranslation;
import com.nri.library.tts.NRITextToSpeech;
import com.nri.babylon.kurento.Room;
import com.nri.babylon.kurento.UserSession;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

public class NriAudioCodec {
    HashMap<String, IncomingAudioCallback> listeners;
    @Autowired
    private NRITextToSpeech nriTextToSpeech;
    @Autowired
    private NRITextTranslation nriTextTranslation;

    public NriAudioCodec(){
        listeners = new HashMap<>();
    }

    public void addListener(IncomingAudioCallback listener, String room, String user){
        System.out.println("Adding listener id: " + room +"_"+ user);
        listeners.put(room +"_"+ user, listener);
    }

    public void createAudioThread(String audioFile, Room room, UserSession user, NRISpeechToText nriSpeechToText){
        AudioThread audioThread = new AudioThread(audioFile, user, room, nriSpeechToText, nriTextToSpeech, nriTextTranslation);
        audioThread.start();
    }
}
