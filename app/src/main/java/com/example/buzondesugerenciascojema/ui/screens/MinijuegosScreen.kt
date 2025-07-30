package com.example.buzondesugerenciascojema.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.buzondesugerenciascojema.R
import com.example.buzondesugerenciascojema.data.Usuario
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.animation.core.*
import com.example.buzondesugerenciascojema.util.MusicPlayer
import com.example.buzondesugerenciascojema.util.SoundGenerator
import androidx.compose.ui.geometry.CornerRadius

data class Minijuego(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val imagenResId: Int,
    val ruta: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinijuegosScreen(
    navController: NavController,
    usuario: Usuario?
) {
    // Música de fondo para el menú de minijuegos
    val context = androidx.compose.ui.platform.LocalContext.current
    val musicPlayer = remember { MusicPlayer(context) }
    val soundGenerator = remember { SoundGenerator(context) }
    
    // Estado para controlar el sonido
    var isSoundEnabled by remember { mutableStateOf(true) }
    
    // Iniciar música cuando se entra a la pantalla
    LaunchedEffect(Unit) {
        if (isSoundEnabled) {
            musicPlayer.playMenuMusic()
        }
    }
    
    // Detener música cuando se sale de la pantalla
    DisposableEffect(Unit) {
        onDispose {
            musicPlayer.stopMusic()
        }
    }
    val minijuegos = listOf(
        Minijuego(
            id = "snake",
            nombre = "Snake",
            descripcion = "Controla la serpiente y come para crecer. ¡No te choques contigo misma!",
            imagenResId = R.drawable.snakenew,
            ruta = "snake"
        ),
        Minijuego(
            id = "tetris",
            nombre = "Tetris",
            descripcion = "Organiza las piezas para formar líneas completas. ¡El clásico juego de bloques!",
            imagenResId = R.drawable.tetris,
            ruta = "tetris"
        ),
        Minijuego(
            id = "space_invaders",
            nombre = "Space Invaders",
            descripcion = "Defiende la Tierra de los invasores alienígenas. ¡Dispara y sobrevive!",
            imagenResId = R.drawable.space,
            ruta = "space_invaders"
        ),
        Minijuego(
            id = "tres_en_raya",
            nombre = "Tres en Raya",
            descripcion = "Juega al clásico tres en raya contra un bot muy difícil de vencer.",
            imagenResId = -1, // Usar -1 para indicar que se usará un vector personalizado
            ruta = "tres_en_raya"
        ),
        Minijuego(
            id = "buscaminas",
            nombre = "Buscaminas",
            descripcion = "Encuentra todas las minas sin explotar. ¡Usa la lógica y las banderas!",
            imagenResId = -3, // Usar -3 para indicar que se usará un vector personalizado
            ruta = "buscaminas"
        ),
        Minijuego(
            id = "pong",
            nombre = "Pong",
            descripcion = "El clásico tenis de mesa. ¡Desliza para mover tu paleta y gana!",
            imagenResId = -4, // Usar -4 para indicar que se usará un vector personalizado
            ruta = "pong"
        ),
        Minijuego(
            id = "trivia",
            nombre = "Trivia",
            descripcion = "Responde preguntas de todas las asignaturas de tu grado a contrarreloj y compite en el ranking.",
            imagenResId = R.drawable.trivia_icon,
            ruta = "trivia"
        ),
        // Minijuego(
        //     id = "dino_runner",
        //     nombre = "Dino Runner",
        //     descripcion = "Corre, salta y esquiva obstáculos como el dinosaurio de Google. ¡Compite en el ranking!",
        //     imagenResId = -10, // Vector personalizado
        //     ruta = "dino_runner"
        // ),
        // Minijuego(
        //     id = "skeleton_survival",
        //     nombre = "Skeleton Survival",
        //     descripcion = "¡Sobrevive a las hordas de esqueletos! Combate con armas progresivas en este shooter arcade.",
        //     imagenResId = -5, // Usar -5 para indicar que se usará un vector personalizado
        //     ruta = "skeleton_survival"
        // )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Minijuegos", 
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
                            painter = painterResource(id = R.drawable.gamepad),
                            contentDescription = "Volver",
                            modifier = Modifier.size(28.dp),
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isSoundEnabled = !isSoundEnabled
                            if (isSoundEnabled) {
                                musicPlayer.playMenuMusic()
                            } else {
                                musicPlayer.stopMusic()
                            }
                        }
                    ) {
                        SoundControlIcon(isEnabled = isSoundEnabled)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6C63FF))
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedMinijuegosBackground()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header con descripción
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎮 Zona de Juegos",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6C63FF)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Diviértete con nuestros minijuegos educativos",
                            fontSize = 16.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Cuadrícula de juegos
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(minijuegos) { juego ->
                        GameCardGrid(
                            juego = juego,
                            onClick = {
                                if (isSoundEnabled) soundGenerator.playClick()
                                navController.navigate(juego.ruta)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameCardGrid(
    juego: Minijuego,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            if (juego.imagenResId > 0) {
                Image(
                    painter = painterResource(id = juego.imagenResId),
                    contentDescription = juego.nombre,
                    modifier = Modifier.size(90.dp),
                    contentScale = ContentScale.Fit
                )
            } else if (juego.imagenResId == -10) {
                // Imagen personalizada para Dino Runner estilo Google Chrome
                Canvas(modifier = Modifier.size(90.dp)) {
                    val baseX = 15f
                    val baseY = 30f
                    val scale = 1.0f
                    val gray = Color(0xFF888888)
                    val darkGray = Color(0xFF444444)
                    // Cuerpo (rectángulo principal)
                    drawRect(
                        color = gray,
                        topLeft = Offset(baseX + 10f * scale, baseY + 20f * scale),
                        size = androidx.compose.ui.geometry.Size(40f * scale, 25f * scale)
                    )
                    // Cabeza (cuadrada)
                    drawRect(
                        color = gray,
                        topLeft = Offset(baseX + 40f * scale, baseY + 10f * scale),
                        size = androidx.compose.ui.geometry.Size(18f * scale, 18f * scale)
                    )
                    // Hocico
                    drawRect(
                        color = gray,
                        topLeft = Offset(baseX + 56f * scale, baseY + 18f * scale),
                        size = androidx.compose.ui.geometry.Size(8f * scale, 8f * scale)
                    )
                    // Ojo
                    drawRect(
                        color = darkGray,
                        topLeft = Offset(baseX + 53f * scale, baseY + 15f * scale),
                        size = androidx.compose.ui.geometry.Size(3f * scale, 3f * scale)
                    )
                    // Patas delanteras
                    drawRect(
                        color = gray,
                        topLeft = Offset(baseX + 15f * scale, baseY + 45f * scale),
                        size = androidx.compose.ui.geometry.Size(5f * scale, 10f * scale)
                    )
                    drawRect(
                        color = gray,
                        topLeft = Offset(baseX + 25f * scale, baseY + 45f * scale),
                        size = androidx.compose.ui.geometry.Size(5f * scale, 10f * scale)
                    )
                    // Patas traseras
                    drawRect(
                        color = gray,
                        topLeft = Offset(baseX + 40f * scale, baseY + 45f * scale),
                        size = androidx.compose.ui.geometry.Size(5f * scale, 10f * scale)
                    )
                    drawRect(
                        color = gray,
                        topLeft = Offset(baseX + 50f * scale, baseY + 45f * scale),
                        size = androidx.compose.ui.geometry.Size(5f * scale, 10f * scale)
                    )
                    // Cola (en ángulo, pixel-art)
                    drawRect(
                        color = gray,
                        topLeft = Offset(baseX + 5f * scale, baseY + 35f * scale),
                        size = androidx.compose.ui.geometry.Size(8f * scale, 5f * scale)
                    )
                    drawRect(
                        color = gray,
                        topLeft = Offset(baseX + 2f * scale, baseY + 30f * scale),
                        size = androidx.compose.ui.geometry.Size(6f * scale, 5f * scale)
                    )
                }
            } else {
                // Otros vectores personalizados...
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.8f))
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(juego.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(juego.descripcion, fontSize = 12.sp, color = Color.DarkGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun AnimatedMinijuegosBackground() {
    // Animaciones más calmadas y suaves
    val infiniteTransition = rememberInfiniteTransition()
    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 50f, // Movimiento más sutil
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing), // Más lento
            repeatMode = RepeatMode.Reverse
        )
    )
    val offset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -40f, // Movimiento más sutil
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing), // Más lento
            repeatMode = RepeatMode.Reverse
        )
    )
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f, // Más tenue
        targetValue = 0.6f, // Menos intenso
        animationSpec = infiniteRepeatable(
            animation = tween(10000), // Más lento
            repeatMode = RepeatMode.Reverse
        )
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.2f, // Más tenue
        targetValue = 0.4f, // Menos intenso
        animationSpec = infiniteRepeatable(
            animation = tween(14000), // Más lento
            repeatMode = RepeatMode.Reverse
        )
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Fondo degradado más suave
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF8B7EC8), // violeta más suave
                    Color(0xFF7BA7D1), // azul más suave
                    Color(0xFFC4A8E8)  // acento violeta más suave
                ),
                startY = 0f,
                endY = size.height
            ),
            size = size
        )
        // Círculo animado 1 - más suave
        drawCircle(
            color = Color(0xFF7BC8D4).copy(alpha = alpha1), // Color más suave
            radius = size.minDimension / 3f, // Más pequeño
            center = androidx.compose.ui.geometry.Offset(size.width * 0.2f + offset1, size.height * 0.25f)
        )
        // Círculo animado 2 - más suave
        drawCircle(
            color = Color(0xFFD4B8E8).copy(alpha = alpha2), // Color más suave
            radius = size.minDimension / 4f, // Más pequeño
            center = androidx.compose.ui.geometry.Offset(size.width * 0.8f + offset2, size.height * 0.7f)
        )
        // Blob/onda decorativa - más suave
        drawOval(
            color = Color(0xFF9BC7E8).copy(alpha = 0.12f), // Más tenue
            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.6f + offset2),
            size = androidx.compose.ui.geometry.Size(size.width * 0.8f, size.height * 0.25f)
        )
    }
}

