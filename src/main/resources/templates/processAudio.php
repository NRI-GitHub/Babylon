<?php

// Make sure to include a WebSocket client library like Textalk/websocket-php by running `composer require textalk/websocket`.
require 'vendor/autoload.php';

use WebSocket\Client;

$authHeader = array(
    "Authorization: 83efc43df7e2433d958a125193ef0d2e"
);

$sampleRate = 16000;
$wordBoost = array("HackerNews", "Twitter");
$wordBoostParam = urlencode(json_encode($wordBoost));
$url = "wss://api.assemblyai.com/v2/realtime/ws?word_boost={$wordBoostParam}&sample_rate={$sampleRate}";
$socket = new Client($url, array('headers' => $authHeader));

$socket->on('message', function ($data) {
    $message = json_decode($data, true);
    if ($message['message_type'] === 'SessionBegins') {
        $session_id = $message['session_id'];
        $expires_at = $message['expires_at'];
        echo "Session ID: $session_id\n";
        echo "Expires at: $expires_at\n";
    } elseif ($message["message_type"] === "PartialTranscript") {
        echo "Partial transcript received: " . $message["text"] . "\n";
    } elseif ($message['message_type'] === 'FinalTranscript') {
        echo "Final transcript received: " . $message["text"] . "\n";
    }
});

$socket->on('error', function ($error) {
    $errorObject = json_decode($error, true);
    if ($errorObject['error_code'] === 4001) {
        echo "Not Authorized";
    }
});

$socket->on('close', function () {
    echo "WebSocket closed";
});

function send_audio($socket, $audio_data) {
    $payload = array(
        "audio_data" => base64_encode($audio_data)
    );
    $socket->send(json_encode($payload));
}

function terminateSession($socket) {
    $payload = array("terminate_session" => true);
    $message = json_encode($payload);
    $socket->send($message);
    $socket->close();
}

?>
<!DOCTYPE html>
<html>
<head>
    <title>Speech-to-Text</title>
</head>
<body>
    <h1>Speech-to-Text</h1>

    <button id="start_button">Start Recording</button>
    <button id="stop_button" disabled>Stop Recording</button>
    <br>
    <p id="transcript"></p>

    <script>
        var socket = new WebSocket('wss://api.assemblyai.com/v2/realtime/ws');
        var mediaRecorder;
        var chunks = [];
        var audioContext;
        var audioStream;
        var sampleRate = 16000;

        socket.onopen = function() {
            console.log('WebSocket connected');
            socket.send(JSON.stringify({ sample_rate: sampleRate }));
        };

        socket.onmessage = function(event) {
            var message = JSON.parse(event.data);
            if (message.message_type === 'SessionBegins') {
                console.log('Session ID: ' + message.session_id);
                console.log('Expires at: ' + message.expires_at);
            } else if (message.message_type === 'PartialTranscript') {
                console.log('Partial transcript received: ' + message.text);
                document.getElementById('transcript').innerText += 'Partial transcript: ' + message.text + '\n';
            } else if (message.message_type === 'FinalTranscript') {
                console.log('Final transcript received: ' + message.text);
                document.getElementById('transcript').innerText += 'Final transcript: ' + message.text + '\n';
            }
        };

        socket.onerror = function(error) {
            console.error('WebSocket error:', error);
        };

        socket.onclose = function() {
            console.log('WebSocket closed');
        };

        function startRecording() {
            navigator.mediaDevices.getUserMedia({ audio: true })
                .then(function(stream) {
                    audioContext = new AudioContext();
                    audioStream = stream;
                    mediaRecorder = new MediaRecorder(stream);
                    mediaRecorder.ondataavailable = function(event) {
                        chunks.push(event.data);
                    };
                    mediaRecorder.onstop = function() {
                        var audioBlob = new Blob(chunks, { type: 'audio/webm' });
                        chunks = [];
                        var reader = new FileReader();
                        reader.onloadend = function() {
                            var audioData = reader.result;
                            send_audio(socket, audioData);
                        };
                        reader.readAsBinaryString(audioBlob);
                    };

                    mediaRecorder.start();
                    console.log('Recording started');

                    document.getElementById('start_button').disabled = true;
                    document.getElementById('stop_button').disabled = false;
                })
                .catch(function(error) {
                    console.error('Error accessing microphone:', error);
                });
        }

        function stopRecording() {
            mediaRecorder.stop();
            audioStream.getTracks().forEach(function(track) {
                track.stop();
            });
            audioContext.close();
            console.log('Recording stopped');

            document.getElementById('start_button').disabled = false;
            document.getElementById('stop_button').disabled = true;
        }

        document.getElementById('start_button').addEventListener('click', startRecording);
        document.getElementById('stop_button').addEventListener('click', stopRecording);
    </script>
</body>
</html>