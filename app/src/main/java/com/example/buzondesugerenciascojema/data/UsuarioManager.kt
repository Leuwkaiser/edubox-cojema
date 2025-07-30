package com.example.buzondesugerenciascojema.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.content.Context

class UsuarioManager(
    private val firebaseService: FirebaseService,
    val authService: AuthService
) {
    private val _currentUser = MutableStateFlow<Usuario?>(null)
    val currentUser: StateFlow<Usuario?> = _currentUser.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    private val usuariosCollection = db.collection("usuarios")

    suspend fun guardarUsuario(usuario: Usuario) {
        try {
            firebaseService.guardarUsuario(usuario)
            _currentUser.value = usuario
        } catch (e: Exception) {
            throw Exception("Error al guardar el usuario: ${e.message}")
        }
    }

    suspend fun actualizarUsuario(usuario: Usuario): Boolean {
        return try {
            val usuarioMap = mapOf(
                "nombreCompleto" to usuario.nombreCompleto,
                "email" to usuario.email,
                "password" to usuario.password,
                "grado" to usuario.grado,
                "grupo" to usuario.grupo,
                "fotoPerfil" to usuario.fotoPerfil,
                "esAdmin" to usuario.esAdmin
            )
            
            usuariosCollection.document(usuario.email)
                .set(usuarioMap)
                .await()
            
            // Actualizar el estado del usuario actual
            _currentUser.value = usuario
            true
        } catch (e: Exception) {
            println("Error al actualizar usuario: ${e.message}")
            false
        }
    }

    suspend fun verificarCredenciales(email: String, password: String): Usuario? {
        return try {
            val usuario = firebaseService.verificarCredenciales(email, password)
            _currentUser.value = usuario
            usuario
        } catch (e: Exception) {
            throw Exception("Error al verificar credenciales: ${e.message}")
        }
    }

    suspend fun obtenerUsuariosPorGradoYGrupo(grado: String, grupo: String): List<Usuario> {
        return try {
            firebaseService.obtenerUsuariosPorGradoYGrupo(grado, grupo)
        } catch (e: Exception) {
            throw Exception("Error al obtener usuarios: ${e.message}")
        }
    }

    suspend fun cerrarSesion(context: Context) {
        try {
            authService.signOut()
            _currentUser.value = null
        } catch (e: Exception) {
            throw Exception("Error al cerrar sesión: ${e.message}")
        }
    }

    suspend fun obtenerUsuario(email: String): Usuario? {
        return try {
            val normalizedEmail = email.trim().lowercase()
            println("[DEBUG] Buscando usuario con email: '$normalizedEmail'")
            val document = usuariosCollection.document(normalizedEmail).get().await()
            println("[DEBUG] Documento existe: ${document.exists()}")
            println("[DEBUG] Datos del documento: ${document.data}")
            if (document.exists()) {
                val data = document.data ?: return null
                val usuario = Usuario(
                    nombreCompleto = data["nombreCompleto"] as? String ?: "",
                    email = data["email"] as? String ?: "",
                    password = data["password"] as? String ?: "",
                    grado = data["grado"] as? String ?: "",
                    grupo = data["grupo"] as? String ?: "",
                    fotoPerfil = data["fotoPerfil"] as? String ?: "",
                    esAdmin = data["esAdmin"] as? Boolean ?: false
                )
                // Siempre actualizar el usuario actual cuando se obtiene exitosamente
                _currentUser.value = usuario
                println("[DEBUG] Usuario mapeado y actualizado: $usuario")
                usuario
            } else {
                println("No se encontró el documento del usuario con email: $normalizedEmail")
                null
            }
        } catch (e: Exception) {
            println("Error al obtener usuario: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    // Método para actualizar el usuario actual inmediatamente
    fun actualizarUsuarioActual(usuario: Usuario) {
        _currentUser.value = usuario
    }

    suspend fun esAdmin(usuarioId: String): Boolean {
        return try {
            val usuario = usuariosCollection.document(usuarioId).get().await()
            usuario.getBoolean("esAdmin") ?: false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun obtenerGradoYGrupo(usuarioId: String): Pair<String?, String?> {
        return try {
            val usuario = usuariosCollection.document(usuarioId).get().await()
            val grado = usuario.getString("grado")
            val grupo = usuario.getString("grupo")
            Pair(grado, grupo)
        } catch (e: Exception) {
            Pair(null, null)
        }
    }
} 