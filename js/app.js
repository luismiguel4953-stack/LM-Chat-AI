function enviarMensaje() {
    const input = document.getElementById("mensaje");
    const chat = document.getElementById("chat");

    if (input.value.trim() === "") return;

    chat.innerHTML += `
        <div class="user">
            ${input.value}
        </div>
    `;

    setTimeout(() => {
        chat.innerHTML += `
            <div class="bot">
                Aún no tengo una IA conectada. Próximamente responderé con ChatGPT o Gemini.
            </div>
        `;

        chat.scrollTop = chat.scrollHeight;
    }, 500);

    input.value = "";
}
