package com.example.buzondesugerenciascojema.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import android.content.res.Configuration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.buzondesugerenciascojema.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.example.buzondesugerenciascojema.util.SoundGenerator
import com.example.buzondesugerenciascojema.util.MusicPlayer

// Constantes del juego
const val BOARD_WIDTH = 10
const val BOARD_HEIGHT = 20
const val INITIAL_DROP_DELAY = 1000L // 1 segundo

// Piezas del Tetris (Tetrominos)
val TETROMINOS = listOf(
    // I-piece
    listOf(
        listOf(1, 1, 1, 1)
    ),
    // O-piece
    listOf(
        listOf(1, 1),
        listOf(1, 1)
    ),
    // T-piece
    listOf(
        listOf(0, 1, 0),
        listOf(1, 1, 1)
    ),
    // S-piece
    listOf(
        listOf(0, 1, 1),
        listOf(1, 1, 0)
    ),
    // Z-piece
    listOf(
        listOf(1, 1, 0),
        listOf(0, 1, 1)
    ),
    // J-piece
    listOf(
        listOf(1, 0, 0),
        listOf(1, 1, 1)
    ),
    // L-piece
    listOf(
        listOf(0, 0, 1),
        listOf(1, 1, 1)
    )
)

// Colores para las piezas
val PIECE_COLORS = listOf(
    Color(0xFF00BCD4), // Cyan
    Color(0xFFFFEB3B), // Yellow
    Color(0xFF9C27B0), // Purple
    Color(0xFF4CAF50), // Green
    Color(0xFFF44336), // Red
    Color(0xFF2196F3), // Blue
    Color(0xFFFF9800)  // Orange
)

data class TetrisPiece(
    val shape: List<List<Int>>,
    val color: Color,
    var x: Int = BOARD_WIDTH / 2 - 1,
    var y: Int = 0
)

