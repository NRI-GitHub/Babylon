package com.nri.babylon.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.Principal;
import java.util.Map;

@Controller
public class ProxyWebSocketHandler extends TextWebSocketHandler {
    private Map<String, WebSocketSession> clientSessions;
    private Map<String, WebSocketSession> assemblyAiSessions;
    private final String apiKey = "a5b95d41a0724260bd5217e587a36c14";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI assemblyAiUri = new URI("wss://api.assemblyai.com/v2/realtime/ws?sample_rate=16000");
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("Authorization", apiKey);

        WebSocketSession assemblyAiSession = new StandardWebSocketClient()
                .doHandshake(this, headers, assemblyAiUri)
                .get();

        clientSessions.put(session.getId(), session);
        assemblyAiSessions.put(session.getId(), assemblyAiSession);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        WebSocketSession assemblyAiSession = assemblyAiSessions.get(session.getId());
        assemblyAiSession.sendMessage(message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        WebSocketSession assemblyAiSession = assemblyAiSessions.get(session.getId());
        assemblyAiSession.close();

        clientSessions.remove(session.getId());
        assemblyAiSessions.remove(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        session.close(CloseStatus.SERVER_ERROR);
    }

    @MessageMapping("/audioData")
    @SendToUser("/queue/reply")
    public void receiveAudioData(@Payload String message, Principal principal) throws Exception {
        System.out.println("I received Payload");
        // Convert the string message back to an array of floats
        JsonObject jsonObject = new Gson().fromJson(message, JsonObject.class);
        float[] audioData = new Gson().fromJson(jsonObject.get("audio_data"), float[].class);

        // Convert float audio data to 16-bit PCM
        ByteBuffer byteBuffer = ByteBuffer.allocate(audioData.length * 2).order(ByteOrder.LITTLE_ENDIAN);
        for (float sample : audioData) {
            byteBuffer.putShort((short) (sample * 32767.0f));
        }
        byte[] audioBytes = byteBuffer.array();

        // Retrieve the AssemblyAI WebSocket session for this user
        WebSocketSession assemblyAiSession = assemblyAiSessions.get(principal.getName());

        // Send the audio data to AssemblyAI API via WebSocket
        if (assemblyAiSession != null && assemblyAiSession.isOpen()) {
            System.out.println("Sending Payload back");
            assemblyAiSession.sendMessage(new BinaryMessage(audioBytes));
        }
    }
}
