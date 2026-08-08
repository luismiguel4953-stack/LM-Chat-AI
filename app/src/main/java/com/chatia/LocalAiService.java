package com.chatia;

import android.os.Handler;
import android.os.Looper;
import java.util.Locale;

public class LocalAiService implements AiService {
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override public void send(String prompt, Callback callback) {
        handler.postDelayed(() -> callback.onSuccess(buildReply(prompt)), 900);
    }

    private String buildReply(String prompt) {
        String text = prompt.toLowerCase(Locale.ROOT).trim();
        if (text.contains("codigo") || text.contains("código")) return "Claro. Ejemplo en Java:\n\n```java\nString saludo = \"Hola desde ChatIA\";\nSystem.out.println(saludo);\n```\n\nPuedes pedirme que lo adapte a Kotlin, Android o backend.";
        if (text.contains("lista") || text.contains("pasos")) return "Te propongo esta lista:\n\n- Define el objetivo.\n- Divide el trabajo en tareas pequeñas.\n- Prioriza lo urgente.\n- Revisa resultados y mejora la siguiente versión.";
        if (text.contains("hola") || text.contains("buenas")) return "¡Hola! Soy ChatIA. Estoy preparada con una arquitectura que puede conectarse a una API real de IA sin exponer claves en la app.";
        if (text.length() > 220) return "Leí tu mensaje largo. Resumen rápido: identifica la idea principal, separa requisitos, riesgos y próximos pasos. Si quieres, puedo convertirlo en una lista accionable.";
        return "Entendido. Puedo ayudarte a responder, resumir, generar ideas, escribir código o planificar tareas. Para producción, conecta AiService a tu backend seguro de IA.";
    }
}
