package com.nri.babylon.config;

import org.kurento.client.KurentoClient;
import com.nri.babylon.kurento.CallHandler;
import com.nri.babylon.kurento.RoomManager;
import com.nri.babylon.kurento.UserRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import com.nri.babylon.Util;
import com.nri.babylon.audio.NriAudioCodec;
import com.nri.babylon.testing.TestLibs;
import com.nri.library.stt.NRISpeechToText;
import com.nri.library.text_translation.NRITextTranslation;
import com.nri.library.tts.NRITextToSpeech;

@Configuration
public class GlobalConfig {
    @Value("${nri.api.speech_to_text.key}")
    private String speechToTextKey;

    @Value("${nri.api.text_to_speech.key}")
    private String textToSpeech;

    @Value("${nri.api.text_translation.key}")
    private String textTranslation;


    @Value("${nri.api.recorder_endpoint_ip_address}")
    private String recorderEndpointIpAddress;

    @Autowired
    private ColorGenerator colorGenerator;

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
    @Bean
    public NriAudioCodec nriAudioCodec(){
        NriAudioCodec nriAudioCodec = new NriAudioCodec();
        return nriAudioCodec;
    }

    @Bean
    public UserRegistry registry() {
        return new UserRegistry();
    }

    @Bean
    public RoomManager roomManager() {
        return new RoomManager();
    }

    @Bean
    public CallHandler groupCallHandler() {
        return new CallHandler();
    }

    @Bean
    public KurentoClient kurentoClient() {
        return KurentoClient.create();
    }

    @Bean
    public Util getUtil(NRITextTranslation nriTextTranslation, NRITextToSpeech nriTextToSpeech) {
        Util util = new Util(recorderEndpointIpAddress, colorGenerator, nriTextTranslation, nriTextToSpeech, speechToTextKey);
        Util.setInstance(util);
        return util;
    }

    @Bean
    public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(32768);
        return container;
    }
}
