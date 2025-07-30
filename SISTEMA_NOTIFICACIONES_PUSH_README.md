# Sistema de Notificaciones Push - EduBox

## Descripción General

Este sistema implementa notificaciones push completas para la aplicación EduBox usando Firebase Cloud Messaging (FCM). Permite enviar notificaciones a usuarios específicos, grupos, roles o a todos los usuarios de la aplicación.

## Características Principales

### 🔔 Funcionalidades
- **Notificaciones Push en Tiempo Real**: Envío instantáneo de notificaciones
- **Envío Selectivo**: A usuarios específicos, grupos, roles o todos los usuarios
- **Persistencia**: Las notificaciones se guardan en Firestore
- **Estadísticas**: Seguimiento de tokens activos/inactivos
- **Gestión de Tokens**: Registro y limpieza automática de tokens FCM

### 📱 Componentes del Sistema

#### 1. **MyFirebaseMessagingService.kt**
- Maneja la recepción de notificaciones push
- Crea canales de notificación para Android
- Guarda notificaciones en Firestore automáticamente
- Registra tokens FCM en Firestore

#### 2. **PushNotificationService.kt**
- Servicio principal para enviar notificaciones
- Integración con Cloud Functions
- Gestión de tokens FCM
- Estadísticas de envío

#### 3. **PushNotificationViewModel.kt**
- ViewModel para la UI de administración
- Manejo de estado de envío
- Integración con el servicio de notificaciones

#### 4. **PushNotificationAdminScreen.kt**
- Interfaz para administradores
- Formularios para diferentes tipos de envío
- Estadísticas en tiempo real

#### 5. **Cloud Functions (functions/index.js)**
- `enviarNotificacionPush`: Envía notificación a un usuario
- `sendNotificationToGroup`: Envía a múltiples usuarios
- `onNotificationCreated`: Trigger automático desde Firestore
- `limpiarTokensInactivos`: Limpieza automática de tokens
- `obtenerEstadisticasTokens`: Estadísticas de tokens

## Configuración

### 1. **Dependencias (build.gradle.kts)**
```kotlin
implementation("com.google.firebase:firebase-messaging-ktx")
implementation("com.google.firebase:firebase-analytics-ktx")
```

### 2. **Permisos (AndroidManifest.xml)**
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.VIBRATE" />
```

### 3. **Servicio FCM (AndroidManifest.xml)**
```xml
<service
    android:name=".MyFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT"/>
    </intent-filter>
