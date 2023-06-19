var socket;
var mediaRecorder;
var chunks = [];
var audioContext;
var audioStream;

function startRecording() {
    navigator.mediaDevices.getUserMedia({ audio: true })
        .then(function (stream) {
            audioContext = new AudioContext();
            audioStream = stream;
            mediaRecorder = new MediaRecorder(stream);
            mediaRecorder.ondataavailable = function (event) {
                chunks.push(event.data);
            };
            mediaRecorder.onstop = function () {
                var audioBlob = new Blob(chunks, { type: 'audio/webm' });
                chunks = [];
                var reader = new FileReader();
                reader.onloadend = function () {
                    var audioData = reader.result;
                    sendAudio(audioData);
                };
                reader.readAsBinaryString(audioBlob);
            };

            mediaRecorder.start();
            console.log('Recording started');

            document.getElementById('start_button').disabled = true;
            document.getElementById('stop_button').disabled = false;
        })
        .catch(function (error) {
            console.error('Error accessing microphone:', error);
        });
}

function stopRecording() {
    mediaRecorder.stop();
    audioStream.getTracks().forEach(function (track) {
        track.stop();
    });
    audioContext.close();
    console.log('Recording stopped');

    document.getElementById('start_button').disabled = false;
    document.getElementById('stop_button').disabled = true;
}

function sendAudio(audioData) {
    if (!socket || socket.readyState !== WebSocket.OPEN) {
        console.error('WebSocket is not open');
        return;
    }

    var payload = {
        audio_data: btoa(audioData)
    };
    socket.send(JSON.stringify(payload));
}

function displayTranscript(transcript) {
    var transcriptElement = document.getElementById('transcript');
    transcriptElement.innerText += transcript + '\n';
}

function initializeWebSocket() {
    // Replace with the actual endpoint of your Spring Boot server
    socket = new WebSocket('wss://' + location.host + '/proxy');

    socket.onopen = function () {
        console.log('WebSocket connected');
    };

    socket.onmessage = function (event) {
        var message = JSON.parse(event.data);
        if (message.message_type === 'PartialTranscript') {
            console.log('Partial transcript received: ' + message.text);
            displayTranscript('Partial transcript: ' + message.text);
        } else if (message.message_type === 'FinalTranscript') {
            console.log('Final transcript received: ' + message.text);
            displayTranscript('Final transcript: ' + message.text);
        }
    };

    socket.onerror = function (error) {
        console.error('WebSocket error:', error);
    };

    socket.onclose = function () {
        console.log('WebSocket closed');
    };
}

document.getElementById('start_button').addEventListener('click', function () {
    initializeWebSocket();
    startRecording();
});

document.getElementById('stop_button').addEventListener('click', stopRecording);
