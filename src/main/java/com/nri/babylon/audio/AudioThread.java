package com.nri.babylon.audio;

import com.nri.library.stt.NRISpeechToText;
import com.nri.library.stt.listeners.OnSpeechToTextListener;
import com.nri.library.text_translation.NRITextTranslation;
import com.nri.library.text_translation.enums.SupportedLanguage;
import com.nri.library.tts.NRITextToSpeech;
import com.nri.library.tts.model.Voice;
import org.springframework.beans.factory.annotation.Autowired;
import ws.schild.jave.EncoderException;

import javax.sound.sampled.AudioFileFormat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class AudioThread extends Thread implements OnSpeechToTextListener{
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
    }
    public void run() {
        try {
            String convertedFile = AudioUtils.convertToMp3(audioFile);

            nriSpeechToText.setOnSpeechToTextListener(this);
            int chunkSize = 44100;
            byte[] audioData = Files.readAllBytes(Path.of(convertedFile));

            for(int i = 0; i < audioData.length; i += chunkSize) {
                int endIndex = Math.min(i + chunkSize, audioData.length);
                byte[] chunk = new byte[endIndex - i];
                System.arraycopy(audioData, i, chunk, 0, chunk.length);
                nriSpeechToText.processAudio(chunk);
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (EncoderException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void speakSaveToFileText(String finalTranscription) {
        try {
            SupportedLanguage fromLanguage = SupportedLanguage.ENGLISH;
            SupportedLanguage toLanguage = SupportedLanguage.FRENCH;


            String orgText = finalTranscription;
            String translatedText = nriTextTranslation.translateText(orgText, fromLanguage, toLanguage);//locking

            List<Voice> voices = nriTextToSpeech.getVoices();
            Voice voice = voices.get(0);

            for (Voice voice1 : voices) {
                if (voice1.getName().contains("Jeff"))
                    voice = voice1;
            }

            String messageToSay = translatedText;
            UUID fileName = UUID.randomUUID();
            String playbackFile = AudioUtils.saveAudio(nriTextToSpeech.textToSpeech(messageToSay, voice), String.valueOf(fileName));
            listener.onOutgoingAudio(playbackFile, room, user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onIncomingPartialTranscript(String s) {}

    @Override
    public void onIncomingFinalTranscript(String s) {
        speakSaveToFileText(s);
    }
}
