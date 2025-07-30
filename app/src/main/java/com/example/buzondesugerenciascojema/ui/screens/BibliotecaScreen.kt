package com.example.buzondesugerenciascojema.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.buzondesugerenciascojema.model.Asignatura
import com.example.buzondesugerenciascojema.model.GradoConstants
import com.example.buzondesugerenciascojema.viewmodels.BibliotecaViewModel
import com.example.buzondesugerenciascojema.data.UsuarioManager
import com.example.buzondesugerenciascojema.ui.components.DocumentoItem
import com.example.buzondesugerenciascojema.ui.components.FloatingAssistantButton
import com.example.buzondesugerenciascojema.R
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.platform.LocalConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibliotecaScreen(
    navController: NavController,
    viewModel: BibliotecaViewModel = viewModel(),
    usuarioManager: UsuarioManager? = null,
    onBackPressed: () -> Unit = {}
) {
    val documentos by viewModel.documentos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val asignaturaSeleccionada by viewModel.asignaturaSeleccionada.collectAsState()
    val gradoSeleccionado by viewModel.gradoSeleccionado.collectAsState()
    val currentUser by usuarioManager?.currentUser?.collectAsState() ?: remember { mutableStateOf(null) }
    var showContent by remember { mutableStateOf(false) }
    val (showSearchMenu, setShowSearchMenu) = remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Cargar documentos cuando se muestra la pantalla
    LaunchedEffect(Unit) { 
        showContent = true
        viewModel.cargarDocumentos()
    }
    
    // Recargar documentos cuando se regresa a la pantalla
    LaunchedEffect(navController) {
        viewModel.cargarDocumentos()
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6C63FF),
                        Color(0xFF42A5F5)
                    )
                )
            )
    ) {
        // TopAppBar con botón de regreso
        TopAppBar(
            title = { 
                Text(
                    "Biblioteca Virtual",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(onClick = { setShowSearchMenu(!showSearchMenu) }) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Buscar",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
        
        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Panel de filtros a la izquierda
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight().padding(end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Menú de búsqueda y filtros
                    AnimatedVisibility(
                        visible = showSearchMenu,
                        enter = fadeIn(animationSpec = tween(500)),
                        exit = fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.10f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    label = { Text("Buscar por nombre") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Asignatura:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    item {
                                        FilterChip(
                                            selected = asignaturaSeleccionada == null,
                                            onClick = { viewModel.cargarDocumentos() },
                                            label = { Text("Todas", color = Color.White) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF6C63FF),
                                                containerColor = Color.White.copy(alpha = 0.15f),
                                                labelColor = Color.White
                                            )
                                        )
                                    }
                                    items(Asignatura.values().toList()) { asignatura ->
                                        FilterChip(
                                            selected = asignaturaSeleccionada == asignatura,
                                            onClick = { viewModel.cargarDocumentosPorAsignatura(asignatura) },
                                            label = { Text(asignatura.nombre, color = Color.White) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF6C63FF),
                                                containerColor = Color.White.copy(alpha = 0.15f),
                                                labelColor = Color.White
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Grado:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    item {
                                        FilterChip(
                                            selected = gradoSeleccionado == null,
                                            onClick = { viewModel.cargarDocumentos() },
                                            label = { Text("Todos", color = Color.White) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF6C63FF),
                                                containerColor = Color.White.copy(alpha = 0.15f),
                                                labelColor = Color.White
                                            )
                                        )
                                    }
                                    items(GradoConstants.GRADOS) { grado ->
                                        FilterChip(
                                            selected = gradoSeleccionado == grado,
                                            onClick = { viewModel.cargarDocumentosPorGrado(grado) },
                                            label = { Text("Grado $grado", color = Color.White) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF6C63FF),
                                                containerColor = Color.White.copy(alpha = 0.15f),
                                                labelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Botón para agregar documento (solo admins)
                        if (currentUser?.esAdmin == true) {
                            AnimatedVisibility(
                                visible = showContent,
                                enter = fadeIn(animationSpec = tween(1000)),
                                exit = fadeOut()
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.15f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Panel de Administración",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Como administrador, puedes agregar nuevos documentos a la biblioteca",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.9f),
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = { navController.navigate("subir-documento") },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp),
                                            shape = RoundedCornerShape(25.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                            contentPadding = PaddingValues()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        Brush.horizontalGradient(
                                                            colors = listOf(
                                                                Color(0xFF6C63FF),
                                                                Color(0xFF42A5F5)
                                                            )
                                                        ),
                                                        shape = RoundedCornerShape(25.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Add,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Subir Nuevo Documento",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
                // Lista de documentos a la derecha
                Column(
                    modifier = Modifier.weight(2f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Lista de documentos
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(animationSpec = tween(1200)),
                        exit = fadeOut()
                    ) {
                        if (isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.White)
                            }
                        } else if (error != null) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = error!!,
                                    color = Color.Red,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else if (documentos.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay documentos disponibles",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White
                                )
                            }
                        } else {
                            // Filtrar por búsqueda
                            val documentosFiltrados = documentos.filter { it.titulo.contains(searchQuery, ignoreCase = true) }
                            val documentosPorAsignatura = documentosFiltrados.groupBy { it.asignatura }
                            var selectedTab by remember { mutableStateOf(documentosPorAsignatura.keys.firstOrNull() ?: "") }
                            Column(Modifier.fillMaxSize()) {
                                // Tabs de asignatura
                                ScrollableTabRow(
                                    selectedTabIndex = documentosPorAsignatura.keys.indexOf(selectedTab),
                                    edgePadding = 0.dp,
                                    containerColor = Color.Transparent
                                ) {
                                    documentosPorAsignatura.keys.forEachIndexed { idx, asignatura ->
                                        Tab(
                                            selected = selectedTab == asignatura,
                                            onClick = { selectedTab = asignatura },
                                            text = { Text(asignatura, color = if (selectedTab == asignatura) Color.White else Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                // Grid de libros
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = 140.dp),
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(bottom = 80.dp)
                                ) {
                                    items(documentosPorAsignatura[selectedTab] ?: emptyList()) { documento ->
                                        var mostrarMenu by remember { mutableStateOf(false) }
                                        var mostrarDialogoEliminar by remember { mutableStateOf(false) }
                                        Card(
                                            modifier = Modifier
                                                .height(240.dp)
                                                .clip(RoundedCornerShape(20.dp)),
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color.White.copy(alpha = 0.18f)
                                            ),
                                            onClick = {
                                                navController.navigate("visualizar-documento/${documento.id}")
                                            }
                                        ) {
                                            Box(Modifier.fillMaxSize()) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(12.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Top
                                                ) {
                                                    // Portada
                                                    if (documento.portadaDrawable.isNotBlank()) {
                                                        androidx.compose.foundation.Image(
                                                            painter = painterResource(
                                                                id = when (documento.portadaDrawable) {
                                                                    "libro" -> R.drawable.libro
                                                                    "logo_cojema" -> R.drawable.logo_cojema
                                                                    "logoia" -> R.drawable.logoia
                                                                    "gamepad" -> R.drawable.gamepad
                                                                    "calendario" -> R.drawable.calendario
                                                                    "buzon" -> R.drawable.buzon
                                                                    "snake" -> R.drawable.snake
                                                                    "logro" -> R.drawable.logro
                                                                    else -> R.drawable.libro
                                                                }
                                                            ),
                                                            contentDescription = "Portada del libro",
                                                            modifier = Modifier.size(90.dp)
                                                        )
                                                    } else if (documento.portadaUrl.isNotBlank()) {
                                                        androidx.compose.foundation.Image(
                                                            painter = rememberAsyncImagePainter(documento.portadaUrl),
                                                            contentDescription = "Portada del libro",
                                                            modifier = Modifier.size(90.dp)
                                                        )
                                                    } else {
                                                        androidx.compose.foundation.Image(
                                                            painter = painterResource(id = R.drawable.libro),
                                                            contentDescription = "Libro por defecto",
                                                            modifier = Modifier.size(90.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Text(
                                                        text = documento.titulo,
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 2,
                                                        textAlign = TextAlign.Center
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Grado: ${documento.grado}°",
                                                        color = Color.White.copy(alpha = 0.8f),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        maxLines = 1
                                                    )
                                                }
                                                if (currentUser?.esAdmin == true) {
                                                    IconButton(
                                                        onClick = { mostrarMenu = true },
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(4.dp)
                                                    ) {
                                                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                                                    }
                                                    DropdownMenu(
                                                        expanded = mostrarMenu,
                                                        onDismissRequest = { mostrarMenu = false }
                                                    ) {
                                                        DropdownMenuItem(
                                                            text = { Text("Eliminar libro") },
                                                            onClick = {
                                                                mostrarMenu = false
                                                                mostrarDialogoEliminar = true
                                                            },
                                                            leadingIcon = {
                                                                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                                            }
                                                        )
                                                    }
                                                    if (mostrarDialogoEliminar) {
                                                        AlertDialog(
                                                            onDismissRequest = { mostrarDialogoEliminar = false },
                                                            title = { Text("¿Eliminar libro?") },
                                                            text = { Text("¿Estás seguro de que deseas eliminar este libro? Esta acción no se puede deshacer.") },
                                                            confirmButton = {
                                                                TextButton(onClick = {
                                                                    mostrarDialogoEliminar = false
                                                                    viewModel.eliminarDocumento(documento.id)
                                                                }) { Text("Eliminar") }
                                                            },
                                                            dismissButton = {
                                                                TextButton(onClick = { mostrarDialogoEliminar = false }) { Text("Cancelar") }
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Layout original en Column
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Menú de búsqueda y filtros
                AnimatedVisibility(
                    visible = showSearchMenu,
                    enter = fadeIn(animationSpec = tween(500)),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.10f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Buscar por nombre") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Asignatura:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    FilterChip(
                                        selected = asignaturaSeleccionada == null,
                                        onClick = { viewModel.cargarDocumentos() },
                                        label = { Text("Todas", color = Color.White) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF6C63FF),
                                            containerColor = Color.White.copy(alpha = 0.15f),
                                            labelColor = Color.White
                                        )
                                    )
                                }
                                items(Asignatura.values().toList()) { asignatura ->
                                    FilterChip(
                                        selected = asignaturaSeleccionada == asignatura,
                                        onClick = { viewModel.cargarDocumentosPorAsignatura(asignatura) },
                                        label = { Text(asignatura.nombre, color = Color.White) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF6C63FF),
                                            containerColor = Color.White.copy(alpha = 0.15f),
                                            labelColor = Color.White
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Grado:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    FilterChip(
                                        selected = gradoSeleccionado == null,
                                        onClick = { viewModel.cargarDocumentos() },
                                        label = { Text("Todos", color = Color.White) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF6C63FF),
                                            containerColor = Color.White.copy(alpha = 0.15f),
                                            labelColor = Color.White
                                        )
                                    )
                                }
                                items(GradoConstants.GRADOS) { grado ->
                                    FilterChip(
                                        selected = gradoSeleccionado == grado,
                                        onClick = { viewModel.cargarDocumentosPorGrado(grado) },
                                        label = { Text("Grado $grado", color = Color.White) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF6C63FF),
                                            containerColor = Color.White.copy(alpha = 0.15f),
                                            labelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Botón para agregar documento (solo admins)
                if (currentUser?.esAdmin == true) {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(animationSpec = tween(1000)),
                        exit = fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.15f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Panel de Administración",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Como administrador, puedes agregar nuevos documentos a la biblioteca",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { navController.navigate("subir-documento") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(25.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    contentPadding = PaddingValues()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(
                                                        Color(0xFF6C63FF),
                                                        Color(0xFF42A5F5)
                                                    )
                                                ),
                                                shape = RoundedCornerShape(25.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Subir Nuevo Documento",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                // Lista de documentos
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(1200)),
                    exit = fadeOut()
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    } else if (error != null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = error!!,
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else if (documentos.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay documentos disponibles",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        }
                    } else {
                        // Filtrar por búsqueda
                        val documentosFiltrados = documentos.filter { it.titulo.contains(searchQuery, ignoreCase = true) }
                        val documentosPorAsignatura = documentosFiltrados.groupBy { it.asignatura }
                        var selectedTab by remember { mutableStateOf(documentosPorAsignatura.keys.firstOrNull() ?: "") }
                        Column(Modifier.fillMaxSize()) {
                            // Tabs de asignatura
                            ScrollableTabRow(
                                selectedTabIndex = documentosPorAsignatura.keys.indexOf(selectedTab),
                                edgePadding = 0.dp,
                                containerColor = Color.Transparent
                            ) {
                                documentosPorAsignatura.keys.forEachIndexed { idx, asignatura ->
                                    Tab(
                                        selected = selectedTab == asignatura,
                                        onClick = { selectedTab = asignatura },
                                        text = { Text(asignatura, color = if (selectedTab == asignatura) Color.White else Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            // Grid de libros
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 140.dp),
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) {
                                items(documentosPorAsignatura[selectedTab] ?: emptyList()) { documento ->
                                    var mostrarMenu by remember { mutableStateOf(false) }
                                    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
                                    Card(
                                        modifier = Modifier
                                            .height(240.dp)
                                            .clip(RoundedCornerShape(20.dp)),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White.copy(alpha = 0.18f)
                                        ),
                                        onClick = {
                                            navController.navigate("visualizar-documento/${documento.id}")
                                        }
                                    ) {
                                        Box(Modifier.fillMaxSize()) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(12.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Top
                                            ) {
                                                // Portada
                                                if (documento.portadaDrawable.isNotBlank()) {
                                                    androidx.compose.foundation.Image(
                                                        painter = painterResource(
                                                            id = when (documento.portadaDrawable) {
                                                                "libro" -> R.drawable.libro
                                                                "logo_cojema" -> R.drawable.logo_cojema
                                                                "logoia" -> R.drawable.logoia
                                                                "gamepad" -> R.drawable.gamepad
                                                                "calendario" -> R.drawable.calendario
                                                                "buzon" -> R.drawable.buzon
                                                                "snake" -> R.drawable.snake
                                                                "logro" -> R.drawable.logro
                                                                else -> R.drawable.libro
                                                            }
                                                        ),
                                                        contentDescription = "Portada del libro",
                                                        modifier = Modifier.size(90.dp)
                                                    )
                                                } else if (documento.portadaUrl.isNotBlank()) {
                                                    androidx.compose.foundation.Image(
                                                        painter = rememberAsyncImagePainter(documento.portadaUrl),
                                                        contentDescription = "Portada del libro",
                                                        modifier = Modifier.size(90.dp)
                                                    )
                                                } else {
                                                    androidx.compose.foundation.Image(
                                                        painter = painterResource(id = R.drawable.libro),
                                                        contentDescription = "Libro por defecto",
                                                        modifier = Modifier.size(90.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = documento.titulo,
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 2,
                                                    textAlign = TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Grado: ${documento.grado}°",
                                                    color = Color.White.copy(alpha = 0.8f),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    maxLines = 1
                                                )
                                            }
                                            if (currentUser?.esAdmin == true) {
                                                IconButton(
                                                    onClick = { mostrarMenu = true },
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(4.dp)
                                                ) {
                                                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                                                }
                                                DropdownMenu(
                                                    expanded = mostrarMenu,
                                                    onDismissRequest = { mostrarMenu = false }
                                                ) {
                                                    DropdownMenuItem(
                                                        text = { Text("Eliminar libro") },
                                                        onClick = {
                                                            mostrarMenu = false
                                                            mostrarDialogoEliminar = true
                                                        },
                                                        leadingIcon = {
                                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                                        }
                                                    )
                                                }
                                                if (mostrarDialogoEliminar) {
                                                    AlertDialog(
                                                        onDismissRequest = { mostrarDialogoEliminar = false },
                                                        title = { Text("¿Eliminar libro?") },
                                                        text = { Text("¿Estás seguro de que deseas eliminar este libro? Esta acción no se puede deshacer.") },
                                                        confirmButton = {
                                                            TextButton(onClick = {
                                                                mostrarDialogoEliminar = false
                                                                viewModel.eliminarDocumento(documento.id)
                                                            }) { Text("Eliminar") }
                                                        },
                                                        dismissButton = {
                                                            TextButton(onClick = { mostrarDialogoEliminar = false }) { Text("Cancelar") }
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Contador de libros en la parte inferior derecha
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "${documentos.size} libros",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6C63FF),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
        
        // Botón flotante del asistente virtual
        FloatingAssistantButton(
            onClick = { navController.navigate("assistant/biblioteca") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
} 