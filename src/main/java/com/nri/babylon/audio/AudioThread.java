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

public class AudioThread extends Thread implements OnSpeechToTextListener {
    private static final Logger log = LoggerFactory.getLogger(AudioThread.class);
    private static AtomicInteger loggingThreadCounter = new AtomicInteger(0);

    public static final int AUDIO_SEGMENT_TIME_MS = 5000;
    private final UserSession user;
    private final Room room;
    private final String audioFile;
    private NRISpeechToText nriSpeechToText;
    private NRITextToSpeech nriTextToSpeech;
    private NRITextTranslation nriTextTranslation;

    public AudioThread(String audioFile, UserSession user, Room room, NRISpeechToText nriSpeechToText, NRITextToSpeech nriTextToSpeech, NRITextTranslation nriTextTranslation) {
        this.audioFile = audioFile;
        this.user = user;
        this.room = room;
        this.nriSpeechToText = nriSpeechToText;
        this.nriTextToSpeech = nriTextToSpeech;
        this.nriTextTranslation = nriTextTranslation;
        nriSpeechToText.setOnSpeechToTextListener(this);

        loggingThreadCounter.set(loggingThreadCounter.intValue() + 1);
    }

    public void run() {

        try {
            File file = new File(audioFile);

            log("audio file : " + audioFile);
            log("audio file exists : " + file.exists());
            String convertedFile = AudioUtils.convertToWav(audioFile);


            int chunkSize = 44100;
            byte[] audioData = Files.readAllBytes(Path.of(convertedFile));

            for (int i = 0; i < audioData.length; i += chunkSize) {
                int endIndex = Math.min(i + chunkSize, audioData.length);
                byte[] chunk = new byte[endIndex - i];
                System.arraycopy(audioData, i, chunk, 0, chunk.length);
                nriSpeechToText.processAudio(chunk);
            }

        } catch (InterruptedException | EncoderException | IOException e) {
            e.printStackTrace();
        }
    }

    private void notifySpeech(String nativeText) {
        for (UserSession participant : room.getParticipants()) {
            if (participant.getName() != user.getName()) {
                try {
                    String translatedText = nriTextTranslation.translateText(nativeText, user.getNativeLanguage(), participant.getNativeLanguage());
                    UUID fileName = UUID.randomUUID();
                    String playbackFile = AudioUtils.saveAudio(nriTextToSpeech.textToSpeech(translatedText, user.getVoice()), String.valueOf(fileName));
                    participant.getAudioCallback().onOutgoingAudio(playbackFile, user);
                } catch (EncoderException | IOException e) {
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        try {
            AudioLogMessage audioLogMessage = new AudioLogMessage(nativeText, user.getSession().getId(), user.getName(), user.getIconColor(), user.getNativeLanguage());
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
        notifySpeech(s);
    }
}
