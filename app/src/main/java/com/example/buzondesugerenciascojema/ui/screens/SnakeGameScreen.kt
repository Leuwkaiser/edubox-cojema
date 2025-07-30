
package com.example.buzondesugerenciascojema.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import android.content.res.Configuration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.buzondesugerenciascojema.R
import com.example.buzondesugerenciascojema.data.Usuario
import com.example.buzondesugerenciascojema.data.RankingService
import com.example.buzondesugerenciascojema.data.RankingEntry
import com.example.buzondesugerenciascojema.ui.components.RankingCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.example.buzondesugerenciascojema.util.SoundGenerator
import com.example.buzondesugerenciascojema.util.MusicPlayer

// Constantes del juego
const val SNAKE_BOARD_WIDTH = 25
const val SNAKE_BOARD_HEIGHT = 25
const val SNAKE_INITIAL_DELAY = 200L

// Colores del juego (estilo Tetris)
val SNAKE_HEAD_COLOR = Color(0xFF4CAF50) // Verde cabeza
val SNAKE_BODY_COLOR = Color(0xFF8BC34A) // Verde cuerpo
val FOOD_COLOR = Color(0xFFF44336) // Rojo manzana
val WALL_COLOR = Color(0xFF9E9E9E) // Gris paredes
val SNAKE_BACKGROUND_COLOR = Color(0xFF1A1A1A) // Negro fondo

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

data class SnakeSegment(
    val x: Int,
    val y: Int
)

