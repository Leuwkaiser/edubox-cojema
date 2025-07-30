package com.example.buzondesugerenciascojema

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.buzondesugerenciascojema.data.AutomatedNotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EventNotificationReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "EventNotificationReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "EventNotificationReceiver activado")
        
        val eventoId = intent.getStringExtra("eventoId") ?: ""
        val tituloEvento = intent.getStringExtra("tituloEvento") ?: ""
        val fechaEvento = intent.getStringExtra("fechaEvento") ?: ""
        val horasAntes = intent.getIntExtra("horasAntes", 24)
        
        Log.d(TAG, "Enviando notificación para evento: $tituloEvento (${horasAntes}h antes)")
        
        // Enviar notificación automática
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val automatedNotificationService = AutomatedNotificationService()
                
                if (horasAntes == 24) {
                    automatedNotificationService.notificarEventoProximo(tituloEvento, fechaEvento)
                } else if (horasAntes == 1) {
                    automatedNotificationService.notificarEventoProximo("⏰ Evento en 1 hora: $tituloEvento", fechaEvento)
                }
                
                Log.d(TAG, "✅ Notificación de evento enviada exitosamente")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al enviar notificación de evento: ${e.message}")
            }
        }
    }
} 