package com.nri.babylon.testing;


import com.nri.library.stt.NRISpeechToText;
import com.nri.library.text_translation.NRITextTranslation;
import com.nri.library.tts.NRITextToSpeech;
import com.nri.library.tts.model.Voice;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class TestLibs {

    @Autowired
    private NRISpeechToText nriSpeechToText;

    @Autowired
    private NRITextToSpeech nriTextToSpeech;

    @Autowired
    private NRITextTranslation nriTextTranslation;


    public void printTest() {
        log("Test Message from Test lib Class");

        nriSpeechToText.printTest();
        nriTextToSpeech.printTest();
        nriTextTranslation.printTest();


        List<Voice> voices = nriTextToSpeech.getVoices();
        log(voices.size()+"the size");
    }

    private void log(String message) {
        System.out.println("TestLib.class : " + message);
    }
}
