package com.nri.babylon.controller;

import com.nri.babylon.audio.AudioUtils;
import com.nri.babylon.audio.NriAudioCodec;
import com.nri.library.text_translation.enums.SupportedLanguage;
import jakarta.servlet.http.HttpServletRequest;
import org.kurento.tutorial.groupcall.Room;
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

import java.io.*;
import java.net.SocketException;
import java.util.Collection;
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
        if (userSession == null) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        System.out.println("[Controller::sendAudio] userSession.getName() : " + userSession.getName());
        String[] translatedAudio = new String[1];
        Object syncObject = new Object();

        nriAudioCodec.addListener((fileLocation, room, user) -> {
            translatedAudio[0] = fileLocation;
            synchronized (syncObject) {
                System.out.println("[Controller::sendAudio] wake Me UP");
                syncObject.notify();
            }
        }, roomName, userName);

        synchronized (syncObject) {
            try {
                System.out.println("[Controller::sendAudio] going to SLEEP");
                syncObject.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        if (translatedAudio[0] == null || translatedAudio[0].isEmpty() || translatedAudio[0].isBlank())
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        System.out.println("[Controller::sendAudio] The File: " + translatedAudio[0]);
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

    @PostMapping("/acceptAudio33/{roomName}/{userName}")
    public ResponseEntity<String> upload(HttpServletRequest request,
                                         @PathVariable("roomName") String roomName,
                                         @PathVariable("userName") String userName) {
        System.out.println("[Controller::acceptAudio] Received Media");

        Room room = roomManager.getRoom(roomName);
        UserSession userSession = room.getParticipant(userName);
        System.out.println("[Controller::sendAudio] userSession.getName() : " + userSession.getName());
        if (userSession == null) return new ResponseEntity<>(HttpStatus.NO_CONTENT);


        File audioFile;
        try {
            // Read the input stream from the request
            InputStream inputStream = request.getInputStream();

            audioFile = saveInputAudio(inputStream, userName);
        } catch (SocketException e) {
            // Handle connection reset
            System.err.println("Client aborted the connection: " + e.getMessage());
            return new ResponseEntity<>("Client aborted the connection", HttpStatus.PARTIAL_CONTENT);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }


        onAudioSavedSuccessfully(audioFile, room, userSession);
        userSession.startRecording(0);
        System.out.println("[Controller::acceptAudio] Done Receiving audio");
        return new ResponseEntity<>("Audio saved successfully!", HttpStatus.OK);
    }

    private File saveInputAudio(InputStream inputStream, String userName) throws IOException {
        // Specify the file path to save the audio
        String fileName = UUID.randomUUID() + "_" + userName;
        String filePath = UPLOADED_FOLDER + fileName + ".webm";
        File file = new File(filePath);

        FileOutputStream fileOutputStream = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, bytesRead);
        }

        return file;
    }

    public void onAudioSavedSuccessfully(File audioFile, Room room, UserSession userSession) {
        SupportedLanguage nativeLanguage = userSession.getNativeLanguage();
        SupportedLanguage translateInto = AudioUtils.getOtherPartyNativeLanguage(room, userSession);
        nriAudioCodec.createAudioThread(audioFile.getAbsolutePath(), room, userSession, nativeLanguage, translateInto);
    }


}
