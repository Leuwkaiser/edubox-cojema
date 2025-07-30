package com.example.buzondesugerenciascojema.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.buzondesugerenciascojema.R
import com.example.buzondesugerenciascojema.data.AuthService
import com.example.buzondesugerenciascojema.data.UsuarioManager
import com.example.buzondesugerenciascojema.data.Usuario
import com.example.buzondesugerenciascojema.util.GradoConstants
import com.example.buzondesugerenciascojema.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import kotlinx.coroutines.delay
import androidx.activity.result.ActivityResultLauncher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    authService: AuthService,
    usuarioManager: UsuarioManager,
    onNavigateToLogin: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    var nombre by remember { mutableStateOf("") }
    var fotoPerfilUri by remember { mutableStateOf<Uri?>(null) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var gradoSeleccionado by remember { mutableStateOf("") }
    var grupoSeleccionado by remember { mutableStateOf("") }
    var mostrarSelectorGrado by remember { mutableStateOf(false) }
    var mostrarSelectorGrupo by remember { mutableStateOf(false) }
    var codigoAdmin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var mostrarDialogoVerificacion by remember { mutableStateOf(false) }
    var codigoCurso by remember { mutableStateOf("") }
    var codigoVerificado by remember { mutableStateOf(false) }
    var mostrarErrorCodigo by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var mostrarErrorCodigoAdmin by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    // Animaciones
    val infiniteTransition = rememberInfiniteTransition(label = "register")
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

    fun verificarCodigoAdmin(codigo: String): Boolean {
        return GradoConstants.validarCodigoAdmin(gradoSeleccionado, grupoSeleccionado, codigo)
    }

    LaunchedEffect(error) {
        if (error != null) {
            delay(10000) // 10 segundos
            error = null
        }
    }

    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { fotoPerfilUri = it }
    }
    
    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // La foto se guarda en la URI proporcionada
        }
    }

    fun validarEmail(email: String): Boolean {
        val dominiosPermitidos = listOf("@gmail.com", "@hotmail.com", "@outlook.com")
        return dominiosPermitidos.any { email.endsWith(it) }
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
                                text = "Únete a EduBox",
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Crea tu cuenta y comienza a explorar",
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
                
                // Lado derecho - Formulario de registro
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
                    RegistroForm(
                        currentStep = currentStep,
                        nombre = nombre,
                        onNombreChange = { nombre = it },
                        fotoPerfilUri = fotoPerfilUri,
                        onFotoPerfilChange = { fotoPerfilUri = it },
                        email = email,
                        onEmailChange = { email = it },
                        password = password,
                        onPasswordChange = { password = it },
                        confirmPassword = confirmPassword,
                        onConfirmPasswordChange = { confirmPassword = it },
                        passwordVisible = passwordVisible,
                        onPasswordVisibilityChange = { passwordVisible = it },
                        confirmPasswordVisible = confirmPasswordVisible,
                        onConfirmPasswordVisibilityChange = { confirmPasswordVisible = it },
                        gradoSeleccionado = gradoSeleccionado,
                        onGradoSeleccionadoChange = { gradoSeleccionado = it },
                        grupoSeleccionado = grupoSeleccionado,
                        onGrupoSeleccionadoChange = { grupoSeleccionado = it },
                        mostrarSelectorGrado = mostrarSelectorGrado,
                        onMostrarSelectorGradoChange = { mostrarSelectorGrado = it },
                        mostrarSelectorGrupo = mostrarSelectorGrupo,
                        onMostrarSelectorGrupoChange = { mostrarSelectorGrupo = it },
                        codigoAdmin = codigoAdmin,
                        onCodigoAdminChange = { codigoAdmin = it },
                        codigoCurso = codigoCurso,
                        onCodigoCursoChange = { codigoCurso = it },
                        codigoVerificado = codigoVerificado,
                        onCodigoVerificadoChange = { codigoVerificado = it },
                        mostrarErrorCodigo = mostrarErrorCodigo,
                        onMostrarErrorCodigoChange = { mostrarErrorCodigo = it },
                        mostrarErrorCodigoAdmin = mostrarErrorCodigoAdmin,
                        onMostrarErrorCodigoAdminChange = { mostrarErrorCodigoAdmin = it },
                        error = error,
                        onErrorChange = { error = it },
                        isLoading = isLoading,
                        onIsLoadingChange = { isLoading = it },
                        onCurrentStepChange = { currentStep = it },
                        onNavigateToLogin = onNavigateToLogin,
                        onMostrarDialogoVerificacionChange = { mostrarDialogoVerificacion = it },
                        galeriaLauncher = galeriaLauncher,
                        camaraLauncher = camaraLauncher,
                        context = context,
                        scope = scope,
                        authService = authService,
                        usuarioManager = usuarioManager,
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
                            text = "Únete a EduBox",
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Crea tu cuenta y comienza a explorar",
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
                
                // Formulario de registro
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
                    RegistroForm(
                        currentStep = currentStep,
                        nombre = nombre,
                        onNombreChange = { nombre = it },
                        fotoPerfilUri = fotoPerfilUri,
                        onFotoPerfilChange = { fotoPerfilUri = it },
                        email = email,
                        onEmailChange = { email = it },
                        password = password,
                        onPasswordChange = { password = it },
                        confirmPassword = confirmPassword,
                        onConfirmPasswordChange = { confirmPassword = it },
                        passwordVisible = passwordVisible,
                        onPasswordVisibilityChange = { passwordVisible = it },
                        confirmPasswordVisible = confirmPasswordVisible,
                        onConfirmPasswordVisibilityChange = { confirmPasswordVisible = it },
                        gradoSeleccionado = gradoSeleccionado,
                        onGradoSeleccionadoChange = { gradoSeleccionado = it },
                        grupoSeleccionado = grupoSeleccionado,
                        onGrupoSeleccionadoChange = { grupoSeleccionado = it },
                        mostrarSelectorGrado = mostrarSelectorGrado,
                        onMostrarSelectorGradoChange = { mostrarSelectorGrado = it },
                        mostrarSelectorGrupo = mostrarSelectorGrupo,
                        onMostrarSelectorGrupoChange = { mostrarSelectorGrupo = it },
                        codigoAdmin = codigoAdmin,
                        onCodigoAdminChange = { codigoAdmin = it },
                        codigoCurso = codigoCurso,
                        onCodigoCursoChange = { codigoCurso = it },
                        codigoVerificado = codigoVerificado,
                        onCodigoVerificadoChange = { codigoVerificado = it },
                        mostrarErrorCodigo = mostrarErrorCodigo,
                        onMostrarErrorCodigoChange = { mostrarErrorCodigo = it },
                        mostrarErrorCodigoAdmin = mostrarErrorCodigoAdmin,
                        onMostrarErrorCodigoAdminChange = { mostrarErrorCodigoAdmin = it },
                        error = error,
                        onErrorChange = { error = it },
                        isLoading = isLoading,
                        onIsLoadingChange = { isLoading = it },
                        onCurrentStepChange = { currentStep = it },
                        onNavigateToLogin = onNavigateToLogin,
                        onMostrarDialogoVerificacionChange = { mostrarDialogoVerificacion = it },
                        galeriaLauncher = galeriaLauncher,
                        camaraLauncher = camaraLauncher,
                        context = context,
                        scope = scope,
                        authService = authService,
                        usuarioManager = usuarioManager,
                        showContent = showContent
                    )
                }
            }
        }
    }

    // Diálogos
    if (mostrarDialogoVerificacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoVerificacion = false },
            title = {
                Text(
                    "Verifica tu correo",
                    fontWeight = FontWeight.Bold,
                    color = TextoPrimario
                )
            },
            text = {
                Column {
                    Text(
                        "Se ha enviado un correo de verificación a tu dirección de email. Por favor, verifica tu correo antes de continuar.",
                        color = TextoSecundario
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Si no encuentras el correo en tu bandeja principal, revisa la carpeta de spam o correo no deseado.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { mostrarDialogoVerificacion = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulPrincipal
                    )
                ) {
                    Text("Aceptar")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (mostrarSelectorGrado) {
        AlertDialog(
            onDismissRequest = { mostrarSelectorGrado = false },
            title = {
                Text(
                    "Seleccionar Grado",
                    fontWeight = FontWeight.Bold,
                    color = TextoPrimario
                )
            },
            text = {
                LazyColumn {
                    items(GradoConstants.GRADOS) { grado ->
                        TextButton(
                            onClick = {
                                gradoSeleccionado = grado
                                mostrarSelectorGrado = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                grado.replaceFirstChar { it.uppercase() },
                                color = TextoPrimario
                            )
                        }
                    }
                }
            },
            confirmButton = { },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (mostrarSelectorGrupo) {
        AlertDialog(
            onDismissRequest = { mostrarSelectorGrupo = false },
            title = {
                Text(
                    "Seleccionar Grupo",
                    fontWeight = FontWeight.Bold,
                    color = TextoPrimario
                )
            },
            text = {
                LazyColumn {
                    items(GradoConstants.GRUPOS) { grupo ->
                        TextButton(
                            onClick = {
                                grupoSeleccionado = grupo
                                mostrarSelectorGrupo = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                grupo,
                                color = TextoPrimario
                            )
                        }
                    }
                }
            },
            confirmButton = { },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun RegistroForm(
    currentStep: Int,
    nombre: String,
    onNombreChange: (String) -> Unit,
    fotoPerfilUri: Uri?,
    onFotoPerfilChange: (Uri?) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: (Boolean) -> Unit,
    confirmPasswordVisible: Boolean,
    onConfirmPasswordVisibilityChange: (Boolean) -> Unit,
    gradoSeleccionado: String,
    onGradoSeleccionadoChange: (String) -> Unit,
    grupoSeleccionado: String,
    onGrupoSeleccionadoChange: (String) -> Unit,
    mostrarSelectorGrado: Boolean,
    onMostrarSelectorGradoChange: (Boolean) -> Unit,
    mostrarSelectorGrupo: Boolean,
    onMostrarSelectorGrupoChange: (Boolean) -> Unit,
    codigoAdmin: String,
    onCodigoAdminChange: (String) -> Unit,
    codigoCurso: String,
    onCodigoCursoChange: (String) -> Unit,
    codigoVerificado: Boolean,
    onCodigoVerificadoChange: (Boolean) -> Unit,
    mostrarErrorCodigo: Boolean,
    onMostrarErrorCodigoChange: (Boolean) -> Unit,
    mostrarErrorCodigoAdmin: Boolean,
    onMostrarErrorCodigoAdminChange: (Boolean) -> Unit,
    error: String?,
    onErrorChange: (String?) -> Unit,
    isLoading: Boolean,
    onIsLoadingChange: (Boolean) -> Unit,
    onCurrentStepChange: (Int) -> Unit,
    onNavigateToLogin: () -> Unit,
    onMostrarDialogoVerificacionChange: (Boolean) -> Unit,
    galeriaLauncher: ActivityResultLauncher<String>,
    camaraLauncher: ActivityResultLauncher<Uri>,
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    authService: AuthService,
    usuarioManager: UsuarioManager,
    showContent: Boolean
) {
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Barra de progreso mejorada
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Paso $currentStep de 4",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextoSecundario
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = currentStep / 4f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = AzulPrincipal,
                    trackColor = GrisClaro.copy(alpha = 0.3f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Contenido del paso actual
        when (currentStep) {
            1 -> Paso1Personal(
                nombre = nombre,
                onNombreChange = onNombreChange,
                fotoPerfilUri = fotoPerfilUri,
                onFotoPerfilChange = onFotoPerfilChange,
                onNavigateToLogin = onNavigateToLogin,
                onNextStep = { onCurrentStepChange(2) },
                galeriaLauncher = galeriaLauncher,
                camaraLauncher = camaraLauncher,
                context = context,
                showContent = showContent
            )
            2 -> Paso2Credenciales(
                email = email,
                onEmailChange = onEmailChange,
                password = password,
                onPasswordChange = onPasswordChange,
                confirmPassword = confirmPassword,
                onConfirmPasswordChange = onConfirmPasswordChange,
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = onPasswordVisibilityChange,
                confirmPasswordVisible = confirmPasswordVisible,
                onConfirmPasswordVisibilityChange = onConfirmPasswordVisibilityChange,
                onPreviousStep = { onCurrentStepChange(1) },
                onNextStep = { onCurrentStepChange(3) },
                showContent = showContent
            )
            3 -> Paso3Academico(
                gradoSeleccionado = gradoSeleccionado,
                onGradoSeleccionadoChange = onGradoSeleccionadoChange,
                grupoSeleccionado = grupoSeleccionado,
                onGrupoSeleccionadoChange = onGrupoSeleccionadoChange,
                mostrarSelectorGrado = mostrarSelectorGrado,
                onMostrarSelectorGradoChange = onMostrarSelectorGradoChange,
                mostrarSelectorGrupo = mostrarSelectorGrupo,
                onMostrarSelectorGrupoChange = onMostrarSelectorGrupoChange,
                codigoAdmin = codigoAdmin,
                onCodigoAdminChange = onCodigoAdminChange,
                codigoCurso = codigoCurso,
                onCodigoCursoChange = onCodigoCursoChange,
                codigoVerificado = codigoVerificado,
                onCodigoVerificadoChange = onCodigoVerificadoChange,
                mostrarErrorCodigo = mostrarErrorCodigo,
                onMostrarErrorCodigoChange = onMostrarErrorCodigoChange,
                mostrarErrorCodigoAdmin = mostrarErrorCodigoAdmin,
                onMostrarErrorCodigoAdminChange = onMostrarErrorCodigoAdminChange,
                onPreviousStep = { onCurrentStepChange(2) },
                onNextStep = { onCurrentStepChange(4) },
                scope = scope,
                showContent = showContent
            )
            4 -> Paso4Verificacion(
                error = error,
                onErrorChange = onErrorChange,
                isLoading = isLoading,
                onIsLoadingChange = onIsLoadingChange,
                onPreviousStep = { onCurrentStepChange(3) },
                onMostrarDialogoVerificacionChange = onMostrarDialogoVerificacionChange,
                scope = scope,
                authService = authService,
                usuarioManager = usuarioManager,
                nombre = nombre,
                email = email,
                password = password,
                gradoSeleccionado = gradoSeleccionado,
                grupoSeleccionado = grupoSeleccionado,
                fotoPerfilUri = fotoPerfilUri,
                codigoAdmin = codigoAdmin,
                onNavigateToLogin = onNavigateToLogin,
                showContent = showContent
            )
        }
        
        // Mensaje de error global
        if (error != null) {
            AnimatedVisibility(
                visible = error != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
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
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { onErrorChange(null) }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Cerrar",
                                tint = RojoError,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
} 

@Composable
fun Paso1Personal(
    nombre: String,
    onNombreChange: (String) -> Unit,
    fotoPerfilUri: Uri?,
    onFotoPerfilChange: (Uri?) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNextStep: () -> Unit,
    galeriaLauncher: ActivityResultLauncher<String>,
    camaraLauncher: ActivityResultLauncher<Uri>,
    context: android.content.Context,
    showContent: Boolean
) {
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Información Personal",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = TextoPrimario,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Avatar con foto de perfil
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AzulPrincipal.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
                    .clickable { galeriaLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (fotoPerfilUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(fotoPerfilUri)
                                .crossfade(true)
                                .build()
                        ),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Agregar foto de perfil",
                        tint = AzulPrincipal,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                // Indicador de click
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AzulPrincipal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Agregar foto",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Toca para agregar foto de perfil",
                fontSize = 12.sp,
                color = TextoSecundario,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Campo de nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = onNombreChange,
                label = { Text("Nombre completo", color = TextoPrimario) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = AzulPrincipal
                    )
                },
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Botones de navegación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AzulPrincipal
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AzulPrincipal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Volver")
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Button(
                    onClick = {
                        if (nombre.isBlank()) {
                            // Mostrar error
                            return@Button
                        }
                        onNextStep()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulPrincipal
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Siguiente")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun Paso2Credenciales(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: (Boolean) -> Unit,
    confirmPasswordVisible: Boolean,
    onConfirmPasswordVisibilityChange: (Boolean) -> Unit,
    onPreviousStep: () -> Unit,
    onNextStep: () -> Unit,
    showContent: Boolean
) {
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Credenciales de Acceso",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = TextoPrimario,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Campo de email
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Campo de contraseña
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Campo de confirmar contraseña
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text("Confirmar contraseña", color = TextoPrimario) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = AzulPrincipal
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { onConfirmPasswordVisibilityChange(!confirmPasswordVisible) }) {
                        Icon(
                            painter = if (confirmPasswordVisible) painterResource(id = R.drawable.visible) else painterResource(id = R.drawable.novisible),
                            contentDescription = if (confirmPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                            tint = AzulPrincipal
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Botones de navegación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onPreviousStep,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AzulPrincipal
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AzulPrincipal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Atrás")
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                            return@Button
                        }
                        if (password != confirmPassword) {
                            return@Button
                        }
                        onNextStep()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulPrincipal
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Siguiente")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun Paso3Academico(
    gradoSeleccionado: String,
    onGradoSeleccionadoChange: (String) -> Unit,
    grupoSeleccionado: String,
    onGrupoSeleccionadoChange: (String) -> Unit,
    mostrarSelectorGrado: Boolean,
    onMostrarSelectorGradoChange: (Boolean) -> Unit,
    mostrarSelectorGrupo: Boolean,
    onMostrarSelectorGrupoChange: (Boolean) -> Unit,
    codigoAdmin: String,
    onCodigoAdminChange: (String) -> Unit,
    codigoCurso: String,
    onCodigoCursoChange: (String) -> Unit,
    codigoVerificado: Boolean,
    onCodigoVerificadoChange: (Boolean) -> Unit,
    mostrarErrorCodigo: Boolean,
    onMostrarErrorCodigoChange: (Boolean) -> Unit,
    mostrarErrorCodigoAdmin: Boolean,
    onMostrarErrorCodigoAdminChange: (Boolean) -> Unit,
    onPreviousStep: () -> Unit,
    onNextStep: () -> Unit,
    scope: kotlinx.coroutines.CoroutineScope,
    showContent: Boolean
) {
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Información Académica",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = TextoPrimario,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Selector de grado
            OutlinedTextField(
                value = gradoSeleccionado,
                onValueChange = { },
                label = { Text("Grado", color = TextoPrimario) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.libro),
                        contentDescription = null,
                        tint = AzulPrincipal
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { onMostrarSelectorGradoChange(true) }) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Seleccionar grado",
                            tint = AzulPrincipal
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Selector de grupo
            OutlinedTextField(
                value = grupoSeleccionado,
                onValueChange = { },
                label = { Text("Grupo", color = TextoPrimario) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.people),
                        contentDescription = null,
                        tint = AzulPrincipal,
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { onMostrarSelectorGrupoChange(true) }) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Seleccionar grupo",
                            tint = AzulPrincipal
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
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
            
            if (gradoSeleccionado.isNotEmpty() && grupoSeleccionado.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campo de código del curso
                OutlinedTextField(
                    value = codigoCurso,
                    onValueChange = onCodigoCursoChange,
                    label = { Text("Código del curso", color = TextoPrimario) },
                    leadingIcon = {
                                            Icon(
                        painter = painterResource(id = R.drawable.key),
                        contentDescription = null,
                        tint = AzulPrincipal,
                        modifier = Modifier.size(24.dp)
                    )
                    },
                    trailingIcon = {
                        if (codigoVerificado) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Código verificado",
                                tint = VerdeExito
                            )
                        }
                    },
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
                
                if (mostrarErrorCodigo) {
                    Text(
                        text = "Código incorrecto",
                        color = RojoError,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        if (GradoConstants.validarCodigoGradoGrupo(gradoSeleccionado, grupoSeleccionado, codigoCurso)) {
                            onCodigoVerificadoChange(true)
                            onMostrarErrorCodigoChange(false)
                        } else {
                            onCodigoVerificadoChange(false)
                            onMostrarErrorCodigoChange(true)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulClaro
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Verificar código",
                        color = AzulOscuro
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Campo de código de administrador
            OutlinedTextField(
                value = codigoAdmin,
                onValueChange = { 
                    onCodigoAdminChange(it)
                    if (it.isNotBlank()) {
                        scope.launch {
                            val esAdmin = GradoConstants.validarCodigoAdmin(gradoSeleccionado, grupoSeleccionado, it)
                            onMostrarErrorCodigoAdminChange(!esAdmin)
                        }
                    } else {
                        onMostrarErrorCodigoAdminChange(false)
                    }
                },
                label = { Text("Código de administrador (opcional)", color = TextoPrimario) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null,
                        tint = AzulPrincipal
                    )
                },
                trailingIcon = {
                    if (codigoAdmin.isNotBlank()) {
                        if (mostrarErrorCodigoAdmin) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Código incorrecto",
                                tint = RojoError
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Código correcto",
                                tint = VerdeExito
                            )
                        }
                    }
                },
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
            
            if (mostrarErrorCodigoAdmin) {
                Text(
                    text = "Código de administrador incorrecto",
                    color = RojoError,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Botones de navegación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onPreviousStep,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AzulPrincipal
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AzulPrincipal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Atrás")
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Button(
                    onClick = {
                        if (gradoSeleccionado.isBlank() || grupoSeleccionado.isBlank()) {
                            return@Button
                        }
                        if (!codigoVerificado) {
                            return@Button
                        }
                        onNextStep()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulPrincipal
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Siguiente")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun Paso4Verificacion(
    error: String?,
    onErrorChange: (String?) -> Unit,
    isLoading: Boolean,
    onIsLoadingChange: (Boolean) -> Unit,
    onPreviousStep: () -> Unit,
    onMostrarDialogoVerificacionChange: (Boolean) -> Unit,
    scope: kotlinx.coroutines.CoroutineScope,
    authService: AuthService,
    usuarioManager: UsuarioManager,
    nombre: String,
    email: String,
    password: String,
    gradoSeleccionado: String,
    grupoSeleccionado: String,
    fotoPerfilUri: Uri?,
    codigoAdmin: String,
    onNavigateToLogin: () -> Unit,
    showContent: Boolean
) {
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Verificación Final",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = TextoPrimario,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Estás a un paso de unirte a EduBox",
                fontSize = 16.sp,
                color = TextoSecundario,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Resumen de información
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = AzulClaro.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Resumen de tu cuenta:",
                        fontWeight = FontWeight.Bold,
                        color = AzulOscuro,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nombre: $nombre", fontSize = 12.sp, color = TextoSecundario)
                    Text("Email: $email", fontSize = 12.sp, color = TextoSecundario)
                    Text("Grado: $gradoSeleccionado", fontSize = 12.sp, color = TextoSecundario)
                    Text("Grupo: $grupoSeleccionado", fontSize = 12.sp, color = TextoSecundario)
                    if (codigoAdmin.isNotBlank()) {
                        Text("Tipo: Administrador", fontSize = 12.sp, color = VerdeExito)
                    } else {
                        Text("Tipo: Estudiante", fontSize = 12.sp, color = TextoSecundario)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Botón de envío de verificación
            Button(
                onClick = {
                    if (email.isBlank()) {
                        onErrorChange("Por favor ingresa tu correo electrónico")
                        return@Button
                    }
                    onIsLoadingChange(true)
                    scope.launch {
                        try {
                            val user = authService.signUp(email, password)
                            if (user != null) {
                                user.sendEmailVerification().await()
                                onMostrarDialogoVerificacionChange(true)
                            } else {
                                onErrorChange("Error al crear la cuenta")
                            }
                        } catch (e: Exception) {
                            onErrorChange("Error al enviar el correo de verificación: ${e.message}")
                        } finally {
                            onIsLoadingChange(false)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulPrincipal
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar correo de verificación")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón de registro final
            Button(
                onClick = {
                    onIsLoadingChange(true)
                    scope.launch {
                        try {
                            val user = authService.currentUser
                            if (user != null) {
                                user.reload().await()
                                val userReloaded = authService.currentUser
                                
                                if (userReloaded != null && userReloaded.isEmailVerified) {
                                    val esAdmin = if (codigoAdmin.isNotBlank()) {
                                        GradoConstants.validarCodigoAdmin(gradoSeleccionado, grupoSeleccionado, codigoAdmin)
                                    } else {
                                        false
                                    }
                                    
                                    val usuario = Usuario(
                                        nombreCompleto = nombre,
                                        email = email,
                                        password = password,
                                        grado = gradoSeleccionado,
                                        grupo = grupoSeleccionado,
                                        fotoPerfil = fotoPerfilUri?.toString() ?: "",
                                        esAdmin = esAdmin
                                    )
                                    
                                    usuarioManager.guardarUsuario(usuario)
                                    onNavigateToLogin()
                                } else {
                                    onErrorChange("Por favor, verifica tu correo electrónico antes de continuar.")
                                }
                            } else {
                                onErrorChange("No se encontró el usuario. Por favor, intenta enviar el correo de verificación nuevamente.")
                            }
                        } catch (e: Exception) {
                            onErrorChange("Error al verificar el correo: ${e.message}")
                        } finally {
                            onIsLoadingChange(false)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeExito
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Completar registro")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Botón de volver
            OutlinedButton(
                onClick = onPreviousStep,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AzulPrincipal
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, AzulPrincipal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Atrás")
            }
        }
    }
}