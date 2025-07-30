package com.example.buzondesugerenciascojema.ui.screens

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buzondesugerenciascojema.util.SoundGenerator
import com.example.buzondesugerenciascojema.viewmodels.DinoGameViewModel
import kotlinx.coroutines.launch
import com.example.buzondesugerenciascojema.data.RankingService
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.DrawScope

@Composable
fun DinoGameScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: DinoGameViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val soundGenerator = remember { SoundGenerator(context) }
    val rankingService = remember { RankingService() }
    var showRanking by remember { mutableStateOf(false) }
    var topScores by remember { mutableStateOf(listOf<Pair<String, Int>>()) }

    // Parámetros de física
    val groundY = 550f // Más abajo en la pantalla
    val dinoX = 100f
    val dinoWidth = 48f
    val dinoHeight = 48f
    val cactusWidth = 24f
    val cactusHeight = 48f

    // Iniciar el juego al entrar
    LaunchedEffect(Unit) {
        viewModel.startGame()
    }

    // Efecto de game over: reproducir sonido y guardar score
    LaunchedEffect(state.isGameOver) {
        if (state.isGameOver) {
            soundGenerator.playError() // Sonido de choque
            // Guardar score en ranking
            scope.launch {
                val nombreUsuario = "Jugador Dino"
                val emailUsuario = "dino@ejemplo.com"
                rankingService.guardarPuntuacion("DinoRunner", state.score, nombreUsuario, emailUsuario)
                val top = rankingService.obtenerTopRanking("DinoRunner", 5)
                topScores = top.map { (it.nombreUsuario ?: "Anon" ) to it.puntuacion }
                showRanking = true
            }
        }
    }

    // Sonido de puntaje (cada 100 puntos)
    LaunchedEffect(state.score) {
        if (state.score > 0 && state.score % 100 == 0) {
            soundGenerator.playScore()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFECECEC))
            .pointerInput(Unit) {
                detectTapGestures {
                    if (!state.isJumping && !state.isGameOver) {
                        viewModel.jump()
                        soundGenerator.playBeep(1200f, 120) // Sonido de salto
                    } else if (state.isGameOver) {
                        viewModel.restart()
                        showRanking = false
                    }
                }
            }
    ) {
        // Suelo y juego
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Suelo
            drawRect(
                color = Color(0xFF888888),
                topLeft = Offset(0f, groundY + dinoHeight),
                size = androidx.compose.ui.geometry.Size(size.width, 20f)
            )
            // Dinosaurio pixel-art
            drawDinoGoogle(
                x = dinoX,
                y = groundY - state.dinoY - dinoHeight,
                pixel = 6f
            )
            // Obstáculos cactus
            state.obstacles.forEach { obsX ->
                drawCactusGoogle(
                    x = obsX,
                    y = groundY - cactusHeight,
                    pixel = 6f
                )
            }
        }
        // Score
        Text(
            text = "Puntaje: ${state.score}",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Black,
            modifier = Modifier.padding(16.dp)
        )
        // Game Over y Ranking
        if (state.isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xAA000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("¡Game Over!", color = Color.White, style = MaterialTheme.typography.headlineLarge)
                    Text("Puntaje: ${state.score}", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        viewModel.restart()
                        showRanking = false
                    }) {
                        Text("Reintentar")
                    }
                }
            }
        }
        // Ranking Dialog
        if (showRanking) {
            AlertDialog(
                onDismissRequest = { showRanking = false },
                title = { Text("Ranking Dino Runner") },
                text = {
                    Column {
                        topScores.forEachIndexed { idx, (user, score) ->
                            Text("${idx + 1}. $user: $score")
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showRanking = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }
    }
}

// Dibuja el dinosaurio estilo Google Chrome en pixel-art
fun DrawScope.drawDinoGoogle(x: Float, y: Float, pixel: Float) {
    val gray = Color(0xFF444444)
    val white = Color.White
    // Cada drawRect es un "pixel" del dino
    // Lista de píxeles: dx, dy, ancho, alto
    val pixels = listOf(
        listOf(0, 4, 4, 2),
        listOf(1, 2, 3, 2),
        listOf(2, 0, 2, 2),
        listOf(0, 6, 1, 2),
        listOf(3, 6, 1, 2),
        listOf(-1, 5, 1, 1),
        listOf(-2, 4, 1, 1),
        listOf(4, 0, 2, 2),
        listOf(6, 1, 1, 1),
        listOf(6, 2, 1, 1),
        listOf(5, 2, 1, 1),
        listOf(3, 4, 1, 1),
        listOf(5, 1, 1, 1) // Ojo
    )
    pixels.forEachIndexed { i, p ->
        val (dx, dy, w, h) = p
        if (i == pixels.lastIndex) {
            // Ojo
            drawRect(white, topLeft = Offset(x + dx * pixel, y + dy * pixel), size = androidx.compose.ui.geometry.Size(w * pixel, h * pixel))
        } else {
            drawRect(gray, topLeft = Offset(x + dx * pixel, y + dy * pixel), size = androidx.compose.ui.geometry.Size(w * pixel, h * pixel))
        }
    }
}

// Dibuja un cactus estilo Google Chrome en pixel-art
fun DrawScope.drawCactusGoogle(x: Float, y: Float, pixel: Float) {
    val green = Color(0xFF888888)
    // Tronco principal
    drawRect(green, topLeft = Offset(x + 2 * pixel, y), size = androidx.compose.ui.geometry.Size(pixel * 4, pixel * 8))
    // Brazo izquierdo
    drawRect(green, topLeft = Offset(x, y + pixel * 3), size = androidx.compose.ui.geometry.Size(pixel * 2, pixel * 2))
    drawRect(green, topLeft = Offset(x, y + pixel * 5), size = androidx.compose.ui.geometry.Size(pixel, pixel * 2))
    // Brazo derecho
    drawRect(green, topLeft = Offset(x + pixel * 6, y + pixel * 4), size = androidx.compose.ui.geometry.Size(pixel * 2, pixel * 2))
    drawRect(green, topLeft = Offset(x + pixel * 7, y + pixel * 6), size = androidx.compose.ui.geometry.Size(pixel, pixel * 2))
} 