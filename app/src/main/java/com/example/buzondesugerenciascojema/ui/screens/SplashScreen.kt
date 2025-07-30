package com.example.buzondesugerenciascojema.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.buzondesugerenciascojema.R
import com.example.buzondesugerenciascojema.ui.theme.*
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.graphicsLayer
import com.example.buzondesugerenciascojema.util.SoundGenerator
import androidx.compose.ui.platform.LocalContext

@Composable
fun SplashScreen(navController: NavController) {
    var showLogo by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(false) }
    var showLoading by remember { mutableStateOf(false) }
    var showParticles by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val soundGenerator = remember { SoundGenerator(context) }
    
    // Animaciones
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    
    // Animación de pulso para el logo
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoPulse"
    )
    
    // Animación de rotación para partículas
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Animación de brillo
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )
    
    // Efecto de partículas flotantes (optimizado para gama baja)
    val particleCount = 3 // Reducido de 7 a 3
    val infiniteTransitionParticles = rememberInfiniteTransition(label = "particles")
    val particleOffsets = List(particleCount) { index ->
        infiniteTransitionParticles.animateFloat(
            initialValue = 0f,
            targetValue = 30f + index * 8, // Menos movimiento
            animationSpec = infiniteRepeatable(
                animation = tween(2500 + index * 200, easing = LinearEasing), // Más corto
                repeatMode = RepeatMode.Reverse
            ), label = "offset$index"
        )
    }
    val particleScales = List(particleCount) { index ->
        infiniteTransitionParticles.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200 + index * 150, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "scale$index"
        )
    }
    particleOffsets.forEachIndexed { index, offsetAnim ->
        val scale = particleScales[index].value
        val offset = offsetAnim.value
        Box(
            modifier = Modifier
                .size((10 + index * 2).dp)
                .scale(scale)
                .graphicsLayer {
                    translationX = (offset * kotlin.math.cos(index * 120f * Math.PI / 180f)).toFloat()
                    translationY = (offset * kotlin.math.sin(index * 120f * Math.PI / 180f)).toFloat()
                    alpha = 0.18f + 0.08f * index
                }
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.22f))
        )
    }
    
    // Secuencia de animaciones
    LaunchedEffect(Unit) {
        delay(300) // Pequeña pausa inicial
        soundGenerator.playSplashSound() // Sonido elegante de inicio
        delay(200) // Esperar a que termine el sonido
        showLogo = true
        delay(800)
        showTitle = true
        delay(600)
        showSubtitle = true
        delay(400)
        showParticles = true
        delay(300)
        showLoading = true
        delay(2000) // Tiempo total en splash
        navController.navigate("login") {
            popUpTo("splash") { inclusive = true }
        }
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
        
        // Partículas flotantes (optimizado para gama baja)
        val particleCount = 3 // Reducido de 7 a 3
        val infiniteTransitionParticles = rememberInfiniteTransition(label = "particles")
        val particleOffsets = List(particleCount) { index ->
            infiniteTransitionParticles.animateFloat(
                initialValue = 0f,
                targetValue = 30f + index * 8, // Menos movimiento
                animationSpec = infiniteRepeatable(
                    animation = tween(2500 + index * 200, easing = LinearEasing), // Más corto
                    repeatMode = RepeatMode.Reverse
                ), label = "offset$index"
            )
        }
        val particleScales = List(particleCount) { index ->
            infiniteTransitionParticles.animateFloat(
                initialValue = 0.9f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2200 + index * 150, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "scale$index"
            )
        }
        particleOffsets.forEachIndexed { index, offsetAnim ->
            val scale = particleScales[index].value
            val offset = offsetAnim.value
            Box(
                modifier = Modifier
                    .size((10 + index * 2).dp)
                    .scale(scale)
                    .graphicsLayer {
                        translationX = (offset * kotlin.math.cos(index * 120f * Math.PI / 180f)).toFloat()
                        translationY = (offset * kotlin.math.sin(index * 120f * Math.PI / 180f)).toFloat()
                        alpha = 0.18f + 0.08f * index
                    }
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f))
            )
        }
        
        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo principal
            AnimatedVisibility(
                visible = showLogo,
                enter = fadeIn(animationSpec = tween(1000)) + scaleIn(
                    initialScale = 0.3f,
                    animationSpec = tween(1000, easing = EaseOutBack)
                ),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(logoScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.3f * shimmer),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_cojema),
                        contentDescription = "Logo EduBox COJEMA",
                        modifier = Modifier.size(140.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Título principal
            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn(animationSpec = tween(800)) + slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(800)
                ),
                exit = fadeOut() + slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(800)
                )
            ) {
                Text(
                    text = "EduBox COJEMA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subtítulo
            AnimatedVisibility(
                visible = showSubtitle,
                enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(1000)
                ),
                exit = fadeOut() + slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(1000)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Plataforma Educativa Integral",
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Conectando estudiantes, docentes y directivos",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Indicador de carga
            AnimatedVisibility(
                visible = showLoading,
                enter = fadeIn(animationSpec = tween(600)) + scaleIn(
                    initialScale = 0.5f,
                    animationSpec = tween(600)
                ),
                exit = fadeOut() + scaleOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Iniciando EduBox...",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        // Información de versión en la esquina inferior
        AnimatedVisibility(
            visible = showSubtitle,
            enter = fadeIn(animationSpec = tween(1200)),
            exit = fadeOut()
        ) {
            Text(
                text = "v1.0.0",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
        
        // Efecto de ondas en el fondo
        repeat(3) { index ->
            val waveScale by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 3000 + (index * 1000),
                        easing = EaseOutQuad
                    ),
                    repeatMode = RepeatMode.Restart
                ),
                label = "wave$index"
            )
            
            val waveAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 3000 + (index * 1000),
                        easing = EaseOutQuad
                    ),
                    repeatMode = RepeatMode.Restart
                ),
                label = "waveAlpha$index"
            )
            
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .scale(waveScale)
                    .clip(CircleShape)
                    .background(
                        Color.White.copy(alpha = waveAlpha * 0.1f)
                    )
                    .align(Alignment.Center)
            )
        }
    }
} 