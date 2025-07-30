package com.example.buzondesugerenciascojema.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.buzondesugerenciascojema.data.SugerenciaService
import com.example.buzondesugerenciascojema.model.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    navController: NavController,
    sugerenciaService: SugerenciaService,
    grado: String,
    grupo: String,
    usuarioId: String
) {
    var sugerencias by remember { mutableStateOf<List<Sugerencia>>(emptyList()) }
    var estadisticas by remember { mutableStateOf(SugerenciaEstadisticas()) }
    var reporteProblematico by remember { mutableStateOf(ReporteContenidoProblematico()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var mostrarEstadisticas by remember { mutableStateOf(false) }
    var mostrarReporte by remember { mutableStateOf(false) }
    var mostrarDialogoComentario by remember { mutableStateOf(false) }
    var sugerenciaSeleccionada by remember { mutableStateOf<Sugerencia?>(null) }
    var accionSeleccionada by remember { mutableStateOf<String?>(null) }
    var comentario by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Función para cargar datos
    suspend fun cargarDatos() {
        try {
            isLoading = true
            error = null
            
            // Cargar sugerencias
            sugerencias = sugerenciaService.obtenerSugerencias(usuarioId, grado, grupo)
            
            // Cargar estadísticas
            estadisticas = sugerenciaService.obtenerEstadisticasSugerencias(grado, grupo)
            
            // Generar reporte de contenido problemático
            reporteProblematico = sugerenciaService.generarReporteContenidoProblematico(grado, grupo)
            
        } catch (e: Exception) {
            error = "Error al cargar datos: ${e.message}"
            println("Error al cargar datos: ${e.message}")
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    // Cargar datos al iniciar
    LaunchedEffect(usuarioId, grado, grupo) {
        scope.launch {
            cargarDatos()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administración") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        scope.launch {
                            cargarDatos()
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (error != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Red
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = error!!,
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { 
                        scope.launch {
                            cargarDatos()
                        }
                    }) {
                        Text("Reintentar")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sección de estadísticas
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.Blue.copy(alpha = 0.1f))
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
                                        text = "📊 Estadísticas de Sugerencias",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(onClick = { mostrarEstadisticas = !mostrarEstadisticas }) {
                                        Icon(
                                            imageVector = if (mostrarEstadisticas) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Expandir/Contraer"
                                        )
                                    }
                                }
                                
                                if (mostrarEstadisticas) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        EstadisticaCard(
                                            titulo = "Total",
                                            valor = estadisticas.total.toString(),
                                            color = Color.Blue
                                        )
                                        EstadisticaCard(
                                            titulo = "Aprobadas",
                                            valor = estadisticas.aprobadas.toString(),
                                            color = Color.Green
                                        )
                                        EstadisticaCard(
                                            titulo = "Rechazadas",
                                            valor = estadisticas.rechazadas.toString(),
                                            color = Color.Red
                                        )
                                        EstadisticaCard(
                                            titulo = "Pendientes",
                                            valor = estadisticas.pendientes.toString(),
                                            color = Color.Yellow
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    LinearProgressIndicator(
                                        progress = { estadisticas.porcentajeAprobacion / 100f },
                                        modifier = Modifier.fillMaxWidth(),
                                        color = Color.Green,
                                        trackColor = Color.Gray.copy(alpha = 0.3f)
                                    )
                                    Text(
                                        text = "Porcentaje de aprobación: ${String.format("%.1f", estadisticas.porcentajeAprobacion)}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }



                    // Sección de reporte de contenido problemático
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
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
                                        text = "🚨 Reporte de Contenido Problemático",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(onClick = { mostrarReporte = !mostrarReporte }) {
                                        Icon(
                                            imageVector = if (mostrarReporte) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Expandir/Contraer"
                                        )
                                    }
                                }
                                
                                if (mostrarReporte) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Text(
                                        text = "Total de sugerencias rechazadas: ${reporteProblematico.totalSugerenciasRechazadas}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    if (reporteProblematico.topAutoresProblematicos.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "👥 Autores con más sugerencias rechazadas:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        reporteProblematico.topAutoresProblematicos.forEach { autor ->
                                            Text(
                                                text = "• $autor",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                            )
                                        }
                                    }
                                    
                                    if (reporteProblematico.topMotivosRechazo.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "📋 Motivos más comunes de rechazo:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        reporteProblematico.topMotivosRechazo.forEach { motivo ->
                                            Text(
                                                text = "• $motivo",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Generado el: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(reporteProblematico.fechaGeneracion)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    // Lista de sugerencias pendientes
                    items(sugerencias.filter { it.estado == "Pendiente" }) { sugerencia ->
                        SugerenciaAdminCard(
                            sugerencia = sugerencia,
                            onAprobar = {
                                sugerenciaSeleccionada = sugerencia
                                accionSeleccionada = "Aprobada"
                                comentario = ""
                                mostrarDialogoComentario = true
                            },
                            onRechazar = {
                                sugerenciaSeleccionada = sugerencia
                                accionSeleccionada = "Rechazada"
                                comentario = ""
                                mostrarDialogoComentario = true
                            }
                        )
                    }
                }
            }
        }
        
        // Diálogo para comentarios
        if (mostrarDialogoComentario && sugerenciaSeleccionada != null && accionSeleccionada != null) {
            AlertDialog(
                onDismissRequest = { 
                    mostrarDialogoComentario = false
                    sugerenciaSeleccionada = null
                    accionSeleccionada = null
                    comentario = ""
                },
                title = {
                    Text(
                        text = if (accionSeleccionada == "Aprobada") "✅ Aprobar Sugerencia" else "❌ Rechazar Sugerencia"
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Sugerencia: ${sugerenciaSeleccionada?.titulo}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (accionSeleccionada == "Aprobada") 
                                "¿Qué propone para llevar a cabo esta sugerencia? (opcional)"
                            else 
                                "¿Por qué rechaza esta sugerencia? (opcional)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = comentario,
                            onValueChange = { comentario = it },
                            label = { 
                                Text(
                                    if (accionSeleccionada == "Aprobada") 
                                        "Comentario (opcional)" 
                                    else 
                                        "Motivo del rechazo (opcional)"
                                ) 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    sugerenciaService.actualizarEstadoSugerencia(
                                        sugerenciaSeleccionada!!.id, 
                                        accionSeleccionada!!, 
                                        comentario
                                    )
                                    cargarDatos()
                                    mostrarDialogoComentario = false
                                    sugerenciaSeleccionada = null
                                    accionSeleccionada = null
                                    comentario = ""
                                } catch (e: Exception) {
                                    error = "Error al ${accionSeleccionada?.lowercase()} sugerencia: ${e.message}"
                                }
                            }
                        }
                    ) {
                        Text(if (accionSeleccionada == "Aprobada") "Aprobar" else "Rechazar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            mostrarDialogoComentario = false
                            sugerenciaSeleccionada = null
                            accionSeleccionada = null
                            comentario = ""
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun EstadisticaCard(
    titulo: String,
    valor: String,
    color: Color
) {
    Card(
        modifier = Modifier.size(80.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.2f)),
        border = BorderStroke(1.dp, color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SugerenciaAdminCard(
    sugerencia: Sugerencia,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Yellow.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, Color.Yellow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sugerencia.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Por: ${sugerencia.autorNombre}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(sugerencia.fecha),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                Row {
                    IconButton(
                        onClick = onAprobar,
                        modifier = Modifier
                            .background(Color.Green.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Aprobar",
                            tint = Color.Green
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = onRechazar,
                        modifier = Modifier
                            .background(Color.Red.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Rechazar",
                            tint = Color.Red
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = sugerencia.contenido,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
} 