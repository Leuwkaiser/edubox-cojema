# 🚀 Desplegar Cloud Functions para Notificaciones Push

## 📋 **Prerrequisitos**

1. **Node.js instalado** (versión 16 o superior)
2. **Firebase CLI instalado**
3. **Proyecto Firebase configurado**

## 🔧 **Paso 1: Instalar Firebase CLI**

```bash
npm install -g firebase-tools
```

## 🔐 **Paso 2: Iniciar sesión en Firebase**

```bash
firebase login
```

## 📁 **Paso 3: Navegar al directorio de funciones**

```bash
cd functions
```

## 📦 **Paso 4: Instalar dependencias**

```bash
npm install
```

## 🚀 **Paso 5: Desplegar las funciones**

```bash
firebase deploy --only functions
```

## ✅ **Verificación**

Después del despliegue, deberías ver algo como:

```
✔  functions[enviarNotificacionPushReal(us-central1)] Successful create operation.
✔  functions[enviarNotificacionPushAGrupo(us-central1)] Successful create operation.
✔  functions[onNotificationCreated(us-central1)] Successful create operation.
```

## 🧪 **Probar las funciones**

### **Opción 1: Desde la app**
1. Instala la app en tu dispositivo
2. Inicia sesión para registrar tu token FCM
3. Ve al Panel de Administración
4. Haz clic en "Gestionar Notificaciones Push"
5. Envía una notificación a tu email
6. **¡Deberías recibir la notificación push real!**

### **Opción 2: Desde Firebase Console**
1. Ve a Firebase Console → Functions
2. Busca la función `enviarNotificacionPushReal`
3. Haz clic en "Testing"
4. Ingresa los datos de prueba:
```json
{
  "data": {
    "token": "tu_token_fcm_aqui",
    "titulo": "Prueba desde Console",
    "mensaje": "¡Esta es una notificación push real!",
    "emailDestinatario": "tu_email@ejemplo.com"
  }
}
```

## 🔍 **Verificar logs**

Para ver los logs de las funciones:

```bash
firebase functions:log
```

## 🛠️ **Solución de problemas**

### **Error: "Functions deploy failed"**
- Verifica que estés en el directorio correcto (`functions/`)
- Asegúrate de que `package.json` existe
- Ejecuta `npm install` nuevamente

### **Error: "Permission denied"**
- Verifica que tengas permisos de administrador en el proyecto Firebase
- Asegúrate de estar logueado con la cuenta correcta

### **Error: "Function not found"**
- Verifica que las funciones estén desplegadas correctamente
- Revisa los logs con `firebase functions:log`

## 📱 **Flujo completo de notificación push**

1. **Usuario instala la app** → Se registra token FCM
2. **Admin envía notificación** → Se llama a Cloud Function
3. **Cloud Function** → Envía notificación push real
4. **FCM** → Entrega la notificación al dispositivo
5. **Dispositivo** → Muestra la notificación (incluso con app cerrada)

## 🎯 **Resultado esperado**

- ✅ Notificaciones llegan incluso con la app cerrada
- ✅ Sonido y vibración funcionan
- ✅ Se guardan en Firestore automáticamente
- ✅ Aparecen en la barra de estado del dispositivo

## 📞 **Soporte**

Si tienes problemas:
1. Revisa los logs: `firebase functions:log`
2. Verifica la configuración de Firebase
3. Asegúrate de que el proyecto tenga habilitado Cloud Functions 