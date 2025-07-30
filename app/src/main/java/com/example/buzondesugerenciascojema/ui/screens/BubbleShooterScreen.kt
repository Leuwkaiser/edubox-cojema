package com.example.buzondesugerenciascojema.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import android.content.res.Configuration
import androidx.compose.ui.text.font.FontWeight
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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Constantes del juego
const val BUBBLE_BOARD_WIDTH = 12
const val BUBBLE_BOARD_HEIGHT = 15
const val BUBBLE_INITIAL_DELAY = 100L

// Colores de las burbujas
val BUBBLE_COLORS = listOf(
    Color(0xFFE91E63), // Rosa
    Color(0xFF2196F3), // Azul
    Color(0xFF4CAF50), // Verde
    Color(0xFFFFEB3B), // Amarillo
    Color(0xFF9C27B0), // Púrpura
    Color(0xFFFF5722)  // Naranja
)

val BUBBLE_BACKGROUND_COLOR = Color(0xFF1A1A1A) // Negro fondo
val BUBBLE_SIZE = 24.dp

data class Bubble(
    val x: Int,
    val y: Int,
    val color: Color,
    val isActive: Boolean = true
)

data class BubbleShooterGameState(
    val bubbles: List<Bubble> = generateInitialBubbles(),
    val currentBubble: Bubble? = generateRandomBubble(),
    val nextBubble: Bubble? = generateRandomBubble(),
    val score: Int = 0,
    val level: Int = 1,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val bubblesToRemove: List<Bubble> = emptyList(),
    val cannonAngle: Float = 0f,
    val accumulatedBubbles: List<Bubble> = emptyList(),
    val gameOverLine: Float = BUBBLE_BOARD_HEIGHT * 0.8f, // Línea de game over
    val trajectory: List<Pair<Int, Int>> = emptyList() // Trayectoria de la bola
)

fun generateInitialBubbles(): List<Bubble> {
    val bubbles = mutableListOf<Bubble>()
    
    // Generar burbujas iniciales en la parte superior
    for (row in 0..7) {
        for (col in 0 until BUBBLE_BOARD_WIDTH) {
            // Patrón hexagonal
            val offset = if (row % 2 == 1) 0.5f else 0f
            val x = col + offset
            if (x < BUBBLE_BOARD_WIDTH) {
                bubbles.add(
                    Bubble(
                        x = x.toInt(),
                        y = row,
                        color = BUBBLE_COLORS.random()
                    )
                )
            }
        }
    }
    
    return bubbles
}

fun generateRandomBubble(): Bubble {
    return Bubble(
        x = BUBBLE_BOARD_WIDTH / 2,
        y = BUBBLE_BOARD_HEIGHT - 1,
        color = BUBBLE_COLORS.random()
    )
}

// Función para encontrar burbujas conectadas del mismo color
fun findConnectedBubbles(bubbles: List<Bubble>, x: Int, y: Int, color: Color): List<Bubble> {
    val visited = mutableSetOf<Pair<Int, Int>>()
    val connected = mutableListOf<Bubble>()
    
    fun dfs(currentX: Int, currentY: Int) {
        if (currentX < 0 || currentX >= BUBBLE_BOARD_WIDTH || 
            currentY < 0 || currentY >= BUBBLE_BOARD_HEIGHT ||
            Pair(currentX, currentY) in visited) return
        
        visited.add(Pair(currentX, currentY))
        
        val bubble = bubbles.find { bubble -> bubble.x == currentX && bubble.y == currentY }
        if (bubble != null && bubble.color == color) {
            connected.add(bubble)
            
            // Vecinos en patrón hexagonal
            val neighbors = if (currentY % 2 == 0) {
                listOf(
                    Pair(currentX - 1, currentY - 1),
                    Pair(currentX, currentY - 1),
                    Pair(currentX - 1, currentY),
                    Pair(currentX + 1, currentY),
                    Pair(currentX - 1, currentY + 1),
                    Pair(currentX, currentY + 1)
                )
            } else {
                listOf(
                    Pair(currentX, currentY - 1),
                    Pair(currentX + 1, currentY - 1),
                    Pair(currentX - 1, currentY),
                    Pair(currentX + 1, currentY),
                    Pair(currentX, currentY + 1),
                    Pair(currentX + 1, currentY + 1)
                )
            }
            
            neighbors.forEach { (nx, ny) ->
                dfs(nx, ny)
            }
        }
    }
    
    dfs(x, y)
    return connected
}