data class Food(
    val x: Int,
    val y: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnakeGameScreen(
    navController: NavController,
    usuario: Usuario?
) {
    val scope = rememberCoroutineScope()
    val rankingService = remember { RankingService() }
    
    // Efectos de sonido y música
    val context = androidx.compose.ui.platform.LocalContext.current
    val soundGenerator = remember { SoundGenerator(context) }
    val musicPlayer = remember { MusicPlayer(context) }
    
    // Iniciar música del juego
    LaunchedEffect(Unit) {
        musicPlayer.playGameMusic("snake")
    }
    
    // Detener música cuando se sale
    DisposableEffect(Unit) {
        onDispose {
            musicPlayer.stopMusic()
        }
    }
    
    var snake by remember { mutableStateOf(listOf(SnakeSegment(SNAKE_BOARD_WIDTH / 2, SNAKE_BOARD_HEIGHT / 2))) }
    var direction by remember { mutableStateOf(Direction.RIGHT) }
    var score by remember { mutableStateOf(0) }
    var isGameRunning by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    var highScore by remember { mutableStateOf(0) }
    var showRanking by remember { mutableStateOf(false) }
    var globalRanking by remember { mutableStateOf<List<RankingEntry>>(emptyList()) }
    var isLoadingRanking by remember { mutableStateOf(false) }
    var userPosition by remember { mutableStateOf(-1) }
    var isNewRecord by remember { mutableStateOf(false) }
    var dropDelay by remember { mutableStateOf(SNAKE_INITIAL_DELAY) }
    
    // Función para generar comida en posición aleatoria
    fun generateFood(currentSnake: List<SnakeSegment>): Food {
        var newFood: Food
        do {
            newFood = Food(
                x = Random.nextInt(1, SNAKE_BOARD_WIDTH - 1), // Evitar bordes
                y = Random.nextInt(1, SNAKE_BOARD_HEIGHT - 1)
            )
        } while (currentSnake.any { it.x == newFood.x && it.y == newFood.y })
        return newFood
    }
    
    var food by remember { mutableStateOf(generateFood(snake)) }
    
    // Animación para el score
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        label = "score"
    )

    // Cargar ranking al inicio
    LaunchedEffect(Unit) {
        isLoadingRanking = true
        try {
            // Probar conexión primero
            val conexionExitosa = rankingService.probarConexion()
            println("DEBUG: Conexión a Firestore: $conexionExitosa")
            
            if (conexionExitosa) {
                globalRanking = rankingService.obtenerTopRanking("snake", 3)
                if (usuario != null) {
                    userPosition = rankingService.obtenerPosicionUsuario("snake", usuario.email)
                }
            } else {
                println("DEBUG: No se pudo conectar a Firestore")
            }
        } catch (e: Exception) {
            println("Error al cargar ranking: ${e.message}")
            e.printStackTrace()
        } finally {
            isLoadingRanking = false
        }
    }

    // Recargar ranking cada vez que se abre el menú de ranking
    LaunchedEffect(showRanking) {
        if (showRanking) {
            isLoadingRanking = true
            try {
                globalRanking = rankingService.obtenerTopRanking("snake", 3)
                if (usuario != null) {
                    userPosition = rankingService.obtenerPosicionUsuario("snake", usuario.email)
                }
            } catch (e: Exception) {
                println("Error al recargar ranking: ${e.message}")
            } finally {
                isLoadingRanking = false
            }
        }
    }

    // Función para guardar puntuación de manera más robusta
    fun guardarPuntuacionSegura() {
        if (usuario != null && score > 0) {
            scope.launch {
                try {
                    println("DEBUG: Intentando guardar puntuación de ${usuario.nombreCompleto}: $score")
                    
                    // Verificar si es nuevo récord antes de guardar
                    val esNuevoRecord = rankingService.esNuevoRecord("snake", score)
                    println("DEBUG: ¿Es nuevo récord? $esNuevoRecord")
                    
                    // Guardar la puntuación
                    val guardadoExitoso = rankingService.guardarPuntuacion(
                        juego = "snake",
                        puntuacion = score,
                        nombreUsuario = usuario.nombreCompleto,
                        emailUsuario = usuario.email
                    )
                    
                    println("DEBUG: ¿Guardado exitoso? $guardadoExitoso")
                    
                    if (guardadoExitoso) {
                        isNewRecord = esNuevoRecord
                        println("DEBUG: Puntuación guardada exitosamente")
                        
                        // Actualizar ranking después de un pequeño delay
                        delay(500L)
                        try {
                            globalRanking = rankingService.obtenerTopRanking("snake", 3)
                            userPosition = rankingService.obtenerPosicionUsuario("snake", usuario.email)
                            println("DEBUG: Ranking actualizado. Posición del usuario: $userPosition")
                        } catch (e: Exception) {
                            println("DEBUG: Error al actualizar ranking: ${e.message}")
                        }
                    } else {
                        println("DEBUG: Error al guardar puntuación")
                    }
                } catch (e: Exception) {
                    println("DEBUG: Error completo al guardar puntuación: ${e.message}")
                    e.printStackTrace()
                }
            }
        } else {
            println("DEBUG: No se puede guardar puntuación - Usuario: ${usuario != null}, Score: $score")
        }
    }

    // Función para mover la serpiente
    fun moveSnake() {
        if (!isGameRunning || gameOver) return
        
        val head = snake.first()
        val newHead = when (direction) {
            Direction.UP -> SnakeSegment(head.x, head.y - 1)
            Direction.DOWN -> SnakeSegment(head.x, head.y + 1)
            Direction.LEFT -> SnakeSegment(head.x - 1, head.y)
            Direction.RIGHT -> SnakeSegment(head.x + 1, head.y)
        }
        
        // Verificar colisión con paredes
        if (newHead.x <= 0 || newHead.x >= SNAKE_BOARD_WIDTH - 1 || 
            newHead.y <= 0 || newHead.y >= SNAKE_BOARD_HEIGHT - 1) {
            gameOver = true
            soundGenerator.playSnakeCrash()
            if (score > highScore) {
                highScore = score
            }
            
            // Guardar puntuación en el ranking global
            guardarPuntuacionSegura()
            return
        }
        
        // Verificar si la serpiente se choca consigo misma
        if (snake.any { it.x == newHead.x && it.y == newHead.y }) {
            gameOver = true
            soundGenerator.playSnakeCrash()
            if (score > highScore) {
                highScore = score
            }
            
            // Guardar puntuación en el ranking global
            guardarPuntuacionSegura()
            return
        }
        
        val newSnake = mutableListOf(newHead)
        newSnake.addAll(snake)
        
        // Verificar si come la comida
        if (newHead.x == food.x && newHead.y == food.y) {
            score += 10
            soundGenerator.playSnakeEat()
            // Aumentar velocidad
            dropDelay = maxOf(50L, SNAKE_INITIAL_DELAY - (score / 50) * 10L)
            food = generateFood(newSnake)
        } else {
            newSnake.removeAt(newSnake.size - 1)
        }
        
        snake = newSnake
    }

    // Game loop
    LaunchedEffect(isGameRunning) {
        while (isGameRunning && !gameOver) {
            delay(dropDelay)
            moveSnake()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Snake", 
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
                    IconButton(onClick = { showRanking = !showRanking }) {
                        Icon(
                            painter = painterResource(id = R.drawable.logro),
                            contentDescription = "Ver ranking",
                            modifier = Modifier.size(28.dp),
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
                    .background(SNAKE_BACKGROUND_COLOR)
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
                            text = "Puntuación: ${animatedScore.toInt()}",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Mejor: $highScore",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        if (userPosition > 0) {
                            Text(
                                text = "Posición: #$userPosition",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Controles de juego
                    IconButton(
                        onClick = {
                            if (gameOver) {
                                // Reiniciar juego
                                snake = listOf(SnakeSegment(SNAKE_BOARD_WIDTH / 2, SNAKE_BOARD_HEIGHT / 2))
                                direction = Direction.RIGHT
                                food = generateFood(snake)
                                score = 0
                                gameOver = false
                                isGameRunning = false
                                isNewRecord = false
                                dropDelay = SNAKE_INITIAL_DELAY
                            } else {
                                isGameRunning = !isGameRunning
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(
                                id = when {
                                    gameOver -> R.drawable.refrescar
                                    isGameRunning -> R.drawable.pausa
                                    else -> R.drawable.play
                                }
                            ),
                            contentDescription = when {
                                gameOver -> "Reiniciar"
                                isGameRunning -> "Pausar"
                                else -> "Jugar"
                            },
                            tint = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Controles de dirección
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Botón arriba
                        Button(
                            onClick = {
                                if (!gameOver && direction != Direction.DOWN) {
                                    direction = Direction.UP
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5))
                        ) {
                            Text("↑", fontSize = 20.sp)
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Botón izquierda
                            Button(
                                onClick = {
                                    if (!gameOver && direction != Direction.RIGHT) {
                                        direction = Direction.LEFT
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                            ) {
                                Text("←", fontSize = 20.sp)
                            }
                            
                            // Botón derecha
                            Button(
                                onClick = {
                                    if (!gameOver && direction != Direction.LEFT) {
                                        direction = Direction.RIGHT
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                            ) {
                                Text("→", fontSize = 20.sp)
                            }
                        }
                        
                        // Botón abajo
                        Button(
                            onClick = {
                                if (!gameOver && direction != Direction.UP) {
                                    direction = Direction.DOWN
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5))
                        ) {
                            Text("↓", fontSize = 20.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Mensajes de estado
                    if (gameOver) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE91E63)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "¡Game Over!",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Puntuación: $score",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                if (isNewRecord) {
                                    Text(
                                        text = "🏆 ¡NUEVO RÉCORD! 🏆",
                                        color = Color(0xFFFFD700),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    
                    if (isGameRunning && !gameOver) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "¡Jugando!",
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
                            repeat(SNAKE_BOARD_HEIGHT) { row ->
                                Row {
                                    repeat(SNAKE_BOARD_WIDTH) { col ->
                                        Box(
                                            modifier = Modifier
                                                .size(11.dp)
                                                .border(0.5.dp, Color.Gray.copy(alpha = 0.3f))
                                        ) {
                                            when {
                                                // Paredes
                                                col == 0 || col == SNAKE_BOARD_WIDTH - 1 || row == 0 || row == SNAKE_BOARD_HEIGHT - 1 -> {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(WALL_COLOR)
                                                    )
                                                }
                                                // Cabeza de la serpiente
                                                snake.firstOrNull()?.let { it.x == col && it.y == row } == true -> {
                                                    SnakeHead(direction = direction)
                                                }
                                                // Cuerpo de la serpiente
                                                snake.drop(1).any { it.x == col && it.y == row } -> {
                                                    SnakeBody()
                                                }
                                                // Comida
                                                food.x == col && food.y == row -> {
                                                    Apple()
                                                }
                                                // Celda vacía
                                                else -> {
                                                    // Celda vacía
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Panel derecho con ranking
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (showRanking) {
                        RankingCard(
                            rankings = globalRanking,
                            titulo = "🏆 Top 3 - Snake",
                            isLoading = isLoadingRanking,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            // Layout vertical original
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(SNAKE_BACKGROUND_COLOR)
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
                            text = "Puntuación: ${animatedScore.toInt()}",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Mejor: $highScore",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        if (userPosition > 0) {
                            Text(
                                text = "Posición: #$userPosition",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    // Controles de juego
                    Row {
                        IconButton(
                            onClick = {
                                if (gameOver) {
                                    // Reiniciar juego
                                    snake = listOf(SnakeSegment(SNAKE_BOARD_WIDTH / 2, SNAKE_BOARD_HEIGHT / 2))
                                    direction = Direction.RIGHT
                                    food = generateFood(snake)
                                    score = 0
                                    gameOver = false
                                    isGameRunning = false
                                    isNewRecord = false
                                    dropDelay = SNAKE_INITIAL_DELAY
                                } else {
                                    isGameRunning = !isGameRunning
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = when {
                                        gameOver -> R.drawable.refrescar
                                        isGameRunning -> R.drawable.pausa
                                        else -> R.drawable.play
                                    }
                                ),
                                contentDescription = when {
                                    gameOver -> "Reiniciar"
                                    isGameRunning -> "Pausar"
                                    else -> "Jugar"
                                },
                                tint = Color.White
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Ranking global (condicional)
                if (showRanking) {
                    RankingCard(
                        rankings = globalRanking,
                        titulo = "🏆 Top 3 - Snake",
                        isLoading = isLoadingRanking,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Tablero del juego
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                        .background(Color.Black, RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    // Renderizar tablero
                    Column {
                        repeat(SNAKE_BOARD_HEIGHT) { row ->
                            Row {
                                repeat(SNAKE_BOARD_WIDTH) { col ->
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .border(0.5.dp, Color.Gray.copy(alpha = 0.3f))
                                    ) {
                                        when {
                                            // Paredes
                                            col == 0 || col == SNAKE_BOARD_WIDTH - 1 || row == 0 || row == SNAKE_BOARD_HEIGHT - 1 -> {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(WALL_COLOR)
                                                )
                                            }
                                            // Cabeza de la serpiente
                                            snake.firstOrNull()?.let { it.x == col && it.y == row } == true -> {
                                                SnakeHead(direction = direction)
                                            }
                                            // Cuerpo de la serpiente
                                            snake.drop(1).any { it.x == col && it.y == row } -> {
                                                SnakeBody()
                                            }
                                            // Comida
                                            food.x == col && food.y == row -> {
                                                Apple()
                                            }
                                            // Celda vacía
                                            else -> {
                                                // Celda vacía
                                            }
                                        }
                                    }
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
                            if (!gameOver && direction != Direction.RIGHT) {
                                direction = Direction.LEFT
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                    ) {
                        Text("←", fontSize = 20.sp)
                    }
                    
                    // Botón arriba
                    Button(
                        onClick = {
                            if (!gameOver && direction != Direction.DOWN) {
                                direction = Direction.UP
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5))
                    ) {
                        Text("↑", fontSize = 20.sp)
                    }
                    
                    // Botón derecha
                    Button(
                        onClick = {
                            if (!gameOver && direction != Direction.LEFT) {
                                direction = Direction.RIGHT
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                    ) {
                        Text("→", fontSize = 20.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Botón abajo
                Button(
                    onClick = {
                        if (!gameOver && direction != Direction.UP) {
                            direction = Direction.DOWN
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5))
                ) {
                    Text("↓", fontSize = 20.sp)
                }
                
                // Mensajes de estado
                if (gameOver) {
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
                                text = "¡Game Over!",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Puntuación: $score",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            if (isNewRecord) {
                                Text(
                                    text = "🏆 ¡NUEVO RÉCORD! 🏆",
                                    color = Color(0xFFFFD700),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (userPosition > 0) {
                                Text(
                                    text = "Posición global: #$userPosition",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
                
                if (isGameRunning && !gameOver) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "¡Jugando!",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Usa los botones para controlar la serpiente",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SnakeHead(direction: Direction) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SNAKE_HEAD_COLOR)
    ) {
        // Ojos de la serpiente
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(1.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ojo izquierdo
            Box(
                modifier = Modifier
                    .size(2.dp)
                    .background(Color.White, CircleShape)
            )
            // Ojo derecho
            Box(
                modifier = Modifier
                    .size(2.dp)
                    .background(Color.White, CircleShape)
            )
        }
        
        // Lengua (pequeña línea roja)
        when (direction) {
            Direction.UP -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .size(width = 1.dp, height = 2.dp)
                        .background(Color.Red)
                )
            }
            Direction.DOWN -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(width = 1.dp, height = 2.dp)
                        .background(Color.Red)
                )
            }
            Direction.LEFT -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(width = 2.dp, height = 1.dp)
                        .background(Color.Red)
                )
            }
            Direction.RIGHT -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(width = 2.dp, height = 1.dp)
                        .background(Color.Red)
                )
            }
        }
    }
}

@Composable
fun SnakeBody() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SNAKE_BODY_COLOR)
    ) {
        // Patrón de escamas (pequeños puntos)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(4.dp)
                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
        )
    }
}

@Composable
fun Apple() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FOOD_COLOR)
    ) {
        // Tallo de la manzana
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(width = 1.dp, height = 2.dp)
                .background(Color(0xFF8B4513))
        )
        
        // Brillo en la manzana
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(3.dp)
                .background(Color.White.copy(alpha = 0.4f), CircleShape)
                .padding(start = 1.dp, top = 1.dp)
        )
    }
} 