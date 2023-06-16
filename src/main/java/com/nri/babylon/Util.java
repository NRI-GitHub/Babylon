package com.nri.babylon;


import com.nri.babylon.config.ColorGenerator;
import com.nri.library.text_translation.NRITextTranslation;
import com.nri.library.text_translation.enums.SupportedLanguage;
import com.nri.library.tts.NRITextToSpeech;
import com.nri.library.tts.model.Voice;

import java.util.ArrayList;
import java.util.List;

public class Util {
    private static Util instance;
    private final String recorderEndpointIpAddress;
    private final ColorGenerator colorGenerator;
    private final NRITextTranslation nriTextTranslation;
    private final NRITextToSpeech nriTextToSpeech;

    public Util(String recorderEndpointIpAddress, ColorGenerator colorGenerator, NRITextTranslation nriTextTranslation, NRITextToSpeech nriTextToSpeech) {
        this.recorderEndpointIpAddress = recorderEndpointIpAddress;
        this.colorGenerator = colorGenerator;
        this.nriTextTranslation = nriTextTranslation;
        this.nriTextToSpeech = nriTextToSpeech;
    }


    public static void setInstance(Util util) {
        instance = util;
    }

    public static String recorderEndpointIpAddress() {
        return instance.getRecorderEndpointIpAddress();
    }

    public static String getUniqueHexColor(String name) {
        return instance.getColorGenerator().getUniqueHexColor(name);
    }

    public static SupportedLanguage getSupportedLanguage(String languageId) throws Exception {
        try {

            ArrayList<SupportedLanguage> supportedLanguages = instance.getNriTextTranslation().getSupportedLanguages();
            for (SupportedLanguage supportedLanguage : supportedLanguages) {
                if (supportedLanguage.getId() == Integer.parseInt(languageId))
                    return supportedLanguage;
            }
        } catch (Exception e) {
        }

        throw new Exception("error finding languageId" + languageId);
    }

    public static Voice getVoice(String voiceId) throws Exception {
        try {
            List<Voice> voices = instance.getNriTextToSpeech().getVoices();
            for (Voice voice : voices) {
                if (voice.getId().toLowerCase().contains(voiceId.toLowerCase().trim()))
                    return voice;
            }

        } catch (Exception e) {
        }

        throw new Exception("error finding voiceId" + voiceId);
    }

    private String getRecorderEndpointIpAddress() {
        return recorderEndpointIpAddress;
    }

    public ColorGenerator getColorGenerator() {
        return colorGenerator;
    }

    public NRITextTranslation getNriTextTranslation() {
        return nriTextTranslation;
    }

    public NRITextToSpeech getNriTextToSpeech() {
        return nriTextToSpeech;
    }
}
