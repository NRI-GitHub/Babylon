package com.nri.babylon.audio;

import com.nri.library.text_translation.enums.SupportedLanguage;
import org.kurento.tutorial.groupcall.Room;
import org.kurento.tutorial.groupcall.UserSession;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collection;

public class AudioUtils {
    public static final String UPLOADED_FOLDER = "./audio/uploaded_audio/";
    public static final String CONVERTED_FOLDER = "./audio/converted_audio/";
    public static final String TRANSLATED_FOLDER = "./audio/translated_audio/";

    public static String convertToWav(String sourceFile) throws EncoderException {
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
        Path mp3Path = Paths.get("audio2/", fileName + ".mp3");
        Files.write(mp3Path, audioData);

        // File references
        File source = mp3Path.toFile();


        return fileName;
    }

    public static void copyFile(String fileName, String newFile) {

        Path sourcePath = Paths.get(fileName);
        Path destinationPath = Paths.get(newFile);
        File sourceFile = sourcePath.toFile();
        if (sourceFile.exists()){
            System.out.println("File .");
        }

        try {
            // Copy the file
            Files.copy(sourcePath, destinationPath, StandardCopyOption.COPY_ATTRIBUTES);
            System.out.println("File copied successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while copying the file: " + e.getMessage());
        }
    }

    public static void deleteFile(String fileName) {
        Path filePath = Path.of(fileName);

        try {
            // Delete the file
            Files.delete(filePath);
            System.out.println("File deleted successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while deleting the file: " + e.getMessage());
        }
    }

    public static void makeDirectories(File file) throws AudioInterceptorException {
        if (file == null)
            throw new AudioInterceptorException("Unable to create file : null");

        File parentDir = file.getParentFile();
        if (parentDir != null) {
            boolean mkdirs = parentDir.mkdirs();
            if (!mkdirs)
                throw new AudioInterceptorException("Unable to directories file : " + parentDir.getAbsolutePath());
        }


        try {
            Path path = Paths.get(file.getAbsolutePath());


            ProcessBuilder pb = new ProcessBuilder("chmod", "777", path.toString());
            Process p = pb.start();
            p.waitFor();
        }catch (Exception e){}


    }

    public static SupportedLanguage getOtherPartyNativeLanguage(Room room, UserSession userSpeakerPerson) {
        Collection<UserSession> participants = room.getParticipants();
        for (UserSession listeningPerson : participants) {
            if (listeningPerson.equals(userSpeakerPerson))
                continue;

            return listeningPerson.getNativeLanguage();
        }
        return null;
    }
}
