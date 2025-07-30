package com.example.buzondesugerenciascojema.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.util.Date

class AutomatedNotificationService {
    private val db = FirebaseFirestore.getInstance()
    private val pushNotificationService = PushNotificationService()
    private val notificacionService = NotificacionService()
    private var localNotificationService: LocalNotificationService? = null
    
    fun setContext(context: android.content.Context) {
        localNotificationService = LocalNotificationService(context)
    }

    /**
     * Envía notificación automática cuando se agrega un nuevo libro
     */
    suspend fun notificarNuevoLibro(
        tituloLibro: String,
        autor: String,
        agregadoPor: String
    ) = withContext(Dispatchers.IO) {
        try {
            val titulo = "📚 Nuevo libro disponible"
            val mensaje = "$tituloLibro por $autor ha sido agregado a la biblioteca"
            
            // Enviar notificación local (simula push)
            localNotificationService?.enviarNotificacionLocal(titulo, mensaje, "BIBLIOTECA")
            
            // También enviar a todos los usuarios (para cuando tengas Cloud Functions)
            enviarNotificacionATodos(titulo, mensaje, TipoNotificacion.BIBLIOTECA)
            
            println("✅ Notificación automática enviada: Nuevo libro - $tituloLibro")
        } catch (e: Exception) {
            println("❌ Error al enviar notificación de nuevo libro: ${e.message}")
        }
    }

    /**
     * Envía notificación automática cuando hay una sugerencia pendiente para admin
     */
    suspend fun notificarSugerenciaPendiente(
        sugerenciaTitulo: String,
        usuarioSugerencia: String
    ) = withContext(Dispatchers.IO) {
        try {
            val titulo = "💡 Nueva sugerencia pendiente"
            val mensaje = "Sugerencia de $usuarioSugerencia: $sugerenciaTitulo"
            
            // Enviar notificación local (simula push)
            localNotificationService?.enviarNotificacionLocal(titulo, mensaje, "SUGERENCIA")
            
            // También enviar solo a administradores (para cuando tengas Cloud Functions)
            enviarNotificacionAAdministradores(titulo, mensaje, TipoNotificacion.SUGERENCIA)
            
            println("✅ Notificación automática enviada: Sugerencia pendiente")
        } catch (e: Exception) {
            println("❌ Error al enviar notificación de sugerencia: ${e.message}")
        }
    }

    /**
     * Envía notificación automática cuando se agrega un nuevo evento
     */
    suspend fun notificarNuevoEvento(
        tituloEvento: String,
        fechaEvento: String,
        agregadoPor: String
    ) = withContext(Dispatchers.IO) {
        try {
            val titulo = "📅 Nuevo evento programado"
            val mensaje = "$tituloEvento - $fechaEvento"
            
            // Enviar notificación local (simula push)
            localNotificationService?.enviarNotificacionLocal(titulo, mensaje, "EVENTO")
            
            // También enviar a todos los usuarios (para cuando tengas Cloud Functions)
            enviarNotificacionATodos(titulo, mensaje, TipoNotificacion.EVENTO)
            
            println("✅ Notificación automática enviada: Nuevo evento - $tituloEvento")
        } catch (e: Exception) {
            println("❌ Error al enviar notificación de evento: ${e.message}")
        }
    }

    /**
     * Envía notificación automática cuando se acerca un evento (24h antes)
     */
    suspend fun notificarEventoProximo(
        tituloEvento: String,
        fechaEvento: String
    ) = withContext(Dispatchers.IO) {
        try {
            val titulo = "⏰ Evento mañana"
            val mensaje = "No olvides: $tituloEvento - $fechaEvento"
            
            // Enviar notificación local (simula push)
            localNotificationService?.enviarNotificacionLocal(titulo, mensaje, "EVENTO")
            
            // También enviar a todos los usuarios (para cuando tengas Cloud Functions)
            enviarNotificacionATodos(titulo, mensaje, TipoNotificacion.EVENTO)
            
            println("✅ Notificación automática enviada: Evento próximo - $tituloEvento")
        } catch (e: Exception) {
            println("❌ Error al enviar notificación de evento próximo: ${e.message}")
        }
    }

