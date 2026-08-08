package com.chatia;

public interface AiService {
    interface Callback { void onSuccess(String reply); void onError(Exception error); }
    void send(String prompt, Callback callback);
}
