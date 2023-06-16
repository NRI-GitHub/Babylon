package com.nri.babylon.audio;

import com.nri.library.stt.NRISpeechToText;
import com.nri.library.text_translation.NRITextTranslation;
import com.nri.library.text_translation.enums.SupportedLanguage;
import com.nri.library.tts.NRITextToSpeech;
import org.kurento.tutorial.groupcall.Room;
import org.kurento.tutorial.groupcall.UserSession;
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
        System.out.println("Adding listener id: " + room +"_"+ user);
        listeners.put(room +"_"+ user, listener);
    }

    public void createAudioThread(String audioFile, Room room, UserSession user, SupportedLanguage fromLanguage, SupportedLanguage toLanguage){
        AudioThread audioThread = new AudioThread(this, audioFile, user, room, nriSpeechToText, nriTextToSpeech,
                nriTextTranslation, fromLanguage, toLanguage);
        audioThread.start();
    }

    @Override
    public void onOutgoingAudio(String fileLocation, String roomName, String userName) {
        System.out.println("finding listener id: " + roomName +"_"+ userName);
        IncomingAudioCallback callback = listeners.get(roomName +"_"+ userName);
        if(callback != null){
            callback.onIncomingAudio(fileLocation, roomName, userName);
            listeners.remove(roomName +"_"+ userName);
        }
    }
}
