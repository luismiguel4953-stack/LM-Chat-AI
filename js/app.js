const HISTORY_KEY = "lm-chat-ai-history-v2";
const SETTINGS_KEY = "lm-chat-ai-settings-v1";
const THEME_KEY = "lm-chat-ai-theme";
const MAX_HISTORY = 100;

const $ = (id) => document.getElementById(id);
const chat = $("chat");
const form = $("chat-form");
const input = $("message-input");
const statusText = $("status");
const sendButton = $("send-button");
const dialog = $("settings-dialog");
const apiUrl = $("api-url");
const modelName = $("model-name");
const apiToken = $("api-token");
const connectionDot = $("connection-dot");
const connectionTitle = $("connection-title");
const connectionDetail = $("connection-detail");

let history = load(HISTORY_KEY, []);
let settings = load(SETTINGS_KEY, { apiUrl: "", model: "", token: "" });

function load(key, fallback) {
  try { return JSON.parse(localStorage.getItem(key)) ?? fallback; } catch { return fallback; }
}
function save(key, value) { localStorage.setItem(key, JSON.stringify(value)); }
function escapeText(value) { return String(value ?? ""); }

function createMessage(role, text) {
  const article = document.createElement("article");
  article.className = `message ${role}`;
  const avatar = document.createElement("span");
  avatar.className = "avatar";
  avatar.textContent = role === "user" ? "🧑" : "✦";
  avatar.setAttribute("aria-hidden", "true");
  const bubble = document.createElement("div");
  bubble.className = "bubble";
  const name = document.createElement("strong");
  name.textContent = role === "user" ? "Tú" : "LM Chat AI";
  const content = document.createElement("p");
  content.textContent = escapeText(text);
  bubble.append(name, content);
  article.append(avatar, bubble);
  return article;
}

function renderHistory() {
  chat.replaceChildren();
  if (!history.length) {
    chat.append(createMessage("bot", "Hola. Soy LM Chat AI. Puedo trabajar en modo local o conectarme a tu backend de IA. Abre ⚙️ Ajustes para configurar el servidor.") );
  } else {
    history.forEach((item) => chat.append(createMessage(item.role, item.text)));
  }
  scrollToBottom();
}
function addMessage(role, text) {
  const item = { role, text, createdAt: new Date().toISOString() };
  history.push(item);
  history = history.slice(-MAX_HISTORY);
  save(HISTORY_KEY, history);
  chat.append(createMessage(role, text));
  scrollToBottom();
}
function scrollToBottom() { chat.scrollTop = chat.scrollHeight; }
function setStatus(text) { statusText.textContent = text; }

function localReply(message) {
  const text = message.trim();
  const normalized = text.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "");
  if (/\b(hola|buenas|hey|saludos)\b/.test(normalized)) return "¡Hola! Estoy listo. Configura una API en Ajustes si quieres respuestas generadas por un modelo real.";
  if (normalized.includes("organizar") || normalized.includes("plan") || normalized.includes("tarea")) return "Podemos hacerlo así: define el objetivo, divide el trabajo en pasos pequeños, prioriza el siguiente paso y revisa el resultado al terminar.";
  if (normalized.includes("resume") || normalized.includes("resumir")) return "Puedo resumir textos cuando me conectes a una IA real. En modo local no voy a fingir que generé un resumen inteligente.";
  if (normalized.includes("idea") || normalized.includes("mejorar")) return "Ideas para LM Chat AI: autenticación, sincronización en la nube, voz, archivos, memoria de conversación y un backend seguro para el modelo de IA.";
  return "Estoy funcionando en modo local. Para obtener respuestas de IA reales, configura tu endpoint de backend en ⚙️ Ajustes.";
}

async function askRealAI(message) {
  if (!settings.apiUrl) return localReply(message);
  const headers = { "Content-Type": "application/json" };
  if (settings.token) headers.Authorization = `Bearer ${settings.token}`;
  const payload = { message, messages: history.slice(-20), model: settings.model || undefined };
  const response = await fetch(settings.apiUrl, { method: "POST", headers, body: JSON.stringify(payload) });
  if (!response.ok) throw new Error(`Servidor respondió ${response.status}`);
  const data = await response.json();
  return data.reply ?? data.message ?? data.content ?? data.output_text ?? data.choices?.[0]?.message?.content ?? "El servidor no devolvió una respuesta reconocible.";
}

