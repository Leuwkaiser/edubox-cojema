package com.example.buzondesugerenciascojema.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.buzondesugerenciascojema.util.SoundGenerator
import com.example.buzondesugerenciascojema.util.MusicPlayer
import kotlin.random.Random

data class LineaGanadora(val start: Pair<Int, Int>, val end: Pair<Int, Int>)

fun obtenerLineaGanadora(board: List<List<String>>): LineaGanadora? {
    val lines = listOf(
        // Filas
        Triple(Pair(0, 0), Pair(0, 2), listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2))),
        Triple(Pair(1, 0), Pair(1, 2), listOf(Pair(1, 0), Pair(1, 1), Pair(1, 2))),
        Triple(Pair(2, 0), Pair(2, 2), listOf(Pair(2, 0), Pair(2, 1), Pair(2, 2))),
        // Columnas
        Triple(Pair(0, 0), Pair(2, 0), listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0))),
        Triple(Pair(0, 1), Pair(2, 1), listOf(Pair(0, 1), Pair(1, 1), Pair(2, 1))),
        Triple(Pair(0, 2), Pair(2, 2), listOf(Pair(0, 2), Pair(1, 2), Pair(2, 2))),
        // Diagonales
        Triple(Pair(0, 0), Pair(2, 2), listOf(Pair(0, 0), Pair(1, 1), Pair(2, 2))),
        Triple(Pair(0, 2), Pair(2, 0), listOf(Pair(0, 2), Pair(1, 1), Pair(2, 0)))
    )
    for ((start, end, cells) in lines) {
        val values = cells.map { (i, j) -> board[i][j] }
        if (values[0].isNotEmpty() && values.all { it == values[0] }) {
            return LineaGanadora(start, end)
        }
    }
    return null
}

@Composable
fun TresEnRayaScreen(navController: NavController) {
    var menuVisible by remember { mutableStateOf(true) }
    var modoBot by remember { mutableStateOf(true) }
    var nombreJugador1 by remember { mutableStateOf("") }
    var nombreJugador2 by remember { mutableStateOf("") }
    var pedirNombres by remember { mutableStateOf(false) }
    var victoriasJugador by remember { mutableStateOf(0) }
    var victoriasBot by remember { mutableStateOf(0) }
    var victoriasJ1 by remember { mutableStateOf(0) }
    var victoriasJ2 by remember { mutableStateOf(0) }
    var dificultadBot by remember { mutableStateOf("Difícil") }
    var lastGameWinner by remember { mutableStateOf<String?>(null) }
    var lastGameStarter by remember { mutableStateOf<String?>(null) }
    
    // Efectos de sonido y música
    val context = androidx.compose.ui.platform.LocalContext.current
    val soundGenerator = remember { SoundGenerator(context) }
    val musicPlayer = remember { MusicPlayer(context) }
    
    // Iniciar música del juego
    LaunchedEffect(Unit) {
        musicPlayer.playGameMusic("tres_en_raya")
    }
    
    // Detener música cuando se sale
    DisposableEffect(Unit) {
        onDispose {
            musicPlayer.stopMusic()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )
            )
    ) {
        // Efecto de partículas de fondo
        ParticleEffect()
        
        if (menuVisible) {
            TresEnRayaMenu(
                onJugarBot = {
                    modoBot = true
                    menuVisible = false
                },
                onJugarJugador = {
                    pedirNombres = true
                },
                navController = navController,
                dificultadBot = dificultadBot,
                onDificultadChange = { dificultadBot = it }
            )
        } else if (modoBot) {
            TresEnRayaTableroBot(
                navController = navController,
                victoriasJugador = victoriasJugador,
                victoriasBot = victoriasBot,
                onVictoriaJugador = { victoriasJugador++ },
                onVictoriaBot = { victoriasBot++ },
                dificultadBot = dificultadBot,
                lastGameWinner = lastGameWinner,
                lastGameStarter = lastGameStarter,
                onUpdateGameHistory = { winner, starter ->
                    lastGameWinner = winner
                    lastGameStarter = starter
                },
                onNewGame = {
                    // Limpiar historial cuando se inicia un nuevo juego
                    lastGameWinner = null
                    lastGameStarter = null
                }
            )
        } else {
            TresEnRayaTableroJugador(
                navController = navController,
                nombreJugador1 = nombreJugador1,
                nombreJugador2 = nombreJugador2,
                victoriasJ1 = victoriasJ1,
                victoriasJ2 = victoriasJ2,
                onVictoriaJ1 = { victoriasJ1++ },
                onVictoriaJ2 = { victoriasJ2++ }
            )
        }
        
        if (pedirNombres) {
            NombreJugadoresDialog(
                onAceptar = { n1, n2 ->
                    nombreJugador1 = n1.ifBlank { "Jugador X" }
                    nombreJugador2 = n2.ifBlank { "Jugador O" }
                    modoBot = false
                    menuVisible = false
                    pedirNombres = false
                },
                onCancelar = {
                    pedirNombres = false
                }
            )
        }
    }
}

