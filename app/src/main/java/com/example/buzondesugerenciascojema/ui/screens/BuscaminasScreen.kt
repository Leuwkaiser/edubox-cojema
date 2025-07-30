package com.example.buzondesugerenciascojema.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
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
import kotlin.random.Random
import kotlinx.coroutines.delay
import com.example.buzondesugerenciascojema.util.SoundGenerator
import com.example.buzondesugerenciascojema.util.MusicPlayer

data class Cell(
    val row: Int,
    val col: Int,
    val isMine: Boolean = false,
    val isRevealed: Boolean = false,
    val isFlagged: Boolean = false,
    val neighborMines: Int = 0
)

@Composable
fun BuscaminasScreen(navController: NavController) {
    var gameState by remember { mutableStateOf("menu") }
    var difficulty by remember { mutableStateOf("easy") }
    var board by remember { mutableStateOf(listOf<List<Cell>>()) }
    var gameOver by remember { mutableStateOf(false) }
    var gameWon by remember { mutableStateOf(false) }
    var minesLeft by remember { mutableStateOf(0) }
    var timeElapsed by remember { mutableStateOf(0) }
    var isFirstClick by remember { mutableStateOf(true) }
    
    val scope = rememberCoroutineScope()
    
    // Efectos de sonido y música
    val context = androidx.compose.ui.platform.LocalContext.current
    val soundGenerator = remember { SoundGenerator(context) }
    val musicPlayer = remember { MusicPlayer(context) }
    
    // Iniciar música del juego
    LaunchedEffect(Unit) {
        musicPlayer.playGameMusic("buscaminas")
    }
    
    // Detener música cuando se sale
    DisposableEffect(Unit) {
        onDispose {
            musicPlayer.stopMusic()
        }
    }
    
    // Configuraciones por dificultad
    val config = when (difficulty) {
        "easy" -> Triple(9, 9, 10) // filas, columnas, minas
        "medium" -> Triple(16, 16, 40)
        "hard" -> Triple(16, 30, 99)
        else -> Triple(9, 9, 10)
    }
    
    val (rows, cols, totalMines) = config
    
    fun initializeBoard() {
        try {
            val newBoard = List(rows) { row ->
                List(cols) { col ->
                    Cell(row = row, col = col)
                }
            }
            board = newBoard
            minesLeft = totalMines
            gameOver = false
            gameWon = false
            timeElapsed = 0
            isFirstClick = true
        } catch (e: Exception) {
            // Fallback a un tablero pequeño si hay error
            board = List(9) { row ->
                List(9) { col ->
                    Cell(row = row, col = col)
                }
            }
            minesLeft = 10
            gameOver = false
            gameWon = false
            timeElapsed = 0
            isFirstClick = true
        }
    }
    
    // Inicializar el tablero cuando cambie la dificultad
    LaunchedEffect(difficulty) {
        initializeBoard()
    }
    
    fun placeMines(firstRow: Int, firstCol: Int) {
        // Verificar que el tablero esté inicializado correctamente
        if (board.isEmpty() || board.size != rows || board[0].size != cols) {
            initializeBoard()
            return
        }
        
        val newBoard = board.toMutableList().map { it.toMutableList() }
        var minesPlaced = 0
        var attempts = 0
        val maxAttempts = rows * cols * 2 // Evitar bucle infinito
        
        while (minesPlaced < totalMines && attempts < maxAttempts) {
            val row = Random.nextInt(rows)
            val col = Random.nextInt(cols)
            
            // Verificar que las coordenadas estén dentro de los límites
            if (row < newBoard.size && col < newBoard[row].size) {
                // No colocar mina en la primera celda clickeada ni en sus alrededores
                if (!newBoard[row][col].isMine && 
                    (abs(row - firstRow) > 1 || abs(col - firstCol) > 1)) {
                    newBoard[row][col] = newBoard[row][col].copy(isMine = true)
                    minesPlaced++
                }
            }
            attempts++
        }
        
        // Calcular minas vecinas
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if (!newBoard[row][col].isMine) {
                    var neighborMines = 0
                    for (dr in -1..1) {
                        for (dc in -1..1) {
                            val nr = row + dr
                            val nc = col + dc
                            if (nr in 0 until rows && nc in 0 until cols && 
                                newBoard[nr][nc].isMine) {
                                neighborMines++
                            }
                        }
                    }
                    newBoard[row][col] = newBoard[row][col].copy(neighborMines = neighborMines)
                }
            }
        }
        
        board = newBoard
    }
    
    fun revealCell(row: Int, col: Int) {
        // Verificar que el tablero esté inicializado y las coordenadas sean válidas
        if (board.isEmpty() || row < 0 || row >= board.size || col < 0 || col >= board[0].size) return
        if (gameOver || gameWon || board[row][col].isRevealed || board[row][col].isFlagged) return
        
        if (isFirstClick) {
            placeMines(row, col)
            isFirstClick = false
        }
        
        val newBoard = board.toMutableList().map { it.toMutableList() }
        
        if (newBoard[row][col].isMine) {
            // Game Over - revelar todas las minas
            soundGenerator.playMineExplosion()
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    if (newBoard[r][c].isMine) {
                        newBoard[r][c] = newBoard[r][c].copy(isRevealed = true)
                    }
                }
            }
            gameOver = true
        } else {
            // Revelar celda y vecinas si es necesario
            fun revealRecursive(r: Int, c: Int) {
                if (r !in 0 until rows || c !in 0 until cols || 
                    newBoard[r][c].isRevealed || newBoard[r][c].isFlagged) return
                
                newBoard[r][c] = newBoard[r][c].copy(isRevealed = true)
                
                if (newBoard[r][c].neighborMines == 0) {
                    for (dr in -1..1) {
                        for (dc in -1..1) {
                            if (dr != 0 || dc != 0) { // No revisar la celda actual
                                revealRecursive(r + dr, c + dc)
                            }
                        }
                    }
                }
            }
            
            revealRecursive(row, col)
            soundGenerator.playReveal()
            
            // Verificar si ganó
            var revealedCount = 0
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    if (newBoard[r][c].isRevealed && !newBoard[r][c].isMine) {
                        revealedCount++
                    }
                }
            }
            
            if (revealedCount == rows * cols - totalMines) {
                gameWon = true
                soundGenerator.playSuccess()
            }
        }
        
        board = newBoard
    }
    
    fun toggleFlag(row: Int, col: Int) {
        // Verificar que el tablero esté inicializado y las coordenadas sean válidas
        if (board.isEmpty() || row < 0 || row >= board.size || col < 0 || col >= board[0].size) return
        if (gameOver || gameWon || board[row][col].isRevealed) return
        
        val newBoard = board.toMutableList().map { it.toMutableList() }
        val cell = newBoard[row][col]
        newBoard[row][col] = cell.copy(isFlagged = !cell.isFlagged)
        
        soundGenerator.playFlag()
        
        if (cell.isFlagged) {
            minesLeft--
        } else {
            minesLeft++
        }
        
        board = newBoard
    }
    
    LaunchedEffect(gameState) {
        if (gameState == "playing") {
            while (gameState == "playing" && !gameOver && !gameWon) {
                delay(1000)
                timeElapsed++
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6C63FF),
                        Color(0xFF42A5F5),
                        Color(0xFFB388FF)
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
                        text = "💣 Buscaminas",
                        fontSize = 32.sp,
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
                                difficulty = "easy"
                                gameState = "playing"
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
                                text = "9x9 - 10 minas",
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
                                difficulty = "medium"
                                gameState = "playing"
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
                                text = "16x16 - 40 minas",
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
                                difficulty = "hard"
                                gameState = "playing"
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
                                text = "16x30 - 99 minas",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = { navController.navigateUp() },
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header con información del juego
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                        ) {
                            Text(
                                text = "💣 $minesLeft",
                                modifier = Modifier.padding(8.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6C63FF)
                            )
                        }
                        
                        Button(
                            onClick = { 
                                gameState = "menu"
                                gameOver = false
                                gameWon = false
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
                                text = "⏱️ $timeElapsed",
                                modifier = Modifier.padding(8.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6C63FF)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Tablero del juego - pantalla completa
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            for (row in 0 until rows) {
                                Row(
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    for (col in 0 until cols) {
                                        val cell = board.getOrNull(row)?.getOrNull(col)
                                        if (cell != null) {
                                            CellView(
                                                cell = cell,
                                                onClick = { revealCell(row, col) },
                                                onLongClick = { toggleFlag(row, col) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Diálogos de fin de juego
                if (gameOver) {
                    GameOverDialog(
                        onRestart = {
                            gameState = "menu"
                            gameOver = false
                        },
                        onBackToMenu = {
                            gameState = "menu"
                            gameOver = false
                        }
                    )
                }
                
                if (gameWon) {
                    GameWonDialog(
                        timeElapsed = timeElapsed,
                        onRestart = {
                            gameState = "menu"
                            gameWon = false
                        },
                        onBackToMenu = {
                            gameState = "menu"
                            gameWon = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CellView(
    cell: Cell,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val cellSize = 35.dp
    
    Box(
        modifier = Modifier
            .size(cellSize)
            .padding(1.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                when {
                    cell.isRevealed -> {
                        if (cell.isMine) Color.Red
                        else Color.LightGray
                    }
                    cell.isFlagged -> Color.Yellow
                    else -> Color.Gray
                }
            )
            .border(
                width = 1.dp,
                color = Color.DarkGray,
                shape = RoundedCornerShape(4.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        when {
            cell.isFlagged -> {
                Text(
                    text = "🚩",
                    fontSize = 12.sp
                )
            }
            cell.isRevealed && cell.isMine -> {
                Text(
                    text = "💣",
                    fontSize = 12.sp
                )
            }
            cell.isRevealed && cell.neighborMines > 0 -> {
                                        val color = when (cell.neighborMines) {
                            1 -> Color.Blue
                            2 -> Color.Green
                            3 -> Color.Red
                            4 -> Color(0xFF000080) // DarkBlue
                            5 -> Color(0xFF8B0000) // DarkRed
                            6 -> Color.Cyan
                            7 -> Color.Black
                            8 -> Color.Gray
                            else -> Color.Black
                        }
                Text(
                    text = cell.neighborMines.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            cell.isRevealed -> {
                // Celda vacía revelada
            }
            else -> {
                // Celda no revelada
            }
        }
    }
}

@Composable
fun GameOverDialog(
    onRestart: () -> Unit,
    onBackToMenu: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                text = "💥 ¡Game Over!",
                color = Color.Red,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Has tocado una mina. ¡Inténtalo de nuevo!",
                color = Color.Gray
            )
        },
        confirmButton = {
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
            ) {
                Text("Reiniciar", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onBackToMenu,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Menú", color = Color.White)
            }
        }
    )
}

@Composable
fun GameWonDialog(
    timeElapsed: Int,
    onRestart: () -> Unit,
    onBackToMenu: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                text = "🎉 ¡Victoria!",
                color = Color.Green,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "¡Has ganado en $timeElapsed segundos!",
                color = Color.Gray
            )
        },
        confirmButton = {
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
            ) {
                Text("Jugar de nuevo", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onBackToMenu,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Menú", color = Color.White)
            }
        }
    )
} 