data class TetrisGameState(
    val board: Array<Array<Color?>> = Array(BOARD_HEIGHT) { Array(BOARD_WIDTH) { null } },
    val currentPiece: TetrisPiece? = null,
    val nextPiece: TetrisPiece? = null,
    val score: Int = 0,
    val level: Int = 1,
    val linesCleared: Int = 0,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TetrisScreen(navController: NavController) {
    var gameState by remember { mutableStateOf(TetrisGameState()) }
    var dropDelay by remember { mutableStateOf(INITIAL_DROP_DELAY) }
    
    // Efectos de sonido y música
    val context = LocalContext.current
    val soundGenerator = remember { SoundGenerator(context) }
    val musicPlayer = remember { MusicPlayer(context) }
    
    // Iniciar música del juego
    LaunchedEffect(Unit) {
        musicPlayer.playGameMusic("tetris")
    }
    
    // Detener música cuando se sale
    DisposableEffect(Unit) {
        onDispose {
            musicPlayer.stopMusic()
        }
    }

    // Inicializar juego
    LaunchedEffect(Unit) {
        startNewGame(gameState) { newState ->
            gameState = newState
        }
    }

    // Bucle del juego
    LaunchedEffect(gameState.isPaused, gameState.isGameOver) {
        while (!gameState.isPaused && !gameState.isGameOver) {
            delay(dropDelay)
            if (!gameState.isPaused && !gameState.isGameOver) {
                movePieceDown(gameState, { newState ->
                    gameState = newState
                    // Actualizar velocidad basada en el nivel
                    dropDelay = maxOf(100L, INITIAL_DROP_DELAY - (newState.level - 1) * 100L)
                }, soundGenerator)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Tetris", 
                        fontWeight = FontWeight.Bold, 
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        soundGenerator.playClick()
                        navController.popBackStack() 
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            soundGenerator.playClick()
                            gameState = gameState.copy(isPaused = !gameState.isPaused)
                        }
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (gameState.isPaused) R.drawable.play else R.drawable.pausa
                            ),
                            contentDescription = if (gameState.isPaused) "Reanudar" else "Pausar",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = {
                            soundGenerator.playClick()
                            startNewGame(gameState) { newState ->
                                gameState = newState
                                dropDelay = INITIAL_DROP_DELAY
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Nuevo juego",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6C63FF))
            )
        }
    ) { paddingValues ->
        val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
        
        if (isLandscape) {
            // Layout horizontal
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFF1A1A1A))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Panel izquierdo con información y controles
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Información del juego
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Puntuación: ${gameState.score}",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Nivel: ${gameState.level}",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Líneas: ${gameState.linesCleared}",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Siguiente pieza
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Siguiente:",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        gameState.nextPiece?.let { piece ->
                            NextPieceDisplay(piece = piece)
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Controles
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Botón izquierda
                            Button(
                                onClick = {
                                    if (!gameState.isPaused && !gameState.isGameOver) {
                                        movePieceLeft(gameState) { newState ->
                                            gameState = newState
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                            ) {
                                Text("←", fontSize = 18.sp)
                            }
                            
                            // Botón rotar
                            Button(
                                onClick = {
                                    if (!gameState.isPaused && !gameState.isGameOver) {
                                        rotatePiece(gameState, { newState ->
                                            gameState = newState
                                        }, soundGenerator)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5))
                            ) {
                                Text("Rotar", fontSize = 14.sp)
                            }
                            
                            // Botón derecha
                            Button(
                                onClick = {
                                    if (!gameState.isPaused && !gameState.isGameOver) {
                                        movePieceRight(gameState) { newState ->
                                            gameState = newState
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                            ) {
                                Text("→", fontSize = 18.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Botón caída rápida
                        Button(
                            onClick = {
                                if (!gameState.isPaused && !gameState.isGameOver) {
                                                            hardDrop(gameState, { newState ->
                            gameState = newState
                        }, soundGenerator)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                        ) {
                            Text("Caída Rápida", fontSize = 14.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Mensajes de estado
                    if (gameState.isGameOver) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE91E63)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "¡Juego Terminado!",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Puntuación final: ${gameState.score}",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    
                    if (gameState.isPaused) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Juego Pausado",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                // Panel central con el tablero
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Tablero del juego
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        // Renderizar tablero
                        Column {
                            repeat(BOARD_HEIGHT) { row ->
                                Row {
                                    repeat(BOARD_WIDTH) { col ->
                                        val cellColor = when {
                                            // Pieza actual
                                            gameState.currentPiece?.let { piece ->
                                                isPieceAt(piece, col, row)
                                            } == true -> gameState.currentPiece!!.color
                                            // Celdas ocupadas
                                            gameState.board[row][col] != null -> gameState.board[row][col]!!
                                            // Celda vacía
                                            else -> Color.Transparent
                                        }
                                        
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .background(cellColor)
                                                .border(0.5.dp, Color.Gray.copy(alpha = 0.3f))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Layout vertical original
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFF1A1A1A))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Información del juego
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Puntuación y nivel
                    Column {
                        Text(
                            text = "Puntuación: ${gameState.score}",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Nivel: ${gameState.level}",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Líneas: ${gameState.linesCleared}",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    
                    // Siguiente pieza
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Siguiente:",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        gameState.nextPiece?.let { piece ->
                            NextPieceDisplay(piece = piece)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Tablero del juego
                Box(
                    modifier = Modifier
                        .size(400.dp)
                        .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                        .background(Color.Black, RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    // Renderizar tablero
                    Column {
                        repeat(BOARD_HEIGHT) { row ->
                            Row {
                                repeat(BOARD_WIDTH) { col ->
                                    val cellColor = when {
                                        // Pieza actual
                                        gameState.currentPiece?.let { piece ->
                                            isPieceAt(piece, col, row)
                                        } == true -> gameState.currentPiece!!.color
                                        // Celdas ocupadas
                                        gameState.board[row][col] != null -> gameState.board[row][col]!!
                                        // Celda vacía
                                        else -> Color.Transparent
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .background(cellColor)
                                            .border(0.5.dp, Color.Gray.copy(alpha = 0.3f))
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Controles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Botón izquierda
                    Button(
                        onClick = {
                            if (!gameState.isPaused && !gameState.isGameOver) {
                                movePieceLeft(gameState) { newState ->
                                    gameState = newState
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                    ) {
                        Text("←", fontSize = 20.sp)
                    }
                    
                    // Botón rotar
                    Button(
                        onClick = {
                            if (!gameState.isPaused && !gameState.isGameOver) {
                                rotatePiece(gameState, { newState ->
                                    gameState = newState
                                }, soundGenerator)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5))
                    ) {
                        Text("Rotar", fontSize = 16.sp)
                    }
                    
                    // Botón derecha
                    Button(
                        onClick = {
                            if (!gameState.isPaused && !gameState.isGameOver) {
                                movePieceRight(gameState) { newState ->
                                    gameState = newState
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                    ) {
                        Text("→", fontSize = 20.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Botón caída rápida
                Button(
                    onClick = {
                        if (!gameState.isPaused && !gameState.isGameOver) {
                            hardDrop(gameState, { newState ->
                                gameState = newState
                            }, soundGenerator)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                ) {
                    Text("Caída Rápida", fontSize = 16.sp)
                }
                
                // Mensajes de estado
                if (gameState.isGameOver) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE91E63)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "¡Juego Terminado!",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Puntuación final: ${gameState.score}",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                
                if (gameState.isPaused) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Juego Pausado",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NextPieceDisplay(piece: TetrisPiece) {
    Column {
        piece.shape.forEach { row ->
            Row {
                row.forEach { cell ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (cell == 1) piece.color else Color.Transparent)
                            .border(0.5.dp, Color.Gray.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}

// Funciones del juego
fun startNewGame(currentState: TetrisGameState, onStateChange: (TetrisGameState) -> Unit) {
    val newPiece = createRandomPiece()
    val nextPiece = createRandomPiece()
    
    onStateChange(
        TetrisGameState(
            currentPiece = newPiece,
            nextPiece = nextPiece
        )
    )
}

fun createRandomPiece(): TetrisPiece {
    val randomIndex = Random.nextInt(TETROMINOS.size)
    return TetrisPiece(
        shape = TETROMINOS[randomIndex],
        color = PIECE_COLORS[randomIndex]
    )
}

fun isPieceAt(piece: TetrisPiece, x: Int, y: Int): Boolean {
    val pieceX = x - piece.x
    val pieceY = y - piece.y
    
    return pieceY >= 0 && pieceY < piece.shape.size &&
           pieceX >= 0 && pieceX < piece.shape[0].size &&
           piece.shape[pieceY][pieceX] == 1
}

fun isValidMove(piece: TetrisPiece, newX: Int, newY: Int, board: Array<Array<Color?>>): Boolean {
    piece.shape.forEachIndexed { rowIndex, row ->
        row.forEachIndexed { colIndex, cell ->
            if (cell == 1) {
                val boardX = newX + colIndex
                val boardY = newY + rowIndex
                
                if (boardX < 0 || boardX >= BOARD_WIDTH || 
                    boardY >= BOARD_HEIGHT ||
                    (boardY >= 0 && board[boardY][boardX] != null)) {
                    return false
                }
            }
        }
    }
    return true
}

fun movePieceLeft(currentState: TetrisGameState, onStateChange: (TetrisGameState) -> Unit) {
    currentState.currentPiece?.let { piece ->
        if (isValidMove(piece, piece.x - 1, piece.y, currentState.board)) {
            onStateChange(currentState.copy(
                currentPiece = piece.copy(x = piece.x - 1)
            ))
        }
    }
}

fun movePieceRight(currentState: TetrisGameState, onStateChange: (TetrisGameState) -> Unit) {
    currentState.currentPiece?.let { piece ->
        if (isValidMove(piece, piece.x + 1, piece.y, currentState.board)) {
            onStateChange(currentState.copy(
                currentPiece = piece.copy(x = piece.x + 1)
            ))
        }
    }
}

fun movePieceDown(currentState: TetrisGameState, onStateChange: (TetrisGameState) -> Unit, soundGenerator: SoundGenerator? = null) {
    currentState.currentPiece?.let { piece ->
        if (isValidMove(piece, piece.x, piece.y + 1, currentState.board)) {
            onStateChange(currentState.copy(
                currentPiece = piece.copy(y = piece.y + 1)
            ))
        } else {
            // Pieza no puede bajar más, colocarla en el tablero
            placePiece(currentState, onStateChange, soundGenerator)
        }
    }
}

fun hardDrop(currentState: TetrisGameState, onStateChange: (TetrisGameState) -> Unit, soundGenerator: SoundGenerator? = null) {
    currentState.currentPiece?.let { piece ->
        var dropDistance = 0
        while (isValidMove(piece, piece.x, piece.y + dropDistance + 1, currentState.board)) {
            dropDistance++
        }
        onStateChange(currentState.copy(
            currentPiece = piece.copy(y = piece.y + dropDistance)
        ))
        // Colocar la pieza inmediatamente después
        placePiece(currentState.copy(
            currentPiece = piece.copy(y = piece.y + dropDistance)
        ), onStateChange, soundGenerator)
    }
}

fun rotatePiece(currentState: TetrisGameState, onStateChange: (TetrisGameState) -> Unit, soundGenerator: SoundGenerator? = null) {
    currentState.currentPiece?.let { piece ->
        val rotatedShape = piece.shape[0].indices.map { col ->
            piece.shape.indices.map { row ->
                piece.shape[piece.shape.size - 1 - row][col]
            }
        }
        
        val rotatedPiece = piece.copy(shape = rotatedShape)
        if (isValidMove(rotatedPiece, piece.x, piece.y, currentState.board)) {
            soundGenerator?.playTetrisRotate()
            onStateChange(currentState.copy(currentPiece = rotatedPiece))
        }
    }
}

fun placePiece(currentState: TetrisGameState, onStateChange: (TetrisGameState) -> Unit, soundGenerator: SoundGenerator? = null) {
    currentState.currentPiece?.let { piece ->
        val newBoard = currentState.board.map { it.clone() }.toTypedArray()
        
        piece.shape.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, cell ->
                if (cell == 1) {
                    val boardX = piece.x + colIndex
                    val boardY = piece.y + rowIndex
                    if (boardY >= 0) {
                        newBoard[boardY][boardX] = piece.color
                    }
                }
            }
        }
        
        // Verificar líneas completas
        val linesToClear = mutableListOf<Int>()
        newBoard.forEachIndexed { rowIndex, row ->
            if (row.all { it != null }) {
                linesToClear.add(rowIndex)
            }
        }
        
        // Limpiar líneas completas
        if (linesToClear.isNotEmpty()) {
            soundGenerator?.playTetrisLine()
        }
        linesToClear.forEach { rowIndex ->
            for (row in rowIndex downTo 1) {
                newBoard[row] = newBoard[row - 1].clone()
            }
            newBoard[0] = Array(BOARD_WIDTH) { null }
        }
        
        // Calcular puntuación
        val newLinesCleared = currentState.linesCleared + linesToClear.size
        val newLevel = (newLinesCleared / 10) + 1
        val newScore = currentState.score + when (linesToClear.size) {
            1 -> 100 * newLevel
            2 -> 300 * newLevel
            3 -> 500 * newLevel
            4 -> 800 * newLevel
            else -> 0
        }
        
        // Crear nueva pieza
        val newPiece = currentState.nextPiece ?: createRandomPiece()
        val nextPiece = createRandomPiece()
        
        // Verificar game over
        val isGameOver = !isValidMove(newPiece, newPiece.x, newPiece.y, newBoard)
        
        if (isGameOver) {
            soundGenerator?.playError()
        }
        
        onStateChange(currentState.copy(
            board = newBoard,
            currentPiece = if (isGameOver) null else newPiece,
            nextPiece = nextPiece,
            score = newScore,
            level = newLevel,
            linesCleared = newLinesCleared,
            isGameOver = isGameOver
        ))
    }
} 