package com.nri.babylon.audio;

import com.nri.library.stt.NRISpeechToText;
import com.nri.library.stt.listeners.OnSpeechToTextListener;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AudioUtils {
    public static final String UPLOADED_FOLDER = "./audio/uploaded_audio/";
    public static final String CONVERTED_FOLDER = "./audio/converted_audio/";
    public static final String TRANSLATED_FOLDER = "./audio/translated_audio/";

    public static String convertToMp3(String sourceFile) throws EncoderException {
        File source = new File(sourceFile);
        String targetFile = CONVERTED_FOLDER + source.getName() +".wav";
        File target = new File(targetFile);

        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("pcm_s16le");
        audio.setBitRate(128000);
        audio.setChannels(1);
        audio.setSamplingRate(16000);


        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setInputFormat("webm");
        attrs.setOutputFormat("wav");
        attrs.setAudioAttributes(audio);
        Encoder encoder = new Encoder();
        encoder.encode(new MultimediaObject(source), target, attrs);

        return targetFile;
    }

    public static String splitWebmFile(String inputFile, Float startTimeInSeconds, Float durationInSeconds) {
        // Input file
        File source = new File(inputFile);
        String outputFile = UPLOADED_FOLDER + startTimeInSeconds.intValue() + source.getName() ;

        // Output file
        File target = new File(outputFile);

        // Audio attributes
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("pcm_s16le");

        // Encoding attributes
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setInputFormat("webm");
        attrs.setOutputFormat("wav");
        attrs.setAudioAttributes(audio);
        attrs.setOffset(startTimeInSeconds); // Start time in seconds
        attrs.setDuration(durationInSeconds); // Duration in seconds

        // Encode (split the video)
        try {
            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(source), target, attrs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outputFile;
    }

    public static String saveAudio(byte[] audioData, String fileName) throws IOException, EncoderException {
        // Save the audio data to an MP3 file
        Path mp3Path = Paths.get(CONVERTED_FOLDER, fileName + ".mp3");
        Files.write(mp3Path, audioData);

        // File references
        File source = mp3Path.toFile();
        String targetFilePath = TRANSLATED_FOLDER + fileName + ".webm";
        File target = new File(targetFilePath);

        // Setup the audio attributes
        AudioAttributes audio = new AudioAttributes();
        audio.setBitRate(128000);
        audio.setChannels(1);
        audio.setSamplingRate(16000);

        // Setup the encoding attributes
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setInputFormat("mp3");
        attrs.setOutputFormat("webm");
        attrs.setAudioAttributes(audio);


        // Encoding
        Encoder encoder = new Encoder();
        encoder.encode(new MultimediaObject(source), target, attrs);

        return targetFilePath;
    }
}
