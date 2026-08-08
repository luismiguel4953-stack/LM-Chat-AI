# LM Chat AI

LM Chat AI es una aplicación web estática en español que simula un asistente conversacional útil sin depender de un servidor. Está pensada como una base para evolucionar hacia una app real con API de IA, autenticación y almacenamiento remoto.

## Funciones actuales

- Interfaz responsive con modo claro y oscuro.
- Historial persistente en `localStorage` del navegador.
- Respuestas locales para saludos, resúmenes breves, planificación, ideas y agradecimientos.
- Botones de prompts sugeridos para iniciar conversaciones rápidamente.
- Renderizado seguro con nodos DOM en lugar de insertar HTML de usuario.
- Controles accesibles con etiquetas, regiones `aria-live` y mensajes de estado.

## Cómo usarla

1. Abre `index.html` en un navegador moderno.
2. Escribe un mensaje o usa uno de los botones sugeridos.
3. Usa “Limpiar chat” para borrar el historial local.
4. Cambia el tema con el botón de modo claro/oscuro.

## Próximos pasos recomendados

- Crear un backend para proteger claves privadas de proveedores de IA.
- Conectar una API real de lenguaje natural.
- Agregar cuentas de usuario y sincronización de conversaciones.
- Añadir pruebas automatizadas de interfaz.
- Permitir exportar conversaciones en JSON o Markdown.
