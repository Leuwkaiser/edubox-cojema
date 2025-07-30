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

class SimpleNotificationService(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "edubox_notifications"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "EduBox Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones automáticas de EduBox"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Envía una notificación local
     */
    suspend fun enviarNotificacion(
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
            
            println("✅ Notificación enviada: $titulo")
        } catch (e: Exception) {
            println("❌ Error al enviar notificación: ${e.message}")
        }
    }

    /**
     * Notifica nuevo documento
     */
    suspend fun notificarNuevoDocumento(tituloDocumento: String, usuarioSubio: String) {
        enviarNotificacion(
            titulo = "📚 Nuevo documento disponible",
            mensaje = "$tituloDocumento ha sido subido por $usuarioSubio",
            tipo = "DOCUMENTO"
        )
    }

    /**
     * Notifica sugerencia pendiente (solo para admins)
     */
    suspend fun notificarSugerenciaPendiente(sugerenciaTitulo: String, usuarioSugerencia: String) {
        enviarNotificacion(
            titulo = "💡 Nueva sugerencia pendiente",
            mensaje = "Sugerencia de $usuarioSugerencia: $sugerenciaTitulo",
            tipo = "SUGERENCIA"
        )
    }

    /**
     * Notifica nuevo evento
     */
    suspend fun notificarNuevoEvento(tituloEvento: String, fechaEvento: String) {
        enviarNotificacion(
            titulo = "📅 Nuevo evento programado",
            mensaje = "$tituloEvento - $fechaEvento",
            tipo = "EVENTO"
        )
    }

    /**
     * Notifica evento próximo
     */
    suspend fun notificarEventoProximo(tituloEvento: String, fechaEvento: String) {
        enviarNotificacion(
            titulo = "⏰ Evento mañana",
            mensaje = "No olvides: $tituloEvento - $fechaEvento",
            tipo = "EVENTO"
        )
    }
} 