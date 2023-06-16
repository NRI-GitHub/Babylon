package com.nri.babylon.view.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nri.library.text_translation.enums.SupportedLanguage;

public class AudioLogMessage {
    private final String message;
    private final String userId;
    private final String userName;
    private final String userIconColor;
    private final SupportedLanguage language;

    public AudioLogMessage(String message, String userId, String userName, String userIconColor, SupportedLanguage language) {
        this.message = message;
        this.userId = userId;
        this.userName = userName;
        this.userIconColor = userIconColor;
        this.language = language;
    }

    public String getMessage() {
        return message;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message", message);
        jsonObject.addProperty("userId", userId);
        jsonObject.addProperty("userName", userName);
        jsonObject.addProperty("userIconColor", userIconColor);
        return jsonObject;
    }
}
