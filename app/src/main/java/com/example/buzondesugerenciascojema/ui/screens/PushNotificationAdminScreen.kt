package com.example.buzondesugerenciascojema.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buzondesugerenciascojema.data.TipoNotificacion
import com.example.buzondesugerenciascojema.viewmodels.PushNotificationViewModel
import com.example.buzondesugerenciascojema.viewmodels.PushNotificationState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PushNotificationAdminScreen(
    onNavigateBack: () -> Unit,
    viewModel: PushNotificationViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    
    var titulo by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var emailDestinatario by remember { mutableStateOf("") }
    var grado by remember { mutableStateOf("") }
    var grupo by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("") }
    var tipoSeleccionado by remember { mutableStateOf(TipoNotificacion.PUSH) }
    var mostrarFormularioGrupo by remember { mutableStateOf(false) }
    var mostrarFormularioRol by remember { mutableStateOf(false) }
    var mostrarFormularioTodos by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.obtenerEstadisticasTokens()
    }

    LaunchedEffect(state.mensaje) {
        if (state.mensaje != null) {
            // Limpiar mensaje después de 3 segundos
            kotlinx.coroutines.delay(3000)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones Push") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estadísticas
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Estadísticas de Tokens FCM",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total de tokens: ${state.estadisticas["totalTokens"] ?: 0}")
                    Text("Tokens activos: ${state.estadisticas["tokensActivos"] ?: 0}")
                    Text("Tokens inactivos: ${state.estadisticas["tokensInactivos"] ?: 0}")
                }
            }

            // Selector de tipo de envío
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Tipo de Envío",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                mostrarFormularioGrupo = false
                                mostrarFormularioRol = false
                                mostrarFormularioTodos = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Usuario")
                        }
                        
                        Button(
                            onClick = {
                                mostrarFormularioGrupo = true
                                mostrarFormularioRol = false
                                mostrarFormularioTodos = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Grupo")
                        }
                        
                        Button(
                            onClick = {
                                mostrarFormularioGrupo = false
                                mostrarFormularioRol = true
                                mostrarFormularioTodos = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Rol")
                        }
                        
                        Button(
                            onClick = {
                                mostrarFormularioGrupo = false
                                mostrarFormularioRol = false
                                mostrarFormularioTodos = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Todos")
                        }
                    }
                }
            }

            // Formulario de notificación
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Detalles de la Notificación",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Tipo de notificación
                    ExposedDropdownMenuBox(
                        expanded = false,
                        onExpandedChange = { },
                    ) {
                        OutlinedTextField(
                            value = tipoSeleccionado.name,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Tipo de Notificación") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Título
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        label = { Text("Título") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mensaje
                    OutlinedTextField(
                        value = mensaje,
                        onValueChange = { mensaje = it },
                        label = { Text("Mensaje") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campos específicos según el tipo de envío
                    when {
                        !mostrarFormularioGrupo && !mostrarFormularioRol && !mostrarFormularioTodos -> {
                            // Usuario específico
                            OutlinedTextField(
                                value = emailDestinatario,
                                onValueChange = { emailDestinatario = it },
                                label = { Text("Email del Destinatario") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        mostrarFormularioGrupo -> {
                            // Grupo específico
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = grado,
                                    onValueChange = { grado = it },
                                    label = { Text("Grado") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = grupo,
                                    onValueChange = { grupo = it },
                                    label = { Text("Grupo") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        mostrarFormularioRol -> {
                            // Por rol
                            OutlinedTextField(
                                value = rol,
                                onValueChange = { rol = it },
                                label = { Text("Rol (admin, estudiante, etc.)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón de envío
                    Button(
                        onClick = {
                            when {
                                !mostrarFormularioGrupo && !mostrarFormularioRol && !mostrarFormularioTodos -> {
                                    if (emailDestinatario.isNotBlank() && titulo.isNotBlank() && mensaje.isNotBlank()) {
                                        viewModel.enviarNotificacionPush(
                                            emailDestinatario = emailDestinatario,
                                            titulo = titulo,
                                            mensaje = mensaje,
                                            tipo = tipoSeleccionado
                                        )
                                    }
                                }
                                mostrarFormularioGrupo -> {
                                    if (grado.isNotBlank() && grupo.isNotBlank() && titulo.isNotBlank() && mensaje.isNotBlank()) {
                                        viewModel.enviarNotificacionPushAGrupo(
                                            grado = grado,
                                            grupo = grupo,
                                            titulo = titulo,
                                            mensaje = mensaje,
                                            tipo = tipoSeleccionado
                                        )
                                    }
                                }
                                mostrarFormularioRol -> {
                                    if (rol.isNotBlank() && titulo.isNotBlank() && mensaje.isNotBlank()) {
                                        viewModel.enviarNotificacionPushPorRol(
                                            rol = rol,
                                            titulo = titulo,
                                            mensaje = mensaje,
                                            tipo = tipoSeleccionado
                                        )
                                    }
                                }
                                mostrarFormularioTodos -> {
                                    if (titulo.isNotBlank() && mensaje.isNotBlank()) {
                                        viewModel.enviarNotificacionPushATodos(
                                            titulo = titulo,
                                            mensaje = mensaje,
                                            tipo = tipoSeleccionado
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading && titulo.isNotBlank() && mensaje.isNotBlank()
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Enviar Notificación")
                        }
                    }
                }
            }

            // Mensaje de estado
            state.mensaje?.let { mensaje ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.esExitoso) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = mensaje,
                        modifier = Modifier.padding(16.dp),
                        color = if (state.esExitoso) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Información adicional
            if (state.usuariosEnviados > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "Notificación enviada a ${state.usuariosEnviados} usuarios",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
} 