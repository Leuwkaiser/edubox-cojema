package com.example.buzondesugerenciascojema.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.buzondesugerenciascojema.MainActivity
import com.example.buzondesugerenciascojema.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class LocalNotificationService(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "edubox_notifications"
    private val channelName = "EduBox Notifications"
    private val channelDescription = "Notificaciones automáticas de EduBox"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = channelDescription
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Envía una notificación local que simula un push
     */
    suspend fun enviarNotificacionLocal(
        titulo: String,
        mensaje: String,
        tipo: String = "GENERAL"
    ) = withContext(Dispatchers.IO) {
        try {
            val notificationId = System.currentTimeMillis().toInt()
            
            // Crear intent para abrir la app
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("notification_type", tipo)
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Crear la notificación
            val notification = NotificationCompat.Builder(context, channelId)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setSmallIcon(R.drawable.logo_cojema)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 500, 200, 500))
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .build()

            // Mostrar la notificación
            notificationManager.notify(notificationId, notification)
            
            println("✅ Notificación local enviada: $titulo")
        } catch (e: Exception) {
            println("❌ Error al enviar notificación local: ${e.message}")
        }
    }

    /**
     * Envía notificación a todos los usuarios (simulado)
     */
    suspend fun enviarNotificacionATodos(
        titulo: String,
        mensaje: String,
        tipo: String = "GENERAL"
    ) = withContext(Dispatchers.IO) {
        try {
            // En una implementación real, esto enviaría a todos los dispositivos
            // Por ahora, solo enviamos una notificación local
            enviarNotificacionLocal(titulo, mensaje, tipo)
            
            println("✅ Notificación simulada enviada a todos: $titulo")
        } catch (e: Exception) {
            println("❌ Error al enviar notificación a todos: ${e.message}")
        }
    }

    /**
     * Envía notificación solo a administradores (simulado)
     */
    suspend fun enviarNotificacionAAdministradores(
        titulo: String,
        mensaje: String,
        tipo: String = "ADMIN"
    ) = withContext(Dispatchers.IO) {
        try {
            // En una implementación real, esto enviaría solo a admins
            // Por ahora, solo enviamos una notificación local
            enviarNotificacionLocal(titulo, mensaje, tipo)
            
            println("✅ Notificación simulada enviada a administradores: $titulo")
        } catch (e: Exception) {
            println("❌ Error al enviar notificación a administradores: ${e.message}")
        }
    }

    /**
     * Programa notificaciones locales para eventos
     */
    suspend fun programarNotificacionEvento(
        tituloEvento: String,
        fechaEvento: String,
        horasAntes: Int
    ) = withContext(Dispatchers.IO) {
        try {
            val titulo = if (horasAntes == 24) {
                "⏰ Evento mañana: $tituloEvento"
            } else {
                "🚨 Evento en $horasAntes hora(s): $tituloEvento"
            }
            
            val mensaje = "No olvides: $tituloEvento - $fechaEvento"
            
            enviarNotificacionLocal(titulo, mensaje, "EVENTO")
            
            println("✅ Notificación de evento programada: $tituloEvento")
        } catch (e: Exception) {
            println("❌ Error al programar notificación de evento: ${e.message}")
        }
    }
} 