@Composable
fun SoundControlIcon(isEnabled: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isEnabled) 1.1f else 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(24.dp)
        ) {
            if (isEnabled) {
                // Icono de sonido activado - versión simplificada
                // Altavoz
                drawPath(
                    path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(size.width * 0.3f, size.height * 0.3f)
                        lineTo(size.width * 0.3f, size.height * 0.7f)
                        lineTo(size.width * 0.5f, size.height * 0.6f)
                        lineTo(size.width * 0.7f, size.height * 0.8f)
                        lineTo(size.width * 0.7f, size.height * 0.2f)
                        lineTo(size.width * 0.5f, size.height * 0.4f)
                        close()
                    },
                    color = Color.White,
                    style = Fill
                )
                // Ondas de sonido (simplificadas)
                drawLine(
                    color = Color.White,
                    start = Offset(size.width * 0.7f, size.height * 0.5f),
                    end = Offset(size.width * 0.85f, size.height * 0.4f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color.White,
                    start = Offset(size.width * 0.7f, size.height * 0.5f),
                    end = Offset(size.width * 0.85f, size.height * 0.6f),
                    strokeWidth = 2f
                )

            } else {
                // Icono de sonido desactivado - versión simplificada
                // Altavoz tachado
                drawPath(
                    path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(size.width * 0.3f, size.height * 0.3f)
                        lineTo(size.width * 0.3f, size.height * 0.7f)
                        lineTo(size.width * 0.5f, size.height * 0.6f)
                        lineTo(size.width * 0.7f, size.height * 0.8f)
                        lineTo(size.width * 0.7f, size.height * 0.2f)
                        lineTo(size.width * 0.5f, size.height * 0.4f)
                        close()
                    },
                    color = Color.White.copy(alpha = 0.5f),
                    style = Fill
                )
                // Línea de tachado
                drawLine(
                    color = Color.White,
                    start = Offset(size.width * 0.2f, size.height * 0.2f),
                    end = Offset(size.width * 0.8f, size.height * 0.8f),
                    strokeWidth = 2f
                )
            }
        }
    }
} 