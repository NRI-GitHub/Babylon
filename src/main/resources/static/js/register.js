document.addEventListener("DOMContentLoaded", function() {
	console.info('DOMContentLoaded');

    var nameElement = document.getElementById('myName');
    var roomElement = document.getElementById('myRoomName');
    var voiceElement = document.getElementById('myVoice');
    var languageElement = document.getElementById('myLanguage');

    var name = nameElement ? nameElement.value : null;
    var room = roomElement ? roomElement.value : null;
    var voiceId = voiceElement ? voiceElement.value : null;
    var languageId = languageElement ? languageElement.value : null;

    if(name && room && voiceId && languageId) {
    	console.info('auto register YES');
        registerMain(name, room, voiceId, languageId);
    } else {
    	console.info('auto register NO');
    }
});
