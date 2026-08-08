# ChatIA

ChatIA es una aplicación Android nativa de asistente conversacional. El proyecto está preparado para compilar un APK instalable y para sustituir el motor local por una API real de inteligencia artificial mediante una capa `AiService`, sin guardar claves API dentro del código del cliente.

## Funciones incluidas

- Pantalla principal de chat con barra superior, logo, botón de nueva conversación y lista de conversaciones recientes.
- Mensajes diferenciados entre usuario e IA, con fecha y hora.
- Campo inferior para mensajes largos, botón de envío y botón de micrófono con reconocimiento de voz del dispositivo.
- Indicador “ChatIA está escribiendo…” mientras se genera la respuesta.
- Historial persistente con `SharedPreferences` y cambio entre conversaciones.
- Perfil de usuario editable, pantalla de configuración y modo claro/oscuro persistente.
- Creación y eliminación de conversaciones; mantén presionada una conversación reciente para eliminarla.
- Acciones sobre respuestas: copiar, compartir y regenerar.
- Respuestas locales para probar el flujo completo sin depender de red.
- Arquitectura preparada para conectar un backend seguro de IA implementando `AiService`.

## Compilar APK

```bash
gradle :app:assembleDebug
```

El APK debug se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Conectar una IA real

1. Crea un backend propio que guarde la clave API en variables de entorno del servidor.
2. Implementa un nuevo servicio Android que cumpla la interfaz `AiService`.
3. Envía los mensajes al backend por HTTPS y devuelve la respuesta por `Callback`.
4. Maneja errores de red llamando a `onError` para que la aplicación muestre el mensaje sin cerrarse.

Nunca coloques claves privadas directamente en la aplicación Android.

## Estado de la rama reparada

La rama actual contiene únicamente el proyecto Android de ChatIA. La configuración usa Android Gradle Plugin 8.7.3 y `compileSdk`/`targetSdk` 35 para evitar depender de versiones preliminares o no disponibles en la mayoría de entornos de compilación.
