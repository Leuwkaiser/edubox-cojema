package com.example.buzondesugerenciascojema.data

import com.example.buzondesugerenciascojema.model.Documento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DocumentoService {
    private val db = FirebaseFirestore.getInstance()
    private val documentosCollection = db.collection("documentos")
    private val usuariosCollection = db.collection("usuarios")
    private val notificacionService = NotificacionService()

    suspend fun obtenerDocumentos(): List<Documento> {
        return try {
            val snapshot = documentosCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Documento::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun obtenerDocumentosPorAsignatura(asignatura: String): List<Documento> {
        return try {
            val snapshot = documentosCollection
                .whereEqualTo("asignatura", asignatura)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Documento::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun obtenerDocumentosPorGrado(grado: Int): List<Documento> {
        return try {
            val snapshot = documentosCollection
                .whereEqualTo("grado", grado)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Documento::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun obtenerDocumentosPorAsignaturaYGrado(asignatura: String, grado: Int): List<Documento> {
        return try {
            val snapshot = documentosCollection
                .whereEqualTo("asignatura", asignatura)
                .whereEqualTo("grado", grado)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Documento::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun agregarDocumento(
        titulo: String,
        descripcion: String,
        asignatura: String,
        grado: Int,
        url: String,
        nombreArchivo: String,
        subidoPor: String,
        tipoArchivo: String = "application/octet-stream",
        portadaUrl: String = "",
        portadaDrawable: String = ""
    ): Result<String> {
        return try {
            // Verificar si el usuario está autenticado
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuario no autenticado. Por favor, inicia sesión primero."))
            }
            
            val documento = Documento(
                titulo = titulo,
                descripcion = descripcion,
                asignatura = asignatura,
                grado = grado,
                url = url,
                nombreArchivo = nombreArchivo,
                fechaSubida = System.currentTimeMillis(),
                subidoPor = subidoPor,
                tipoArchivo = tipoArchivo,
                esEnlaceExterno = true,
                portadaUrl = portadaUrl,
                portadaDrawable = portadaDrawable
            )
            
            println("DEBUG: Intentando agregar documento a Firestore...")
            println("DEBUG: Usuario autenticado: ${currentUser.email}")
            println("DEBUG: Documento: $documento")
            println("DEBUG: Colección: documentos (se creará automáticamente si no existe)")
            
            // Intentar agregar el documento - Firestore creará la colección automáticamente
            val docRef = documentosCollection.add(documento).await()
            println("DEBUG: Documento agregado exitosamente con ID: ${docRef.id}")
            println("DEBUG: Colección 'documentos' creada automáticamente")
            
            // Notificar a los estudiantes sobre el nuevo libro
            try {
                val estudiantes = obtenerEmailsEstudiantes()
                if (estudiantes.isNotEmpty()) {
                    // Crear notificación para cada estudiante
                    estudiantes.forEach { email ->
                        notificacionService.crearNotificacion(
                            titulo = "📚 Nuevo libro disponible",
                            mensaje = "Se ha agregado '$titulo' a la biblioteca. ¡Échale un vistazo!",
                            tipo = TipoNotificacion.NUEVO_LIBRO,
                            destinatarioEmail = email,
                            leida = false
                        )
                    }
                    println("DEBUG: Notificaciones enviadas a ${estudiantes.size} estudiantes")
                }
            } catch (e: Exception) {
                println("DEBUG: Error al enviar notificaciones: ${e.message}")
                // No fallar la operación principal por errores de notificación
            }
            
            Result.success(docRef.id)
            
        } catch (e: Exception) {
            println("DEBUG: Error al agregar documento: ${e.message}")
            println("DEBUG: Tipo de error: ${e.javaClass.simpleName}")
            
            when {
                e.message?.contains("permission-denied", ignoreCase = true) == true -> {
                    Result.failure(Exception("Error de permisos: Las reglas de Firestore no permiten crear documentos. Necesitas actualizar las reglas de seguridad en Firebase Console."))
                }
                e.message?.contains("unauthenticated", ignoreCase = true) == true -> {
                    Result.failure(Exception("Usuario no autenticado. Por favor, inicia sesión primero."))
                }
                e.message?.contains("network", ignoreCase = true) == true -> {
                    Result.failure(Exception("Error de conexión. Verifica tu conexión a internet."))
                }
                else -> {
                    Result.failure(Exception("Error al agregar documento: ${e.message}"))
                }
            }
        }
    }

    suspend fun obtenerDocumentoPorId(documentoId: String): Result<Documento> {
        return try {
            val docSnapshot = documentosCollection.document(documentoId).get().await()
            if (docSnapshot.exists()) {
                val documento = docSnapshot.toObject(Documento::class.java)?.copy(id = docSnapshot.id)
                if (documento != null) {
                    Result.success(documento)
                } else {
                    Result.failure(Exception("Error al convertir el documento"))
                }
            } else {
                Result.failure(Exception("Documento no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarDocumento(documentoId: String): Result<Unit> {
        return try {
            documentosCollection.document(documentoId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Función auxiliar para obtener emails de estudiantes
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