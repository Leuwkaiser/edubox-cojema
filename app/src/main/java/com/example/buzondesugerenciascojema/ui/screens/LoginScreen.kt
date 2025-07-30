package com.example.buzondesugerenciascojema.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buzondesugerenciascojema.R
import com.example.buzondesugerenciascojema.data.AuthService
import com.example.buzondesugerenciascojema.data.UsuarioManager
import com.example.buzondesugerenciascojema.data.Usuario
import com.example.buzondesugerenciascojema.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    usuarioManager: UsuarioManager,
    onNavigateToRegistro: () -> Unit,
    onNavigateToHome: (Boolean) -> Unit,
    onGoogleSignInClick: () -> Unit,
    callbackManager: com.facebook.CallbackManager
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var recordarContrasena by remember { mutableStateOf(false) }
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var recoveryEmail by remember { mutableStateOf("") }
    var recoveryMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    // Animaciones mejoradas
    var showContent by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Efecto para mostrar contenido con animación
    LaunchedEffect(Unit) { 
        showContent = true 
    }

    // Cargar credenciales guardadas al iniciar
    LaunchedEffect(Unit) {
        val (savedEmail, savedPassword) = usuarioManager.authService.getSavedCredentials(context)
        if (savedEmail != null && savedPassword != null) {
            email = savedEmail
            password = savedPassword
            recordarContrasena = true
        }
    }

    // Cambiar color de la status bar
    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = view.context as android.app.Activity
        activity.window.statusBarColor = AzulOscuro.toArgb()
        WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = GradientePrimario
                )
            )
    ) {
        // Fondo decorativo con patrón
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )

        if (isLandscape) {
            // Layout horizontal para landscape
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lado izquierdo - Logo y branding
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo animado
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(animationSpec = tween(1000)) + scaleIn(
                            initialScale = 0.3f,
                            animationSpec = tween(1000, easing = EaseOutBack)
                        ),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_cojema),
                                contentDescription = "Logo EduBox COJEMA",
                                modifier = Modifier.size(100.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Título y descripción
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(animationSpec = tween(1200)) + slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = tween(1200)
                        ),
                        exit = fadeOut() + slideOutHorizontally(
                            targetOffsetX = { -it },
                            animationSpec = tween(1200)
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "EduBox COJEMA",
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Plataforma Educativa Integral",
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Accede a sugerencias, biblioteca, juegos y más",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(48.dp))
                
                // Lado derecho - Formulario de login
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 16.dp
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    LoginForm(
                        email = email,
                        onEmailChange = { email = it },
                        password = password,
                        onPasswordChange = { password = it },
                        passwordVisible = passwordVisible,
                        onPasswordVisibilityChange = { passwordVisible = it },
                        recordarContrasena = recordarContrasena,
                        onRecordarContrasenaChange = { recordarContrasena = it },
                        isLoading = isLoading,
                        error = error,
                        onLoginClick = {
                            if (email.isBlank() || password.isBlank()) {
                                error = "Por favor complete todos los campos"
                                return@LoginForm
                            }
                            isLoading = true
                            scope.launch {
                                try {
                                    usuarioManager.authService.signIn(email, password, recordarContrasena, context)
                                    val usuario = usuarioManager.obtenerUsuario(email)
                                    if (usuario != null) {
                                        usuarioManager.actualizarUsuarioActual(usuario)
                                    }
                                    onNavigateToHome(recordarContrasena)
                                } catch (e: Exception) {
                                    error = "Error al iniciar sesión: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        onGoogleSignInClick = onGoogleSignInClick,
                        onRegistroClick = onNavigateToRegistro,
                        onRecuperarClick = { showRecoveryDialog = true },
                        showContent = showContent
                    )
                }
            }
        } else {
            // Layout vertical para portrait
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo animado
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(1000)) + scaleIn(
                        initialScale = 0.3f,
                        animationSpec = tween(1000, easing = EaseOutBack)
                    ),
                    exit = fadeOut() + scaleOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_cojema),
                            contentDescription = "Logo EduBox COJEMA",
                            modifier = Modifier.size(100.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Título y descripción
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(1200)) + slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(1200)
                    ),
                    exit = fadeOut() + slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(1200)
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "EduBox COJEMA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Plataforma Educativa Integral",
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Accede a sugerencias, biblioteca, juegos y más",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Formulario de login
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 16.dp
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    LoginForm(
                        email = email,
                        onEmailChange = { email = it },
                        password = password,
                        onPasswordChange = { password = it },
                        passwordVisible = passwordVisible,
                        onPasswordVisibilityChange = { passwordVisible = it },
                        recordarContrasena = recordarContrasena,
                        onRecordarContrasenaChange = { recordarContrasena = it },
                        isLoading = isLoading,
                        error = error,
                        onLoginClick = {
                            if (email.isBlank() || password.isBlank()) {
                                error = "Por favor complete todos los campos"
                                return@LoginForm
                            }
                            isLoading = true
                            scope.launch {
                                try {
                                    usuarioManager.authService.signIn(email, password, recordarContrasena, context)
                                    val usuario = usuarioManager.obtenerUsuario(email)
                                    if (usuario != null) {
                                        usuarioManager.actualizarUsuarioActual(usuario)
                                    }
                                    onNavigateToHome(recordarContrasena)
                                } catch (e: Exception) {
                                    error = "Error al iniciar sesión: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        onGoogleSignInClick = onGoogleSignInClick,
                        onRegistroClick = onNavigateToRegistro,
                        onRecuperarClick = { showRecoveryDialog = true },
                        showContent = showContent
                    )
                }
            }
        }
    }

    // Diálogo de recuperación de contraseña
    if (showRecoveryDialog) {
        RecoveryPasswordDialog(
            email = recoveryEmail,
            onEmailChange = { recoveryEmail = it },
            message = recoveryMessage,
            onDismiss = { showRecoveryDialog = false },
            onSendRecovery = {
                scope.launch {
                    try {
                        usuarioManager.authService.enviarCorreoRecuperacion(recoveryEmail)
                        recoveryMessage = "Se ha enviado un correo de recuperación"
                    } catch (e: Exception) {
                        recoveryMessage = "Error: ${e.message}"
                    }
                }
            }
        )
    }
}

