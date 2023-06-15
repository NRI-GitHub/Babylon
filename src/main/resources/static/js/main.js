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
  
    if (button.classList.contains("rounded-mic-button-on")) {
      button.classList.remove("rounded-mic-button-on");
      button.classList.add("rounded-mic-button-off");
      micIcon.setAttribute("name", "mic-off");
      button.setAttribute("title", "Turn Microphone On");
    } else if (button.classList.contains("rounded-mic-button-off")) {
      button.classList.remove("rounded-mic-button-off");
      button.classList.add("rounded-mic-button-on");
      micIcon.setAttribute("name", "mic");
      button.setAttribute("title", "Turn Microphone Off");
    }
  }

  function endCall() {
    alert("Call Ended!");
  }

  function leftClick() {
    var btn = document.getElementById("btn");
    btn.style.transform = "translateX(0)";
    document.getElementById("translatedBtn").style.color = "#fff";
    document.getElementById("nativeBtn").style.color = "#000";
  }
  
  function rightClick() {
    var btn = document.getElementById("btn");
    btn.style.transform = "translateX(110px)";
    document.getElementById("translatedBtn").style.color = "#000";
    document.getElementById("nativeBtn").style.color = "#fff";
  }

  document.addEventListener("DOMContentLoaded", function() {
    var toggleState = localStorage.getItem("toggleState");
    if (toggleState === "left") {
      leftClick();
    } else if (toggleState === "right") {
      rightClick();
    } else {
      // Set initial state when no toggle state is stored
      leftClick();
      localStorage.setItem("toggleState", "left");
    }
  });