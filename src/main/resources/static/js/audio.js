var localVideo;
var remoteVideo;
var webRtcPeer;
var ws;
var startButton;

window.onload = function() {
    startButton = document.getElementById('startButton');
    remoteVideo = document.getElementById('remoteVideo');
    localVideo = document.getElementById('localVideo');
    startButton.addEventListener('click', start);
};

function start() {

    showLocalSpinner();
    showRemoteSpinner();

    var options = {
        localVideo: localVideo,
        remoteVideo: remoteVideo,
        mediaConstraints: { audio: true, video: true },
        onicecandidate: onIceCandidate
    };

    webRtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerSendrecv(options, function (error) {
        if (error) {
            return console.error(error);
        }

        this.generateOffer(onOffer);
    });

    ws = new WebSocket('ws://' + location.host + '/websocket');
    ws.onmessage = function(message) {
        var parsedMessage = JSON.parse(message.data);
        console.info('Received message: ' + message.data);

        switch (parsedMessage.id) {
            case 'PROCESS_SDP_ANSWER':
                webRtcPeer.processAnswer(parsedMessage.sdpAnswer);
                startVideo(remoteVideo);
                break;
            case 'ADD_ICE_CANDIDATE':
                webRtcPeer.addIceCandidate(parsedMessage.candidate);
                break;
            case 'ERROR':
                console.error('Error message from server: ' + parsedMessage.message);
                stop();
                break;
            default:
                console.error('Unrecognized message', parsedMessage);
        }
    };


}



function onIceCandidate(candidate) {
    console.log('Local candidate: ' + JSON.stringify(candidate));
    var message = {
        id: 'ADD_ICE_CANDIDATE',
        candidate: candidate
    };
    sendMessage(message);
}

function onOffer(error, offerSdp) {
    if (error) return console.error('Error generating the offer');

    console.info('Invoking SDP offer callback function');
    var message = {
        id: 'PROCESS_SDP_OFFER',
        sdpOffer: offerSdp
    };
    sendMessage(message);
}

function stop() {
    if (webRtcPeer) {
        var message = {
            id: 'STOP'
        };
        sendMessage(message);
        dispose();
    }
}

function dispose() {
    if (webRtcPeer) {
        webRtcPeer.dispose();
        webRtcPeer = null;
    }
    hideLocalSpinner();
    hideRemoteSpinner();
    if (ws) {
        ws.close();
        ws = null;
    }
}

function sendMessage(message) {
    var jsonMessage = JSON.stringify(message);
    console.log('Sending message: ' + jsonMessage);
    ws.send(jsonMessage);
}

function showLocalSpinner() {
    videoInput = document.getElementById('localVideo');
    videoInput.style.background = 'center transparent url("/img/spinner.gif") no-repeat';
}

function showRemoteSpinner() {
    videoInput = document.getElementById('remoteVideo');
    videoInput.style.background = 'center transparent url("/img/spinner.gif") no-repeat';
}

function hideLocalSpinner() {
    videoInput = document.getElementById('localVideo');
    videoInput.style.background = '';
}

function hideRemoteSpinner() {
    videoInput = document.getElementById('remoteVideo');
    videoInput.style.background = '';
}


function startVideo(video)
{
  // Manually start the <video> HTML element
  // This is used instead of the 'autoplay' attribute, because iOS Safari
  // requires a direct user interaction in order to play a video with audio.
  // Ref: https://developer.mozilla.org/en-US/docs/Web/HTML/Element/video
  video.play().catch((err) => {
    if (err.name === 'NotAllowedError') {
      console.error("[start] Browser doesn't allow playing video: " + err);
    }
    else {
      console.error("[start] Error in video.play(): " + err);
    }
  });
}