// Función para calcular dónde aterriza la burbuja
fun calculateBubbleLanding(bubble: Bubble, existingBubbles: List<Bubble>): Bubble {
    // Simulación simple: la burbuja sube hasta chocar con otra o llegar al tope
    var currentY = bubble.y
    var currentX = bubble.x
    
    while (currentY > 0) {
        // Verificar si hay una burbuja en la posición actual
        val hasCollision = existingBubbles.any { existingBubble -> existingBubble.x == currentX && existingBubble.y == currentY }
        if (hasCollision) {
            break
        }
        currentY--
    }
    
    return bubble.copy(x = currentX, y = currentY + 1)
}

// Función para calcular trayectoria de la bola
fun calculateTrajectory(angle: Float, gameState: BubbleShooterGameState): List<Pair<Int, Int>> {
    val trajectory = mutableListOf<Pair<Int, Int>>()
    val cannonX = BUBBLE_BOARD_WIDTH / 2
    val cannonY = BUBBLE_BOARD_HEIGHT - 1
    
    var currentX = cannonX.toFloat()
    var currentY = cannonY.toFloat()
    val radians = Math.toRadians(angle.toDouble())
    val dx = -sin(radians).toFloat() * 0.5f
    val dy = -cos(radians).toFloat() * 0.5f
    
    repeat(50) { // Máximo 50 pasos
        currentX += dx
        currentY += dy
        
        // Verificar rebotes en paredes
        if (currentX < 0) {
            currentX = 0f
            // Rebote
            currentX = -currentX
        } else if (currentX >= BUBBLE_BOARD_WIDTH) {
            currentX = BUBBLE_BOARD_WIDTH - 1f
            // Rebote
            currentX = BUBBLE_BOARD_WIDTH - 1f - (currentX - (BUBBLE_BOARD_WIDTH - 1f))
        }
        
        // Verificar colisión con burbujas existentes
        val bubbleX = currentX.toInt()
        val bubbleY = currentY.toInt()
        
        if (bubbleY < 0) return@repeat
        
        val hasCollision = gameState.bubbles.any { existingBubble -> existingBubble.x == bubbleX && existingBubble.y == bubbleY }
        if (hasCollision) return@repeat
        
        trajectory.add(Pair(bubbleX, bubbleY))
    }
    
    return trajectory
}

// Función para disparar burbuja
fun shootBubble(x: Int, y: Int, gameState: BubbleShooterGameState): BubbleShooterGameState {
    val newBubble = Bubble(x = x, y = y, color = gameState.currentBubble?.color ?: BUBBLE_COLORS.random())
    
    // Simular trayectoria de la burbuja
    val finalPosition = calculateBubbleLanding(newBubble, gameState.bubbles)
    
    // Agregar la burbuja a la posición final
    val newBubbles = gameState.bubbles + finalPosition
    
    // Encontrar burbujas conectadas del mismo color
    val connectedBubbles = findConnectedBubbles(newBubbles, finalPosition.x, finalPosition.y, finalPosition.color)
    
    // Si hay 3 o más burbujas conectadas, eliminarlas
    val bubblesToRemove = if (connectedBubbles.size >= 3) connectedBubbles else emptyList()
    val updatedBubbles = newBubbles.filter { bubble -> bubble !in bubblesToRemove }
    
    // Calcular puntuación
    val newScore = gameState.score + (bubblesToRemove.size * 10)
    
    // Verificar si las burbujas han llegado muy abajo (game over)
    val lowestBubble = updatedBubbles.minByOrNull { bubble -> bubble.y }
    val isGameOver = lowestBubble?.y ?: 0 >= gameState.gameOverLine.toInt()
    
    // Acumular burbujas si no se eliminaron suficientes
    val newAccumulatedBubbles = if (bubblesToRemove.size < 3) {
        gameState.accumulatedBubbles + finalPosition
    } else {
        gameState.accumulatedBubbles
    }
    
    // Verificar si hay 3 o más burbujas acumuladas del mismo color
    val accumulatedGroups = findConnectedBubbles(newAccumulatedBubbles, finalPosition.x, finalPosition.y, finalPosition.color)
    val finalBubblesToRemove = if (accumulatedGroups.size >= 3) {
        bubblesToRemove + accumulatedGroups
    } else {
        bubblesToRemove
    }
    
    val finalBubbles = updatedBubbles.filter { bubble -> bubble !in accumulatedGroups }
    val finalAccumulatedBubbles = if (accumulatedGroups.size >= 3) {
        newAccumulatedBubbles.filter { bubble -> bubble !in accumulatedGroups }
    } else {
        newAccumulatedBubbles
    }
    
    return gameState.copy(
        bubbles = finalBubbles,
        currentBubble = gameState.nextBubble,
        nextBubble = generateRandomBubble(),
        score = newScore + (accumulatedGroups.size * 10),
        bubblesToRemove = finalBubblesToRemove,
        isGameOver = isGameOver,
        accumulatedBubbles = finalAccumulatedBubbles,
        trajectory = emptyList()
    )
}

