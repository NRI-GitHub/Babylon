package com.nri.babylon.controller;

import com.nri.library.stt.NRISpeechToText;
import com.nri.library.stt.listeners.OnSpeechToTextListener;
import com.nri.library.text_translation.NRITextTranslation;
import com.nri.library.tts.NRITextToSpeech;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.*;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

@RestController()
public class AudioController {
    private LinkedBlockingDeque<String> fileQueue = new LinkedBlockingDeque<>();

    @Autowired
    private NRISpeechToText nriSpeechToText;

    @Autowired
    private NRITextToSpeech nriTextToSpeech;

    @Autowired
    private NRITextTranslation nriTextTranslation;

    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private static final String UPLOADED_FOLDER = "./audio/uploaded_audio/";
    private static final String CONVERTED_FOLDER = "./audio/converted_audio/";
    private static final int AUDIO_SEGMENT_TIME_MS = 5000;
    private static long lastFileCreationTime = 0;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public AudioController() {
        new Thread(() -> {
            while (true) {
                try {
                    String filePath = fileQueue.take();

                    String fileWithExtension = new File(filePath).getName();
                    String fileName = fileWithExtension.replace(".webm", "");
                    System.out.println("Convert name: " + fileName);
                    String finalFile = convertToMp3(filePath, fileName);
                    processMp3(finalFile);
                } catch (InterruptedException | EncoderException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @GetMapping("/sendAudio")
    public ResponseEntity<InputStreamResource> getAudio() {
        System.out.println("[Controller::sendAudio] Sending Media");

        if (byteArrayOutputStream != null) {
            byte[] audioBytes = byteArrayOutputStream.toByteArray();
            InputStream audioStream = new ByteArrayInputStream(audioBytes);
            InputStreamResource resource = new InputStreamResource(audioStream);

            System.out.println("[Controller::sendAudio] Done sending media");
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/webm")) // adjust the media type as needed
                    .body(resource);
        } else {
            System.out.println("[Controller::sendAudio] No media to send");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @PostMapping("/acceptAudio")
    public ResponseEntity<String> upload(HttpServletRequest request) {
        System.out.println("[Controller::acceptAudio] Received Media");

        try {
            // Read the input stream from the request
            InputStream inputStream = request.getInputStream();

            // Specify the file path to save the audio
            UUID fileName = UUID.randomUUID();
            String filePath = UPLOADED_FOLDER + fileName + ".webm";
            File file = new File(filePath);

            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
            }

            String finalFile = convertToMp3(String.valueOf(file), String.valueOf(fileName));
            processMp3(finalFile);

        } catch (SocketException e) {
            // Handle connection reset
            System.err.println("Client aborted the connection: " + e.getMessage());
            return new ResponseEntity<>("Client aborted the connection", HttpStatus.PARTIAL_CONTENT);
        } catch (IOException | EncoderException | InterruptedException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        System.out.println("[Controller::acceptAudio] Done Receiving audio");
        return new ResponseEntity<>("Audio saved successfully!", HttpStatus.OK);
    }

    /*@PostMapping("/acceptAudio")
    public ResponseEntity<String> upload(HttpServletRequest request) {
        System.out.println("[Controller::acceptAudio] Received Media");

        FileOutputStream fileOutputStream = null;
        String filePath = null;
        long lastFileCreationTime = 0; // Initialize here instead of using a static variable

        try {
            InputStream inputStream = request.getInputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastFileCreationTime >= 5000 || fileOutputStream == null) {
                    lastFileCreationTime = currentTime;

                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                        fileQueue.put(filePath); // Put file into queue for processing
                        System.out.println("[Controller::acceptAudio] Finishing file." + filePath);
                    }

                    String timestamp = String.valueOf(lastFileCreationTime);
                    filePath = UPLOADED_FOLDER + "_" + timestamp + ".webm";
                    fileOutputStream = new FileOutputStream(filePath);
                    System.out.println("[Controller::acceptAudio] Creating new file " + filePath);
                }

                fileOutputStream.write(buffer, 0, bytesRead);
            }

            if (fileOutputStream != null) {
                fileOutputStream.close();
                fileQueue.put(filePath); // Put the last file into queue for processing
            }

        } catch (SocketException e) {
            System.err.println("Client aborted the connection: " + e.getMessage());
            return new ResponseEntity<>("Client aborted the connection", HttpStatus.PARTIAL_CONTENT);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        System.out.println("[Controller::acceptAudio] Done Receiving audio");
        return new ResponseEntity<>("Audio saved successfully!", HttpStatus.OK);
    }*/

    private void processMp3(String finalFile) throws IOException, InterruptedException {
        nriSpeechToText.setOnSpeechToTextListener(new OnSpeechToTextListener() {
            public void onIncomingPartialTranscript(String partialTranscription) {
            }

            public void onIncomingFinalTranscript(String finalTranscription) {

            }
        });
        File wavFile = new File(finalFile);

        int chunkSize = 44100;
        byte[] audioData = Files.readAllBytes(wavFile.toPath());

        for(int i = 0; i < audioData.length; i += chunkSize) {
            int endIndex = Math.min(i + chunkSize, audioData.length);
            byte[] chunk = new byte[endIndex - i];
            System.arraycopy(audioData, i, chunk, 0, chunk.length);
            nriSpeechToText.processAudio(chunk);
        }
    }

    private String convertToMp3(String sourceFile, String sourceName) throws EncoderException {
        String targetFile = CONVERTED_FOLDER + sourceName +".wav";
        File source = new File(sourceFile);
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
}
