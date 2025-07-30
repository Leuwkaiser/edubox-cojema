package com.example.buzondesugerenciascojema.data

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GoogleAuthService {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("484441940977-i13p8a4u54b0q31hrivi366dq6go8nff.apps.googleusercontent.com")
            .requestEmail()
            .requestProfile()
            .build()
        
        return GoogleSignIn.getClient(context, gso)
    }
    
    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<Usuario> {
        return try {
            println("GoogleAuthService: Iniciando autenticación con Firebase")
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            
            val user = authResult.user
            if (user != null) {
                println("GoogleAuthService: Usuario autenticado en Firebase: ${user.email}")
                
                // Verificar si el usuario ya existe en Firestore
                val existingUser = checkIfUserExists(user.email ?: "")
                
                if (existingUser != null) {
                    println("GoogleAuthService: Usuario existente encontrado en Firestore")
                    // Usuario existe, retornar sus datos
                    Result.success(existingUser)
                } else {
                    println("GoogleAuthService: Usuario nuevo, creando registro en Firestore")
                    // Usuario nuevo, crear registro básico
                    val newUser = Usuario(
                        nombreCompleto = user.displayName ?: "",
                        email = user.email ?: "",
                        password = "",
                        grado = "",
                        grupo = "",
                        codigoGrado = "",
                        fotoPerfil = "",
                        esAdmin = false
                    )
                    
                    // Guardar en Firestore
                    saveUserToFirestore(user.uid, newUser)
                    println("GoogleAuthService: Usuario nuevo guardado en Firestore")
                    
                    Result.success(newUser)
                }
            } else {
                println("GoogleAuthService: Error - usuario Firebase es null")
                Result.failure(Exception("Error al autenticar con Google: usuario es null"))
            }
        } catch (e: Exception) {
            println("GoogleAuthService: Excepción durante autenticación: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    private suspend fun checkIfUserExists(email: String): Usuario? {
        return try {
            println("GoogleAuthService: Verificando si existe usuario con email: $email")
            val document = firestore.collection("usuarios")
                .whereEqualTo("email", email)
                .get()
                .await()
            
            println("GoogleAuthService: Documentos encontrados: ${document.size()}")
            
            if (!document.isEmpty) {
                val userData = document.documents[0].data
                println("GoogleAuthService: Datos del usuario: $userData")
                userData?.let {
                    val usuario = Usuario(
                        nombreCompleto = it["nombreCompleto"] as? String ?: "",
                        email = it["email"] as? String ?: "",
                        password = it["password"] as? String ?: "",
                        grado = it["grado"] as? String ?: "",
                        grupo = it["grupo"] as? String ?: "",
                        codigoGrado = it["codigoGrado"] as? String ?: "",
                        fotoPerfil = it["fotoPerfil"] as? String ?: "",
                        esAdmin = it["esAdmin"] as? Boolean ?: false
                    )
                    println("GoogleAuthService: Usuario mapeado: $usuario")
                    usuario
                }
            } else {
                println("GoogleAuthService: No se encontró usuario con email: $email")
                null
            }
        } catch (e: Exception) {
            println("GoogleAuthService: Error al verificar usuario: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    private suspend fun saveUserToFirestore(userId: String, user: Usuario) {
        try {
            println("GoogleAuthService: Guardando usuario en Firestore con ID: $userId")
            println("GoogleAuthService: Datos del usuario a guardar: $user")
            
            firestore.collection("usuarios")
                .document(userId)
                .set(user)
                .await()
            
            println("GoogleAuthService: Usuario guardado exitosamente en Firestore")
        } catch (e: Exception) {
            println("GoogleAuthService: Error al guardar usuario en Firestore: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    
    suspend fun updateUserProfile(userId: String, nombre: String, grupo: String, grado: String, codigo: String): Result<Boolean> {
        return try {
            val updates = mapOf(
                "nombreCompleto" to nombre,
                "grupo" to grupo,
                "grado" to grado,
                "codigoGrado" to codigo
            )
            
            firestore.collection("usuarios")
                .document(userId)
                .update(updates)
                .await()
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun signOut() {
        try {
            auth.signOut()
            println("GoogleAuthService: Usuario desautenticado de Firebase")
        } catch (e: Exception) {
            println("GoogleAuthService: Error al desautenticar: ${e.message}")
        }
    }
    
    fun isUserSignedIn(): Boolean {
        val currentUser = auth.currentUser
        val isSignedIn = currentUser != null
        println("GoogleAuthService: Usuario autenticado: $isSignedIn")
        return isSignedIn
    }
    
    fun getCurrentUser(): Usuario? {
        val firebaseUser = auth.currentUser
        println("GoogleAuthService: getCurrentUser - Firebase user: ${firebaseUser?.email}")
        
        return firebaseUser?.let {
            // Intentar obtener datos completos desde Firestore
            val email = it.email ?: ""
            if (email.isNotEmpty()) {
                // Usar runBlocking para obtener datos de Firestore de forma síncrona
                try {
                    val document = firestore.collection("usuarios")
                        .whereEqualTo("email", email)
                        .get()
                        .result // Usar .result en lugar de .await() para llamada síncrona
                    
                    if (!document.isEmpty) {
                        val userData = document.documents[0].data
                        userData?.let { data ->
                            val usuario = Usuario(
                                nombreCompleto = data["nombreCompleto"] as? String ?: it.displayName ?: "",
                                email = data["email"] as? String ?: "",
                                password = data["password"] as? String ?: "",
                                grado = data["grado"] as? String ?: "",
                                grupo = data["grupo"] as? String ?: "",
                                codigoGrado = data["codigoGrado"] as? String ?: "",
                                fotoPerfil = data["fotoPerfil"] as? String ?: "",
                                esAdmin = data["esAdmin"] as? Boolean ?: false
                            )
                            println("GoogleAuthService: getCurrentUser - Usuario desde Firestore: $usuario")
                            return usuario
                        }
                    }
                } catch (e: Exception) {
                    println("GoogleAuthService: getCurrentUser - Error al obtener de Firestore: ${e.message}")
                }
            }
            
            // Fallback: crear usuario básico desde Firebase Auth
            val usuario = Usuario(
                nombreCompleto = it.displayName ?: "",
                email = it.email ?: "",
                password = "",
                grado = "",
                grupo = "",
                codigoGrado = "",
                fotoPerfil = "",
                esAdmin = false
            )
            println("GoogleAuthService: getCurrentUser - Usuario básico creado: $usuario")
            usuario
        }
    }
} 