// --- NUEVO: Animar disparo y rebotes ---
suspend fun animateBubbleShot(
    angle: Float,
    gameState: BubbleShooterGameState,
    onUpdate: (BubbleShooterGameState) -> Unit
): BubbleShooterGameState {
    val cannonX = BUBBLE_BOARD_WIDTH / 2
    val cannonY = BUBBLE_BOARD_HEIGHT - 1
    var currentX = cannonX.toFloat()
    var currentY = cannonY.toFloat()
    val radians = Math.toRadians(angle.toDouble())
    var dx = -sin(radians).toFloat() * 0.5f
    var dy = -cos(radians).toFloat() * 0.5f
    val color = gameState.currentBubble?.color ?: BUBBLE_COLORS.random()
    val bubbles = gameState.bubbles
    var landed = false
    var finalX = currentX.toInt()
    var finalY = currentY.toInt()
    val maxSteps = 100
    var steps = 0
    while (!landed && steps < maxSteps) {
        currentX += dx
        currentY += dy
        // Rebote en paredes
        if (currentX < 0) {
            currentX = 0f
            dx = -dx
        } else if (currentX >= BUBBLE_BOARD_WIDTH - 1) {
            currentX = BUBBLE_BOARD_WIDTH - 1f
            dx = -dx
        }
        val bubbleX = currentX.toInt()
        val bubbleY = currentY.toInt()
        // Colisión con burbujas existentes o tope
        val hasCollision = bubbles.any { it.x == bubbleX && it.y == bubbleY } || bubbleY <= 0
        if (hasCollision) {
            landed = true
            finalX = bubbleX
            finalY = bubbleY + if (bubbleY <= 0) 0 else 1
        }
        // Actualizar animación
        onUpdate(gameState.copy(trajectory = listOf(Pair(bubbleX, bubbleY))))
        kotlinx.coroutines.delay(10)
        steps++
    }
    // Colocar la burbuja en la posición final
    val newBubble = Bubble(x = finalX, y = finalY, color = color)
    val newBubbles = bubbles + newBubble
    // El resto igual que shootBubble
    val connectedBubbles = findConnectedBubbles(newBubbles, newBubble.x, newBubble.y, newBubble.color)
    val bubblesToRemove = if (connectedBubbles.size >= 3) connectedBubbles else emptyList()
    val updatedBubbles = newBubbles.filter { bubble -> bubble !in bubblesToRemove }
    val newScore = gameState.score + (bubblesToRemove.size * 10)
    val lowestBubble = updatedBubbles.maxByOrNull { bubble -> bubble.y }
    val isGameOver = lowestBubble?.y ?: 0 >= BUBBLE_BOARD_HEIGHT - 1
    return gameState.copy(
        bubbles = updatedBubbles,
        currentBubble = gameState.nextBubble,
        nextBubble = generateRandomBubble(),
        score = newScore,
        bubblesToRemove = bubblesToRemove,
        isGameOver = isGameOver,
        trajectory = emptyList()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BubbleShooterScreen(
    navController: NavController,
    usuario: Usuario?
) {
    val scope = rememberCoroutineScope()
    val rankingService = remember { RankingService() }
    
    var gameState by remember { mutableStateOf(BubbleShooterGameState()) }
    var showRanking by remember { mutableStateOf(false) }
    var globalRanking by remember { mutableStateOf<List<RankingEntry>>(emptyList()) }
    var isLoadingRanking by remember { mutableStateOf(false) }
    var userPosition by remember { mutableStateOf(-1) }
    var isNewRecord by remember { mutableStateOf(false) }
    
    // Animación para el score
    val animatedScore by animateFloatAsState(
        targetValue = gameState.score.toFloat(),
        label = "score"
    )

    // Calcular trayectoria inicial y cuando cambie el ángulo
    LaunchedEffect(gameState.cannonAngle, gameState.bubbles) {
        val trajectory = calculateTrajectory(gameState.cannonAngle, gameState)
        gameState = gameState.copy(trajectory = trajectory)
    }

    // Cargar ranking al inicio
    LaunchedEffect(Unit) {
        isLoadingRanking = true
        try {
            globalRanking = rankingService.obtenerTopRanking("bubble_shooter", 3)
            if (usuario != null) {
                userPosition = rankingService.obtenerPosicionUsuario("bubble_shooter", usuario.email)
            }
        } catch (e: Exception) {
            println("Error al cargar ranking: ${e.message}")
        } finally {
            isLoadingRanking = false
        }
    }

    // Recargar ranking cada vez que se abre el menú de ranking
    LaunchedEffect(showRanking) {
        if (showRanking) {
            isLoadingRanking = true
            try {
                globalRanking = rankingService.obtenerTopRanking("bubble_shooter", 3)
                if (usuario != null) {
                    userPosition = rankingService.obtenerPosicionUsuario("bubble_shooter", usuario.email)
                }
            } catch (e: Exception) {
                println("Error al recargar ranking: ${e.message}")
            } finally {
                isLoadingRanking = false
            }
        }
    }

    // Función para guardar puntuación
    fun guardarPuntuacionSegura() {
        if (usuario != null && gameState.score > 0) {
            scope.launch {
                try {
                    println("DEBUG: Guardando puntuación Bubble Shooter: ${usuario.nombreCompleto}: ${gameState.score}")
                    rankingService.guardarPuntuacion(
                        juego = "bubble_shooter",
                        puntuacion = gameState.score,
                        nombreUsuario = usuario.nombreCompleto,
                        emailUsuario = usuario.email
                    )
                    
                    // Verificar si es un nuevo récord
                    val esNuevoRecord = rankingService.esNuevoRecord("bubble_shooter", gameState.score)
                    if (esNuevoRecord) {
                        isNewRecord = true
                    }
                    
                    // Actualizar ranking
                    globalRanking = rankingService.obtenerTopRanking("bubble_shooter", 3)
                    userPosition = rankingService.obtenerPosicionUsuario("bubble_shooter", usuario.email)
                } catch (e: Exception) {
                    println("Error al guardar puntuación: ${e.message}")
                }
            }
        }
    }

    // Función para reiniciar el juego
    fun reiniciarJuego() {
        gameState = BubbleShooterGameState()
        isNewRecord = false
    }

    // Función para disparar (ahora animada y con rebotes)
    fun disparar() {
        if (!gameState.isGameOver && !gameState.isPaused) {
            scope.launch {
                gameState = withContext(Dispatchers.Default) {
                    animateBubbleShot(gameState.cannonAngle, gameState) { updated ->
                        gameState = updated
                    }
                }
                if (gameState.isGameOver) {
                    guardarPuntuacionSegura()
                }
            }
        }
    }

    // Función para cambiar ángulo del cañón
    fun cambiarAngulo(delta: Float) {
        if (!gameState.isGameOver && !gameState.isPaused) {
            val newAngle = (gameState.cannonAngle + delta).coerceIn(-60f, 60f)
            gameState = gameState.copy(cannonAngle = newAngle)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bubble Shooter") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showRanking = !showRanking }) {
                        Icon(
                            painter = painterResource(id = R.drawable.logro),
                            contentDescription = "Ranking",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
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
                    .background(BUBBLE_BACKGROUND_COLOR)
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
                    // Header con información del juego
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Score
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Puntuación",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${animatedScore.toInt()}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Nivel
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Nivel",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "${gameState.level}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Burbujas acumuladas
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFF5722))
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Acumuladas",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = "${gameState.accumulatedBubbles.size}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Controles del juego
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Botón izquierda
                            Button(
                                onClick = { cambiarAngulo(-5f) },
                                enabled = !gameState.isGameOver && !gameState.isPaused,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("←")
                            }
                            
                            // Botón disparar
                            Button(
                                onClick = { disparar() },
                                enabled = !gameState.isGameOver && !gameState.isPaused,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Text("DISPARAR")
                            }
                            
                            // Botón derecha
                            Button(
                                onClick = { cambiarAngulo(5f) },
                                enabled = !gameState.isGameOver && !gameState.isPaused,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("→")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Controles adicionales
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { reiniciarJuego() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Reiniciar")
                            }
                            
                            Button(
                                onClick = { 
                                    gameState = gameState.copy(isPaused = !gameState.isPaused)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Text(if (gameState.isPaused) "Reanudar" else "Pausar")
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
                    // Tablero de burbujas
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                            .border(2.dp, Color(0xFF444444), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        // Dibujar burbujas
                        gameState.bubbles.forEach { bubble ->
                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = (bubble.x * 20).dp,
                                        y = (bubble.y * 20).dp
                                    )
                                    .size(20.dp)
                                    .background(bubble.color, RoundedCornerShape(50))
                                    .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(50))
                            )
                        }
                        
                        // Dibujar burbujas acumuladas
                        gameState.accumulatedBubbles.forEach { bubble ->
                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = (bubble.x * 20).dp,
                                        y = (bubble.y * 20).dp
                                    )
                                    .size(20.dp)
                                    .background(bubble.color, RoundedCornerShape(50))
                                    .border(2.dp, Color.Red, RoundedCornerShape(50))
                            )
                        }
                        
                        // Dibujar trayectoria
                        gameState.trajectory.forEach { (x, y) ->
                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = (x * 20).dp,
                                        y = (y * 20).dp
                                    )
                                    .size(4.dp)
                                    .background(Color.Yellow.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
                            )
                        }
                        
                        // Línea de game over
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .offset(y = (gameState.gameOverLine * 20).dp)
                                .background(Color.Red)
                        )
                        
                        // Área del cañón
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(60.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Burbuja actual
                            gameState.currentBubble?.let { bubble ->
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(bubble.color, RoundedCornerShape(50))
                                        .border(2.dp, Color.White, RoundedCornerShape(50))
                                )
                            }
                            
                            // Cañón
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(30.dp)
                                    .graphicsLayer(
                                        rotationZ = gameState.cannonAngle
                                    )
                                    .background(Color.Gray, RoundedCornerShape(4.dp))
                            )
                            
                            // Burbuja siguiente
                            gameState.nextBubble?.let { bubble ->
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(bubble.color, RoundedCornerShape(50))
                                        .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(50))
                                )
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
                    .background(BUBBLE_BACKGROUND_COLOR)
            ) {
                // Header con información del juego
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Score
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Puntuación",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${animatedScore.toInt()}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Nivel
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Nivel",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "${gameState.level}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Burbujas acumuladas
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF5722))
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Acumuladas",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                            Text(
                                text = "${gameState.accumulatedBubbles.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                
                // Área del juego
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    // Tablero de burbujas
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                            .border(2.dp, Color(0xFF444444), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        // Área de burbujas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            // Dibujar burbujas
                            gameState.bubbles.forEach { bubble ->
                                Box(
                                    modifier = Modifier
                                        .offset(
                                            x = (bubble.x * BUBBLE_SIZE.value).dp,
                                            y = (bubble.y * BUBBLE_SIZE.value).dp
                                        )
                                        .size(BUBBLE_SIZE)
                                        .background(bubble.color, RoundedCornerShape(50))
                                        .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(50))
                                )
                            }
                            
                            // Dibujar burbujas acumuladas
                            gameState.accumulatedBubbles.forEach { bubble ->
                                Box(
                                    modifier = Modifier
                                        .offset(
                                            x = (bubble.x * BUBBLE_SIZE.value).dp,
                                            y = (bubble.y * BUBBLE_SIZE.value).dp
                                        )
                                        .size(BUBBLE_SIZE)
                                        .background(bubble.color, RoundedCornerShape(50))
                                        .border(2.dp, Color.Red, RoundedCornerShape(50))
                                )
                            }
                            
                            // Dibujar trayectoria
                            gameState.trajectory.forEach { (x, y) ->
                                Box(
                                    modifier = Modifier
                                        .offset(
                                            x = (x * BUBBLE_SIZE.value).dp,
                                            y = (y * BUBBLE_SIZE.value).dp
                                        )
                                        .size(4.dp)
                                        .background(Color.Yellow.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
                                )
                            }
                            
                            // Línea de game over
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .offset(y = (gameState.gameOverLine * BUBBLE_SIZE.value).dp)
                                    .background(Color.Red)
                            )
                        }
                        
                        // Área del cañón
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Burbuja actual
                            gameState.currentBubble?.let { bubble ->
                                Box(
                                    modifier = Modifier
                                        .size(BUBBLE_SIZE)
                                        .background(bubble.color, RoundedCornerShape(50))
                                        .border(2.dp, Color.White, RoundedCornerShape(50))
                                )
                            }
                            
                            // Cañón
                            Box(
                                modifier = Modifier
                                    .width(8.dp)
                                    .height(40.dp)
                                    .graphicsLayer(
                                        rotationZ = gameState.cannonAngle
                                    )
                                    .background(Color.Gray, RoundedCornerShape(4.dp))
                            )
                            
                            // Burbuja siguiente
                            gameState.nextBubble?.let { bubble ->
                                Box(
                                    modifier = Modifier
                                        .size(BUBBLE_SIZE)
                                        .background(bubble.color, RoundedCornerShape(50))
                                        .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(50))
                                )
                            }
                        }
                    }
                    
                    // Controles del juego
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Botón izquierda
                        Button(
                            onClick = { cambiarAngulo(-5f) },
                            enabled = !gameState.isGameOver && !gameState.isPaused,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("←")
                        }
                        
                        // Botón disparar
                        Button(
                            onClick = { disparar() },
                            enabled = !gameState.isGameOver && !gameState.isPaused,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("DISPARAR")
                        }
                        
                        // Botón derecha
                        Button(
                            onClick = { cambiarAngulo(5f) },
                            enabled = !gameState.isGameOver && !gameState.isPaused,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("→")
                        }
                    }
                }
                
                // Controles adicionales
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { reiniciarJuego() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Reiniciar")
                    }
                    
                    Button(
                        onClick = { 
                            gameState = gameState.copy(isPaused = !gameState.isPaused)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text(if (gameState.isPaused) "Reanudar" else "Pausar")
                    }
                }
            }
        }
        
        // Overlay de game over
        if (gameState.isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¡GAME OVER!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Puntuación final: ${gameState.score}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        if (isNewRecord) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "¡NUEVO RÉCORD!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { reiniciarJuego() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Jugar de nuevo")
                        }
                    }
                }
            }
        }
        
        // Overlay de ranking
        if (showRanking) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .heightIn(max = 400.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ranking Bubble Shooter",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showRanking = false }) {
                                Text("✕", style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (isLoadingRanking) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else {
                            RankingCard(
                                rankings = globalRanking,
                                titulo = "🏆 Top 3 - Bubble Shooter",
                                isLoading = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            if (userPosition > 0) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Tu posición: #$userPosition",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 