package com.example.buzondesugerenciascojema.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.buzondesugerenciascojema.model.TriviaQuestionProvider
import com.example.buzondesugerenciascojema.viewmodels.TriviaViewModel
import com.example.buzondesugerenciascojema.viewmodels.TriviaGameState
import kotlinx.coroutines.delay
import com.example.buzondesugerenciascojema.util.SoundGenerator
import com.example.buzondesugerenciascojema.util.MusicPlayer

@Composable
fun TriviaScreen(
    navController: NavController,
    userGrade: Int,
    triviaViewModel: TriviaViewModel = viewModel()
) {
    val gameState by triviaViewModel.gameState.collectAsState()
    var showStartAnim by remember { mutableStateOf(true) }
    
    // Efectos de sonido y música
    val context = androidx.compose.ui.platform.LocalContext.current
    val soundGenerator = remember { SoundGenerator(context) }
    val musicPlayer = remember { MusicPlayer(context) }
    
    // Iniciar música del juego
    LaunchedEffect(Unit) {
        musicPlayer.playGameMusic("trivia")
    }
    
    // Detener música cuando se sale
    DisposableEffect(Unit) {
        onDispose {
            musicPlayer.stopMusic()
        }
    }

    LaunchedEffect(Unit) {
        showStartAnim = true
        delay(800)
        showStartAnim = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF6C63FF)),
        contentAlignment = Alignment.Center
    ) {
        when (gameState) {
            is TriviaGameState.NotStarted -> {
                AnimatedVisibility(
                    visible = showStartAnim,
                    enter = fadeIn(animationSpec = tween(800)),
                    exit = fadeOut(animationSpec = tween(800))
                ) {
                    Text(
                        text = "¡Bienvenido a la Trivia!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    )
                }
                AnimatedVisibility(
                    visible = !showStartAnim,
                    enter = fadeIn(animationSpec = tween(800)),
                    exit = fadeOut(animationSpec = tween(800))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Responde preguntas de todas las asignaturas de tu grado a contrarreloj.\n¡Suma puntos por cada acierto y compite en el ranking!",
                            color = Color.White,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Medium
                        )
                        Button(
                            onClick = {
                                soundGenerator.playClick()
                                val questions = TriviaQuestionProvider.getQuestionsForGrade(userGrade)
                                triviaViewModel.startGame(questions)
                            },
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.padding(top = 24.dp)
                        ) {
                            Text("Comenzar", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            is TriviaGameState.Playing -> {
                val state = gameState as TriviaGameState.Playing
                TriviaQuestionView(
                    question = state.question,
                    questionNumber = state.questionNumber,
                    totalQuestions = state.totalQuestions,
                    score = state.score,
                    timeLeft = state.timeLeft,
                    onAnswer = { triviaViewModel.answerQuestion(it) }
                )
            }
            is TriviaGameState.Finished -> {
                val state = gameState as TriviaGameState.Finished
                TriviaResultView(
                    score = state.score,
                    maxScore = state.maxScore,
                    onRestart = { triviaViewModel.resetGame() },
                    onBack = { navController.popBackStack() },
                    soundGenerator = soundGenerator
                )
            }
            else -> {}
        }
    }
}

@Composable
fun TriviaQuestionView(
    question: com.example.buzondesugerenciascojema.model.TriviaQuestion,
    questionNumber: Int,
    totalQuestions: Int,
    score: Int,
    timeLeft: Int,
    onAnswer: (Int) -> Unit
) {
    var selected by remember { mutableStateOf(-1) }
    var showFeedback by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    
    // Efectos de sonido
    val context = androidx.compose.ui.platform.LocalContext.current
    val soundGenerator = remember { SoundGenerator(context) }

    LaunchedEffect(selected) {
        if (selected != -1) {
            showFeedback = true
            isCorrect = selected == question.correctAnswer
            
            // Efectos de sonido según la respuesta
            if (isCorrect) {
                soundGenerator.playCorrectAnswer()
            } else {
                soundGenerator.playWrongAnswer()
            }
            
            delay(700)
            showFeedback = false
            onAnswer(selected)
            selected = -1
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Pregunta $questionNumber de $totalQuestions", color = Color(0xFF6C63FF), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(question.question, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))
            question.options.forEachIndexed { idx, opt ->
                Button(
                    onClick = { if (selected == -1) selected = idx },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            showFeedback && idx == question.correctAnswer -> Color(0xFF4CAF50)
                            showFeedback && idx == selected && idx != question.correctAnswer -> Color(0xFFD32F2F)
                            else -> Color(0xFFE3F2FD)
                        }
                    ),
                    enabled = selected == -1
                ) {
                    Text(opt, color = Color.Black, fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = timeLeft / 10f,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF6C63FF)
            )
            Text("Tiempo restante: $timeLeft s", color = Color.Gray, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Puntaje: $score", color = Color(0xFF6C63FF), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
fun TriviaResultView(
    score: Int,
    maxScore: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit,
    soundGenerator: SoundGenerator
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .background(Color.White, shape = RoundedCornerShape(24.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("¡Juego terminado!", color = Color(0xFF6C63FF), fontWeight = FontWeight.Bold, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Puntaje final: $score de $maxScore", color = Color.Black, fontSize = 22.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { 
            soundGenerator.playClick()
            onRestart() 
        }, shape = RoundedCornerShape(20.dp)) {
            Text("Volver a jugar", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = { 
            soundGenerator.playClick()
            onBack() 
        }, shape = RoundedCornerShape(20.dp)) {
            Text("Volver al menú", fontSize = 18.sp)
        }
    }
} 