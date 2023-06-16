package com.nri.babylon.audio;

import com.nri.library.stt.NRISpeechToText;
import com.nri.library.stt.listeners.OnSpeechToTextListener;
import com.nri.library.text_translation.NRITextTranslation;
import com.nri.library.text_translation.enums.SupportedLanguage;
import com.nri.library.tts.NRITextToSpeech;
import com.nri.library.tts.model.Voice;
import org.kurento.client.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ws.schild.jave.EncoderException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class AudioThread extends Thread implements OnSpeechToTextListener{
    private static final Logger log = LoggerFactory.getLogger(AudioThread.class);
    private static AtomicInteger loggingThreadCounter = new AtomicInteger(0);

    public static final int AUDIO_SEGMENT_TIME_MS = 5000;
    private final String user;
    private final String room;
    private final OutgoingAudioCallback listener;
    private final String audioFile;
    private NRISpeechToText nriSpeechToText;
    private NRITextToSpeech nriTextToSpeech;
    private NRITextTranslation nriTextTranslation;

    public AudioThread(OutgoingAudioCallback listener, String audioFile, String user, String room, NRISpeechToText nriSpeechToText, NRITextToSpeech nriTextToSpeech, NRITextTranslation nriTextTranslation) {
        this.listener = listener;
        this.audioFile = audioFile;
        this.user = user;
        this.room = room;
        this.nriSpeechToText = nriSpeechToText;
        this.nriTextToSpeech = nriTextToSpeech;
        this.nriTextTranslation = nriTextTranslation;

        loggingThreadCounter.set(loggingThreadCounter.intValue() +1);
    }
    public void run() {
        try {
            String convertedFile = AudioUtils.convertToMp3(audioFile);

            synchronized (nriSpeechToText) {
                nriSpeechToText.setOnSpeechToTextListener(this);
                int chunkSize = 44100;
                byte[] audioData = Files.readAllBytes(Path.of(convertedFile));

                for(int i = 0; i < audioData.length; i += chunkSize) {
                    int endIndex = Math.min(i + chunkSize, audioData.length);
                    byte[] chunk = new byte[endIndex - i];
                    System.arraycopy(audioData, i, chunk, 0, chunk.length);
                    nriSpeechToText.processAudio(chunk);
                }
            }

        } catch (InterruptedException | EncoderException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void speakSaveToFileText(String finalTranscription) {
        log("speakSaveToFileText : start");

        try {
            log("speakSaveToFileText : " + finalTranscription);
            SupportedLanguage fromLanguage = SupportedLanguage.ENGLISH;
            SupportedLanguage toLanguage = SupportedLanguage.FRENCH;

            String orgText = finalTranscription;
            String translatedText;

            synchronized (nriTextTranslation) {
                translatedText = nriTextTranslation.translateText(orgText, fromLanguage, toLanguage);//locking
            }

            log("speakSaveToFileText : " + translatedText);

            List<Voice> voices;
            Voice voice;

            String playbackFile;
            synchronized (nriTextToSpeech) {
                voices = nriTextToSpeech.getVoices();
                voice = voices.get(0);

                for (Voice voice1 : voices) {
                    if (voice1.getName().contains("Jeff"))
                        voice = voice1;
                }

                String messageToSay = translatedText;
                UUID fileName = UUID.randomUUID();
                log("speakSaveToFileText : AudioUtils.saveAudio start");
                playbackFile = AudioUtils.saveAudio(nriTextToSpeech.textToSpeech(messageToSay, voice), String.valueOf(fileName));
                log("speakSaveToFileText : AudioUtils.saveAudio end");
            }

            listener.onOutgoingAudio(playbackFile, user, room);
        } catch (Exception e) {
            log("speakSaveToFileText : Exception");

            throw new RuntimeException(e);
        }

        log("speakSaveToFileText : end");
    }

    private void log(String message) {
        int count = loggingThreadCounter.get();

        log.info("[AudioThread::{}] index: {}, message: {}", count, message);
    }

    @Override
    public void onIncomingPartialTranscript(String s) {}

    @Override
    public void onIncomingFinalTranscript(String s) {
        if(s.isBlank() || s.isEmpty()) return;
        speakSaveToFileText(s);
    }
}
