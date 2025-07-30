package com.example.buzondesugerenciascojema

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.buzondesugerenciascojema.ui.theme.BuzonDeSugerenciasCojemaTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.example.buzondesugerenciascojema.data.AuthService
import com.example.buzondesugerenciascojema.data.FirebaseService
import com.example.buzondesugerenciascojema.data.UsuarioManager
import com.example.buzondesugerenciascojema.data.SugerenciaService
import com.example.buzondesugerenciascojema.ui.navigation.NavGraph
import androidx.navigation.compose.rememberNavController
import com.facebook.CallbackManager

class BuzonDeSugerenciasApp : ComponentActivity() {
    private lateinit var callbackManager: CallbackManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callbackManager = CallbackManager.Factory.create()
        
        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        
        // Crear instancias de Firebase
        val firebaseAuth = FirebaseAuth.getInstance()
        val firebaseService = FirebaseService()
        
        // Crear instancias de servicios
        val authService = AuthService(firebaseAuth)
        val usuarioManager = UsuarioManager(firebaseService, authService)
        val sugerenciaService = SugerenciaService()
        
        setContent {
            BuzonDeSugerenciasCojemaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        authService = authService,
                        usuarioManager = usuarioManager,
                        sugerenciaService = sugerenciaService,
                        callbackManager = callbackManager
                    )
                }
            }
        }
    }
} 