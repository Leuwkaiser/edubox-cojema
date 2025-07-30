package com.example.buzondesugerenciascojema.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.util.*

class EventSchedulerService(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val automatedNotificationService = AutomatedNotificationService()
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Programa notificaciones para todos los eventos futuros
     */
    suspend fun programarNotificacionesEventos() = withContext(Dispatchers.IO) {
        try {
            val eventosSnapshot = db.collection("eventos")
                .whereGreaterThan("fecha", Date())
                .get()
                .await()

            for (doc in eventosSnapshot.documents) {
                val titulo = doc.getString("titulo") ?: ""
                val fecha = doc.getString("fecha") ?: ""
                val id = doc.id

                // Programar notificación 24 horas antes
                programarNotificacionEvento(id, titulo, fecha, 24)
                
                // Programar notificación 1 hora antes
                programarNotificacionEvento(id, titulo, fecha, 1)
            }

            println("✅ Notificaciones programadas para ${eventosSnapshot.size()} eventos")
        } catch (e: Exception) {
            println("❌ Error al programar notificaciones de eventos: ${e.message}")
        }
    }

    /**
     * Programa una notificación específica para un evento
     */
    private fun programarNotificacionEvento(
        eventoId: String,
        tituloEvento: String,
        fechaEvento: String,
        horasAntes: Int
    ) {
        try {
            // Parsear la fecha del evento
            val fechaEventoDate = parseFecha(fechaEvento)
            if (fechaEventoDate == null) {
                println("❌ No se pudo parsear la fecha: $fechaEvento")
                return
            }

            // Calcular cuándo enviar la notificación
            val tiempoNotificacion = fechaEventoDate.time - (horasAntes * 60 * 60 * 1000)
            
            // Solo programar si la notificación es en el futuro
            if (tiempoNotificacion > System.currentTimeMillis()) {
                val intent = Intent(context, com.example.buzondesugerenciascojema.EventNotificationReceiver::class.java).apply {
                    putExtra("eventoId", eventoId)
                    putExtra("tituloEvento", tituloEvento)
                    putExtra("fechaEvento", fechaEvento)
                    putExtra("horasAntes", horasAntes)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    "${eventoId}_${horasAntes}".hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    tiempoNotificacion,
                    pendingIntent
                )

                println("✅ Notificación programada para evento '$tituloEvento' ${horasAntes}h antes")
            }
        } catch (e: Exception) {
            println("❌ Error al programar notificación: ${e.message}")
        }
    }

    /**
     * Cancela todas las notificaciones programadas
     */
    fun cancelarTodasLasNotificaciones() {
        try {
            // Crear un intent genérico para cancelar
            val intent = Intent(context, com.example.buzondesugerenciascojema.EventNotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
            
            println("✅ Todas las notificaciones programadas canceladas")
        } catch (e: Exception) {
            println("❌ Error al cancelar notificaciones: ${e.message}")
        }
    }

    /**
     * Parsea una fecha en formato string a Date
     */
    private fun parseFecha(fechaString: String): Date? {
        return try {
            // Intentar diferentes formatos de fecha
            val formatos = listOf(
                "dd/MM/yyyy HH:mm",
                "dd-MM-yyyy HH:mm",
                "yyyy-MM-dd HH:mm",
                "dd/MM/yyyy",
                "dd-MM-yyyy",
                "yyyy-MM-dd"
            )

            for (formato in formatos) {
                try {
                    val dateFormat = java.text.SimpleDateFormat(formato, Locale.getDefault())
                    return dateFormat.parse(fechaString)
                } catch (e: Exception) {
                    continue
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Verifica y programa notificaciones para eventos que se acercan
     */
    suspend fun verificarEventosProximos() = withContext(Dispatchers.IO) {
        try {
            val ahora = Date()
            val mañana = Date(ahora.time + 24 * 60 * 60 * 1000) // 24 horas

            val eventosSnapshot = db.collection("eventos")
                .whereGreaterThanOrEqualTo("fecha", ahora)
                .whereLessThan("fecha", mañana)
                .get()
                .await()

            for (doc in eventosSnapshot.documents) {
                val titulo = doc.getString("titulo") ?: ""
                val fecha = doc.getString("fecha") ?: ""

                automatedNotificationService.notificarEventoProximo(titulo, fecha)
            }

            println("✅ Verificación de eventos próximos completada")
        } catch (e: Exception) {
            println("❌ Error al verificar eventos próximos: ${e.message}")
        }
    }
} 