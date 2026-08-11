# LM Chat AI

Asistente web en español, preparado para funcionar como PWA y conectarse a un backend de IA real.

## Estado actual
- Chat con historial local persistente.
- Modo claro/oscuro.
- Reconocimiento de voz mediante las APIs disponibles del navegador.
- Notificaciones del navegador con permiso explícito.
- Diseño responsive para móvil y escritorio.
- PWA instalable y funcionamiento offline de la interfaz.
- Integración con un endpoint de backend configurable desde Ajustes.
- Sin claves privadas incluidas en el código.

## Conectar una IA real
La interfaz acepta un endpoint `POST` configurable desde **⚙️ Ajustes**. El frontend envía JSON con:

```json
{
  "message": "Hola",
  "messages": [],
  "model": "modelo"
}
```

El backend debe responder JSON con uno de estos campos: `reply`, `message`, `content`, `output_text` o `choices[0].message.content`.

**Producción:** no pongas una API key privada de OpenAI, Gemini u otro proveedor en este repositorio ni en el navegador. Usa un backend/función segura, autentica usuarios y aplica límites de uso.

## Publicación
Para convertir esta versión web en una aplicación de Play Store se necesita un empaquetado Android (por ejemplo, una aplicación nativa o una capa Trusted Web Activity/WebView bien configurada), además de firma, política de privacidad, declaraciones de datos y pruebas en dispositivos reales.

## Privacidad
El historial de chat se guarda localmente en el navegador. El contenido enviado al backend depende del endpoint que configure el usuario. Antes de producción debe añadirse una política de privacidad que describa exactamente el tratamiento de datos.
