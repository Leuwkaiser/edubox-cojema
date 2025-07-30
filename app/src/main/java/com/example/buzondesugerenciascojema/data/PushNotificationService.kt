package com.example.buzondesugerenciascojema.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class PushNotificationService {
    private val db = FirebaseFirestore.getInstance()

    /**
     * Envía una notificación push a un usuario específico
     * Nota: Para notificaciones push reales, usa Firebase Console
     */
    suspend fun enviarNotificacionPush(
        emailDestinatario: String,
        titulo: String,
        mensaje: String,
        tipo: TipoNotificacion = TipoNotificacion.PUSH,
        datosAdicionales: Map<String, String> = emptyMap()
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Crear notificación en Firestore
            val notificacionId = UUID.randomUUID().toString()
            val notificacionService = NotificacionService()
            notificacionService.crearNotificacion(
                titulo = titulo,
                mensaje = mensaje,
                tipo = tipo,
                destinatarioEmail = emailDestinatario,
                leida = false
            )

            // Obtener el token FCM del usuario
            val tokenDoc = db.collection("fcm_tokens").document(emailDestinatario).get().await()
            if (!tokenDoc.exists()) {
                println("❌ Usuario $emailDestinatario no tiene token FCM registrado")
                return@withContext false
            }

            val token = tokenDoc.getString("token")
            if (token.isNullOrEmpty()) {
                println("❌ Token FCM vacío para usuario $emailDestinatario")
                return@withContext false
            }

            // Para notificaciones push reales, usar Firebase Console
            println("✅ Notificación creada en Firestore para: $emailDestinatario")
            println("📱 Token FCM: $token")
            println("💡 Para enviar notificación push real, usa Firebase Console > Cloud Messaging")
            
            return@withContext true

        } catch (e: Exception) {
            println("❌ Error al enviar notificación: ${e.message}")
            return@withContext false
        }
    }

    /**
     * Envía notificación push a un grupo específico (grado y grupo)
     */
    suspend fun enviarNotificacionPushAGrupo(
        grado: String,
        grupo: String,
        titulo: String,
        mensaje: String,
        tipo: TipoNotificacion = TipoNotificacion.PUSH,
        datosAdicionales: Map<String, String> = emptyMap()
    ): Int = withContext(Dispatchers.IO) {
        try {
            val usuarios = db.collection("usuarios")
                .whereEqualTo("grado", grado)
                .whereEqualTo("grupo", grupo)
                .get()
                .await()

            var exitosos = 0
            for (usuario in usuarios.documents) {
                val email = usuario.getString("email")
                if (email != null) {
                    val enviado = enviarNotificacionPush(
                        emailDestinatario = email,
                        titulo = titulo,
                        mensaje = mensaje,
                        tipo = tipo,
                        datosAdicionales = datosAdicionales
                    )
                    if (enviado) exitosos++
                }
            }

            println("✅ Notificaciones creadas para grupo $grado-$grupo: $exitosos/${usuarios.size()} exitosos")
            return@withContext exitosos

        } catch (e: Exception) {
            println("❌ Error al enviar notificaciones al grupo: ${e.message}")
            return@withContext 0
        }
    }

    /**
     * Envía notificación push a todos los usuarios
     */
    suspend fun enviarNotificacionPushATodos(
        titulo: String,
        mensaje: String,
        tipo: TipoNotificacion = TipoNotificacion.PUSH,
        datosAdicionales: Map<String, String> = emptyMap()
    ): Int = withContext(Dispatchers.IO) {
        try {
            val tokens = db.collection("fcm_tokens").get().await()
            var exitosos = 0

            for (tokenDoc in tokens.documents) {
                val email = tokenDoc.getString("email")
                if (email != null) {
                    val enviado = enviarNotificacionPush(
                        emailDestinatario = email,
                        titulo = titulo,
                        mensaje = mensaje,
                        tipo = tipo,
                        datosAdicionales = datosAdicionales
                    )
                    if (enviado) exitosos++
                }
            }

            println("✅ Notificaciones creadas para todos los usuarios: $exitosos/${tokens.size()} exitosos")
            return@withContext exitosos

        } catch (e: Exception) {
            println("❌ Error al enviar notificaciones a todos: ${e.message}")
            return@withContext 0
        }
    }

    /**
     * Envía notificación push a usuarios por rol (admin, estudiante, etc.)
     */
    suspend fun enviarNotificacionPushPorRol(
        rol: String,
        titulo: String,
        mensaje: String,
        tipo: TipoNotificacion = TipoNotificacion.PUSH,
        datosAdicionales: Map<String, String> = emptyMap()
    ): Int = withContext(Dispatchers.IO) {
        try {
            val usuarios = db.collection("usuarios")
                .whereEqualTo("rol", rol)
                .get()
                .await()

            var exitosos = 0
            for (usuario in usuarios.documents) {
                val email = usuario.getString("email")
                if (email != null) {
                    val enviado = enviarNotificacionPush(
                        emailDestinatario = email,
                        titulo = titulo,
                        mensaje = mensaje,
                        tipo = tipo,
                        datosAdicionales = datosAdicionales
                    )
                    if (enviado) exitosos++
                }
            }

            println("✅ Notificaciones creadas para usuarios con rol $rol: $exitosos/${usuarios.size()} exitosos")
            return@withContext exitosos

        } catch (e: Exception) {
            println("❌ Error al enviar notificaciones por rol: ${e.message}")
            return@withContext 0
        }
    }

    /**
     * Registra el token FCM de un usuario
     */
    suspend fun registrarTokenFCM(email: String, token: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val tokenDoc = db.collection("fcm_tokens").document(email)
            tokenDoc.set(mapOf(
                "email" to email,
                "token" to token,
                "fechaRegistro" to com.google.firebase.Timestamp.now(),
                "fechaActualizacion" to com.google.firebase.Timestamp.now(),
                "activo" to true
            )).await()

            // También actualizar en la colección de usuarios
            val userDoc = db.collection("usuarios").document(email)
            userDoc.update("fcmToken", token).await()

            println("✅ Token FCM registrado exitosamente para: $email")
            println("📱 Token: $token")
            return@withContext true

        } catch (e: Exception) {
            println("❌ Error al registrar token FCM: ${e.message}")
            return@withContext false
        }
    }

    /**
     * Desactiva el token FCM de un usuario
     */
    suspend fun desactivarTokenFCM(email: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val tokenDoc = db.collection("fcm_tokens").document(email)
            tokenDoc.update(
                mapOf(
                    "activo" to false,
                    "fechaDesactivacion" to com.google.firebase.Timestamp.now()
                )
            ).await()

            println("✅ Token FCM desactivado para: $email")
            return@withContext true

        } catch (e: Exception) {
            println("❌ Error al desactivar token FCM: ${e.message}")
            return@withContext false
        }
    }

    /**
     * Obtiene estadísticas de tokens FCM
     */
    suspend fun obtenerEstadisticasTokens(): Map<String, Any> = withContext(Dispatchers.IO) {
        try {
            val tokensActivos = db.collection("fcm_tokens")
                .whereEqualTo("activo", true)
                .get()
                .await()

            val tokensInactivos = db.collection("fcm_tokens")
                .whereEqualTo("activo", false)
                .get()
                .await()

            return@withContext mapOf(
                "totalTokens" to (tokensActivos.size() + tokensInactivos.size()),
                "tokensActivos" to tokensActivos.size(),
                "tokensInactivos" to tokensInactivos.size()
            )

        } catch (e: Exception) {
            println("❌ Error al obtener estadísticas de tokens: ${e.message}")
            return@withContext emptyMap()
        }
    }

    /**
     * Obtiene el token FCM de un usuario específico
     */
    suspend fun obtenerTokenFCM(email: String): String? = withContext(Dispatchers.IO) {
        try {
            val tokenDoc = db.collection("fcm_tokens").document(email).get().await()
            if (tokenDoc.exists()) {
                val token = tokenDoc.getString("token")
                println("📱 Token FCM para $email: $token")
                return@withContext token
            } else {
                println("❌ No se encontró token FCM para: $email")
                return@withContext null
            }
        } catch (e: Exception) {
            println("❌ Error al obtener token FCM: ${e.message}")
            return@withContext null
        }
    }
} 