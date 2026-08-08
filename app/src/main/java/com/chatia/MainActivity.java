package com.chatia;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.text.DateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private final List<Conversation> conversations = new ArrayList<>();
    private final AiService aiService = new LocalAiService();
    private ChatStore store;
    private Conversation current;
    private LinearLayout recentList, messagesList;
    private HorizontalScrollView recentScroller;
    private ScrollView messageScroller;
    private TextView typingIndicator;
    private EditText input;
    private String lastUserPrompt = "";
    private static final int SPEECH_REQUEST = 44;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        store = new ChatStore(this);
        conversations.addAll(store.load());
        if (conversations.isEmpty()) conversations.add(new Conversation("Nueva conversación"));
        current = conversations.get(0);
        setContentView(buildLayout());
        renderConversations(); renderMessages();
    }

    private View buildLayout() {
        LinearLayout root = column(); root.setBackgroundColor(Color.rgb(241,245,249));
        root.addView(topBar());
        recentScroller = new HorizontalScrollView(this); recentScroller.setHorizontalScrollBarEnabled(false);
        recentList = row(); recentList.setPadding(16,12,16,12); recentScroller.addView(recentList);
        root.addView(recentScroller, new LinearLayout.LayoutParams(-1, -2));
        messageScroller = new ScrollView(this); messagesList = column(); messagesList.setPadding(16, 8, 16, 8); messageScroller.addView(messagesList);
        root.addView(messageScroller, new LinearLayout.LayoutParams(-1, 0, 1));
        typingIndicator = text("", 14, Color.rgb(71,85,105)); typingIndicator.setPadding(20, 6, 20, 6); root.addView(typingIndicator);
        root.addView(composer()); return root;
    }

    private View topBar() {
        LinearLayout bar = row(); bar.setGravity(Gravity.CENTER_VERTICAL); bar.setPadding(18,20,18,14); bar.setBackgroundColor(Color.rgb(15,23,42));
        TextView logo = text("◉", 30, Color.rgb(96,165,250)); TextView title = text("  ChatIA", 24, Color.WHITE); title.setTypeface(null, 1);
        Button add = button("+ Nueva"); add.setOnClickListener(v -> newConversation());
        bar.addView(logo); bar.addView(title, new LinearLayout.LayoutParams(0, -2, 1)); bar.addView(add); return bar;
    }

    private View composer() {
        LinearLayout box = row(); box.setPadding(12, 10, 12, 14); box.setGravity(Gravity.BOTTOM); box.setBackgroundColor(Color.WHITE);
        input = new EditText(this); input.setHint("Escribe un mensaje largo o corto..."); input.setMinLines(1); input.setMaxLines(6); input.setTextSize(16); input.setBackgroundColor(Color.rgb(248,250,252)); input.setPadding(18,12,18,12);
        Button mic = button("🎤"); mic.setOnClickListener(v -> startVoiceInput());
        Button send = button("Enviar"); send.setOnClickListener(v -> sendMessage(input.getText().toString()));
        box.addView(input, new LinearLayout.LayoutParams(0, -2, 1)); box.addView(mic); box.addView(send); return box;
    }

    private void renderConversations() {
        recentList.removeAllViews();
        for (Conversation conversation : conversations) {
            Button item = button(conversation.title); item.setAllCaps(false);
            item.setOnClickListener(v -> { current = conversation; renderMessages(); });
            item.setOnLongClickListener(v -> { deleteConversation(conversation); return true; });
            recentList.addView(item);
        }
    }

    private void renderMessages() {
        messagesList.removeAllViews();
        if (current.messages.isEmpty()) addWelcome();
        for (ChatMessage message : current.messages) messagesList.addView(messageView(message));
        scrollBottom();
    }

    private void addWelcome() {
        current.messages.add(new ChatMessage(UUID.randomUUID().toString(), "Hola, soy ChatIA. Escribe una pregunta, pega un texto para resumir o pide código. Mantendré el historial de esta conversación.", false, System.currentTimeMillis()));
        save();
    }

    private View messageView(ChatMessage message) {
        LinearLayout wrapper = column(); wrapper.setGravity(message.fromUser ? Gravity.RIGHT : Gravity.LEFT); wrapper.setPadding(0, 6, 0, 6);
        TextView bubble = text(message.text + "\n\n" + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(message.createdAt)), 15, Color.WHITE);
        bubble.setPadding(18,14,18,14); bubble.setTextIsSelectable(true); bubble.setBackgroundColor(message.fromUser ? Color.rgb(22,163,74) : Color.rgb(37,99,235));
        wrapper.addView(bubble, new LinearLayout.LayoutParams((int)(getResources().getDisplayMetrics().widthPixels * .82), -2));
        if (!message.fromUser) wrapper.addView(actionsFor(message));
        return wrapper;
    }

    private View actionsFor(ChatMessage message) {
        LinearLayout actions = row(); actions.setGravity(Gravity.LEFT);
        Button copy = smallButton("Copiar"); copy.setOnClickListener(v -> copyText(message.text));
        Button share = smallButton("Compartir"); share.setOnClickListener(v -> shareText(message.text));
        Button regen = smallButton("Regenerar"); regen.setOnClickListener(v -> regenerate());
        actions.addView(copy); actions.addView(share); actions.addView(regen); return actions;
    }

    private void sendMessage(String text) {
        String prompt = text.trim(); if (prompt.isEmpty()) return;
        lastUserPrompt = prompt; input.setText(""); hideKeyboard();
        current.messages.add(new ChatMessage(UUID.randomUUID().toString(), prompt, true, System.currentTimeMillis()));
        current.title = prompt.length() > 28 ? prompt.substring(0, 28) + "…" : prompt; current.updatedAt = System.currentTimeMillis(); save(); renderMessages();
        typingIndicator.setText("ChatIA está escribiendo…");
        aiService.send(prompt, new AiService.Callback() {
            @Override public void onSuccess(String reply) { typingIndicator.setText(""); current.messages.add(new ChatMessage(UUID.randomUUID().toString(), reply, false, System.currentTimeMillis())); current.updatedAt = System.currentTimeMillis(); save(); renderMessages(); }
            @Override public void onError(Exception error) { typingIndicator.setText(""); Toast.makeText(MainActivity.this, "Error de conexión. Inténtalo de nuevo.", Toast.LENGTH_LONG).show(); }
        });
    }

    private void regenerate() { if (!lastUserPrompt.isEmpty()) sendMessage(lastUserPrompt); }
    private void newConversation() { Conversation c = new Conversation("Nueva conversación"); conversations.add(0, c); current = c; save(); renderConversations(); renderMessages(); }
    private void deleteConversation(Conversation c) { conversations.remove(c); if (conversations.isEmpty()) conversations.add(new Conversation("Nueva conversación")); current = conversations.get(0); save(); renderConversations(); renderMessages(); }
    private void save() { conversations.sort(Comparator.comparingLong((Conversation item) -> item.updatedAt).reversed()); store.save(conversations); renderConversations(); }
    private void scrollBottom() { messageScroller.post(() -> messageScroller.fullScroll(View.FOCUS_DOWN)); }
    private void copyText(String value) { ((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Respuesta ChatIA", value)); Toast.makeText(this, "Respuesta copiada", Toast.LENGTH_SHORT).show(); }
    private void shareText(String value) { Intent intent = new Intent(Intent.ACTION_SEND); intent.setType("text/plain"); intent.putExtra(Intent.EXTRA_TEXT, value); startActivity(Intent.createChooser(intent, "Compartir respuesta")); }

    private void startVoiceInput() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) { requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 7); return; }
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES");
        try { startActivityForResult(intent, SPEECH_REQUEST); } catch (Exception e) { Toast.makeText(this, "Dictado no disponible en este dispositivo", Toast.LENGTH_LONG).show(); }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK && data != null) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS); if (matches != null && !matches.isEmpty()) input.setText(matches.get(0));
        }
    }

    private LinearLayout row() { LinearLayout v = new LinearLayout(this); v.setOrientation(LinearLayout.HORIZONTAL); return v; }
    private LinearLayout column() { LinearLayout v = new LinearLayout(this); v.setOrientation(LinearLayout.VERTICAL); return v; }
    private TextView text(String value, int sp, int color) { TextView v = new TextView(this); v.setText(value); v.setTextSize(sp); v.setTextColor(color); return v; }
    private Button button(String value) { Button b = new Button(this); b.setText(value); b.setTextColor(Color.WHITE); b.setBackgroundColor(Color.rgb(37,99,235)); return b; }
    private Button smallButton(String value) { Button b = button(value); b.setTextSize(12); b.setAllCaps(false); return b; }
    private void hideKeyboard() { ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(input.getWindowToken(), 0); }
}
