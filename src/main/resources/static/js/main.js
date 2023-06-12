function changeCameraColor() {
    var button = document.getElementById("cameraButton");
    
    if (button.classList.contains("rounded-camera-button-on")) {
      button.classList.remove("rounded-camera-button-on");
      button.classList.add("rounded-camera-button-off");
    } else if (button.classList.contains("rounded-camera-button-off")) {
        button.classList.remove("rounded-camera-button-off");
        button.classList.add("rounded-camera-button-on");
    }

    var cameraButton = document.getElementById("cameraButton");
    var cameraIcon = cameraButton.querySelector("ion-icon");

    if (cameraIcon.getAttribute("name") === "videocam") {
        cameraIcon.setAttribute("name", "videocam-off");
    } else {
        cameraIcon.setAttribute("name", "videocam");
    }
  }

  function changeMicColor() {
    var button = document.getElementById("micButton");
    
    if (button.classList.contains("rounded-mic-button-on")) {
      button.classList.remove("rounded-mic-button-on");
      button.classList.add("rounded-mic-button-off");
    } else if (button.classList.contains("rounded-mic-button-off")) {
        button.classList.remove("rounded-mic-button-off");
        button.classList.add("rounded-mic-button-on");
    }

    var micButton = document.getElementById("micButton");
    var micIcon = micButton.querySelector("ion-icon");

    if (micIcon.getAttribute("name") === "mic") {
        micIcon.setAttribute("name", "mic-off");
    } else {
        micIcon.setAttribute("name", "mic");
    }
  }

  function endCall() {
    alert("Call Ended!");
  }