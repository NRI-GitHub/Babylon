package com.nri.babylon.view.model;

public class Register {
    private String name;
    private String room;
    private String voice;
    private String languageId;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public String getLanguageId() {
        return languageId;
    }

    public void setLanguageId(String languageId) {
        this.languageId = languageId;
    }

    public boolean validate() {
        if (name == null || name.isEmpty())
            return false;
        if (room == null || room.isEmpty())
            return false;
        if (voice == null || voice.isEmpty())
            return false;
        if (languageId == null || languageId.isEmpty())
            return false;


        name = name.replace(" ", "_");
        return true;
    }
}

