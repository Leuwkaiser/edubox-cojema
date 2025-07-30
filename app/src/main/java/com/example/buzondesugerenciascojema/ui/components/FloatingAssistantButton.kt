package com.example.buzondesugerenciascojema.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import com.example.buzondesugerenciascojema.R
import com.example.buzondesugerenciascojema.ui.theme.*
import com.example.buzondesugerenciascojema.util.SoundGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FloatingAssistantButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val soundGenerator = remember { SoundGenerator(context) }
    
    // Animación de pulso mejorada
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Animación de rotación sutil
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Efecto de brillo
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )
    
    Box(
        modifier = modifier
            .zIndex(1000f) // Asegurar que esté por encima de otros elementos
    ) {
        // Efecto de brillo de fondo
        Box(
            modifier = Modifier
                .size(64.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AzulPrincipal.copy(alpha = 0.3f * shimmer),
                            Color.Transparent
                        )
                    )
                )
        )
        
        // Botón principal
        Box(
            modifier = Modifier
                .size(56.dp)
                .scale(if (isPressed) 0.9f else scale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AzulPrincipal,
                            AzulGradiente
                        )
                    )
                )
                .clickable {
                    isPressed = true
                    soundGenerator.playClick()
                    onClick()
                    // Reset del estado después de un breve delay
                    scope.launch {
                        delay(150)
                        isPressed = false
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Logo personalizado del asistente con rotación sutil
            Image(
                painter = painterResource(id = R.drawable.logoia),
                contentDescription = "Asistente Virtual COJEMA",
                modifier = Modifier
                    .size(32.dp)
                    .scale(1f + (shimmer * 0.1f)), // Efecto de respiración
                contentScale = ContentScale.Fit
            )
        }
        
        // Efecto de partículas flotantes
        repeat(3) { index ->
            val particleScale by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 2000 + (index * 500),
                        easing = EaseOutQuad
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "particle$index"
            )
            
            val particleOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 20f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 3000 + (index * 300),
                        easing = EaseInOutQuad
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "particleOffset$index"
            )
            
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .scale(particleScale)
                    .offset(
                        x = (particleOffset * kotlin.math.cos(index * 120f * kotlin.math.PI / 180f)).dp,
                        y = (particleOffset * kotlin.math.sin(index * 120f * kotlin.math.PI / 180f)).dp
                    )
                    .clip(CircleShape)
                    .background(
                        Color.White.copy(alpha = 0.6f * particleScale)
                    )
            )
        }
        
        // Indicador de disponibilidad
        Box(
            modifier = Modifier
                .size(12.dp)
                .offset(x = 44.dp, y = 4.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            VerdeExito,
                            VerdeClaro
                        )
                    )
                )
        )
    }
} 