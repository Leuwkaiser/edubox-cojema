package com.example.buzondesugerenciascojema.data

import com.example.buzondesugerenciascojema.model.Evento
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class EventoService {
    private val db = FirebaseFirestore.getInstance()
    private val eventosCollection = db.collection("eventos")
    private val usuariosCollection = db.collection("usuarios")
    private val notificacionService = NotificacionService()

    suspend fun agregarEvento(evento: Evento): String {
        val id = UUID.randomUUID().toString()
        val eventoConId = evento.copy(id = id)
        eventosCollection.document(id).set(eventoConId).await()
        
        // Notificar a todos los usuarios sobre el nuevo evento
        try {
            val todosUsuarios = usuariosCollection.get().await()
            if (todosUsuarios.size() > 0) {
                // Crear notificación para cada usuario
                for (doc in todosUsuarios.documents) {
                    val email = doc.getString("email")
                    if (!email.isNullOrEmpty()) {
                        notificacionService.crearNotificacion(
                            titulo = "📅 Nuevo evento programado",
                            mensaje = "Se ha agregado '${evento.titulo}' al calendario. ¡No te lo pierdas!",
                            tipo = TipoNotificacion.NUEVO_EVENTO,
                            destinatarioEmail = email,
                            leida = false
                        )
                    }
                }
                println("✅ Notificaciones de nuevo evento enviadas a ${todosUsuarios.size()} usuarios")
            }
        } catch (e: Exception) {
            println("❌ Error al enviar notificaciones de nuevo evento: ${e.message}")
            // No fallar la operación principal por errores de notificación
        }
        
        return id
    }

    suspend fun editarEvento(evento: Evento) {
        eventosCollection.document(evento.id).set(evento).await()
    }

    suspend fun eliminarEvento(eventoId: String) {
        eventosCollection.document(eventoId).delete().await()
    }

    suspend fun obtenerEventos(): List<Evento> {
        val snapshot = eventosCollection.get().await()
        return snapshot.documents.mapNotNull { it.toObject(Evento::class.java)?.copy(id = it.id) }
    }

    // Función para enviar recordatorios de eventos próximos
    suspend fun enviarRecordatoriosEventosProximos() {
        try {
            val eventos = obtenerEventos()
            val hoy = java.time.LocalDate.now()
            val manana = hoy.plusDays(1)
            
            for (evento in eventos) {
                // Convertir Timestamp a LocalDate
                val fechaEvento = java.time.Instant.ofEpochMilli(evento.fecha.time)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                
                // Si el evento es mañana, enviar recordatorio
                if (fechaEvento == manana) {
                    val titulo = "📅 Recordatorio: ${evento.titulo}"
                    val mensaje = "Mañana es el evento: ${evento.titulo}\n\n${evento.descripcion}"
                    
                                    // Enviar notificación a todos los usuarios
                val todosUsuarios = usuariosCollection.get().await()
                for (doc in todosUsuarios.documents) {
                    val email = doc.getString("email")
                    if (!email.isNullOrEmpty()) {
                        notificacionService.crearNotificacion(
                            titulo = titulo,
                            mensaje = mensaje,
                            tipo = TipoNotificacion.EVENTO,
                            destinatarioEmail = email,
                            leida = false
                        )
                    }
                }
                    
                    println("Recordatorio enviado para evento: ${evento.titulo}")
                }
            }
        } catch (e: Exception) {
            println("Error al enviar recordatorios de eventos: ${e.message}")
        }
    }

    // Función para verificar eventos que están por comenzar (1 hora antes)
    suspend fun enviarNotificacionesEventosPorComenzar() {
        try {
            val eventos = obtenerEventos()
            val ahora = java.time.LocalDateTime.now()
            val unaHoraDespues = ahora.plusHours(1)
            
            for (evento in eventos) {
                // Convertir Timestamp a LocalDateTime
                val fechaEvento = java.time.Instant.ofEpochMilli(evento.fecha.time)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime()
                
                // Si el evento comienza en la próxima hora
                if (fechaEvento.isAfter(ahora) && fechaEvento.isBefore(unaHoraDespues)) {
                    val titulo = "⏰ Evento por comenzar: ${evento.titulo}"
                    val mensaje = "El evento '${evento.titulo}' comenzará en menos de 1 hora.\n\n${evento.descripcion}"
                    
                    // Enviar notificación push a todos los usuarios
                    notificacionService.enviarNotificacionPushATodos(titulo, mensaje)
                    
                    // También crear notificación en Firestore
                    notificacionService.crearNotificacion(
                        titulo = titulo,
                        mensaje = mensaje,
                        tipo = TipoNotificacion.EVENTO,
                        destinatarioEmail = "todos",
                        leida = false
                    )
                    
                    println("Notificación de evento por comenzar enviada: ${evento.titulo}")
                }
            }
        } catch (e: Exception) {
            println("Error al enviar notificaciones de eventos por comenzar: ${e.message}")
        }
    }

    suspend fun marcarNotificado(eventoId: String, usuarioId: String) {
        val docRef = eventosCollection.document(eventoId)
        val snapshot = docRef.get().await()
        val evento = snapshot.toObject(Evento::class.java)
        if (evento != null) {
            val nuevosNotificados = (evento.notificados + usuarioId).distinct()
            docRef.update("notificados", nuevosNotificados).await()
        }
    }

    // Función auxiliar para obtener emails de todos los estudiantes
    private suspend fun obtenerEmailsEstudiantes(): List<String> {
        return try {
            val snapshot = usuariosCollection
                .whereEqualTo("esAdmin", false)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.getString("email")
            }
        } catch (e: Exception) {
            println("DEBUG: Error al obtener emails de estudiantes: ${e.message}")
            emptyList()
        }
    }
} 