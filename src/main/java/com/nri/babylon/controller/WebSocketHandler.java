package com.nri.babylon.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.nri.babylon.Util;
import org.kurento.client.*;
import org.kurento.jsonrpc.JsonUtils;
import org.kurento.tutorial.groupcall.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(Handler.class);
    private static final Gson gson = new GsonBuilder().create();

    private final ConcurrentHashMap<String, UserSession> users = new ConcurrentHashMap<>();

    @Autowired
    private KurentoClient kurento;

    private static void startWebRtcEndpoint(WebRtcEndpoint webRtcEp) {
        // Calling gatherCandidates() is when the Endpoint actually starts working.
        // In this tutorial, this is emphasized for demonstration purposes by
        // launching the ICE candidate gathering in its own method.
        webRtcEp.gatherCandidates();
    }

    /**
     * Invoked after WebSocket negotiation has succeeded and the WebSocket connection is
     * opened and ready for use.
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("[Handler::afterConnectionEstablished] New WebSocket connection, sessionId: {}",
                session.getId());
    }

    /**
     * Invoked after the WebSocket connection has been closed by either side, or after a
     * transport error has occurred. Although the session may technically still be open,
     * depending on the underlying implementation, sending messages at this point is
     * discouraged and most likely will not succeed.
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        if (!status.equalsCode(CloseStatus.NORMAL)) {
            log.warn("[Handler::afterConnectionClosed] status: {}, sessionId: {}", status, session.getId());
        }

        stop(session);
    }

    /**
     * Invoked when a new WebSocket message arrives.
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);


        log.info("[Handler::handleTextMessage] message: {}, sessionId: {}", jsonMessage, sessionId);

        try {
            String messageId = jsonMessage.get("id").getAsString();
            switch (messageId) {
                case "PROCESS_SDP_OFFER":
                    // Start: Create user session and process SDP Offer
                    handleProcessSdpOffer(session, jsonMessage);
                    break;
                case "ADD_ICE_CANDIDATE":
                    handleAddIceCandidate(session, jsonMessage);
                    break;
                case "STOP":
                    handleStop(session, jsonMessage);
                    break;
                case "ERROR":
                    handleError(session, jsonMessage);
                    break;
                default:
                    // Ignore the message
                    log.warn("[Handler::handleTextMessage] Skip, invalid message, id: {}",
                            messageId);
                    break;
            }
        } catch (Throwable ex) {
            log.error("[Handler::handleTextMessage] Exception: {}, sessionId: {}", ex, sessionId);
            sendError(session, "[Kurento] Exception: " + ex.getMessage());
        }
    }

    /**
     * Handle an error from the underlying WebSocket message transport.
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("[Handler::handleTransportError] Exception: {}, sessionId: {}", exception, session.getId());

        session.close(CloseStatus.SERVER_ERROR);
    }

    private synchronized void sendMessage(WebSocketSession session, String message) {
        log.debug("[Handler::sendMessage] {}", message);

        if (!session.isOpen()) {
            log.warn("[Handler::sendMessage] Skip, WebSocket session isn't open");
            return;
        }

        String sessionId = session.getId();
        if (!users.containsKey(sessionId)) {
            log.warn("[Handler::sendMessage] Skip, unknown user, id: {}", sessionId);
            return;
        }

        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException ex) {
            log.error("[Handler::sendMessage] Exception: {}", ex.getMessage());
        }
    }

    // PROCESS_SDP_OFFER ---------------------------------------------------------

    private void sendError(WebSocketSession session, String errMsg) {
        log.error(errMsg);

        if (users.containsKey(session.getId())) {
            JsonObject message = new JsonObject();
            message.addProperty("id", "ERROR");
            message.addProperty("message", errMsg);
            sendMessage(session, message.toString());
        }
    }

    private void initBaseEventListeners(WebSocketSession session, BaseRtpEndpoint baseRtpEp, String className) {
        log.info("[Handler::initBaseEventListeners] name: {}, class: {}, sessionId: {}", baseRtpEp.getName(), className, session.getId());

        // Event: Some error happened
        baseRtpEp.addErrorListener(ev -> {
            log.error("[{}::ErrorEvent] Error code {}: '{}', source: {}, timestamp: {}, tags: {}, description: {}",
                    className, ev.getErrorCode(), ev.getType(), ev.getSource().getName(),
                    ev.getTimestampMillis(), ev.getTags(), ev.getDescription());

            sendError(session, "[Kurento] " + ev.getDescription());
            stop(session);
        });

        // Event: Media is flowing into this sink
        baseRtpEp.addMediaFlowInStateChangedListener(
                ev -> log.info("[{}::{}] source: {}, timestamp: {}, tags: {}, state: {}, padName: {}, mediaType: {}",
                        className, ev.getType(), ev.getSource().getName(), ev.getTimestampMillis(),
                        ev.getTags(), ev.getState(), ev.getPadName(), ev.getMediaType()));

        // Event: Media is flowing out of this source
        baseRtpEp.addMediaFlowOutStateChangedListener(
                ev -> log.info("[{}::{}] source: {}, timestamp: {}, tags: {}, state: {}, padName: {}, mediaType: {}",
                        className, ev.getType(), ev.getSource().getName(), ev.getTimestampMillis(),
                        ev.getTags(), ev.getState(), ev.getPadName(), ev.getMediaType()));

        // Event: [TODO write meaning of this event]
        baseRtpEp.addConnectionStateChangedListener(
                ev -> log.info("[{}::{}] source: {}, timestamp: {}, tags: {}, oldState: {}, newState: {}",
                        className, ev.getType(), ev.getSource().getName(), ev.getTimestampMillis(),
                        ev.getTags(), ev.getOldState(), ev.getNewState()));

        // Event: [TODO write meaning of this event]
        baseRtpEp.addMediaStateChangedListener(
                ev -> log.info("[{}::{}] source: {}, timestamp: {}, tags: {}, oldState: {}, newState: {}",
                        className, ev.getType(), ev.getSource().getName(), ev.getTimestampMillis(),
                        ev.getTags(), ev.getOldState(), ev.getNewState()));

        // Event: This element will (or will not) perform media transcoding
        baseRtpEp.addMediaTranscodingStateChangedListener(
                ev -> log.info("[{}::{}] source: {}, timestamp: {}, tags: {}, state: {}, binName: {}, mediaType: {}",
                        className, ev.getType(), ev.getSource().getName(), ev.getTimestampMillis(),
                        ev.getTags(), ev.getState(), ev.getBinName(), ev.getMediaType()));
    }

    private void initWebRtcEventListeners(WebSocketSession session, WebRtcEndpoint webRtcEp) {
        log.info("[Handler::initWebRtcEventListeners] name: {}, sessionId: {}",
                webRtcEp.getName(), session.getId());

        // Event: The ICE backend found a local candidate during Trickle ICE
        webRtcEp.addIceCandidateFoundListener(
                ev -> {
                    log.debug("[WebRtcEndpoint::{}] source: {}, timestamp: {}, tags: {}, candidate: {}",
                            ev.getType(), ev.getSource().getName(), ev.getTimestampMillis(),
                            ev.getTags(), JsonUtils.toJson(ev.getCandidate()));

                    JsonObject message = new JsonObject();
                    message.addProperty("id", "ADD_ICE_CANDIDATE");
                    message.add("candidate", JsonUtils.toJsonObject(ev.getCandidate()));
                    sendMessage(session, message.toString());
                });

        // Event: The ICE backend changed state
        webRtcEp.addIceComponentStateChangedListener(
                ev -> log.debug("[WebRtcEndpoint::{}] source: {}, timestamp: {}, tags: {}, streamId: {}, componentId: {}, state: {}",
                        ev.getType(), ev.getSource().getName(), ev.getTimestampMillis(),
                        ev.getTags(), ev.getStreamId(), ev.getComponentId(), ev.getState()));

        // Event: The ICE backend finished gathering ICE candidates
        webRtcEp.addIceGatheringDoneListener(
                ev -> log.info("[WebRtcEndpoint::{}] source: {}, timestamp: {}, tags: {}",
                        ev.getType(), ev.getSource().getName(), ev.getTimestampMillis(),
                        ev.getTags()));

        // Event: The ICE backend selected a new pair of ICE candidates for use
        webRtcEp.addNewCandidatePairSelectedListener(
                ev -> log.info("[WebRtcEndpoint::{}] name: {}, timestamp: {}, tags: {}, streamId: {}, local: {}, remote: {}",
                        ev.getType(), ev.getSource().getName(), ev.getTimestampMillis(),
                        ev.getTags(), ev.getCandidatePair().getStreamId(),
                        ev.getCandidatePair().getLocalCandidate(),
                        ev.getCandidatePair().getRemoteCandidate()));
    }

    private void initWebRtcEndpoint(WebSocketSession session, WebRtcEndpoint webRtcEp, String sdpOffer) {
        initBaseEventListeners(session, webRtcEp, "WebRtcEndpoint");
        initWebRtcEventListeners(session, webRtcEp);

        String sessionId = session.getId();
        String name = "user" + sessionId + "_webrtcendpoint";
        webRtcEp.setName(name);

        // Continue the SDP Negotiation: Generate an SDP Answer
        String sdpAnswer = webRtcEp.processOffer(sdpOffer);

        log.info("[Handler::initWebRtcEndpoint] name: {}, SDP Offer from browser to KMS:\n{}",
                name, sdpOffer);
        log.info("[Handler::initWebRtcEndpoint] name: {}, SDP Answer from KMS to browser:\n{}",
                name, sdpAnswer);

        JsonObject message = new JsonObject();
        message.addProperty("id", "PROCESS_SDP_ANSWER");
        message.addProperty("sdpAnswer", sdpAnswer);
        sendMessage(session, message.toString());
    }

    private void handleProcessSdpOffer(WebSocketSession session, JsonObject jsonMessage) {

        // ---- Session handling
        String sessionId = session.getId();
        log.info("[Handler::handleStart] User count: {}", users.size());
        log.info("[Handler::handleStart] New user, id: {}", sessionId);
        UserSession user = new UserSession(null, null, null, null, null, null);
        users.put(sessionId, user);

        // ---- Media pipeline
        log.info("[Handler::handleStart] Create Media Pipeline");
        MediaPipeline pipeline = kurento.createMediaPipeline();
        //user.setMediaPipeline(pipeline);

        WebRtcEndpoint webRtcEp = new WebRtcEndpoint.Builder(pipeline).build();
        //user.setWebRtcEndpoint(webRtcEp);
        webRtcEp.connect(webRtcEp, MediaType.VIDEO);

        String ipAddress = Util.recorderEndpointIpAddress();

        //Connect to our recording endpoint
        RecorderEndpoint recordMyAudio = new RecorderEndpoint.Builder(pipeline, "https://"+ipAddress+"/acceptAudio/"+sessionId).withMediaProfile(MediaProfileSpecType.WEBM_AUDIO_ONLY).build();
        webRtcEp.connect(recordMyAudio, MediaType.AUDIO);
        recordMyAudio.record();

        //Playback the translated audio
        PlayerEndpoint receivedAudio = new PlayerEndpoint.Builder(pipeline, "https://"+ipAddress+"/sendAudio/"+sessionId).build();
        receivedAudio.connect(webRtcEp, MediaType.AUDIO);

        // ---- Endpoint configuration
        String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
        initWebRtcEndpoint(session, webRtcEp, sdpOffer);

        new Thread(() -> {
            try {
                stopAndStartAudioTest(receivedAudio);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // ---- Endpoint startup
        startWebRtcEndpoint(webRtcEp);
    }


    // ADD_ICE_CANDIDATE ---------------------------------------------------------
    private void handleAddIceCandidate(WebSocketSession session, JsonObject jsonMessage) {
        String sessionId = session.getId();
        if (!users.containsKey(sessionId)) {
            log.warn("[Handler::handleAddIceCandidate] Skip, unknown user, id: {}",
                    sessionId);
            return;
        }

        UserSession user = users.get(sessionId);
        JsonObject jsonCandidate =
                jsonMessage.get("candidate").getAsJsonObject();
        IceCandidate candidate =
                new IceCandidate(jsonCandidate.get("candidate").getAsString(),
                        jsonCandidate.get("sdpMid").getAsString(),
                        jsonCandidate.get("sdpMLineIndex").getAsInt());

        /*WebRtcEndpoint webRtcEp = user.getWebRtcEndpoint();
        webRtcEp.addIceCandidate(candidate);*/
    }


    // STOP ----------------------------------------------------------------------
    private void stop(WebSocketSession session) {
        // Remove the user session and release all resources
        UserSession user = users.remove(session.getId());
        if (user != null) {
            //MediaPipeline mediaPipeline = user.getMediaPipeline();
            MediaPipeline mediaPipeline = null;
            if (mediaPipeline != null) {
                log.info("[Handler::stop] Release the Media Pipeline");
                mediaPipeline.release();
            }
        }
    }

    private void handleStop(WebSocketSession session, JsonObject jsonMessage) {
        stop(session);
    }


    // ERROR ---------------------------------------------------------------------
    private void handleError(WebSocketSession session, JsonObject jsonMessage) {
        String errMsg = jsonMessage.get("message").getAsString();
        log.error("Browser error: " + errMsg);

        log.info("Assume that the other side stops after an error...");
        stop(session);
    }


    // ---------------------------------------------------------------------------
    private void stopAndStartAudioTest(PlayerEndpoint audio) throws InterruptedException {
        // Connect the next PlayerEndpoint, if it exists
        Thread.sleep(5000);
        audio.play();


        Thread.sleep(5000);
        // Disconnect the current PlayerEndpoint
        audio.stop();
    }

    public void startRecordingWhenAudioOccurs(MediaPipeline pipeline, WebRtcEndpoint webRtcEp) {
        // Specify the URI where the recording should be stored
        String recordingUri = "file:///path/to/recorded/file.webm";

        // Create the RecorderEndpoint
        RecorderEndpoint recorderEndpoint = new RecorderEndpoint.Builder(pipeline, recordingUri)
                .withMediaProfile(MediaProfileSpecType.WEBM_AUDIO_ONLY)
                .build();

        // Connect the WebRtcEndpoint to the RecorderEndpoint (for audio)
        webRtcEp.connect(recorderEndpoint, MediaType.AUDIO);

        // Listen for the MediaFlowInStateChanged event
        recorderEndpoint.addMediaFlowInStateChangeListener(event -> {
            // Check if the media type is AUDIO
            if (event.getMediaType() == MediaType.AUDIO && event.getState() == MediaFlowState.FLOWING) {
                // Start recording
                recorderEndpoint.record();
                System.out.println("Recording started");
            }
        });
    }


    public UserSession getUserById(String sessionId){
        return users.get(sessionId);
    }
}

