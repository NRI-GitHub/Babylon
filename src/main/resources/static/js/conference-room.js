/*
 * (C) Copyright 2014 Kurento (http://kurento.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

var ws = new WebSocket('wss://' + location.host + '/groupcall');
var participants = {};
var name;

window.onbeforeunload = function() {
	ws.close();
};

ws.onopen = function(event) {
  console.log('WebSocket is open now.');
  tryAutoLogin();
};

ws.onmessage = function(message) {
	var parsedMessage = JSON.parse(message.data);
	console.info('Received message: ' + message.data);

	switch (parsedMessage.id) {
	case 'existingParticipants':
		onExistingParticipants(parsedMessage);
		break;
	case 'incomingAudioLog':
		onIncomingAudioLog(parsedMessage);
		break;
	case 'newParticipantArrived':
		onNewParticipant(parsedMessage);
		break;
	case 'participantLeft':
		onParticipantLeft(parsedMessage);
		break;
	case 'receiveVideoAnswer':
		receiveVideoResponse(parsedMessage);
		break;
	case 'iceCandidate':
		participants[parsedMessage.name].rtcPeer.addIceCandidate(parsedMessage.candidate, function (error) {
	        if (error) {
		      console.error("Error adding candidate: " + error);
		      return;
	        }
	    });
	    break;
	default:
		console.error('Unrecognized message', parsedMessage);
	}
}

function autoRegister() {
    console.info('auto reg was clicked');
    registerMain("TimTim", "nri5764a7cc", "voiceId", 1);
}

function register() {
    name = document.getElementById('name').value;
    var room = document.getElementById('roomName').value;
    var voiceId = document.getElementById('voice').value;
    var languageId = document.getElementById('language').value;

    registerMain(name, room, voiceId, languageId);
}

function registerMain(userName, room, voiceId, languageId){
    user = userName;

    // Check if room name is "nri5764a7cc", if not, show an alert dialog
    if (room !== 'nri5764a7cc') {
        alert('Unsupported room name!');
        return;  // Stop execution of the function if room name isn't correct
    }

    document.getElementById('room-header').innerText = 'ROOM ' + room;
    document.getElementById('join').style.display = 'none';
    document.getElementById('room').style.display = 'block';

    var message = {
        id : 'joinRoom',
        name : name,
        room : room,
        languageId : languageId,
        voiceId : voiceId
    }
    sendMessage(message);
}


function onNewParticipant(request) {
	receiveVideo(request.name);
}

function receiveVideoResponse(result) {
	participants[result.name].rtcPeer.processAnswer (result.sdpAnswer, function (error) {
		if (error) return console.error (error);
	});
}

function callResponse(message) {
	if (message.response != 'accepted') {
		console.info('Call not accepted by peer. Closing call');
		stop();
	} else {
		webRtcPeer.processAnswer(message.sdpAnswer, function (error) {
			if (error) return console.error (error);
		});
	}
}

function onExistingParticipants(msg) {
	var constraints = {
		audio : true,
		video : {
			mandatory : {
				maxWidth : 320,
				maxFrameRate : 15,
				minFrameRate : 15
			}
		}
	};
	console.log(name + " registered in room " + room);
	var participant = new Participant(name);
	participants[name] = participant;
	var video = participant.getVideoElement();

	var options = {
	      localVideo: video,
	      mediaConstraints: constraints,
	      onicecandidate: participant.onIceCandidate.bind(participant)
	    }
	participant.rtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerSendonly(options,
		function (error) {
		  if(error) {
			  return console.error(error);
		  }
		  this.generateOffer (participant.offerToReceiveVideo.bind(participant));
	});

	msg.data.forEach(receiveVideo);
}

function leaveRoom() {
	sendMessage({
		id : 'leaveRoom'
	});

	for ( var key in participants) {
		participants[key].dispose();
	}

	document.getElementById('join').style.display = 'block';
	document.getElementById('room').style.display = 'none';

	ws.close();

	window.location.href = "/register";
}

function receiveVideo(sender) {
	var participant = new Participant(sender);
	participants[sender] = participant;
	var video = participant.getVideoElement();

	var options = {
      remoteVideo: video,
      onicecandidate: participant.onIceCandidate.bind(participant)
    }

	participant.rtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerRecvonly(options,
			function (error) {
			  if(error) {
				  return console.error(error);
			  }
			  this.generateOffer (participant.offerToReceiveVideo.bind(participant));
	});;
}

function onParticipantLeft(request) {
	console.log('Participant ' + request.name + ' left');
	var participant = participants[request.name];
	participant.dispose();
	delete participants[request.name];
}

function onIncomingAudioLog(parsedMessage) {
  var message = parsedMessage.data.message;
  var userId = parsedMessage.data.userId;
  var userName = parsedMessage.data.userName;
  var userIconColor = parsedMessage.data.userIconColor;

  // Create chat entry elements
  var chatEntry = document.createElement('div');
  chatEntry.className = 'log-entry';
  var userImage = document.createElement('div');
  userImage.className = 'person-picture';
  userImage.textContent = userName.charAt(0); // Set the first character of the name as the icon
  userImage.style.backgroundColor = userIconColor;

  var logContent = document.createElement('div');
  logContent.className = 'log-content';
  var messageParagraph = document.createElement('p');
  messageParagraph.textContent = message;

  // Append elements to chat entry
  logContent.appendChild(messageParagraph);
  chatEntry.appendChild(userImage);
  chatEntry.appendChild(logContent);

  // Append chat entry to the chat box
  var chatBox = document.getElementById('chat-box');
  chatBox.appendChild(chatEntry);

  // Scroll to the bottom of the chat box
  chatBox.scrollTop = chatBox.scrollHeight;
}

function tryAutoLogin(){
    var nameElement = document.getElementById('myName');
    var roomElement = document.getElementById('myRoomName');
    var voiceElement = document.getElementById('myVoice');
    var languageElement = document.getElementById('myLanguage');

    name = nameElement ? nameElement.value : null;
    var room = roomElement ? roomElement.value : null;
    var voiceId = voiceElement ? voiceElement.value : null;
    var languageId = languageElement ? languageElement.value : null;

    if(name && room && voiceId && languageId) {
        console.info('auto register YES');
        registerMain(name, room, voiceId, languageId);
    } else {
        console.info('auto register NO');
    }
}

function sendMessage(message) {
	var jsonMessage = JSON.stringify(message);
	console.log('Sending message: ' + jsonMessage);
	ws.send(jsonMessage);
}

function toggleCamera() {
  var button = document.getElementById("cameraButton");
  var cameraIcon = button.querySelector("ion-icon");

  if (button.classList.contains("rounded-camera-button-on")) {
    button.classList.remove("rounded-camera-button-on");
    button.classList.add("rounded-camera-button-off");
    cameraIcon.setAttribute("name", "videocam-off");
    button.setAttribute("title", "Turn Camera On");
  } else if (button.classList.contains("rounded-camera-button-off")) {
    button.classList.remove("rounded-camera-button-off");
    button.classList.add("rounded-camera-button-on");
    cameraIcon.setAttribute("name", "videocam");
    button.setAttribute("title", "Turn Camera Off");
  }
}

function toggleMic() {
  var button = document.getElementById("micButton");
  var micIcon = button.querySelector("ion-icon");
  var muteMe = participants[name];

  if (button.classList.contains("rounded-mic-button-on")) {
    button.classList.remove("rounded-mic-button-on");
    button.classList.add("rounded-mic-button-off");
    micIcon.setAttribute("name", "mic-off");
    button.setAttribute("title", "Turn Microphone On");

    // Call the function here to toggle audio off
    muteMe.rtcPeer.audioEnabled = false;

  } else if (button.classList.contains("rounded-mic-button-off")) {
    button.classList.remove("rounded-mic-button-off");
    button.classList.add("rounded-mic-button-on");
    micIcon.setAttribute("name", "mic");
    button.setAttribute("title", "Turn Microphone Off");

    // Call the function here to toggle audio on
    muteMe.rtcPeer.audioEnabled = true;
  }
}

function endCall() {
  alert("Call Ended!");
}
