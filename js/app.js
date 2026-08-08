const STORAGE_KEY = "lm-chat-ai-history";
const THEME_KEY = "lm-chat-ai-theme";
const MAX_HISTORY = 80;

const chat = document.getElementById("chat");
const form = document.getElementById("chat-form");
const input = document.getElementById("message-input");
const statusText = document.getElementById("status");
const sendButton = document.getElementById("send-button");
const clearButton = document.getElementById("clear-chat");
const themeButton = document.getElementById("theme-toggle");
const suggestionButtons = document.querySelectorAll("[data-prompt]");

let history = loadHistory();

function loadHistory() {
    try {
        return JSON.parse(localStorage.getItem(STORAGE_KEY)) ?? [];
    } catch {
        return [];
    }
}

function saveHistory() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(history.slice(-MAX_HISTORY)));
}

function createMessage(role, text) {
    const message = document.createElement("article");
    message.className = `message ${role}`;

    const avatar = document.createElement("span");
    avatar.className = "avatar";
    avatar.setAttribute("aria-hidden", "true");
    avatar.textContent = role === "user" ? "🧑" : "🤖";

    const bubble = document.createElement("div");
    bubble.className = "bubble";

    const name = document.createElement("strong");
    name.textContent = role === "user" ? "Tú" : "LM Chat AI";

    const content = document.createElement("p");
    content.textContent = text;

    bubble.append(name, content);
    message.append(avatar, bubble);
    return message;
}

function renderHistory() {
    if (history.length === 0) return;
    chat.replaceChildren(...history.map(({ role, text }) => createMessage(role, text)));
    scrollToBottom();
}

function addMessage(role, text) {
    history.push({ role, text, createdAt: new Date().toISOString() });
    saveHistory();
    chat.append(createMessage(role, text));
    scrollToBottom();
}

function scrollToBottom() {
    chat.scrollTop = chat.scrollHeight;
}

function normalize(text) {
    return text.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "");
}

function buildReply(message) {
    const clean = normalize(message);
    const words = message.trim().split(/\s+/).filter(Boolean);

    if (/\b(hola|buenas|hey|saludos)\b/.test(clean)) {
        return "¡Hola! Ya funciono como una app local: guardo el historial, tengo modo claro/oscuro y puedo ayudarte con ideas, resúmenes o planificación básica.";
    }

    if (clean.includes("resume") || clean.includes("resumir")) {
        const textToSummarize = message.replace(/resume este texto:?|resumir:?/i, "").trim();
        if (textToSummarize.length < 30) {
            return "Pega un texto un poco más largo después de 'Resume este texto:' y te daré un resumen breve.";
        }
        return `Resumen sugerido: ${textToSummarize.split(/[.!?]/).filter(Boolean).slice(0, 2).join(". ").trim()}.`;
    }

    if (clean.includes("organizar") || clean.includes("plan") || clean.includes("tarea")) {
        return "Plan rápido: 1) define el objetivo principal, 2) divide en 3 tareas pequeñas, 3) asigna 25 minutos a la primera, 4) revisa el avance y ajusta prioridades.";
    }

    if (clean.includes("idea") || clean.includes("mejorar")) {
        return "Ideas útiles: conectar una API real de IA, agregar autenticación, sincronizar conversaciones, permitir exportar chats y mostrar indicadores de escritura más avanzados.";
    }

    if (clean.includes("gracias")) {
        return "¡Con gusto! Si quieres, puedo seguir ayudándote a convertir esto en una app con backend, login y conexión a una IA real.";
    }

    if (words.length <= 3) {
        return "Cuéntame un poco más para darte una respuesta útil. Por ejemplo: 'ayúdame a crear una lista de tareas para estudiar'.";
    }

    return `Entendido. Sobre “${message.slice(0, 80)}${message.length > 80 ? "..." : ""}”: te recomiendo convertirlo en pasos concretos, validar el resultado con un usuario real y guardar lo aprendido para la siguiente iteración.`;
}

function setStatus(text) {
    statusText.textContent = text;
}

function handleSubmit(event) {
    event.preventDefault();
    const message = input.value.trim();
    if (!message) return;

    addMessage("user", message);
    input.value = "";
    input.focus();
    sendButton.disabled = true;
    setStatus("LM Chat AI está escribiendo...");

    window.setTimeout(() => {
        addMessage("bot", buildReply(message));
        sendButton.disabled = false;
        setStatus("Respuesta lista.");
    }, 450);
}

function clearChat() {
    history = [];
    saveHistory();
    chat.replaceChildren(createMessage("bot", "Chat limpio. ¿Qué quieres hacer ahora?"));
    setStatus("Historial eliminado de este navegador.");
}

function applyTheme(theme) {
    document.documentElement.dataset.theme = theme;
    themeButton.textContent = theme === "light" ? "🌙 Modo oscuro" : "☀️ Modo claro";
    themeButton.setAttribute("aria-pressed", String(theme === "light"));
    localStorage.setItem(THEME_KEY, theme);
}

function toggleTheme() {
    const current = document.documentElement.dataset.theme === "light" ? "dark" : "light";
    applyTheme(current);
}

function fillSuggestion(event) {
    input.value = event.currentTarget.dataset.prompt;
    input.focus();
}

form.addEventListener("submit", handleSubmit);
clearButton.addEventListener("click", clearChat);
themeButton.addEventListener("click", toggleTheme);
suggestionButtons.forEach((button) => button.addEventListener("click", fillSuggestion));

applyTheme(localStorage.getItem(THEME_KEY) ?? "dark");
renderHistory();
