package com.nri.babylon.config;

import com.nri.babylon.testing.TestLibs;
import com.nri.library.stt.NRISpeechToText;
import com.nri.library.text_translation.NRITextTranslation;
import com.nri.library.tts.NRITextToSpeech;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlobalConfig {
    @Value("${nri.api.speech_to_text.key}")
    private String speechToTextKey;

    @Value("${nri.api.text_to_speech.key}")
    private String textToSpeech;

    @Value("${nri.api.text_translation.key}")
    private String textTranslation;


    @Bean
    public TestLibs testLibsProvider(){
        return new TestLibs();
    }

    @Bean
    public NRISpeechToText nriSpeechToTextProvider() throws Exception {
        NRISpeechToText nriSpeechToText = new NRISpeechToText(speechToTextKey);
        return nriSpeechToText;
    }

    @Bean
    public NRITextToSpeech nriTextToSpeechProvider(){
        NRITextToSpeech nriTextToSpeech = new NRITextToSpeech(textToSpeech, true);
        return nriTextToSpeech;
    }

    @Bean
    public NRITextTranslation nriTextTranslation(){
        NRITextTranslation nriTextTranslation = new NRITextTranslation(textTranslation);
        return nriTextTranslation;
    }
}
