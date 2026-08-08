package com.chatia;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChatStore {
    private static final String PREFS = "chatia_store";
    private static final String KEY = "conversations";
    private final SharedPreferences preferences;

    public ChatStore(Context context) { preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE); }

    public List<Conversation> load() {
        List<Conversation> conversations = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(preferences.getString(KEY, "[]"));
            for (int i = 0; i < array.length(); i++) conversations.add(Conversation.fromJson(array.getJSONObject(i)));
        } catch (Exception ignored) { }
        conversations.sort(Comparator.comparingLong((Conversation item) -> item.updatedAt).reversed());
        return conversations;
    }

    public void save(List<Conversation> conversations) {
        JSONArray array = new JSONArray();
        try { for (Conversation conversation : conversations) array.put(conversation.toJson()); } catch (Exception ignored) { }
        preferences.edit().putString(KEY, array.toString()).apply();
    }
}
