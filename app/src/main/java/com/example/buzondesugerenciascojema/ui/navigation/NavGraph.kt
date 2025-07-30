package com.example.buzondesugerenciascojema.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.buzondesugerenciascojema.ui.screens.*
import com.example.buzondesugerenciascojema.data.AuthService
import com.example.buzondesugerenciascojema.data.UsuarioManager
import com.example.buzondesugerenciascojema.data.SugerenciaService
import com.example.buzondesugerenciascojema.data.Usuario
import com.example.buzondesugerenciascojema.data.AIService
import com.example.buzondesugerenciascojema.viewmodels.NotificacionViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.facebook.CallbackManager
import com.example.buzondesugerenciascojema.ui.screens.CompleteProfileScreen
import com.example.buzondesugerenciascojema.data.GoogleAuthService

@Composable
fun NavGraph(
    navController: NavHostController,
    authService: AuthService,
    usuarioManager: UsuarioManager,
    sugerenciaService: SugerenciaService,
    callbackManager: CallbackManager,
    onGoogleSignInClick: (() -> Unit)? = null,
    googleAuthService: GoogleAuthService? = null
) {
    val notificacionViewModel: NotificacionViewModel = viewModel()
    var mantenerSesionGlobal by remember { mutableStateOf(true) }
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                navController = navController
            )
        }

        composable("login") {
            LoginScreen(
                usuarioManager = usuarioManager,
                onNavigateToRegistro = { navController.navigate("registro") },
                onNavigateToHome = { sesion ->
                    mantenerSesionGlobal = sesion
                    navController.navigate("home")
                },
                onGoogleSignInClick = { onGoogleSignInClick?.invoke() },
                callbackManager = callbackManager
            )
        }
        
        composable("home") {
            HomeScreen(
                navController = navController,
                usuarioManager = usuarioManager,
                notificacionViewModel = notificacionViewModel,
                mantenerSesion = mantenerSesionGlobal
            )
        }
        
        composable("admin/{grado}/{grupo}") { backStackEntry ->
            val grado = backStackEntry.arguments?.getString("grado") ?: ""
            val grupo = backStackEntry.arguments?.getString("grupo") ?: ""
            var usuarioId by remember { mutableStateOf("") }
            
            LaunchedEffect(Unit) {
                try {
                    val email = usuarioManager.authService.currentUser?.email
                    if (email != null) {
                        usuarioId = email
                    }
                } catch (e: Exception) {
                    println("Error al obtener usuario: ${e.message}")
                }
            }
            
            AdminScreen(
                navController = navController,
                sugerenciaService = sugerenciaService,
                grado = grado,
                grupo = grupo,
                usuarioId = usuarioId
            )
        }
        
        composable("registro") {
            RegistroScreen(
                authService = authService,
                usuarioManager = usuarioManager,
                onNavigateToLogin = { navController.navigate("login") }
            )
        }
        
        composable("google_sign_in") {
            // Esta pantalla se maneja automáticamente en MainActivity
            // No necesitamos contenido aquí
        }
        
        composable("complete_profile") {
            var currentUser by remember { mutableStateOf<Usuario?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            
            LaunchedEffect(Unit) {
                try {
                    println("NavGraph: Obteniendo usuario actual para complete_profile")
                    currentUser = googleAuthService?.getCurrentUser()
                    println("NavGraph: Usuario obtenido: $currentUser")
                } catch (e: Exception) {
                    println("NavGraph: Error al obtener usuario: ${e.message}")
                } finally {
                    isLoading = false
                }
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (currentUser != null) {
                googleAuthService?.let { authService ->
                    CompleteProfileScreen(
                        user = currentUser!!,
                        googleAuthService = authService,
                        onProfileComplete = { updatedUser ->
                            println("NavGraph: Perfil completado, navegando a home")
                            navController.navigate("home") {
                                popUpTo("complete_profile") { inclusive = true }
                            }
                        }
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: No se pudo obtener la información del usuario")
                }
            }
        }

        composable("perfil") {
            var usuario by remember { mutableStateOf<Usuario?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            
            LaunchedEffect(Unit) {
                try {
                    val email = usuarioManager.authService.currentUser?.email
                    if (email != null) {
                        usuario = usuarioManager.obtenerUsuario(email)
                    }
                } catch (e: Exception) {
                    println("Error al obtener usuario: ${e.message}")
                } finally {
                    isLoading = false
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (usuario != null) {
                PerfilScreen(
                    navController = navController,
                    usuarioManager = usuarioManager,
                    usuario = usuario!!
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se pudo cargar la información del usuario")
                }
            }
        }

        composable("buzon/{grado}/{grupo}/{usuarioId}") { backStackEntry ->
            val grado = backStackEntry.arguments?.getString("grado") ?: ""
            val grupo = backStackEntry.arguments?.getString("grupo") ?: ""
            val usuarioId = backStackEntry.arguments?.getString("usuarioId") ?: ""
            
            if (grado.isBlank() || grupo.isBlank() || usuarioId.isBlank()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: Faltan parámetros necesarios")
                }
            } else {
                BuzonScreen(
                    sugerenciaService = sugerenciaService,
                    grado = grado,
                    grupo = grupo,
                    usuarioId = usuarioId,
                    onBackPressed = { navController.navigate("home") }
                )
            }
        }

        // Rutas de la Biblioteca Virtual
        composable("biblioteca") {
            BibliotecaScreen(
                navController = navController,
                usuarioManager = usuarioManager,
                onBackPressed = { navController.navigate("home") }
            )
        }

        composable("subir-documento") {
            SubirDocumentoScreen(
                navController = navController
            )
        }
        
        // Ruta del Asistente Virtual
        composable("assistant") {
            val aiService = remember { AIService() }
            var currentUser by remember { mutableStateOf<Usuario?>(null) }
            
            LaunchedEffect(Unit) {
                try {
                    val email = usuarioManager.authService.currentUser?.email
                    if (email != null) {
                        currentUser = usuarioManager.obtenerUsuario(email)
                    }
                } catch (e: Exception) {
                    println("Error al obtener usuario: ${e.message}")
                }
            }
            
            AssistantScreen(
                aiService = aiService,
                onBackPressed = { navController.navigate("home") },
                userName = currentUser?.nombreCompleto?.split(" ")?.firstOrNull() ?: ""
            )
        }
        
        // Ruta del Asistente desde Buzón de Sugerencias
        composable("assistant/buzon") {
            val aiService = remember { AIService() }
            var currentUser by remember { mutableStateOf<Usuario?>(null) }
            
            LaunchedEffect(Unit) {
                try {
                    val email = usuarioManager.authService.currentUser?.email
                    if (email != null) {
                        currentUser = usuarioManager.obtenerUsuario(email)
                    }
                } catch (e: Exception) {
                    println("Error al obtener usuario: ${e.message}")
                }
            }
            
            AssistantScreen(
                aiService = aiService,
                onBackPressed = { navController.navigate("home") },
                userName = currentUser?.nombreCompleto?.split(" ")?.firstOrNull() ?: ""
            )
        }
        
        // Ruta del Asistente desde Biblioteca
        composable("assistant/biblioteca") {
            val aiService = remember { AIService() }
            var currentUser by remember { mutableStateOf<Usuario?>(null) }
            
            LaunchedEffect(Unit) {
                try {
                    val email = usuarioManager.authService.currentUser?.email
                    if (email != null) {
                        currentUser = usuarioManager.obtenerUsuario(email)
                    }
                } catch (e: Exception) {
                    println("Error al obtener usuario: ${e.message}")
                }
            }
            
            AssistantScreen(
                aiService = aiService,
                onBackPressed = { navController.navigate("home") },
                userName = currentUser?.nombreCompleto?.split(" ")?.firstOrNull() ?: ""
            )
        }
        
        // Ruta del Asistente desde Panel de Administración
        composable("assistant/admin") {
            val aiService = remember { AIService() }
            var currentUser by remember { mutableStateOf<Usuario?>(null) }
            
            LaunchedEffect(Unit) {
                try {
                    val email = usuarioManager.authService.currentUser?.email
                    if (email != null) {
                        currentUser = usuarioManager.obtenerUsuario(email)
                    }
                } catch (e: Exception) {
                    println("Error al obtener usuario: ${e.message}")
                }
            }
            
            AssistantScreen(
                aiService = aiService,
                onBackPressed = { navController.navigate("home") },
                userName = currentUser?.nombreCompleto?.split(" ")?.firstOrNull() ?: ""
            )
        }

        composable("visualizar-documento/{documentoId}") { backStackEntry ->
            val documentoId = backStackEntry.arguments?.getString("documentoId") ?: ""
            VisualizarDocumentoScreen(
                navController = navController,
                documentoId = documentoId
            )
        }

        composable("calendario") {
            var usuario by remember { mutableStateOf<Usuario?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                try {
                    val email = usuarioManager.authService.currentUser?.email
                    if (email != null) {
                        usuario = usuarioManager.obtenerUsuario(email)
                    }
                } catch (e: Exception) {
                    println("Error al obtener usuario: ${e.message}")
                } finally {
                    isLoading = false
                }
            }
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                CalendarioScreen(
                    navController = navController,
                    usuario = usuario
                )
            }
        }

        // Rutas de Minijuegos
        composable("minijuegos") {
            var usuario by remember { mutableStateOf<Usuario?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                try {
                    val email = usuarioManager.authService.currentUser?.email
                    if (email != null) {
                        usuario = usuarioManager.obtenerUsuario(email)
                    }
                } catch (e: Exception) {
                    println("Error al obtener usuario: ${e.message}")
                } finally {
                    isLoading = false
                }
            }
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                MinijuegosScreen(
                    navController = navController,
                    usuario = usuario
                )
            }
        }

        composable("snake") {
            var usuario by remember { mutableStateOf<Usuario?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                try {
                    val email = usuarioManager.authService.currentUser?.email
                    if (email != null) {
                        usuario = usuarioManager.obtenerUsuario(email)
                    }
                } catch (e: Exception) {
                    println("Error al obtener usuario: ${e.message}")
                } finally {
                    isLoading = false
                }
            }
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                SnakeGameScreen(
                    navController = navController,
                    usuario = usuario
                )
            }
        }

        composable("tetris") {
            TetrisScreen(
                navController = navController
            )
        }

        composable("space_invaders") {
            var usuario by remember { mutableStateOf<Usuario?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                try {
                    val email = usuarioManager.authService.currentUser?.email
                    if (email != null) {
                        usuario = usuarioManager.obtenerUsuario(email)
                    }
                } catch (e: Exception) {
                    println("Error al obtener usuario: ${e.message}")
                } finally {
                    isLoading = false
                }
            }
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                SpaceInvadersScreen(
                    navController = navController,
                    usuario = usuario
                )
            }
        }

        // composable("skeleton_survival") {
        //     SkeletonSurvivalScreen(
        //         navController = navController
        //     )
        // }

        composable("bubble_shooter") {
            var usuario by remember { mutableStateOf<Usuario?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                try {
                    val email = usuarioManager.authService.currentUser?.email
                    if (email != null) {
                        usuario = usuarioManager.obtenerUsuario(email)
                    }
                } catch (e: Exception) {
                    println("Error al obtener usuario: ${e.message}")
                } finally {
                    isLoading = false
                }
            }
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                BubbleShooterScreen(
                    navController = navController,
                    usuario = usuario
                )
            }
        }

        composable("notificaciones") {
            NotificacionesScreen(
                navController = navController,
                usuarioManager = usuarioManager,
                viewModel = notificacionViewModel
            )
        }



        composable("trivia") {
            var usuario by remember { mutableStateOf<Usuario?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                try {
                    val email = usuarioManager.authService.currentUser?.email
                    if (email != null) {
                        usuario = usuarioManager.obtenerUsuario(email)
                    }
                } catch (e: Exception) {
                    println("Error al obtener usuario: "+e.message)
                } finally {
                    isLoading = false
                }
            }
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (usuario != null) {
                TriviaScreen(
                    navController = navController,
                    userGrade = usuario!!.grado.toIntOrNull() ?: 6
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se pudo cargar la información del usuario")
                }
            }
        }

               composable("tres_en_raya") {
           TresEnRayaScreen(
               navController = navController
           )
       }
       composable("buscaminas") {
           BuscaminasScreen(
               navController = navController
           )
       }
       composable("pong") {
           PongScreen(
               navController = navController
           )
       }

        composable("dino_runner") {
            DinoGameScreen()
        }

        

        // Pantalla Acerca de EduBox
        composable("acerca-edubox") {
            AcercaDeEduBoxScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
    }
} 