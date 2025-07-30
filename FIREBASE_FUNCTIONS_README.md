# Firebase Cloud Functions - Notificaciones Push

## 🚀 Configuración de Notificaciones Push

Para que las notificaciones push funcionen **realmente** cuando la app está cerrada, necesitas desplegar las Firebase Cloud Functions.

### 📋 Requisitos Previos

1. **Firebase CLI** instalado:
   ```bash
   npm install -g firebase-tools
   ```

2. **Node.js** versión 18 o superior

3. **Proyecto Firebase** configurado

### 🔧 Pasos para Desplegar

#### 1. Inicializar Firebase Functions (si no está inicializado)
```bash
firebase init functions
```

#### 2. Instalar dependencias
```bash
cd functions
npm install
```

#### 3. Desplegar las funciones
```bash
firebase deploy --only functions
```

### 📱 Funciones Implementadas

#### `sendNotification`
- **Propósito**: Enviar notificación push a un usuario específico
- **Parámetros**: `token`, `titulo`, `mensaje`, `email`
- **Uso**: Llamada desde la app Android

#### `sendNotificationToGroup`
- **Propósito**: Enviar notificación push a múltiples usuarios
- **Parámetros**: `tokens[]`, `titulo`, `mensaje`
- **Uso**: Para notificaciones masivas

#### `onNotificationCreated`
- **Propósito**: Se ejecuta automáticamente cuando se crea una notificación en Firestore
- **Trigger**: Creación de documento en colección `notificaciones`
- **Uso**: Notificaciones automáticas

### 🔔 Tipos de Notificaciones que Funcionan

1. **✅ Sugerencias**: Cuando un admin aprueba/rechaza
2. **✅ Biblioteca**: Cuando se agrega un nuevo documento
3. **✅ Eventos**: Recordatorios automáticos (1 día y 1 hora antes)
4. **✅ Manuales**: Notificaciones enviadas por admins

### 🛠️ Configuración Adicional

#### Permisos de Firestore
Asegúrate de que las reglas de Firestore permitan:
- Lectura de la colección `fcm_tokens`
- Escritura en la colección `notificaciones`

#### Configuración de Android
La app ya tiene configurado:
- `MyFirebaseMessagingService`
- Canal de notificaciones
- Manejo de tokens FCM

### 🧪 Probar las Notificaciones

1. **Desde la app**: Usar los botones en AdminScreen
2. **Desde Firebase Console**: Enviar mensaje de prueba
3. **Logs**: Revisar logs en Firebase Console > Functions

### 📊 Monitoreo

- **Logs**: Firebase Console > Functions > Logs
- **Métricas**: Firebase Console > Functions > Usage
- **Errores**: Se muestran en los logs de la función

### 🔧 Solución de Problemas

#### Error: "Function not found"
- Verificar que las funciones estén desplegadas
- Revisar el nombre de la función en el código

#### Error: "Permission denied"
- Verificar reglas de Firestore
- Asegurar que el usuario esté autenticado

#### Notificaciones no llegan
- Verificar que el token FCM esté guardado
- Revisar logs de la función
- Verificar permisos de notificaciones en Android

### 📝 Notas Importantes

- Las notificaciones funcionan **con la app cerrada**
- Los tokens FCM se actualizan automáticamente
- Las funciones tienen manejo de errores
- Fallback a notificaciones locales si falla FCM

### 🎯 Resultado Final

Después de desplegar las Cloud Functions:
- ✅ Notificaciones push reales
- ✅ Funcionan con app cerrada
- ✅ Notificaciones automáticas
- ✅ Notificaciones manuales
- ✅ Manejo de errores robusto 