async function handleSubmit(event) {
  event.preventDefault();
  const message = input.value.trim();
  if (!message || sendButton.disabled) return;
  addMessage("user", message);
  input.value = "";
  sendButton.disabled = true;
  setStatus(settings.apiUrl ? "Conectando con la IA…" : "Trabajando en modo local…");
  try {
    const reply = await askRealAI(message);
    addMessage("bot", reply);
    setStatus("Respuesta lista.");
  } catch (error) {
    addMessage("bot", `No pude conectar con la IA: ${error.message}. Revisa el endpoint en Ajustes. No inventaré una respuesta como si el servidor funcionara.`);
    setStatus("Error de conexión.");
  } finally {
    sendButton.disabled = false;
    input.focus();
  }
}

function applyTheme(theme) {
  document.documentElement.dataset.theme = theme;
  $("theme-toggle").textContent = theme === "light" ? "🌙 Tema oscuro" : "☀️ Tema claro";
  localStorage.setItem(THEME_KEY, theme);
}
function toggleTheme() { applyTheme(document.documentElement.dataset.theme === "light" ? "dark" : "light"); }
function updateConnection() {
  const online = Boolean(settings.apiUrl);
  connectionDot.className = `dot ${online ? "online" : "offline"}`;
  connectionTitle.textContent = online ? "IA configurada" : "Modo local";
  connectionDetail.textContent = online ? settings.apiUrl : "Sin servidor de IA configurado";
}

function openSettings() {
  apiUrl.value = settings.apiUrl;
  modelName.value = settings.model;
  apiToken.value = settings.token;
  dialog.showModal();
}
function saveSettings(event) {
  if (event.submitter?.value !== "save") return;
  settings = { apiUrl: apiUrl.value.trim(), model: modelName.value.trim(), token: apiToken.value.trim() };
  save(SETTINGS_KEY, settings);
  updateConnection();
  setStatus(settings.apiUrl ? "Servidor de IA configurado." : "Modo local activado.");
}

async function enableNotifications() {
  if (!("Notification" in window)) return setStatus("Este navegador no admite notificaciones.");
  const permission = await Notification.requestPermission();
  setStatus(permission === "granted" ? "Notificaciones activadas." : "Permiso de notificaciones rechazado.");
  if (permission === "granted") new Notification("LM Chat AI", { body: "Notificaciones activadas correctamente." });
}

function enableVoice() {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!SpeechRecognition) return setStatus("El reconocimiento de voz no está disponible en este navegador.");
  const recognition = new SpeechRecognition();
  recognition.lang = "es-DO";
  recognition.interimResults = false;
  recognition.onstart = () => setStatus("Escuchando… habla ahora.");
  recognition.onresult = (event) => { input.value = event.results[0][0].transcript; input.focus(); setStatus("Voz convertida a texto."); };
  recognition.onerror = () => setStatus("No se pudo usar el micrófono. Revisa el permiso del navegador.");
  recognition.start();
}

$("clear-chat").addEventListener("click", () => { history = []; save(HISTORY_KEY, history); renderHistory(); setStatus("Historial local eliminado."); });
$("theme-toggle").addEventListener("click", toggleTheme);
$("settings-toggle").addEventListener("click", openSettings);
$("settings-form").addEventListener("submit", saveSettings);
$("notify-toggle").addEventListener("click", enableNotifications);
$("voice-toggle").addEventListener("click", enableVoice);
document.querySelectorAll("[data-prompt]").forEach((button) => button.addEventListener("click", () => { input.value = button.dataset.prompt; input.focus(); }));
form.addEventListener("submit", handleSubmit);

applyTheme(localStorage.getItem(THEME_KEY) ?? "dark");
updateConnection();
renderHistory();
if ("serviceWorker" in navigator) navigator.serviceWorker.register("sw.js").catch(() => {});
