package com.chatia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Conversation {
    public final String id;
    public String title;
    public long updatedAt;
    public final List<ChatMessage> messages = new ArrayList<>();

    public Conversation(String title) { this(UUID.randomUUID().toString(), title, System.currentTimeMillis()); }
    public Conversation(String id, String title, long updatedAt) { this.id = id; this.title = title; this.updatedAt = updatedAt; }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        JSONArray items = new JSONArray();
        for (ChatMessage message : messages) items.put(message.toJson());
        json.put("id", id); json.put("title", title); json.put("updatedAt", updatedAt); json.put("messages", items);
        return json;
    }

    public static Conversation fromJson(JSONObject json) throws JSONException {
        Conversation conversation = new Conversation(json.getString("id"), json.getString("title"), json.getLong("updatedAt"));
        JSONArray items = json.getJSONArray("messages");
        for (int i = 0; i < items.length(); i++) conversation.messages.add(ChatMessage.fromJson(items.getJSONObject(i)));
        return conversation;
    }
}
