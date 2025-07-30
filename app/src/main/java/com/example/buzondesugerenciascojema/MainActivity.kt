package com.example.buzondesugerenciascojema

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.buzondesugerenciascojema.ui.navigation.NavGraph
import com.example.buzondesugerenciascojema.ui.theme.BuzonDeSugerenciasCojemaTheme
import com.example.buzondesugerenciascojema.data.AuthService
import com.example.buzondesugerenciascojema.data.UsuarioManager
import com.example.buzondesugerenciascojema.data.SugerenciaService
import com.example.buzondesugerenciascojema.data.FirebaseService

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import androidx.activity.result.contract.ActivityResultContracts
import com.example.buzondesugerenciascojema.data.GoogleAuthService
import com.example.buzondesugerenciascojema.data.Usuario

class MainActivity : ComponentActivity() {
    private lateinit var callbackManager: CallbackManager
    private lateinit var googleAuthService: GoogleAuthService
    private var navController: androidx.navigation.NavHostController? = null
    private var onGoogleSignInSuccess: ((String) -> Unit)? = null
    
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("GOOGLE_SIGN_IN", "Resultado de Google Sign-In recibido")
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            Log.d("GOOGLE_SIGN_IN", "Cuenta de Google obtenida: ${account.email}")
            handleGoogleSignIn(account)
        } catch (e: ApiException) {
            Log.e("GOOGLE_SIGN_IN", "Error en Google Sign-In: ${e.message}")
            Log.e("GOOGLE_SIGN_IN", "Código de error: ${e.statusCode}")
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e("GOOGLE_SIGN_IN", "Excepción general en Google Sign-In: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callbackManager = CallbackManager.Factory.create()

        // Solicitar permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }

        // Inicializar servicios
        val firebaseAuth = FirebaseAuth.getInstance()
        val firebaseService = FirebaseService()
        val authService = AuthService(firebaseAuth)
        val usuarioManager = UsuarioManager(firebaseService, authService)
        val sugerenciaService = SugerenciaService()
        googleAuthService = GoogleAuthService()
        
        // Configurar contexto para notificaciones
        sugerenciaService.setContext(this)

        // Configurar notificaciones push
        configurarNotificacionesPush(authService)
        

        
        // Mostrar información de depuración
        Log.d("EDUBOX_DEBUG", "=== CONFIGURACIÓN DE NOTIFICACIONES PUSH ===")
                            Log.d("EDUBOX_DEBUG", "Para obtener tu token FCM, busca en Logcat:")
                    Log.d("EDUBOX_DEBUG", "Tag: FCM, Mensaje: 'Token obtenido: [tu_token_aqui]'")
                    Log.d("EDUBOX_DEBUG", "Usa ese token en Firebase Console > Cloud Messaging")
                    Log.d("EDUBOX_DEBUG", "=== SISTEMA DE NOTIFICACIONES ACTIVADO ===")
                    Log.d("EDUBOX_DEBUG", "Las notificaciones se enviarán automáticamente cuando:")
                    Log.d("EDUBOX_DEBUG", "- Se agregue un nuevo documento")
                    Log.d("EDUBOX_DEBUG", "- Se cree una sugerencia pendiente")

        setContent {
            BuzonDeSugerenciasCojemaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    navController = rememberNavController()
                    this@MainActivity.navController = navController
                    
                    // Configurar callback para navegación después de autenticación
                    onGoogleSignInSuccess = { route ->
                        try {
                            navController.navigate(route) {
                                popUpTo("splash") { inclusive = true }
                            }
                        } catch (e: Exception) {
                            Log.e("GOOGLE_SIGN_IN", "Error en navegación: ${e.message}")
                        }
                    }
                    
                    NavGraph(
                        navController = navController,
                        authService = authService,
                        usuarioManager = usuarioManager,
                        sugerenciaService = sugerenciaService,
                        callbackManager = callbackManager,
                        onGoogleSignInClick = { startGoogleSignIn() },
                        googleAuthService = googleAuthService
                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        // (No se requiere lógica de mantener sesión iniciada ni recordar contraseña aquí, solo eliminar referencias si las hay)
    }

    private fun configurarNotificacionesPush(authService: AuthService) {
        // Obtener y guardar el token FCM
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            Log.d("FCM", "Token obtenido: $token")
            lifecycleScope.launch {
                try {
                    // Guardar token localmente
                    authService.saveFcmTokenLocally(this@MainActivity, token)
                    
                    // Registrar token en Firestore si el usuario está autenticado
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        val pushNotificationService = com.example.buzondesugerenciascojema.data.PushNotificationService()
                        pushNotificationService.registrarTokenFCM(currentUser.email ?: "", token)
                        Log.d("FCM", "Token registrado en Firestore para: ${currentUser.email}")
                    }
                } catch (e: Exception) {
                    Log.e("FCM", "Error al registrar token: ${e.message}")
                }
            }
        }

        // Configurar suscripción a temas (opcional)
        FirebaseMessaging.getInstance().subscribeToTopic("general")
            .addOnSuccessListener {
                Log.d("FCM", "Suscripción al tema 'general' exitosa")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Error al suscribirse al tema: ${e.message}")
            }
    }
    
    fun startGoogleSignIn() {
        try {
            Log.d("GOOGLE_SIGN_IN", "Iniciando proceso de Google Sign-In")
            
            // Limpiar estado previo de Google Sign-In
            try {
                val signInClient = googleAuthService.getGoogleSignInClient(this)
                signInClient.signOut()
                Log.d("GOOGLE_SIGN_IN", "Estado previo de Google Sign-In limpiado")
            } catch (e: Exception) {
                Log.e("GOOGLE_SIGN_IN", "Error al limpiar estado previo: ${e.message}")
            }
            
            val signInClient = googleAuthService.getGoogleSignInClient(this)
            googleSignInLauncher.launch(signInClient.signInIntent)
        } catch (e: Exception) {
            Log.e("GOOGLE_SIGN_IN", "Error al iniciar Google Sign-In: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun handleGoogleSignIn(account: GoogleSignInAccount) {
        lifecycleScope.launch {
            try {
                Log.d("GOOGLE_SIGN_IN", "Iniciando autenticación con Google para: ${account.email}")
                val result = googleAuthService.signInWithGoogle(account)
                result.fold(
                    onSuccess = { user ->
                        Log.d("GOOGLE_SIGN_IN", "Usuario autenticado exitosamente: ${user.email}")
                        Log.d("GOOGLE_SIGN_IN", "Grado: '${user.grado}', Grupo: '${user.grupo}'")
                        
                        // Limpiar el estado de Google Sign-In
                        try {
                            val signInClient = googleAuthService.getGoogleSignInClient(this@MainActivity)
                            signInClient.signOut()
                            Log.d("GOOGLE_SIGN_IN", "Estado de Google Sign-In limpiado")
                        } catch (e: Exception) {
                            Log.e("GOOGLE_SIGN_IN", "Error al limpiar Google Sign-In: ${e.message}")
                        }
                        
                        // Usar el callback para navegación segura
                        if (user.grupo.isBlank() || user.grado.isBlank()) {
                            Log.d("GOOGLE_SIGN_IN", "Usuario nuevo, navegando a complete_profile")
                            onGoogleSignInSuccess?.invoke("complete_profile")
                        } else {
                            Log.d("GOOGLE_SIGN_IN", "Usuario existente, navegando a home")
                            onGoogleSignInSuccess?.invoke("home")
                        }
                    },
                    onFailure = { exception ->
                        Log.e("GOOGLE_SIGN_IN", "Error al autenticar con Google: ${exception.message}")
                        exception.printStackTrace()
                        
                        // Limpiar el estado de Google Sign-In en caso de error
                        try {
                            val signInClient = googleAuthService.getGoogleSignInClient(this@MainActivity)
                            signInClient.signOut()
                        } catch (e: Exception) {
                            Log.e("GOOGLE_SIGN_IN", "Error al limpiar Google Sign-In después de error: ${e.message}")
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("GOOGLE_SIGN_IN", "Excepción en handleGoogleSignIn: ${e.message}")
                e.printStackTrace()
                
                // Limpiar el estado de Google Sign-In en caso de excepción
                try {
                    val signInClient = googleAuthService.getGoogleSignInClient(this@MainActivity)
                    signInClient.signOut()
                } catch (cleanupException: Exception) {
                    Log.e("GOOGLE_SIGN_IN", "Error al limpiar Google Sign-In después de excepción: ${cleanupException.message}")
                }
            }
        }
    }
    

}