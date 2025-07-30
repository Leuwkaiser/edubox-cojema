package com.example.buzondesugerenciascojema.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.buzondesugerenciascojema.model.Documento
import com.example.buzondesugerenciascojema.viewmodels.BibliotecaViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.res.painterResource
import com.example.buzondesugerenciascojema.R
import android.content.Intent
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VisualizarDocumentoScreen(
    navController: NavController,
    documentoId: String,
    viewModel: BibliotecaViewModel = viewModel()
) {
    val documento by viewModel.documentoActual.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var mostrarMenu by remember { mutableStateOf(false) }
    var mostrarDescripcion by remember { mutableStateOf(false) }
    var pantallaCompleta by remember { mutableStateOf(false) }

    LaunchedEffect(documentoId) {
        viewModel.cargarDocumentoPorId(documentoId)
    }

    if (pantallaCompleta) {
        // Modo pantalla completa - solo el WebView
        Box(modifier = Modifier.fillMaxSize()) {
            documento?.let {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                builtInZoomControls = true
                                displayZoomControls = true  // Mostrar controles de zoom en pantalla completa
                                setSupportZoom(true)
                                setSupportMultipleWindows(true)
                                allowFileAccess = true
                                allowContentAccess = true
                            }
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    println("DEBUG: Página cargada en pantalla completa: $url")
                                }
                            }
                            loadUrl(it.url)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } ?: run {
                // Mostrar mensaje si no hay documento
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "Cargando documento...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            // Botón flotante pequeño para salir de pantalla completa
            FloatingActionButton(
                onClick = { 
                    println("DEBUG: Saliendo de pantalla completa")
                    pantallaCompleta = false 
                },
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.TopStart)
                    .padding(16.dp)
                    .size(48.dp),
                containerColor = Color(0xFF6C63FF).copy(alpha = 0.8f)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.minimize),
                    contentDescription = "Salir de pantalla completa",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    } else {
        // Modo normal con TopAppBar
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            documento?.titulo ?: "Documento",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Regresar",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        // Botón para pantalla completa
                        IconButton(onClick = { 
                            println("DEBUG: Entrando en pantalla completa")
                            pantallaCompleta = true 
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.fullscreen),
                                contentDescription = "Pantalla completa",
                                tint = Color.White
                            )
                        }
                        // Botón de descarga
                        val context = LocalContext.current
                        IconButton(onClick = {
                            documento?.url?.let { url ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.download),
                                contentDescription = "Descargar o abrir en Google Drive",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { mostrarMenu = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Más opciones",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = mostrarMenu,
                            onDismissRequest = { mostrarMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Ver información") },
                                onClick = {
                                    mostrarMenu = false
                                    mostrarDescripcion = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Info, contentDescription = null)
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF6C63FF)
                    )
                )
            }
        ) { paddingValues ->
            documento?.let {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Solo el WebView centrado, sin portada ni texto extra
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        AndroidView(
                            factory = { context ->
                                WebView(context).apply {
                                    settings.apply {
                                        javaScriptEnabled = true
                                        domStorageEnabled = true
                                        loadWithOverviewMode = true
                                        useWideViewPort = true
                                        builtInZoomControls = true
                                        displayZoomControls = true
                                        setSupportZoom(true)
                                        setSupportMultipleWindows(true)
                                        allowFileAccess = true
                                        allowContentAccess = true
                                    }
                                    webViewClient = object : WebViewClient() {
                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            println("DEBUG: Página cargada en modo normal: $url")
                                        }
                                    }
                                    loadUrl(it.url)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                when {
                    isLoading -> CircularProgressIndicator()
                    error != null -> Text(error ?: "Error desconocido", color = Color.Red)
                    else -> Text("Documento no encontrado", color = Color.Red)
                }
            }
        }
        
        // Diálogo para mostrar la descripción (solo en modo normal)
        if (mostrarDescripcion && documento != null && !pantallaCompleta) {
            AlertDialog(
                onDismissRequest = { mostrarDescripcion = false },
                title = { Text("Información del documento") },
                text = { 
                    Column {
                        Text(
                            text = "Descripción:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Text(
                            text = documento!!.descripcion,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Asignatura:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Text(
                            text = documento!!.asignatura,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Grado:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Text(
                            text = "${documento!!.grado}°",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Subido por:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Text(
                            text = documento!!.subidoPor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { mostrarDescripcion = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }
    }
}