package com.nri.babylon.audio;

import com.nri.babylon.view.model.AudioLogMessage;
import com.nri.library.stt.NRISpeechToText;
import com.nri.library.stt.listeners.OnSpeechToTextListener;
import com.nri.library.text_translation.NRITextTranslation;
import com.nri.library.text_translation.enums.SupportedLanguage;
import com.nri.library.tts.NRITextToSpeech;
import com.nri.library.tts.model.Voice;
import org.kurento.tutorial.groupcall.Room;
import org.kurento.tutorial.groupcall.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ws.schild.jave.EncoderException;

import java.io.File;
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
    private final UserSession user;
    private final Room room;
    private final OutgoingAudioCallback listener;
    private final String audioFile;
    private final SupportedLanguage fromLanguage;
    private final SupportedLanguage toLanguage;
    private NRISpeechToText nriSpeechToText;
    private NRITextToSpeech nriTextToSpeech;
    private NRITextTranslation nriTextTranslation;

    public AudioThread(OutgoingAudioCallback listener,
                       String audioFile, UserSession user,
                       Room room, NRISpeechToText nriSpeechToText,
                       NRITextToSpeech nriTextToSpeech,
                       NRITextTranslation nriTextTranslation, SupportedLanguage fromLanguage, SupportedLanguage toLanguage) {
        this.listener = listener;
        this.audioFile = audioFile;
        this.user = user;
        this.room = room;
        this.nriSpeechToText = nriSpeechToText;
        this.nriTextToSpeech = nriTextToSpeech;
        this.nriTextTranslation = nriTextTranslation;
        this.fromLanguage = fromLanguage;
        this.toLanguage = toLanguage;

        loggingThreadCounter.set(loggingThreadCounter.intValue() +1);
    }
    public void run() {
        try {
            File file = new File(audioFile);

            log("audio file : " + audioFile);
            log("audio file exists : " + file.exists());
            String convertedFile = AudioUtils.convertToWav(audioFile);

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
            e.printStackTrace();
            //throw new RuntimeException(e);
        }
    }

    private void speakSaveToFileText(String finalTranscription, SupportedLanguage fromLanguage, SupportedLanguage toLanguage) {
        notifyEveryoneInTheRoomOfMySpokenText(finalTranscription, fromLanguage, user);
        log("speakSaveToFileText : start");

        try {
            log("speakSaveToFileText : " + finalTranscription);

            String orgText = finalTranscription;
            String translatedText;

            if (toLanguage == null){
                log("speakSaveToFileText : not going to translate. There is NO one on the call right now");
                return;
            }

            if (fromLanguage == toLanguage){
                log("speakSaveToFileText : not going to translate. both parties speak the same language");
                return;
            }

            synchronized (nriTextTranslation) {
                translatedText = nriTextTranslation.translateText(orgText, fromLanguage, toLanguage);//locking
                notifyEveryoneInTheRoomOfMySpokenText(translatedText, fromLanguage, user);
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

            listener.onOutgoingAudio(playbackFile, user.getName(), room.getName());

            for (UserSession participant : room.getParticipants()) {

                user.playNewAudio(new File(playbackFile), participant);
            }

        } catch (Exception e) {
            log("speakSaveToFileText : Exception");

            throw new RuntimeException(e);
        }

        log("speakSaveToFileText : end");
    }

    private void notifyEveryoneInTheRoomOfMySpokenText(String transcription, SupportedLanguage language, UserSession speaker) {
        String name = speaker.getName();
        String id = speaker.getSession().getId();
        String userIconColor = speaker.getIconColor();
        String message = transcription;

        AudioLogMessage audioLogMessage = new AudioLogMessage(message, id, name, userIconColor, language);
        try {
            room.sendAudioLog(audioLogMessage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        speakSaveToFileText(s, fromLanguage, toLanguage);
    }
}