</service>
```

## Uso del Sistema

### Para Administradores

#### Enviar Notificación Push Real (Firebase Console)
Para enviar notificaciones push que lleguen incluso con la app cerrada:

1. **Ve a Firebase Console** → Tu proyecto → Cloud Messaging
2. **Crea una nueva campaña** → "Send your first message"
3. **Configura la notificación:**
   - Título: "Tu título aquí"
   - Mensaje: "Tu mensaje aquí"
   - Imagen (opcional)
4. **Selecciona el público objetivo:**
   - **Usuarios específicos**: Ingresa los tokens FCM
   - **Grupos**: Usa temas (topics)
   - **Todos**: Envía a todos los usuarios
5. **Programa el envío** (inmediato o programado)
6. **Revisa y envía**

#### Enviar Notificación a Usuario Específico (App)
```kotlin
val viewModel: PushNotificationViewModel = viewModel()
viewModel.enviarNotificacionPush(
    emailDestinatario = "usuario@ejemplo.com",
    titulo = "Nuevo Libro",
    mensaje = "Se ha agregado un nuevo libro a la biblioteca",
    tipo = TipoNotificacion.BIBLIOTECA
)
```

#### Enviar Notificación a Grupo
```kotlin
viewModel.enviarNotificacionPushAGrupo(
    grado = "1",
    grupo = "A",
    titulo = "Recordatorio",
    mensaje = "No olviden la tarea de matemáticas",
    tipo = TipoNotificacion.EVENTO
)
```

#### Enviar Notificación a Todos
```kotlin
viewModel.enviarNotificacionPushATodos(
    titulo = "Mantenimiento",
    mensaje = "La aplicación estará en mantenimiento mañana",
    tipo = TipoNotificacion.PUSH
)
```

### Para Desarrolladores

#### Registro de Token FCM
```kotlin
val pushNotificationService = PushNotificationService()
pushNotificationService.registrarTokenFCM(email, token)
```

#### Obtener Estadísticas
```kotlin
val estadisticas = pushNotificationService.obtenerEstadisticasTokens()
println("Tokens activos: ${estadisticas["tokensActivos"]}")
```

## Estructura de Datos

### Colección: `fcm_tokens`
```json
{
  "email": "usuario@ejemplo.com",
  "token": "fcm_token_here",
  "fechaRegistro": "timestamp",
  "fechaActualizacion": "timestamp",
  "activo": true
}
```

### Colección: `notificaciones`
```json
{
  "id": "uuid",
  "titulo": "Título de la notificación",
  "mensaje": "Mensaje de la notificación",
  "tipo": "PUSH",
  "destinatarioEmail": "usuario@ejemplo.com",
  "leida": false,
  "fecha": "timestamp"
}
```

## Tipos de Notificación

```kotlin
enum class TipoNotificacion {
    NUEVO_LIBRO,
    SUGERENCIA_APROBADA,
    SUGERENCIA_DESAPROBADA,
    NUEVO_EVENTO,
    SUGERENCIA_PENDIENTE,
    SUGERENCIA_INNAPROPIADA,
    SUGERENCIA,
    BIBLIOTECA,
    EVENTO,
    PUSH
}
```

## Flujo de Notificación

### Opción 1: Firebase Console (Recomendado para notificaciones push reales)
1. **Registro de Token**: La app registra el token FCM al iniciar
2. **Envío**: El administrador envía desde Firebase Console
3. **FCM**: Firebase envía la notificación al dispositivo
4. **Recepción**: `MyFirebaseMessagingService` recibe la notificación
5. **Persistencia**: Se guarda en Firestore automáticamente
6. **UI**: Se muestra la notificación al usuario

### Opción 2: App Interna (Para notificaciones en la app)
1. **Registro de Token**: La app registra el token FCM al iniciar
2. **Envío**: El administrador envía notificación desde la UI
3. **Firestore**: Se crea la notificación en la base de datos
4. **Trigger**: Se activa automáticamente el envío push
5. **Recepción**: `MyFirebaseMessagingService` recibe la notificación
6. **UI**: Se muestra la notificación al usuario

## Configuración de Firebase

### 1. **google-services.json**
Asegúrate de tener el archivo `google-services.json` en `app/` con la configuración correcta.

### 2. **Cloud Functions**
```bash
cd functions
npm install
firebase deploy --only functions
```

### 3. **Firestore Rules**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /fcm_tokens/{email} {
      allow read, write: if request.auth != null && request.auth.token.email == email;
    }
    match /notificaciones/{notificacionId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## Troubleshooting

### Problemas Comunes

1. **Notificaciones no llegan**
   - Verificar que el token FCM esté registrado
   - Revisar logs de Cloud Functions
   - Confirmar permisos de notificación

2. **Error en Cloud Functions**
   - Verificar configuración de Firebase
   - Revisar logs en Firebase Console
   - Confirmar que las funciones estén desplegadas

3. **Tokens no se registran**
   - Verificar conexión a internet
   - Revisar logs de la aplicación
   - Confirmar configuración de google-services.json

### Logs Útiles

```kotlin
// En MyFirebaseMessagingService
Log.d("FCM", "Token obtenido: $token")
Log.d("FCM", "Mensaje recibido: ${remoteMessage.data}")

// En PushNotificationService
println("Notificación push enviada exitosamente a: $emailDestinatario")
```

## Próximas Mejoras

- [ ] Notificaciones programadas
- [ ] Plantillas de notificación
- [ ] Análisis de engagement
- [ ] Notificaciones con imágenes
- [ ] Soporte para iOS
- [ ] Notificaciones en segundo plano

## Contribución

Para contribuir al sistema de notificaciones:

1. Crear una rama para tu feature
2. Implementar los cambios
3. Probar exhaustivamente
4. Crear un pull request con documentación

## Contacto

Para soporte técnico o preguntas sobre el sistema de notificaciones, contacta al equipo de desarrollo. 