@Composable
fun LoginForm(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: (Boolean) -> Unit,
    recordarContrasena: Boolean,
    onRecordarContrasenaChange: (Boolean) -> Unit,
    isLoading: Boolean,
    error: String?,
    onLoginClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onRegistroClick: () -> Unit,
    onRecuperarClick: () -> Unit,
    showContent: Boolean
) {
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título del formulario
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = tween(1400)) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(1400)
            ),
            exit = fadeOut() + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(1400)
            )
        ) {
            Text(
                text = "Iniciar Sesión",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = TextoPrimario,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Campo de email
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = tween(1600)) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(1600)
            ),
            exit = fadeOut() + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(1600)
            )
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Correo electrónico", color = TextoPrimario) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        tint = AzulPrincipal
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzulPrincipal,
                    unfocusedBorderColor = GrisClaro,
                    focusedLabelColor = AzulPrincipal,
                    unfocusedLabelColor = GrisClaro,
                    focusedTextColor = TextoPrimario,
                    unfocusedTextColor = TextoPrimario,
                    cursorColor = AzulPrincipal
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Campo de contraseña
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = tween(1800)) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(1800)
            ),
            exit = fadeOut() + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(1800)
            )
        ) {
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Contraseña", color = TextoPrimario) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = AzulPrincipal
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { onPasswordVisibilityChange(!passwordVisible) }) {
                        Icon(
                            painter = if (passwordVisible) painterResource(id = R.drawable.visible) else painterResource(id = R.drawable.novisible),
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                            tint = AzulPrincipal
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzulPrincipal,
                    unfocusedBorderColor = GrisClaro,
                    focusedLabelColor = AzulPrincipal,
                    unfocusedLabelColor = GrisClaro,
                    focusedTextColor = TextoPrimario,
                    unfocusedTextColor = TextoPrimario,
                    cursorColor = AzulPrincipal
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Opciones adicionales
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = tween(2000)) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(2000)
            ),
            exit = fadeOut() + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(2000)
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = recordarContrasena,
                        onCheckedChange = onRecordarContrasenaChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = AzulPrincipal,
                            uncheckedColor = GrisClaro
                        )
                    )
                    Text(
                        text = "Recordar contraseña",
                        color = TextoSecundario,
                        fontSize = 14.sp
                    )
                }
                
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    color = AzulPrincipal,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onRecuperarClick() },
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Mensaje de error
        if (error != null) {
            AnimatedVisibility(
                visible = error != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = RojoError.copy(alpha = 0.1f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RojoError)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = RojoError,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error!!,
                            color = RojoError,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Botón de login
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = tween(2200)) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(2200)
            ),
            exit = fadeOut() + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(2200)
            )
        ) {
            Button(
                onClick = onLoginClick,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulPrincipal
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Iniciar Sesión",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Separador
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(
                modifier = Modifier.weight(1f),
                color = Color.White.copy(alpha = 0.3f)
            )
            Text(
                text = " o ",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Divider(
                modifier = Modifier.weight(1f),
                color = Color.White.copy(alpha = 0.3f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Botón de Google
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = tween(2300)) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(2300)
            ),
            exit = fadeOut() + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(2300)
            )
        ) {
            OutlinedButton(
                onClick = onGoogleSignInClick,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google),
                        contentDescription = "Google",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continuar con Google",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Enlace de registro
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = tween(2400)) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(2400)
            ),
            exit = fadeOut() + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(2400)
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿No tienes cuenta? ",
                    color = TextoSecundario,
                    fontSize = 14.sp
                )
                Text(
                    text = "Regístrate aquí",
                    color = AzulPrincipal,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onRegistroClick() }
                )
            }
        }
    }
}

@Composable
fun RecoveryPasswordDialog(
    email: String,
    onEmailChange: (String) -> Unit,
    message: String?,
    onDismiss: () -> Unit,
    onSendRecovery: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Recuperar Contraseña",
                fontWeight = FontWeight.Bold,
                color = TextoPrimario
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Correo electrónico") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulPrincipal,
                        unfocusedBorderColor = GrisClaro
                    )
                )
                if (message != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        color = if (message.startsWith("Error")) RojoError else VerdeExito,
                        fontSize = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSendRecovery,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulPrincipal
                )
            ) {
                Text("Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
} 