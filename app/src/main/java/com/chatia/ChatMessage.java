package com.chatia;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatMessage {
    public final String id;
    public final String text;
    public final boolean fromUser;
    public final long createdAt;

    public ChatMessage(String id, String text, boolean fromUser, long createdAt) {
        this.id = id; this.text = text; this.fromUser = fromUser; this.createdAt = createdAt;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id); json.put("text", text); json.put("fromUser", fromUser); json.put("createdAt", createdAt);
        return json;
    }

    public static ChatMessage fromJson(JSONObject json) throws JSONException {
        return new ChatMessage(json.getString("id"), json.getString("text"), json.getBoolean("fromUser"), json.getLong("createdAt"));
    }
}