    /**
     * Envía notificación automática cuando un usuario sube un documento
     */
    suspend fun notificarDocumentoSubido(
        tituloDocumento: String,
        usuarioSubio: String
    ) = withContext(Dispatchers.IO) {
        try {
            val titulo = "📄 Nuevo documento disponible"
            val mensaje = "$tituloDocumento ha sido subido por $usuarioSubio"
            
            // Enviar notificación local (simula push)
            localNotificationService?.enviarNotificacionLocal(titulo, mensaje, "DOCUMENTO")
            
            // También enviar a todos los usuarios (para cuando tengas Cloud Functions)
            enviarNotificacionATodos(titulo, mensaje, TipoNotificacion.DOCUMENTO)
            
            println("✅ Notificación automática enviada: Documento subido - $tituloDocumento")
        } catch (e: Exception) {
            println("❌ Error al enviar notificación de documento: ${e.message}")
        }
    }

    /**
     * Envía notificación automática cuando hay un nuevo logro
     */
    suspend fun notificarNuevoLogro(
        usuario: String,
        logro: String
    ) = withContext(Dispatchers.IO) {
        try {
            val titulo = "🏆 ¡Nuevo logro!"
            val mensaje = "$usuario ha conseguido: $logro"
            
            // Enviar solo al usuario específico
            pushNotificationService.enviarNotificacionPush(
                emailDestinatario = usuario,
                titulo = titulo,
                mensaje = mensaje,
                tipo = TipoNotificacion.LOGRO
            )
            
            println("✅ Notificación automática enviada: Nuevo logro - $usuario")
        } catch (e: Exception) {
            println("❌ Error al enviar notificación de logro: ${e.message}")
        }
    }

    /**
     * Envía notificación a todos los usuarios
     */
    private suspend fun enviarNotificacionATodos(
        titulo: String,
        mensaje: String,
        tipo: TipoNotificacion
    ) = withContext(Dispatchers.IO) {
        try {
            // Obtener todos los usuarios
            val usuariosSnapshot = db.collection("usuarios").get().await()
            
            for (doc in usuariosSnapshot.documents) {
                val email = doc.getString("email")
                if (!email.isNullOrEmpty()) {
                    pushNotificationService.enviarNotificacionPush(
                        emailDestinatario = email,
                        titulo = titulo,
                        mensaje = mensaje,
                        tipo = tipo
                    )
                }
            }
            
            println("✅ Notificación enviada a ${usuariosSnapshot.size()} usuarios")
        } catch (e: Exception) {
            println("❌ Error al enviar notificación a todos: ${e.message}")
        }
    }

    /**
     * Envía notificación solo a administradores
     */
    private suspend fun enviarNotificacionAAdministradores(
        titulo: String,
        mensaje: String,
        tipo: TipoNotificacion
    ) = withContext(Dispatchers.IO) {
        try {
            // Obtener solo usuarios administradores
            val adminSnapshot = db.collection("usuarios")
                .whereEqualTo("rol", "admin")
                .get()
                .await()
            
            for (doc in adminSnapshot.documents) {
                val email = doc.getString("email")
                if (!email.isNullOrEmpty()) {
                    pushNotificationService.enviarNotificacionPush(
                        emailDestinatario = email,
                        titulo = titulo,
                        mensaje = mensaje,
                        tipo = tipo
                    )
                }
            }
            
            println("✅ Notificación enviada a ${adminSnapshot.size()} administradores")
        } catch (e: Exception) {
            println("❌ Error al enviar notificación a administradores: ${e.message}")
        }
    }

    /**
     * Programa notificaciones automáticas para eventos próximos
     */
    suspend fun programarNotificacionesEventos() = withContext(Dispatchers.IO) {
        try {
            val hoy = Date()
            val mañana = Date(hoy.time + 24 * 60 * 60 * 1000) // 24 horas
            
            // Buscar eventos que ocurran mañana
            val eventosSnapshot = db.collection("eventos")
                .whereGreaterThanOrEqualTo("fecha", mañana)
                .whereLessThan("fecha", Date(mañana.time + 24 * 60 * 60 * 1000))
                .get()
                .await()
            
            for (doc in eventosSnapshot.documents) {
                val titulo = doc.getString("titulo") ?: ""
                val fecha = doc.getString("fecha") ?: ""
                
                notificarEventoProximo(titulo, fecha)
            }
            
            println("✅ Notificaciones de eventos próximos programadas")
        } catch (e: Exception) {
            println("❌ Error al programar notificaciones de eventos: ${e.message}")
        }
    }
} 