@Composable
fun ParticleEffect() {
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween<Float>(3000),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        repeat(20) { index ->
            val x = size.width * Random.nextFloat()
            val y = size.height * animatedProgress
            
            drawCircle(
                color = Color(0xFF6C63FF).copy(alpha = 0.3f),
                radius = 2f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun TresEnRayaMenu(
    onJugarBot: () -> Unit,
    onJugarJugador: () -> Unit,
    navController: NavController,
    dificultadBot: String,
    onDificultadChange: (String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween<Float>(2000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Título animado
        Text(
            text = "TRES EN RAYA",
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.scale(scale)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "¡El clásico juego de estrategia!",
            fontSize = 16.sp,
            color = Color(0xFFB8B8B8),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(60.dp))
        
        // Selector de dificultad
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2A2A3E).copy(alpha = 0.8f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Dificultad de Botso",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DificultadButton(
                        text = "Fácil",
                        isSelected = dificultadBot == "Fácil",
                        onClick = { onDificultadChange("Fácil") },
                        color = Color(0xFF10B981)
                    )
                    DificultadButton(
                        text = "Medio",
                        isSelected = dificultadBot == "Medio",
                        onClick = { onDificultadChange("Medio") },
                        color = Color(0xFFF59E0B)
                    )
                    DificultadButton(
                        text = "Difícil",
                        isSelected = dificultadBot == "Difícil",
                        onClick = { onDificultadChange("Difícil") },
                        color = Color(0xFFEF4444)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Botón Jugar vs Bot
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(800, delayMillis = 200)
            ) + fadeIn(animationSpec = tween(800, delayMillis = 200))
        ) {
            GameModeButton(
                text = "VS BOTSO",
                icon = "🤖",
                onClick = onJugarBot,
                gradient = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF6C63FF), Color(0xFF8B5CF6))
                )
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Botón Jugar vs Jugador
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(800, delayMillis = 400)
            ) + fadeIn(animationSpec = tween(800, delayMillis = 400))
        ) {
            GameModeButton(
                text = "VS JUGADOR",
                icon = "👥",
                onClick = onJugarJugador,
                gradient = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF10B981), Color(0xFF059669))
                )
            )
        }
        
        Spacer(modifier = Modifier.height(60.dp))
        
        // Botón Volver
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(800, delayMillis = 600)
            ) + fadeIn(animationSpec = tween(800, delayMillis = 600))
        ) {
            Button(
                onClick = { navController.navigateUp() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                border = BorderStroke(2.dp, Color(0xFF6C63FF))
            ) {
                Text(
                    text = "VOLVER",
                    color = Color(0xFF6C63FF),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GameModeButton(
    text: String,
    icon: String,
    onClick: () -> Unit,
    gradient: Brush
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100)
    )
    
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .scale(scale),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient, RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = icon,
                    fontSize = 32.sp
                )
                
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun NombreJugadoresDialog(
    onAceptar: (String, String) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre1 by remember { mutableStateOf("") }
    var nombre2 by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onCancelar) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2A2A3E)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Nombres de los Jugadores",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = nombre1,
                    onValueChange = { nombre1 = it },
                    label = { Text("Jugador 1 (X)", color = Color(0xFF9A8C98)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = nombre2,
                    onValueChange = { nombre2 = it },
                    label = { Text("Jugador 2 (O)", color = Color(0xFFF2E9E4)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onCancelar,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A4E69)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar", color = Color.White)
                    }
                    
                    Button(
                        onClick = { onAceptar(nombre1, nombre2) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C63FF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Jugar", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun DificultadButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(80.dp)
            .height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) color else Color(0xFF4A4E69)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TresEnRayaTableroBot(
    navController: NavController,
    victoriasJugador: Int,
    victoriasBot: Int,
    onVictoriaJugador: () -> Unit,
    onVictoriaBot: () -> Unit,
    dificultadBot: String,
    lastGameWinner: String?,
    lastGameStarter: String?,
    onUpdateGameHistory: (String?, String?) -> Unit,
    onNewGame: () -> Unit
) {
    // Efectos de sonido
    val context = androidx.compose.ui.platform.LocalContext.current
    val soundGenerator = remember { SoundGenerator(context) }
    var board by remember { mutableStateOf(List(3) { MutableList(3) { "" } }) }
    var isPlayerTurn by remember { mutableStateOf(true) }
    var winner by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var animatingBotMove by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val scope = rememberCoroutineScope()
    val lineaGanadora = obtenerLineaGanadora(board)
    
    // Limpiar historial cuando se inicia un nuevo juego
    LaunchedEffect(Unit) {
        onNewGame()
    }

    fun checkWinner(board: List<List<String>>): String? {
        val lines = listOf(
            // Filas
            listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2)),
            listOf(Pair(1, 0), Pair(1, 1), Pair(1, 2)),
            listOf(Pair(2, 0), Pair(2, 1), Pair(2, 2)),
            // Columnas
            listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0)),
            listOf(Pair(0, 1), Pair(1, 1), Pair(2, 1)),
            listOf(Pair(0, 2), Pair(1, 2), Pair(2, 2)),
            // Diagonales
            listOf(Pair(0, 0), Pair(1, 1), Pair(2, 2)),
            listOf(Pair(0, 2), Pair(1, 1), Pair(2, 0))
        )
        
        for (line in lines) {
            val values = line.map { (i, j) -> board[i][j] }
            if (values[0].isNotEmpty() && values.all { it == values[0] }) {
                return if (values[0] == "X") "Tú" else "Botso"
            }
        }
        
        if (board.flatten().all { it.isNotEmpty() }) return "Empate"
        return null
    }

    fun minimax(board: List<List<String>>, depth: Int, isMaximizing: Boolean): Int {
        val winner = checkWinner(board)
        if (winner == "Botso") return 10 - depth
        if (winner == "Tú") return depth - 10
        if (winner == "Empate") return 0
        
        if (isMaximizing) {
            var bestScore = Int.MIN_VALUE
            for (i in 0..2) {
                for (j in 0..2) {
                    if (board[i][j].isEmpty()) {
                        val newBoard = board.map { it.toMutableList() }.toMutableList()
                        newBoard[i][j] = "O"
                        val score = minimax(newBoard, depth + 1, false)
                        bestScore = maxOf(bestScore, score)
                    }
                }
            }
            return bestScore
        } else {
            var bestScore = Int.MAX_VALUE
            for (i in 0..2) {
                for (j in 0..2) {
                    if (board[i][j].isEmpty()) {
                        val newBoard = board.map { it.toMutableList() }.toMutableList()
                        newBoard[i][j] = "X"
                        val score = minimax(newBoard, depth + 1, true)
                        bestScore = minOf(bestScore, score)
                    }
                }
            }
            return bestScore
        }
    }

    fun findBestMove(board: List<List<String>>): Pair<Int, Int> {
        var bestScore = Int.MIN_VALUE
        var bestMove = Pair(0, 0)
        
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j].isEmpty()) {
                    val newBoard = board.map { it.toMutableList() }.toMutableList()
                    newBoard[i][j] = "O"
                    val score = minimax(newBoard, 0, false)
                    if (score > bestScore) {
                        bestScore = score
                        bestMove = Pair(i, j)
                    }
                }
            }
        }
        return bestMove
    }

    fun findRandomMove(board: List<List<String>>): Pair<Int, Int> {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j].isEmpty()) {
                    emptyCells.add(Pair(i, j))
                }
            }
        }
        return emptyCells.random()
    }

    fun findMediumMove(board: List<List<String>>): Pair<Int, Int> {
        // 70% probabilidad de jugar perfecto, 30% de jugar aleatorio
        return if (Random.nextFloat() < 0.7f) {
            findBestMove(board)
        } else {
            findRandomMove(board)
        }
    }

    fun botMove(board: List<List<String>>): Pair<Int, Int> {
        return when (dificultadBot) {
            "Fácil" -> findRandomMove(board)
            "Medio" -> findMediumMove(board)
            "Difícil" -> findBestMove(board)
            else -> findBestMove(board)
        }
    }

    fun playerMove(i: Int, j: Int) {
        if (board[i][j].isEmpty() && isPlayerTurn && winner == null) {
            soundGenerator.playPlaceX()
            
            val newBoard = board.map { it.toMutableList() }.toMutableList()
            newBoard[i][j] = "X"
            board = newBoard
            
            val currentWinner = checkWinner(board)
            if (currentWinner != null) {
                winner = currentWinner
                if (currentWinner == "Tú") {
                    soundGenerator.playWin()
                }
                showDialog = true
                return
            }
            
            isPlayerTurn = false
            
            scope.launch {
                delay(500)
                val botMove = botMove(board)
                animatingBotMove = botMove
                delay(1000)
                
                soundGenerator.playPlaceO()
                val newBoardAfterBot = board.map { it.toMutableList() }.toMutableList()
                newBoardAfterBot[botMove.first][botMove.second] = "O"
                board = newBoardAfterBot
                animatingBotMove = null
                
                val botsoWinner = checkWinner(board)
                if (botsoWinner != null) {
                    winner = botsoWinner
                    if (botsoWinner == "Botso") {
                        soundGenerator.playWin()
                    }
                    showDialog = true
                } else {
                    isPlayerTurn = true
                }
            }
        }
    }

    fun resetGame() {
        board = List(3) { MutableList(3) { "" } }
        winner = null
        showDialog = false
        animatingBotMove = null
        
        // Lógica para determinar quién inicia la siguiente partida
        if (lastGameWinner == null) {
            // Primera partida: siempre inicia el jugador
            isPlayerTurn = true
            onUpdateGameHistory(null, "Tú")
        } else if (lastGameWinner == "Empate") {
            // Si hubo empate, inicia quien no inició en la ronda anterior
            isPlayerTurn = lastGameStarter != "Tú"
            val newStarter = if (isPlayerTurn) "Tú" else "Botso"
            onUpdateGameHistory(null, newStarter)
        } else {
            // El ganador inicia la siguiente partida
            isPlayerTurn = lastGameWinner == "Tú"
            onUpdateGameHistory(null, lastGameWinner)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header con marcador
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A3E).copy(alpha = 0.8f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tres en Raya vs Botso",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Dificultad: $dificultadBot",
                        color = when (dificultadBot) {
                            "Fácil" -> Color(0xFF10B981)
                            "Medio" -> Color(0xFFF59E0B)
                            "Difícil" -> Color(0xFFEF4444)
                            else -> Color(0xFF6C63FF)
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ScoreCard(
                            label = "Tú",
                            score = victoriasJugador,
                            color = Color(0xFF9A8C98)
                        )
                        ScoreCard(
                            label = "Botso",
                            score = victoriasBot,
                            color = Color(0xFFF2E9E4)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Tablero
            Card(
                modifier = Modifier.size(320.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A3E).copy(alpha = 0.8f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Column {
                        for (i in 0..2) {
                            Row {
                                for (j in 0..2) {
                                    GameCell(
                                        value = board[i][j],
                                        isAnimating = animatingBotMove == (i to j),
                                        onClick = { playerMove(i, j) },
                                        enabled = board[i][j].isEmpty() && isPlayerTurn && winner == null && animatingBotMove == null
                                    )
                                    if (j < 2) Spacer(modifier = Modifier.width(8.dp))
                                }
                            }
                            if (i < 2) Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    
                    // Línea ganadora
                    if (lineaGanadora != null && winner != null) {
                        WinningLine(lineaGanadora)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { 
                        soundGenerator.playClick()
                        resetGame() 
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A4E69)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reiniciar", color = Color.White)
                }
                
                Button(
                    onClick = { 
                        soundGenerator.playClick()
                        navController.navigateUp() 
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6C63FF)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Volver", color = Color.White)
                }
            }
        }
        
        // Diálogo de resultado
        if (showDialog && winner != null) {
            GameResultDialog(
                winner = winner!!,
                onPlayAgain = {
                    // Guardar el ganador de la partida actual antes de resetear
                    onUpdateGameHistory(winner, lastGameStarter)
                    if (winner == "Tú") onVictoriaJugador()
                    if (winner == "Botso") onVictoriaBot()
                    resetGame()
                },
                onBack = {
                    // Guardar el ganador de la partida actual antes de salir
                    onUpdateGameHistory(winner, lastGameStarter)
                    if (winner == "Tú") onVictoriaJugador()
                    if (winner == "Botso") onVictoriaBot()
                    navController.navigateUp()
                }
            )
        }
    }
}

@Composable
fun ScoreCard(label: String, score: Int, color: Color) {
    Card(
        modifier = Modifier.padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = score.toString(),
                color = color,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GameCell(
    value: String,
    isAnimating: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.1f else 1f,
        animationSpec = tween(300)
    )
    
    Box(
        modifier = Modifier
            .size(90.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF4A4E69),
                        Color(0xFF3A3E59)
                    )
                )
            )
            .clickable(enabled = enabled, onClick = onClick)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF6C63FF).copy(alpha = if (enabled) 0.8f else 0.3f),
                        Color(0xFF8B5CF6).copy(alpha = if (enabled) 0.8f else 0.3f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            value == "X" -> {
                Text(
                    text = "X",
                    fontSize = 48.sp,
                    color = Color(0xFF9A8C98),
                    fontWeight = FontWeight.Bold
                )
            }
            value == "O" -> {
                AnimatedO(animating = isAnimating)
            }
            isAnimating -> {
                AnimatedO(animating = true)
            }
        }
    }
}

@Composable
fun AnimatedO(animating: Boolean = false) {
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = if (animating) {
            infiniteRepeatable(
                animation = tween<Float>(1000),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween<Float>(500)
        }
    )
    
    Canvas(modifier = Modifier.size(48.dp)) {
        drawArc(
            color = Color(0xFFF2E9E4),
            startAngle = 0f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            style = Stroke(width = 8f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun WinningLine(lineaGanadora: LineaGanadora) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween<Float>(2000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cell = size.width / 3f
        val (start, end) = lineaGanadora
        val startOffset = Offset(
            x = start.second * cell + cell / 2,
            y = start.first * cell + cell / 2
        )
        val endOffset = Offset(
            x = end.second * cell + cell / 2,
            y = end.first * cell + cell / 2
        )
        
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFC107),
                    Color(0xFFFFD54F),
                    Color(0xFFFFC107)
                )
            ),
            start = startOffset,
            end = endOffset,
            strokeWidth = 12f,
            cap = StrokeCap.Round
        )
        
        // Efecto de brillo
        drawLine(
            color = Color.White.copy(alpha = 0.6f * animatedProgress),
            start = startOffset,
            end = endOffset,
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun GameResultDialog(
    winner: String,
    onPlayAgain: () -> Unit,
    onBack: () -> Unit
) {
    Dialog(onDismissRequest = { }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A2E)
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (winner == "Empate") "¡Empate!" else "¡$winner gana!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (winner) {
                        "Tú" -> Color(0xFF10B981)
                        "Bot" -> Color(0xFFEF4444)
                        else -> Color(0xFF6C63FF)
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onPlayAgain,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C63FF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Jugar de nuevo", color = Color.White)
                    }
                    
                    Button(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A4E69)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Volver", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun TresEnRayaTableroJugador(
    navController: NavController,
    nombreJugador1: String,
    nombreJugador2: String,
    victoriasJ1: Int,
    victoriasJ2: Int,
    onVictoriaJ1: () -> Unit,
    onVictoriaJ2: () -> Unit
) {
    var board by remember { mutableStateOf(List(3) { MutableList(3) { "" } }) }
    var isPlayerXTurn by remember { mutableStateOf(true) }
    var winner by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val lineaGanadora = obtenerLineaGanadora(board)

    fun checkWinner(board: List<List<String>>): String? {
        val lines = listOf(
            listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2)),
            listOf(Pair(1, 0), Pair(1, 1), Pair(1, 2)),
            listOf(Pair(2, 0), Pair(2, 1), Pair(2, 2)),
            listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0)),
            listOf(Pair(0, 1), Pair(1, 1), Pair(2, 1)),
            listOf(Pair(0, 2), Pair(1, 2), Pair(2, 2)),
            listOf(Pair(0, 0), Pair(1, 1), Pair(2, 2)),
            listOf(Pair(0, 2), Pair(1, 1), Pair(2, 0))
        )
        
        for (line in lines) {
            val values = line.map { (i, j) -> board[i][j] }
            if (values[0].isNotEmpty() && values.all { it == values[0] }) {
                return if (values[0] == "X") nombreJugador1 else nombreJugador2
            }
        }
        
        if (board.flatten().all { it.isNotEmpty() }) return "Empate"
        return null
    }

    fun playerMove(i: Int, j: Int) {
        if (board[i][j].isEmpty() && winner == null) {
            val newBoard = board.map { it.toMutableList() }.toMutableList()
            newBoard[i][j] = if (isPlayerXTurn) "X" else "O"
            board = newBoard
            
            val currentWinner = checkWinner(board)
            if (currentWinner != null) {
                winner = currentWinner
                showDialog = true
            } else {
                isPlayerXTurn = !isPlayerXTurn
            }
        }
    }

    fun resetGame() {
        board = List(3) { MutableList(3) { "" } }
        isPlayerXTurn = true
        winner = null
        showDialog = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header con marcador
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A3E).copy(alpha = 0.8f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tres en Raya - 2 Jugadores",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ScoreCard(
                            label = nombreJugador1,
                            score = victoriasJ1,
                            color = Color(0xFF9A8C98)
                        )
                        ScoreCard(
                            label = nombreJugador2,
                            score = victoriasJ2,
                            color = Color(0xFFF2E9E4)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Turno: ${if (isPlayerXTurn) nombreJugador1 else nombreJugador2}",
                        color = if (isPlayerXTurn) Color(0xFF9A8C98) else Color(0xFFF2E9E4),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Tablero
            Card(
                modifier = Modifier.size(320.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A3E).copy(alpha = 0.8f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Column {
                        for (i in 0..2) {
                            Row {
                                for (j in 0..2) {
                                    GameCell(
                                        value = board[i][j],
                                        isAnimating = false,
                                        onClick = { playerMove(i, j) },
                                        enabled = board[i][j].isEmpty() && winner == null
                                    )
                                    if (j < 2) Spacer(modifier = Modifier.width(8.dp))
                                }
                            }
                            if (i < 2) Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    
                    // Línea ganadora
                    if (lineaGanadora != null && winner != null) {
                        WinningLine(lineaGanadora)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { resetGame() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A4E69)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reiniciar", color = Color.White)
                }
                
                Button(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6C63FF)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Volver", color = Color.White)
                }
            }
        }
        
        // Diálogo de resultado
        if (showDialog && winner != null) {
            GameResultDialog(
                winner = winner!!,
                onPlayAgain = {
                    if (winner == nombreJugador1) onVictoriaJ1()
                    if (winner == nombreJugador2) onVictoriaJ2()
                    resetGame()
                },
                onBack = {
                    if (winner == nombreJugador1) onVictoriaJ1()
                    if (winner == nombreJugador2) onVictoriaJ2()
                    navController.navigateUp()
                }
            )
        }
    }
} 