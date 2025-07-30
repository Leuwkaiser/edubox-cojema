package com.example.buzondesugerenciascojema.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.buzondesugerenciascojema.model.Asignatura
import com.example.buzondesugerenciascojema.model.GradoConstants
import com.example.buzondesugerenciascojema.model.PortadasDisponibles
import com.example.buzondesugerenciascojema.viewmodels.BibliotecaViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.buzondesugerenciascojema.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubirDocumentoScreen(
    navController: NavController,
    viewModel: BibliotecaViewModel = viewModel()
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var asignaturaSeleccionada by remember { mutableStateOf<Asignatura?>(null) }
    var gradoSeleccionado by remember { mutableStateOf<Int?>(null) }
    var url by remember { mutableStateOf("") }
    var nombreArchivo by remember { mutableStateOf("") }
    var tipoArchivo by remember { mutableStateOf("") }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var portadaUrl by remember { mutableStateOf("") }
    var portadaLocalSeleccionada by remember { mutableStateOf("libro") } // Por defecto libro
    var usarPortadaLocal by remember { mutableStateOf(true) } // Por defecto usar portada local
    
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val documentos by viewModel.documentos.collectAsState()

    // Observar cuando se complete la agregación exitosamente
    LaunchedEffect(documentos) {
        if (documentos.isNotEmpty() && !isLoading && error == null) {
            showSuccessMessage = true
            // Esperar 2 segundos y luego navegar a la biblioteca
            kotlinx.coroutines.delay(2000)
            navController.navigate("biblioteca") {
                popUpTo("subir-documento") { inclusive = true }
            }
        }
    }

    // Mostrar mensaje de éxito
    if (showSuccessMessage) {
        AlertDialog(
            onDismissRequest = { showSuccessMessage = false },
            title = { Text("¡Éxito!") },
            text = { 
                Text(
                    "El documento se ha agregado correctamente a la biblioteca.\n\n" +
                    "Serás redirigido a la biblioteca en unos segundos."
                ) 
            },
            confirmButton = {
                TextButton(onClick = { 
                    showSuccessMessage = false
                    navController.navigate("biblioteca") {
                        popUpTo("subir-documento") { inclusive = true }
                    }
                }) {
                    Text("Ir a Biblioteca")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Documento") },
                navigationIcon = {
                    IconButton(onClick = { 
                        navController.navigate("biblioteca") {
                            popUpTo("subir-documento") { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color.White
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título del documento") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Descripción
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // URL del documento
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Enlace del documento") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 2,
                maxLines = 3,
                placeholder = { Text("https://drive.google.com/... o enlace directo") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Nombre del archivo
            OutlinedTextField(
                value = nombreArchivo,
                onValueChange = { nombreArchivo = it },
                label = { Text("Nombre del archivo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Ej: Libro_Matematicas_Grado6.pdf") }
            )

            // Tipo de archivo
            OutlinedTextField(
                value = tipoArchivo,
                onValueChange = { tipoArchivo = it },
                label = { Text("Tipo de archivo (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Ej: PDF, DOC, PPT, etc.") }
            )

            // Selección de tipo de portada
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📖 Portada del libro:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Opciones de portada
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = usarPortadaLocal,
                            onClick = { usarPortadaLocal = true },
                            label = { Text("Portada local") }
                        )
                        FilterChip(
                            selected = !usarPortadaLocal,
                            onClick = { usarPortadaLocal = false },
                            label = { Text("Enlace externo") }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (usarPortadaLocal) {
                        // Selector de portadas locales
                        Text(
                            text = "Selecciona una portada:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(PortadasDisponibles.PORTADAS.toList()) { (drawableName, nombreAmigable) ->
                                Card(
                                    modifier = Modifier.width(100.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (portadaLocalSeleccionada == drawableName) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    ),
                                    onClick = { portadaLocalSeleccionada = drawableName }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Image(
                                            painter = painterResource(
                                                id = when (drawableName) {
                                                    "libro" -> R.drawable.libro
                                                    "logo_cojema" -> R.drawable.logo_cojema
                                                    "logoia" -> R.drawable.logoia
                                                    "gamepad" -> R.drawable.gamepad
                                                    "calendario" -> R.drawable.calendario
                                                    "buzon" -> R.drawable.buzon
                                                    "snake" -> R.drawable.snakenew
                                                    "logro" -> R.drawable.logro
                                                    else -> R.drawable.libro
                                                }
                                            ),
                                            contentDescription = nombreAmigable,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = nombreAmigable,
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center,
                                            color = if (portadaLocalSeleccionada == drawableName) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Información sobre portadas locales
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 Para agregar nuevas portadas, coloca las imágenes en la carpeta drawable del proyecto.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        // Campo para enlace externo de portada
                        OutlinedTextField(
                            value = portadaUrl,
                            onValueChange = { portadaUrl = it },
                            label = { Text("Enlace público de la portada (Drive)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Pega aquí el enlace público de la imagen de portada") }
                        )
                        
                        // Vista previa de la portada externa
                        if (portadaUrl.isNotBlank()) {
                            androidx.compose.foundation.Image(
                                painter = rememberAsyncImagePainter(portadaUrl),
                                contentDescription = "Vista previa de la portada",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                        }
                    }
                }
            }

            // Asignatura
            Text(
                text = "Asignatura:",
                style = MaterialTheme.typography.bodyMedium
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(Asignatura.values().toList()) { asignatura ->
                    FilterChip(
                        selected = asignaturaSeleccionada == asignatura,
                        onClick = { asignaturaSeleccionada = asignatura },
                        label = { Text(asignatura.nombre) }
                    )
                }
            }

            // Grado
            Text(
                text = "Grado:",
                style = MaterialTheme.typography.bodyMedium
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(GradoConstants.GRADOS) { grado ->
                    FilterChip(
                        selected = gradoSeleccionado == grado,
                        onClick = { gradoSeleccionado = grado },
                        label = { Text("Grado $grado") }
                    )
                }
            }

            // Información sobre enlaces
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "💡 Información sobre enlaces:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Puedes usar enlaces de Google Drive, Dropbox, OneDrive, etc.\n" +
                               "• Para Google Drive: Comparte el archivo y copia el enlace\n" +
                               "• Para enlaces directos: Usa URLs que terminen en .pdf, .doc, etc.\n" +
                               "• Asegúrate de que el enlace sea público o accesible",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Estado de validación
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (titulo.isNotBlank() && descripcion.isNotBlank() && 
                                       asignaturaSeleccionada != null && gradoSeleccionado != null && 
                                       url.isNotBlank() && nombreArchivo.isNotBlank()) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📋 Estado de validación:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val validationItems = listOf(
                        "Título" to titulo.isNotBlank(),
                        "Descripción" to descripcion.isNotBlank(),
                        "Asignatura" to (asignaturaSeleccionada != null),
                        "Grado" to (gradoSeleccionado != null),
                        "URL" to url.isNotBlank(),
                        "Nombre del archivo" to nombreArchivo.isNotBlank(),
                        "Portada" to (usarPortadaLocal || portadaUrl.isNotBlank())
                    )
                    
                    validationItems.forEach { (field, isValid) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = field,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = if (isValid) Icons.Filled.CheckCircle else Icons.Filled.Close,
                                contentDescription = if (isValid) "Válido" else "Faltante",
                                tint = if (isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            // Botón de agregar
            Button(
                onClick = {
                    if (titulo.isNotBlank() && descripcion.isNotBlank() && 
                        asignaturaSeleccionada != null && gradoSeleccionado != null && 
                        url.isNotBlank() && nombreArchivo.isNotBlank() &&
                        (usarPortadaLocal || portadaUrl.isNotBlank())) {
                        
                        println("DEBUG: Intentando agregar documento...")
                        println("DEBUG: Título: $titulo")
                        println("DEBUG: Asignatura: ${asignaturaSeleccionada!!.nombre}")
                        println("DEBUG: Grado: $gradoSeleccionado")
                        println("DEBUG: URL: $url")
                        println("DEBUG: Portada local: $portadaLocalSeleccionada")
                        println("DEBUG: Portada URL: $portadaUrl")
                        
                        viewModel.agregarDocumento(
                            titulo = titulo,
                            descripcion = descripcion,
                            asignatura = asignaturaSeleccionada!!.nombre,
                            grado = gradoSeleccionado!!,
                            url = url,
                            nombreArchivo = nombreArchivo,
                            subidoPor = "admin", // TODO: Obtener del usuario actual
                            tipoArchivo = tipoArchivo.ifBlank { "application/octet-stream" },
                            portadaUrl = if (usarPortadaLocal) "" else portadaUrl,
                            portadaDrawable = if (usarPortadaLocal) portadaLocalSeleccionada else ""
                        )
                    } else {
                        println("DEBUG: Campos faltantes:")
                        println("DEBUG: Título vacío: ${titulo.isBlank()}")
                        println("DEBUG: Descripción vacía: ${descripcion.isBlank()}")
                        println("DEBUG: Asignatura no seleccionada: ${asignaturaSeleccionada == null}")
                        println("DEBUG: Grado no seleccionado: ${gradoSeleccionado == null}")
                        println("DEBUG: URL vacía: ${url.isBlank()}")
                        println("DEBUG: Nombre archivo vacío: ${nombreArchivo.isBlank()}")
                        println("DEBUG: Portada no seleccionada: ${!usarPortadaLocal && portadaUrl.isBlank()}")
                    }
                },
                enabled = titulo.isNotBlank() && descripcion.isNotBlank() && 
                         asignaturaSeleccionada != null && gradoSeleccionado != null && 
                         url.isNotBlank() && nombreArchivo.isNotBlank() && 
                         (usarPortadaLocal || portadaUrl.isNotBlank()) && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregando...")
                } else {
                    Text("Agregar Documento")
                }
            }

            // Mostrar error si existe
            if (error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Error:",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.limpiarError() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Cerrar")
                        }
                    }
                }
            }

            // Mostrar estado de carga
            if (isLoading) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Subiendo documento a la biblioteca...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
} 