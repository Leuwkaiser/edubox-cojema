package com.example.buzondesugerenciascojema.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.UUID

class NotificacionService {
    private val db = FirebaseFirestore.getInstance()
    private val notificacionesCollection = db.collection("notificaciones")

    // Crear una nueva notificación
    suspend fun crearNotificacion(
        titulo: String,
        mensaje: String,
        tipo: TipoNotificacion,
        destinatarioEmail: String,
        leida: Boolean = false
    ): String {
        val id = UUID.randomUUID().toString()
        val notificacion = mapOf(
            "id" to id,
            "titulo" to titulo,
            "mensaje" to mensaje,
            "tipo" to tipo.name,
            "destinatarioEmail" to destinatarioEmail,
            "leida" to leida,
            "fecha" to com.google.firebase.Timestamp.now()
        )
        
        notificacionesCollection.document(id).set(notificacion).await()
        return id
    }

    // Obtener notificaciones de un usuario
    suspend fun obtenerNotificaciones(email: String): List<Notificacion> {
        val snapshot = notificacionesCollection
            .whereEqualTo("destinatarioEmail", email)
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Notificacion::class.java)?.copy(id = doc.id)
        }
    }

    // Marcar notificación como leída
    suspend fun marcarComoLeida(notificacionId: String) {
        notificacionesCollection.document(notificacionId)
            .update("leida", true)
            .await()
    }

    // Eliminar notificación
    suspend fun eliminarNotificacion(notificacionId: String) {
        notificacionesCollection.document(notificacionId).delete().await()
    }

    // Eliminar todas las notificaciones de un usuario
    suspend fun eliminarTodasLasNotificaciones(email: String) {
        val snapshot = notificacionesCollection
            .whereEqualTo("destinatarioEmail", email)
            .get()
            .await()
        
        val batch = db.batch()
        snapshot.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        batch.commit().await()
    }

    // Obtener cantidad de notificaciones no leídas
    suspend fun obtenerCantidadNoLeidas(email: String): Int {
        val snapshot = notificacionesCollection
            .whereEqualTo("destinatarioEmail", email)
            .whereEqualTo("leida", false)
            .get()
            .await()
        
        return snapshot.size()
    }

    // Enviar notificación push a un usuario específico
    suspend fun enviarNotificacionPush(email: String, titulo: String, mensaje: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            val tokenDoc = db.collection("fcm_tokens").document(email).get().await()
            
            if (tokenDoc.exists()) {
                val token = tokenDoc.getString("token")
                if (token != null) {
                    // Crear notificación en Firestore (las notificaciones push reales se manejan desde Firebase Console)
                    crearNotificacion(titulo, mensaje, TipoNotificacion.PUSH, email, false)
                    println("Notificación push enviada a: $email")
                    println("Para notificaciones push reales, usa Firebase Console > Cloud Messaging")
                } else {
                    println("Token FCM no encontrado para: $email")
                }
            } else {
                println("Usuario no tiene token FCM registrado: $email")
            }
        } catch (e: Exception) {
            println("Error al enviar notificación push: ${e.message}")
            // Fallback: crear solo notificación en Firestore
            crearNotificacion(titulo, mensaje, TipoNotificacion.PUSH, email, false)
        }
    }

    // Enviar notificación push a un grupo específico
    suspend fun enviarNotificacionPushAGrupo(grado: String, grupo: String, titulo: String, mensaje: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            val usuarios = db.collection("usuarios")
                .whereEqualTo("grado", grado)
                .whereEqualTo("grupo", grupo)
                .get()
                .await()
            
            for (usuario in usuarios.documents) {
                val email = usuario.getString("email")
                if (email != null) {
                    enviarNotificacionPush(email, titulo, mensaje)
                }
            }
            println("Notificación push enviada al grupo $grado-$grupo")
        } catch (e: Exception) {
            println("Error al enviar notificación push al grupo: ${e.message}")
        }
    }

    // Enviar notificación push a todos los usuarios
    suspend fun enviarNotificacionPushATodos(titulo: String, mensaje: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            val tokens = db.collection("fcm_tokens").get().await()
            
            for (tokenDoc in tokens.documents) {
                val email = tokenDoc.getString("email")
                if (email != null) {
                    enviarNotificacionPush(email, titulo, mensaje)
                }
            }
            println("Notificación push enviada a todos los usuarios")
        } catch (e: Exception) {
            println("Error al enviar notificación push a todos: ${e.message}")
        }
    }
}