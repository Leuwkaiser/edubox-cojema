package com.example.buzondesugerenciascojema.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.buzondesugerenciascojema.data.SugerenciaService
import com.example.buzondesugerenciascojema.model.Sugerencia
import com.example.buzondesugerenciascojema.ui.components.FloatingAssistantButton
import java.util.Date
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch
import com.example.buzondesugerenciascojema.R
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.border

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuzonScreen(
    sugerenciaService: SugerenciaService,
    grado: String,
    grupo: String,
    usuarioId: String,
    onBackPressed: () -> Unit = {}
) {
    var sugerencias by remember { mutableStateOf<List<Sugerencia>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var mostrarDialogoEditar by remember { mutableStateOf(false) }
    var sugerenciaSeleccionada by remember { mutableStateOf<Sugerencia?>(null) }
    var esAdmin by remember { mutableStateOf(false) }
    var titulo by remember { mutableStateOf("") }
    var contenido by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    // Función para recargar sugerencias
    fun recargarSugerencias() {
        scope.launch {
            try {
                isLoading = true
                error = null
                esAdmin = sugerenciaService.esAdmin(usuarioId)
                sugerencias = sugerenciaService.obtenerSugerencias(usuarioId, grado, grupo)
            } catch (e: Exception) {
                error = "Error al cargar sugerencias: ${e.message}"
                println("Error al cargar sugerencias: ${e.message}")
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // Cargar sugerencias al iniciar
    LaunchedEffect(usuarioId, grado, grupo) {
        recargarSugerencias()
    }

    // Fondo decorativo moderno
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6C63FF), // violeta
                        Color(0xFF42A5F5)  // azul
                    )
                )
            )
    ) {
        // Partículas decorativas
        val particleCount = 7
        val infiniteTransition = rememberInfiniteTransition(label = "particles")
        val particleOffsets = List(particleCount) { index ->
            infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 40f + index * 10,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000 + index * 300, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "offset$index"
            )
        }
        val particleScales = List(particleCount) { index ->
            infiniteTransition.animateFloat(
                initialValue = 0.7f,
                targetValue = 1.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3500 + index * 200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "scale$index"
            )
        }
        particleOffsets.forEachIndexed { index, offsetAnim ->
            val scale = particleScales[index].value
            val offset = offsetAnim.value
            Box(
                modifier = Modifier
                    .size((12 + index * 2).dp)
                    .scale(scale)
                    .graphicsLayer {
                        translationX = (offset * kotlin.math.cos(index * 50f * Math.PI / 180f)).toFloat()
                        translationY = (offset * kotlin.math.sin(index * 50f * Math.PI / 180f)).toFloat()
                        alpha = 0.18f + 0.08f * index
                    }
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
                    .align(Alignment.TopStart)
            )
        }
        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 32.dp)
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TopAppBar con botón de regreso
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            // Título animado
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(800)) + scaleIn(initialScale = 0.8f, animationSpec = tween(800)),
                exit = fadeOut() + scaleOut()
            ) {
                Text(
                    text = if (esAdmin) "Panel de Administración" else "Buzón de Sugerencias",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            // Lista de sugerencias con animación
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(initialOffsetY = { it }, animationSpec = tween(1000)),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }, animationSpec = tween(1000))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(sugerencias) { sugerencia ->
                            SugerenciaCardModern(
                                sugerencia = sugerencia,
                                usuarioId = usuarioId,
                                esAdmin = esAdmin,
                                onEditar = {
                                    sugerenciaSeleccionada = sugerencia
                                    mostrarDialogoEditar = true
                                },
                                onEliminar = {
                                    scope.launch {
                                        try {
                                            if (sugerenciaService.borrarSugerencia(sugerencia.id, usuarioId)) {
                                                recargarSugerencias()
                                            } else {
                                                error = "No tienes permiso para eliminar esta sugerencia"
                                            }
                                        } catch (e: Exception) {
                                            error = "Error al eliminar sugerencia: ${e.message}"
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        // FAB animado
        val fabScale by remember { mutableStateOf(1f) }
        FloatingActionButton(
            onClick = { mostrarDialogoCrear = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .scale(fabScale),
            containerColor = Color(0xFF6C63FF),
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Crear sugerencia")
        }
    }

    if (mostrarDialogoEditar && sugerenciaSeleccionada != null) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoEditar = false
                sugerenciaSeleccionada = null
                titulo = ""
                contenido = ""
            },
            title = { Text("Editar Sugerencia", color = Color(0xFF6C63FF), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        label = { Text("Título") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = titulo.length !in 5..100
                    )
                    if (titulo.isNotEmpty() && titulo.length !in 5..100) {
                        Text("El título debe tener entre 5 y 100 caracteres", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contenido,
                        onValueChange = { contenido = it },
                        label = { Text("Contenido") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        isError = contenido.length !in 20..1000
                    )
                    if (contenido.isNotEmpty() && contenido.length !in 20..1000) {
                        Text("El contenido debe tener entre 20 y 1000 caracteres", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF6C63FF).copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, Color(0xFF6C63FF))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "📋 Reglas para sugerencias:",
                                color = Color(0xFF6C63FF),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text("• Título: 5-100 caracteres", color = Color(0xFF6C63FF), style = MaterialTheme.typography.bodySmall)
                            Text("• Contenido: 20-1000 caracteres", color = Color(0xFF6C63FF), style = MaterialTheme.typography.bodySmall)
                            Text("• No usar lenguaje inapropiado", color = Color(0xFF6C63FF), style = MaterialTheme.typography.bodySmall)
                            Text("• Ser respetuoso y constructivo", color = Color(0xFF6C63FF), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (titulo.length in 5..100 && contenido.length in 20..1000) {
                            scope.launch {
                                try {
                                    if (sugerenciaService.editarSugerencia(
                                            sugerenciaSeleccionada!!.id,
                                            titulo,
                                            contenido,
                                            usuarioId
                                        )) {
                                        mostrarDialogoEditar = false
                                        sugerenciaSeleccionada = null
                                        titulo = ""
                                        contenido = ""
                                        recargarSugerencias()
                                    } else {
                                        error = "No tienes permiso para editar esta sugerencia"
                                    }
                                } catch (e: Exception) {
                                    error = "Error al editar sugerencia: ${e.message}"
                                }
                            }
                        }
                    },
                    enabled = titulo.length in 5..100 && contenido.length in 20..1000,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                ) {
                    Text("Guardar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoEditar = false
                        sugerenciaSeleccionada = null
                        titulo = ""
                        contenido = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarDialogoCrear) {
        var errorValidacion by remember { mutableStateOf<String?>(null) }
        var mostrarErrorValidacion by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoCrear = false
                titulo = ""
                contenido = ""
                errorValidacion = null
                mostrarErrorValidacion = false
            },
            title = { Text("Crear Sugerencia", color = Color(0xFF6C63FF), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = {
                            titulo = it
                            errorValidacion = null
                            mostrarErrorValidacion = false
                        },
                        label = { Text("Título") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = mostrarErrorValidacion && (titulo.length !in 5..100)
                    )
                    if (mostrarErrorValidacion && titulo.length !in 5..100) {
                        Text("El título debe tener entre 5 y 100 caracteres", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contenido,
                        onValueChange = {
                            contenido = it
                            errorValidacion = null
                            mostrarErrorValidacion = false
                        },
                        label = { Text("Contenido") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        isError = mostrarErrorValidacion && (contenido.length !in 20..1000)
                    )
                    if (mostrarErrorValidacion && contenido.length !in 20..1000) {
                        Text("El contenido debe tener entre 20 y 1000 caracteres", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF6C63FF).copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, Color(0xFF6C63FF))
                    ) {
                        // Cartel de reglas eliminado
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val validacion = sugerenciaService.validarSugerencia(titulo, contenido)
                                if (!validacion.esValida) {
                                    errorValidacion = validacion.motivo
                                    mostrarErrorValidacion = true
                                    return@launch
                                }
                                sugerenciaService.crearSugerencia(
                                    Sugerencia(
                                        id = UUID.randomUUID().toString(),
                                        titulo = titulo,
                                        contenido = contenido,
                                        autorId = usuarioId,
                                        autorNombre = "",
                                        grado = grado,
                                        grupo = grupo,
                                        fecha = Date(),
                                        estado = "Pendiente"
                                    )
                                )
                                mostrarDialogoCrear = false
                                titulo = ""
                                contenido = ""
                                errorValidacion = null
                                mostrarErrorValidacion = false
                                recargarSugerencias()
                            } catch (e: Exception) {
                                errorValidacion = "Error al crear sugerencia: ${e.message}"
                                mostrarErrorValidacion = true
                            }
                        }
                    },
                    enabled = true,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                ) {
                    Text("Crear", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoCrear = false
                        titulo = ""
                        contenido = ""
                        errorValidacion = null
                        mostrarErrorValidacion = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Tarjeta de sugerencia rediseñada
@Composable
fun SugerenciaCardModern(
    sugerencia: Sugerencia,
    usuarioId: String,
    esAdmin: Boolean,
    onEditar: (Sugerencia) -> Unit,
    onEliminar: () -> Unit
) {
    var mostrarMenu by remember { mutableStateOf(false) }
    var mostrarMensaje by remember { mutableStateOf(false) }
    var mostrarComentarios by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val transition = updateTransition(targetState = sugerencia.estado, label = "estadoTrans")
    val estadoColor by transition.animateColor(label = "estadoColor") { estado ->
        when (estado) {
            "Aprobada" -> Color(0xFF43A047)
            "Rechazada" -> Color(0xFFE53935)
            else -> Color(0xFF757575)
        }
    }
    val cardElevation by transition.animateDp(label = "elev") { estado ->
        when (estado) {
            "Aprobada" -> 10.dp
            "Rechazada" -> 10.dp
            else -> 4.dp
        }
    }
    // Avatar animado
    val avatarColor by transition.animateColor(label = "avatarColor") { estado ->
        when (estado) {
            "Aprobada" -> Color(0xFFC8E6C9)
            "Rechazada" -> Color(0xFFFFCDD2)
            else -> Color(0xFFE3E3E3)
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                shadowElevation = cardElevation.toPx()
            },
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar animado
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(avatarColor)
                            .border(2.dp, estadoColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (sugerencia.autorId == usuarioId) "Y" else sugerencia.autorNombre.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleMedium,
                            color = estadoColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(
                            text = if (sugerencia.autorId == usuarioId) "Yo" else sugerencia.autorNombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF222222)
                        )
                        Text(
                            text = formatearFechaBonita(sugerencia.fecha),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF757575)
                        )
                    }
                }
                // Chip de estado
                Surface(
                    color = estadoColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, estadoColor),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = when (sugerencia.estado) {
                                "Aprobada" -> Icons.Default.Check
                                "Rechazada" -> Icons.Default.Close
                                else -> Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = estadoColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = sugerencia.estado,
                            style = MaterialTheme.typography.labelSmall,
                            color = estadoColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                // Menú de opciones
                if (esAdmin || sugerencia.autorId == usuarioId) {
                    Box(
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        IconButton(onClick = { mostrarMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                        }
                        DropdownMenu(
                            expanded = mostrarMenu,
                            onDismissRequest = { mostrarMenu = false }
                        ) {
                            if (sugerencia.autorId == usuarioId && sugerencia.estado == "Pendiente") {
                                DropdownMenuItem(
                                    text = { Text("Editar") },
                                    onClick = {
                                        mostrarMenu = false
                                        onEditar(sugerencia)
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Eliminar") },
                                onClick = {
                                    mostrarMenu = false
                                    onEliminar()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = sugerencia.titulo,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF222222),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = sugerencia.contenido,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF444444)
            )
            // Feedback visual de estado
            AnimatedVisibility(
                visible = mostrarMensaje,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = estadoColor.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val scale by animateFloatAsState(
                            targetValue = if (mostrarMensaje) 1.2f else 1f,
                            animationSpec = repeatable(
                                iterations = 3,
                                animation = tween(500)
                            )
                        )
                        Icon(
                            imageVector = when (sugerencia.estado) {
                                "Aprobada" -> Icons.Default.Check
                                "Rechazada" -> Icons.Default.Close
                                else -> Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                                .scale(scale)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (sugerencia.estado) {
                                "Aprobada" -> "✅ ¡Tu sugerencia ha sido aprobada!"
                                "Rechazada" -> "❌ Tu sugerencia ha sido rechazada"
                                else -> "Estado: ${sugerencia.estado}"
                            },
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            // Botón de comentarios con badge
            val hayComentarios = sugerencia.comentarios.isNotEmpty()
            val esAutor = sugerencia.autorId == usuarioId
            val comentarioNoLeido = esAutor && hayComentarios && !sugerencia.comentariosLeidosPor.contains(usuarioId)
            if ((esAutor || esAdmin) && hayComentarios) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            mostrarComentarios = true
                            if (esAutor && !sugerencia.comentariosLeidosPor.contains(usuarioId)) {
                                scope.launch {
                                    com.example.buzondesugerenciascojema.data.SugerenciaService().marcarComentariosComoLeidos(sugerencia.id, usuarioId)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                    ) {
                        Text("Ver comentarios del administrador", color = Color.White)
                        if (comentarioNoLeido) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color.Blue, shape = CircleShape)
                            )
                        }
                    }
                }
            }
            if (mostrarComentarios) {
                AlertDialog(
                    onDismissRequest = { mostrarComentarios = false },
                    title = { Text("Comentarios del administrador") },
                    text = {
                        Column {
                            sugerencia.comentarios.forEach { comentario ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                                ) {
                                    Column(Modifier.padding(8.dp)) {
                                        Text(comentario.toString(), color = Color(0xFF222222))
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { mostrarComentarios = false }) {
                            Text("Cerrar")
                        }
                    }
                )
            }
        }
    }
    // Animación de feedback de estado
    LaunchedEffect(sugerencia.estado) {
        if (sugerencia.estado != "Pendiente") {
            mostrarMensaje = true
            kotlinx.coroutines.delay(3000)
            mostrarMensaje = false
        }
    }
}

// Función para formatear la fecha de manera bonita
fun formatearFechaBonita(fecha: Date): String {
    val ahora = Date()
    val diferencia = ahora.time - fecha.time
    val segundos = diferencia / 1000
    val minutos = segundos / 60
    val horas = minutos / 60
    val dias = horas / 24

    return when {
        segundos < 60 -> "Hace un momento"
        minutos < 60 -> "Hace ${minutos} min"
        horas < 24 -> "Hace ${horas} h"
        dias == 1L -> "Ayer"
        dias < 7 -> "Hace ${dias} días"
        else -> {
            val formatter = SimpleDateFormat("dd 'de' MMMM", Locale("es", "ES"))
            formatter.format(fecha)
        }
    }
} 