package com.example.buzondesugerenciascojema.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.firestore.FirebaseFirestore
import android.content.SharedPreferences

private val Context.dataStore by preferencesDataStore(name = "settings")

class AuthService(private val auth: FirebaseAuth) {
    init {
        // Asegurarse de que no haya sesión al iniciar
        auth.signOut()
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Inicia sesión. Si mantenerSesion es false, se puede cerrar sesión automáticamente al cerrar la app.
     */
    suspend fun signIn(email: String, password: String, recordarContrasena: Boolean = false, context: Context): FirebaseUser? {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            if (recordarContrasena) {
                saveCredentials(context, email, password)
            } else {
                clearCredentials(context)
            }
            // Guardar token FCM en Firestore solo cuando el usuario inicie sesión
            result.user?.let { _ ->
                saveFcmTokenToFirestore(result.user?.email, context)
            }
            result.user
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signUp(email: String, password: String): FirebaseUser? {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            // Guardar token FCM en Firestore
            result.user?.let { user ->
                // No podemos guardar el token aquí porque no tenemos context
                // Se guardará cuando el usuario inicie sesión
            }
            result.user
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signOut() {
        // Eliminar token FCM de Firestore
        currentUser?.email?.let { email ->
            removeFcmTokenFromFirestore(email)
        }
        auth.signOut()
    }

    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun verificarEmail(): Boolean {
        return try {
            auth.currentUser?.reload()?.await()
            auth.currentUser?.isEmailVerified ?: false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun enviarCorreoRecuperacion(email: String) {
        try {
            auth.sendPasswordResetEmail(email).await()
        } catch (e: Exception) {
            throw Exception("Error al enviar el correo de recuperación: ${e.message}")
        }
    }

    /**
     * Limpia las credenciales guardadas manualmente.
     * Se puede usar cuando el usuario desmarca "Recordar contraseña".
     */
    suspend fun clearSavedCredentials(_context: Context) {
        // clearCredentials(context) // Eliminado
    }

    private suspend fun saveCredentials(context: Context, email: String, password: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey("email")] = email
            preferences[stringPreferencesKey("password")] = password
        }
    }

    private suspend fun clearCredentials(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey("email"))
            preferences.remove(stringPreferencesKey("password"))
        }
    }

    suspend fun getSavedCredentials(context: Context): Pair<String?, String?> {
        val preferences = context.dataStore.data.first()
        val email = preferences[stringPreferencesKey("email")]
        val password = preferences[stringPreferencesKey("password")]
        return Pair(email, password)
    }

    // Guardar token FCM en preferencias locales
    suspend fun saveFcmTokenLocally(context: Context, token: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey("fcm_token")] = token
        }
    }

    // Recuperar token FCM de preferencias locales
    suspend fun getFcmTokenLocally(context: Context): String? {
        val preferences = context.dataStore.data.first()
        return preferences[stringPreferencesKey("fcm_token")]
    }

    // Enviar token FCM a Firestore
    private suspend fun saveFcmTokenToFirestore(email: String?, context: Context) {
        if (email == null) return
        
        try {
            val token = getFcmTokenLocally(context)
            if (token != null) {
                val db = FirebaseFirestore.getInstance()
                db.collection("fcm_tokens").document(email).set(
                    mapOf(
                        "token" to token,
                        "email" to email,
                        "timestamp" to com.google.firebase.Timestamp.now()
                    )
                ).await()
                println("Token FCM guardado en Firestore para: $email")
            }
        } catch (e: Exception) {
            println("Error al guardar token FCM: ${e.message}")
        }
    }

    // Eliminar token FCM de Firestore al cerrar sesión
    suspend fun removeFcmTokenFromFirestore(email: String?) {
        if (email == null) return
        
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("fcm_tokens").document(email).delete().await()
            println("Token FCM eliminado de Firestore para: $email")
        } catch (e: Exception) {
            println("Error al eliminar token FCM: ${e.message}")
        }
    }

    fun saveMantenerSesion(context: Context, mantener: Boolean) {
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("mantener_sesion", mantener).apply()
    }
    fun getMantenerSesion(context: Context): Boolean {
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("mantener_sesion", true)
    }
} 