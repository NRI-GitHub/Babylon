package com.nri.babylon.controller;

import com.nri.babylon.audio.IncomingAudioCallback;
import com.nri.babylon.audio.NriAudioCodec;
import com.nri.library.stt.NRISpeechToText;
import com.nri.library.stt.listeners.OnSpeechToTextListener;
import com.nri.library.text_translation.NRITextTranslation;
import com.nri.library.tts.NRITextToSpeech;
import jakarta.servlet.http.HttpServletRequest;
import org.kurento.tutorial.groupcall.RoomManager;
import org.kurento.tutorial.groupcall.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import java.util.UUID;

@RestController
public class AudioController {
    //private LinkedBlockingDeque<String> audioFilesQueue = new LinkedBlockingDeque<>();

    @Autowired
    private NriAudioCodec nriAudioCodec;

    @Autowired
    private RoomManager roomManager;

    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private static final String UPLOADED_FOLDER = "./audio/uploaded_audio/";

    @GetMapping("/sendAudio/{roomName}/{userName}")
    public ResponseEntity<InputStreamResource> getAudio(@PathVariable("roomName") String roomName,
                                                        @PathVariable("userName") String userName) {
        System.out.println("[Controller::sendAudio] Sending Media");
        System.out.println("[Controller::sendAudio] userName : " + userName);

        UserSession userSession = roomManager.getRoom(roomName).getParticipant(userName);
        System.out.println("[Controller::sendAudio] userSession.getName() : " + userSession.getName());
        String[] translatedAudio = new String[1];
        Object syncObject = new Object();

        nriAudioCodec.addListener((fileLocation, room, user) -> {
            if(roomName.equals(room) && userName.equals(user)) {
                translatedAudio[0] = fileLocation;
                synchronized (syncObject) {
                    syncObject.notify();
                }
            }
        }, roomName, userName);

        synchronized (syncObject) {
            try {
                syncObject.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        File file = new File(translatedAudio[0]);

        if (file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamResource resource = new InputStreamResource(fileInputStream);

                System.out.println("[Controller::sendAudio] Done sending media");
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("audio/webm"))
                        .body(resource);
            } catch (IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            System.out.println("[Controller::sendAudio] No media to send");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @PostMapping("/acceptAudio/{roomName}/{userName}")
    public ResponseEntity<String> upload(HttpServletRequest request,
                                         @PathVariable("roomName") String roomName,
                                         @PathVariable("userName") String userName) {
        System.out.println("[Controller::acceptAudio] Received Media");

        UserSession userSession = roomManager.getRoom(roomName).getParticipant(userName);
        System.out.println("[Controller::sendAudio] userSession.getName() : " + userSession.getName());
        String filePath = null;

        try {
            // Read the input stream from the request
            InputStream inputStream = request.getInputStream();

            // Specify the file path to save the audio
            String fileName = UUID.randomUUID() + "_" +userName;
            filePath = UPLOADED_FOLDER + fileName + ".webm";
            File file = new File(filePath);

            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
            }

        } catch (SocketException e) {
            // Handle connection reset
            System.err.println("Client aborted the connection: " + e.getMessage());
            return new ResponseEntity<>("Client aborted the connection", HttpStatus.PARTIAL_CONTENT);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            nriAudioCodec.createAudioThread(filePath, roomName, userName);
            //userSession.getRecordMyAudio().record();
        }
        System.out.println("[Controller::acceptAudio] Done Receiving audio");
        return new ResponseEntity<>("Audio saved successfully!", HttpStatus.OK);
    }
}
