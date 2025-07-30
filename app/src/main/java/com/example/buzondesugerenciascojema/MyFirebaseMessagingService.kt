package com.example.buzondesugerenciascojema

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.os.Build
import android.media.RingtoneManager
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "MyFirebaseMsgService"
        private const val CHANNEL_ID = "default_channel"
        private const val CHANNEL_NAME = "Notificaciones EduBox"
        private const val CHANNEL_DESCRIPTION = "Canal para notificaciones de EduBox"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Mensaje recibido: ${remoteMessage.data}")
        Log.d(TAG, "Título: ${remoteMessage.notification?.title}")
        Log.d(TAG, "Cuerpo: ${remoteMessage.notification?.body}")

        // Crear canal de notificación si no existe
        createNotificationChannel()

        // Obtener datos del mensaje
        val titulo = remoteMessage.notification?.title ?: remoteMessage.data["titulo"] ?: "Nueva notificación"
        val mensaje = remoteMessage.notification?.body ?: remoteMessage.data["mensaje"] ?: "Tienes un nuevo mensaje"
        val tipo = remoteMessage.data["tipo"] ?: "GENERAL"
        val notificacionId = remoteMessage.data["notificacionId"] ?: System.currentTimeMillis().toString()

        // Guardar notificación en Firestore si el usuario está autenticado
        val user = FirebaseAuth.getInstance().currentUser
        user?.email?.let { email ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val notificacionService = com.example.buzondesugerenciascojema.data.NotificacionService()
                    notificacionService.crearNotificacion(
                        titulo = titulo,
                        mensaje = mensaje,
                        tipo = com.example.buzondesugerenciascojema.data.TipoNotificacion.valueOf(tipo.uppercase()),
                        destinatarioEmail = email,
                        leida = false
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error al guardar notificación en Firestore: ${e.message}")
                }
            }
        }

        // Mostrar notificación local
        showNotification(titulo, mensaje, notificacionId)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM: $token")
        
        // Actualizar el token en Firestore si el usuario está autenticado
        val user = FirebaseAuth.getInstance().currentUser
        user?.email?.let { email ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = FirebaseFirestore.getInstance()
                    
                    // Guardar token en colección de usuarios
                    val userDoc = db.collection("usuarios").document(email)
                    userDoc.update("fcmToken", token).await()
                    
                    // También guardar en colección específica de tokens
                    val tokenDoc = db.collection("fcm_tokens").document(email)
                    tokenDoc.set(mapOf(
                        "email" to email,
                        "token" to token,
                        "fechaActualizacion" to com.google.firebase.Timestamp.now()
                    )).await()
                    
                    Log.d(TAG, "Token FCM actualizado para: $email")
                } catch (e: Exception) {
                    Log.e(TAG, "Error al actualizar token FCM: ${e.message}")
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(titulo: String, mensaje: String, notificacionId: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Intent para abrir la aplicación
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notificacionId", notificacionId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Sonido de notificación
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setSmallIcon(R.drawable.notificacion) // Usar el ícono de notificación de la app
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))

        notificationManager.notify(notificacionId.hashCode(), notificationBuilder.build())
    }
} 