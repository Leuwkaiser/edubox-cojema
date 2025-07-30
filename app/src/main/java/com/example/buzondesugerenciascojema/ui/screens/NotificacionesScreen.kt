package com.example.buzondesugerenciascojema.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.buzondesugerenciascojema.R
import com.example.buzondesugerenciascojema.data.Notificacion
import com.example.buzondesugerenciascojema.data.TipoNotificacion
import com.example.buzondesugerenciascojema.data.UsuarioManager
import com.example.buzondesugerenciascojema.viewmodels.NotificacionViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    navController: NavController,
    usuarioManager: UsuarioManager,
    viewModel: NotificacionViewModel
) {
    val currentUser by usuarioManager.currentUser.collectAsState()
    val email = currentUser?.email
    
    val notificaciones by viewModel.notificaciones.collectAsState()
    val cantidadNoLeidas by viewModel.cantidadNoLeidas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf<Notificacion?>(null) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(email) {
        Log.d("NotificacionesScreen", "[DEBUG] Entrando a NotificacionesScreen con email: '$email'")
        if (email != null) {
            viewModel.cargarNotificaciones(email)
            // Actualizar el contador de no leídas
            viewModel.actualizarContadorNoLeidas(email)
            // Marcar todas como leídas automáticamente
            viewModel.marcarTodasComoLeidas(email)
        }
    }
    
    // Recargar notificaciones cuando se regrese a esta pantalla
    LaunchedEffect(Unit) {
        email?.let { userEmail ->
            viewModel.cargarNotificaciones(userEmail)
            viewModel.actualizarContadorNoLeidas(userEmail)
        }
    }

    Scaffold(
        floatingActionButton = {
            // Eliminar el FloatingActionButton de eliminar todas las notificaciones
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.notificacion),
                            contentDescription = "Notificaciones",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Notificaciones",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (cantidadNoLeidas > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error
                            ) {
                                Text(
                                    text = cantidadNoLeidas.toString(),
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    // Solo dejar el botón de recargar notificaciones
                    if (email != null) {
                        IconButton(
                            onClick = { 
                                viewModel.cargarNotificaciones(email)
                                viewModel.actualizarContadorNoLeidas(email)
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.refrescar),
                                contentDescription = "Recargar notificaciones"
                            )
                        }
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
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                notificaciones.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.notificacion),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "No hay notificaciones",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Cuando tengas notificaciones, aparecerán aquí",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        // Debug info
                        if (email != null) {
                            Text(
                                text = "Email: $email",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                            Text(
                                text = "Contador no leídas: $cantidadNoLeidas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                            Text(
                                text = "Total notificaciones: ${notificaciones.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                            Text(
                                text = "Estado de carga: $isLoading",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notificaciones) { notificacion ->
                            NotificacionItem(
                                notificacion = notificacion,
                                onNotificacionClick = {
                                    if (!notificacion.leida) {
                                        viewModel.marcarComoLeida(notificacion.id, email ?: "")
                                        // Recargar notificaciones después de marcar como leída
                                        viewModel.cargarNotificaciones(email ?: "")
                                    }
                                },
                                onDeleteClick = {
                                    showDeleteDialog = notificacion
                                }
                            )
                        }
                    }
                }
            }

            // Snackbar para errores
            error?.let { errorMessage ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.limpiarError() }) {
                            Text("Cerrar")
                        }
                    }
                ) {
                    Text(errorMessage)
                }
            }
            
            // Snackbar para mensajes de éxito
            successMessage?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    action = {
                        TextButton(onClick = { viewModel.limpiarMensajeExito() }) {
                            Text("Cerrar", color = Color.White)
                        }
                    }
                ) {
                    Text(message, color = Color.White)
                }
            }
        }
    }

    // Dialog de confirmación para eliminar notificación individual
    showDeleteDialog?.let { notificacion ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar notificación") },
            text = { Text("¿Estás seguro de que quieres eliminar esta notificación?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarNotificacion(notificacion.id, email ?: "")
                        showDeleteDialog = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog de confirmación para eliminar todas las notificaciones
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Eliminar todas las notificaciones") },
            text = { Text("¿Estás seguro de que quieres eliminar todas las notificaciones? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.borrarTodasLasNotificaciones()
                        }
                        showDeleteAllDialog = false
                    }
                ) {
                    Text("Eliminar todas")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun NotificacionItem(
    notificacion: Notificacion,
    onNotificacionClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNotificacionClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notificacion.leida) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icono según el tipo de notificación
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                                            when (notificacion.tipo) {
                        "NUEVO_LIBRO" -> Color(0xFF4CAF50)
                        "SUGERENCIA_APROBADA" -> Color(0xFF2196F3)
                        "SUGERENCIA_DESAPROBADA" -> Color(0xFFF44336)
                        "NUEVO_EVENTO" -> Color(0xFFFF9800)
                        "SUGERENCIA_PENDIENTE" -> Color(0xFF9C27B0)
                        "SUGERENCIA_INNAPROPIADA" -> Color(0xFFFF5722)
                        else -> Color.Gray
                    }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = when (notificacion.tipo) {
                        "NUEVO_LIBRO" -> painterResource(id = R.drawable.libro)
                        "SUGERENCIA_APROBADA" -> painterResource(id = R.drawable.like)
                        "SUGERENCIA_DESAPROBADA" -> painterResource(id = R.drawable.dislike)
                        "NUEVO_EVENTO" -> painterResource(id = R.drawable.calendario)
                        "SUGERENCIA_PENDIENTE" -> painterResource(id = R.drawable.sobre)
                        "SUGERENCIA_INNAPROPIADA" -> painterResource(id = R.drawable.dislike)
                        else -> painterResource(id = R.drawable.notificacion)
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Contenido de la notificación
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notificacion.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (notificacion.leida) FontWeight.Normal else FontWeight.Bold,
                    color = if (notificacion.leida) 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = notificacion.mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = dateFormat.format(notificacion.fecha),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Indicador de no leída
            if (!notificacion.leida) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botón de eliminar
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}