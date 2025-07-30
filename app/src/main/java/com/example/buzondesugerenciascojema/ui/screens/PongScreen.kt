package com.example.buzondesugerenciascojema.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.delay
import com.example.buzondesugerenciascojema.util.SoundGenerator

data class Paddle(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val speed: Float = 0f
)

data class Ball(
    val x: Float,
    val y: Float,
    val radius: Float,
    val velocityX: Float,
    val velocityY: Float
)

@Composable
fun PongScreen(navController: NavController) {
    var gameState by remember { mutableStateOf("menu") }
    var difficulty by remember { mutableStateOf("easy") }
    var playerScore by remember { mutableStateOf(0) }
    var computerScore by remember { mutableStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }
    var winner by remember { mutableStateOf("") }
    
    // Efectos de sonido
    val context = androidx.compose.ui.platform.LocalContext.current
    val soundGenerator = remember { SoundGenerator(context) }
    
    // Dimensiones del juego
    val screenWidth = 400f
    val screenHeight = 600f
    val paddleWidth = 80f
    val paddleHeight = 15f
    val ballRadius = 8f
    
    // Estado del juego
    var playerPaddle by remember { mutableStateOf(Paddle(screenWidth / 2f - paddleWidth / 2f, screenHeight - 50f, paddleWidth, paddleHeight)) }
    var computerPaddle by remember { mutableStateOf(Paddle(screenWidth / 2f - paddleWidth / 2f, 50f, paddleWidth, paddleHeight)) }
    var ball by remember { mutableStateOf(Ball(screenWidth / 2f, screenHeight / 2f, ballRadius, 5f, 5f)) }
    
    // Configuración de dificultad
    val computerSpeed = when (difficulty) {
        "easy" -> 2f
        "medium" -> 3f
        "hard" -> 4f
        else -> 2f
    }
    
    fun resetGame() {
        playerPaddle = Paddle(screenWidth / 2f - paddleWidth / 2f, screenHeight - 50f, paddleWidth, paddleHeight)
        computerPaddle = Paddle(screenWidth / 2f - paddleWidth / 2f, 50f, paddleWidth, paddleHeight)
        ball = Ball(screenWidth / 2f, screenHeight / 2f, ballRadius, 5f, 5f)
        gameOver = false
        winner = ""
    }
    
    fun updateGame() {
        if (gameOver) return
        
        // Mover la pelota
        ball = ball.copy(
            x = ball.x + ball.velocityX,
            y = ball.y + ball.velocityY
        )
        
        // Mover la paleta de la computadora
        val computerTargetX = ball.x - paddleWidth / 2f
        val currentComputerX = computerPaddle.x
        val computerMoveSpeed = computerSpeed
        
        val newComputerX = when {
            currentComputerX < computerTargetX -> min(currentComputerX + computerMoveSpeed, computerTargetX)
            currentComputerX > computerTargetX -> max(currentComputerX - computerMoveSpeed, computerTargetX)
            else -> currentComputerX
        }
        
        computerPaddle = computerPaddle.copy(x = newComputerX.coerceIn(0f, screenWidth - paddleWidth))
        
        // Colisión con paredes laterales
        if (ball.x <= ballRadius || ball.x >= screenWidth - ballRadius) {
            ball = ball.copy(velocityX = -ball.velocityX)
        }
        
        // Colisión con paletas
        val playerPaddleRect = playerPaddle
        val computerPaddleRect = computerPaddle
        
        // Colisión con paleta del jugador
        if (ball.y + ballRadius >= playerPaddleRect.y && 
            ball.y - ballRadius <= playerPaddleRect.y + playerPaddleRect.height &&
            ball.x >= playerPaddleRect.x && 
            ball.x <= playerPaddleRect.x + playerPaddleRect.width) {
            
            ball = ball.copy(velocityY = -abs(ball.velocityY))
            
            // Ajustar velocidad basado en dónde golpea la paleta
            val hitPosition = (ball.x - playerPaddleRect.x) / playerPaddleRect.width
            val angle = (hitPosition - 0.5f) * 0.5f // -0.25 a 0.25 radianes
            ball = ball.copy(
                velocityX = ball.velocityX + angle * 2f,
                velocityY = -abs(ball.velocityY)
            )
            
            // Efecto de sonido de rebote
            soundGenerator.playBounce()
        }
        
        // Colisión con paleta de la computadora
        if (ball.y - ballRadius <= computerPaddleRect.y + computerPaddleRect.height && 
            ball.y + ballRadius >= computerPaddleRect.y &&
            ball.x >= computerPaddleRect.x && 
            ball.x <= computerPaddleRect.x + computerPaddleRect.width) {
            
            ball = ball.copy(velocityY = abs(ball.velocityY))
            
            // Ajustar velocidad basado en dónde golpea la paleta
            val hitPosition = (ball.x - computerPaddleRect.x) / computerPaddleRect.width
            val angle = (hitPosition - 0.5f) * 0.5f
            ball = ball.copy(
                velocityX = ball.velocityX + angle * 2f,
                velocityY = abs(ball.velocityY)
            )
            
            // Efecto de sonido de rebote
            soundGenerator.playBounce()
        }
        
        // Puntuación - verificar si la pelota sale por arriba o abajo
        if (ball.y - ballRadius <= 0) {
            // La pelota sale por arriba - punto para el jugador
            playerScore++
            soundGenerator.playScore()
            if (playerScore >= 11) {
                gameOver = true
                winner = "¡Jugador Gana!"
                soundGenerator.playSuccess()
            } else {
                // Reiniciar la pelota en el centro con dirección hacia abajo
                ball = Ball(screenWidth / 2f, screenHeight / 2f, ballRadius, 5f, 5f)
            }
        } else if (ball.y + ballRadius >= screenHeight) {
            // La pelota sale por abajo - punto para la computadora
            computerScore++
            soundGenerator.playScore()
            if (computerScore >= 11) {
                gameOver = true
                winner = "¡Computadora Gana!"
                soundGenerator.playSuccess()
            } else {
                // Reiniciar la pelota en el centro con dirección hacia arriba
                ball = Ball(screenWidth / 2f, screenHeight / 2f, ballRadius, 5f, -5f)
            }
        }
        
        // Mantener la pelota dentro de los límites horizontales
        ball = ball.copy(
            x = ball.x.coerceIn(ballRadius, screenWidth - ballRadius)
        )
    }
    
    // Game loop
    LaunchedEffect(gameState) {
        if (gameState == "playing") {
            while (gameState == "playing") {
                updateGame()
                delay(16) // ~60 FPS
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF16213e),
                        Color(0xFF0f3460)
                    )
                )
            )
    ) {
        when (gameState) {
            "menu" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🏓 Pong",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = "Selecciona la dificultad:",
                        fontSize = 18.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                soundGenerator.playClick()
                                difficulty = "easy"
                                gameState = "playing"
                                resetGame()
                            },
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🟢 Fácil",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6C63FF)
                            )
                            Text(
                                text = "Computadora lenta",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                soundGenerator.playClick()
                                difficulty = "medium"
                                gameState = "playing"
                                resetGame()
                            },
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🟡 Medio",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6C63FF)
                            )
                            Text(
                                text = "Computadora media",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                soundGenerator.playClick()
                                difficulty = "hard"
                                gameState = "playing"
                                resetGame()
                            },
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🔴 Difícil",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6C63FF)
                            )
                            Text(
                                text = "Computadora rápida",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = { 
                            soundGenerator.playClick()
                            navController.navigateUp() 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.9f))
                    ) {
                        Text(
                            text = "Volver",
                            color = Color(0xFF6C63FF),
                            fontSize = 16.sp
                        )
                    }
                }
            }
            
            "playing" -> {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header con puntuación
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                        ) {
                            Text(
                                text = "Computadora: $computerScore",
                                modifier = Modifier.padding(8.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6C63FF)
                            )
                        }
                        
                        Button(
                            onClick = { 
                                soundGenerator.playClick()
                                gameState = "menu"
                                playerScore = 0
                                computerScore = 0
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.9f))
                        ) {
                            Text(
                                text = "🔄 Reiniciar",
                                color = Color(0xFF6C63FF),
                                fontSize = 14.sp
                            )
                        }
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                        ) {
                            Text(
                                text = "Jugador: $playerScore",
                                modifier = Modifier.padding(8.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6C63FF)
                            )
                        }
                    }
                    
                    // Área de juego
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Campo de juego
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectDragGestures { change, _ ->
                                        val newX = change.position.x - paddleWidth / 2f
                                        playerPaddle = playerPaddle.copy(
                                            x = newX.coerceIn(0f, screenWidth - paddleWidth)
                                        )
                                    }
                                }
                        ) {
                            // Línea central
                            drawLine(
                                color = Color.White.copy(alpha = 0.3f),
                                start = Offset(size.width / 2f, 0f),
                                end = Offset(size.width / 2f, size.height),
                                strokeWidth = 2f
                            )
                            
                            // Puntos centrales
                            for (i in 0..10) {
                                val y = (size.height / 10f) * i
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.5f),
                                    radius = 4f,
                                    center = Offset(size.width / 2f, y)
                                )
                            }
                            
                            // Paleta del jugador
                            drawRect(
                                color = Color(0xFF00ff88),
                                topLeft = Offset(
                                    playerPaddle.x * (size.width / screenWidth),
                                    playerPaddle.y * (size.height / screenHeight)
                                ),
                                size = androidx.compose.ui.geometry.Size(
                                    playerPaddle.width * (size.width / screenWidth),
                                    playerPaddle.height * (size.height / screenHeight)
                                )
                            )
                            
                            // Paleta de la computadora
                            drawRect(
                                color = Color(0xFFff4444),
                                topLeft = Offset(
                                    computerPaddle.x * (size.width / screenWidth),
                                    computerPaddle.y * (size.height / screenHeight)
                                ),
                                size = androidx.compose.ui.geometry.Size(
                                    computerPaddle.width * (size.width / screenWidth),
                                    computerPaddle.height * (size.height / screenHeight)
                                )
                            )
                            
                            // Pelota
                            drawCircle(
                                color = Color(0xFFffff00),
                                radius = ball.radius * (size.width / screenWidth),
                                center = Offset(
                                    ball.x * (size.width / screenWidth),
                                    ball.y * (size.height / screenHeight)
                                )
                            )
                        }
                    }
                }
                
                // Diálogo de fin de juego
                if (gameOver) {
                    AlertDialog(
                        onDismissRequest = { },
                        title = {
                            Text(
                                text = winner,
                                color = if (winner.contains("Jugador")) Color.Green else Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            Text(
                                text = "Puntuación final: Jugador $playerScore - Computadora $computerScore",
                                color = Color.Gray
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    soundGenerator.playClick()
                                    gameState = "menu"
                                    playerScore = 0
                                    computerScore = 0
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                            ) {
                                Text("Nuevo Juego", color = Color.White)
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = {
                                    soundGenerator.playClick()
                                    gameState = "menu"
                                    playerScore = 0
                                    computerScore = 0
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                            ) {
                                Text("Menú", color = Color.White)
                            }
                        }
                    )
                }
            }
        }
    }
} 