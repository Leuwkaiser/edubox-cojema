package com.example.buzondesugerenciascojema.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.FirebaseFirestoreSettings

class FirebaseService {
    private val db = FirebaseFirestore.getInstance()
    private val usuariosCollection = db.collection("usuarios")

    // Método de prueba para verificar la conexión
    suspend fun probarConexion(): Boolean {
        return try {
            // Intentar escribir un documento de prueba
            val docRef = db.collection("prueba").document("conexion")
            docRef.set(mapOf("timestamp" to System.currentTimeMillis())).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun guardarUsuario(usuario: Usuario) {
        try {
            // Convertir el objeto Usuario a un Map
            val usuarioMap = mapOf(
                "nombreCompleto" to usuario.nombreCompleto,
                "email" to usuario.email,
                "password" to usuario.password,
                "grado" to usuario.grado,
                "grupo" to usuario.grupo,
                "codigoGrado" to usuario.codigoGrado,
                "fotoPerfil" to usuario.fotoPerfil,
                "esAdmin" to usuario.esAdmin
            )
            
            // Guardar en Firestore usando await directamente
            usuariosCollection.document(usuario.email).set(usuarioMap).await()
            println("Usuario guardado correctamente en Firestore")
        } catch (e: Exception) {
            println("Error en guardarUsuario: ${e.message}")
            throw Exception("Error al guardar usuario: ${e.message}")
        }
    }

    suspend fun obtenerUsuario(email: String): Usuario? {
        return try {
            val document = usuariosCollection.document(email).get().await()
            if (document.exists()) {
                document.toObject(Usuario::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            throw Exception("Error al obtener usuario: ${e.message}")
        }
    }

    suspend fun verificarCredenciales(email: String, password: String): Usuario? {
        return try {
            val document = usuariosCollection.document(email).get().await()
            if (document.exists()) {
                val usuario = document.toObject(Usuario::class.java)
                if (usuario?.password == password) {
                    usuario
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            throw Exception("Error al verificar credenciales: ${e.message}")
        }
    }

    suspend fun obtenerUsuariosPorGradoYGrupo(grado: String, grupo: String): List<Usuario> {
        return try {
            val querySnapshot = usuariosCollection
                .whereEqualTo("grado", grado)
                .whereEqualTo("grupo", grupo)
                .get()
                .await()
            
            querySnapshot.documents.mapNotNull { document ->
                document.toObject(Usuario::class.java)
            }
        } catch (e: Exception) {
            throw Exception("Error al obtener usuarios: ${e.message}")
        }
    }
} 