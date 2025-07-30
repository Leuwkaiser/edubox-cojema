package com.example.buzondesugerenciascojema.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.buzondesugerenciascojema.data.AIService
import com.example.buzondesugerenciascojema.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.core.content.ContextCompat

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    aiService: AIService,
    onBackPressed: () -> Unit = {},
    userName: String = ""
) {
    // 1. Estado y contexto
    val context = LocalContext.current
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var currentMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // 2. Launchers y funciones
    // Launcher para solicitar permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        if (cameraGranted) {
            // Permiso de cámara concedido, mostrar diálogo de selección
            showImageSourceDialog = true
        } else {
            // Permiso de cámara denegado
            messages = messages + ChatMessage(
                text = "Necesito permiso de cámara para tomar fotos. Puedes habilitarlo en Configuración > Aplicaciones > EduBox > Permisos.",
                isUser = false
            )
        }
    }

    // Función para verificar y solicitar permisos
    fun checkAndRequestPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        
        if (cameraPermission == PackageManager.PERMISSION_GRANTED) {
            // Permiso de cámara concedido
            showImageSourceDialog = true
        } else {
            // Solicitar solo permiso de cámara
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA)
            )
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            if (bitmap != null) {
                val image = InputImage.fromBitmap(bitmap, 0)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                isLoading = true
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val recognizedText = visionText.text.trim()
                        messages = messages + ChatMessage(
                            text = if (recognizedText.isNotBlank()) recognizedText else "No se detectó texto en la imagen.",
                            isUser = false
                        )
                        isLoading = false
                    }
                    .addOnFailureListener { e ->
                        messages = messages + ChatMessage(
                            text = "Ocurrió un error al procesar la imagen.",
                            isUser = false
                        )
                        isLoading = false
                    }
            }
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success && cameraImageUri != null) {
            val inputStream = context.contentResolver.openInputStream(cameraImageUri!!)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            if (bitmap != null) {
                val image = InputImage.fromBitmap(bitmap, 0)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                isLoading = true
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val recognizedText = visionText.text.trim()
                        messages = messages + ChatMessage(
                            text = if (recognizedText.isNotBlank()) recognizedText else "No se detectó texto en la imagen.",
                            isUser = false
                        )
                        isLoading = false
                    }
                    .addOnFailureListener { e ->
                        messages = messages + ChatMessage(
                            text = "Ocurrió un error al procesar la imagen.",
                            isUser = false
                        )
                        isLoading = false
                    }
            }
        }
    }
    fun createImageFile(): File? {
        return try {
            File.createTempFile(
                "botso_photo_",
                ".jpg",
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )
        } catch (e: IOException) {
            null
        }
    }

    // 3. Efectos y lógica
    // (LaunchedEffect, etc.)
    // Mensaje de bienvenida inicial personalizado
    LaunchedEffect(Unit) {
        val greetings = if (userName.isNotEmpty()) {
            listOf(
                "¡Hola $userName! 👋 Soy Botso IA, tu asistente virtual personalizado de COJEMA. ¿En qué puedo ayudarte hoy?",
                "¡Bienvenido $userName! 😊 Soy Botso IA, tu amigo virtual de COJEMA. ¿Qué te gustaría hacer hoy?",
                "¡Hola $userName! 🌟 Soy Botso IA, tu asistente personal de COJEMA. ¿En qué puedo ser útil hoy?",
                "¡Qué tal $userName! 🎉 Soy Botso IA, tu asistente virtual de COJEMA. ¿Cómo puedo ayudarte?"
            )
        } else {
            listOf(
                "¡Hola! 👋 Soy Botso IA, tu asistente virtual personalizado de COJEMA. ¿En qué puedo ayudarte hoy?",
                "¡Bienvenido! 😊 Soy Botso IA, tu amigo virtual de COJEMA. ¿Qué te gustaría hacer hoy?",
                "¡Hola! 🌟 Soy Botso IA, tu asistente personal de COJEMA. ¿En qué puedo ser útil hoy?",
                "¡Qué tal! 🎉 Soy Botso IA, tu asistente virtual de COJEMA. ¿Cómo puedo ayudarte?"
            )
        }
        
        val randomGreeting = greetings.random()
        
        messages = listOf(
            ChatMessage(
                text = randomGreeting,
                isUser = false
            )
        )
    }
    
    // Auto-scroll al último mensaje
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
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
        // TopAppBar
        TopAppBar(
            title = { 
                Text(
                    "Asistente Virtual",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp)
        ) {

            // Chat messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    ChatMessageItem(message = message)
                }
                
                if (isLoading) {
                    item {
                        ChatMessageItem(
                            message = ChatMessage(
                                text = "Escribiendo...",
                                isUser = false
                            )
                        )
                    }
                }
            }
            
            // Input area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = currentMessage,
                        onValueChange = { currentMessage = it },
                        placeholder = { Text("Escribe tu mensaje...", color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White
                        ),
                        maxLines = 3
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = {
                            if (currentMessage.isNotBlank() && !isLoading) {
                                val userMessage = currentMessage
                                currentMessage = ""
                                
                                scope.launch {
                                    // Agregar mensaje del usuario
                                    messages = messages + ChatMessage(
                                        text = userMessage,
                                        isUser = true
                                    )
                                    
                                    isLoading = true
                                    
                                    try {
                                        // Obtener respuesta del asistente
                                        val response = aiService.sendMessage(
                                            message = userMessage,
                                            userName = userName
                                        )
                                        messages = messages + ChatMessage(
                                            text = response,
                                            isUser = false
                                        )
                                    } catch (e: Exception) {
                                        messages = messages + ChatMessage(
                                            text = "Lo siento, hubo un error al procesar tu mensaje. Inténtalo de nuevo.",
                                            isUser = false
                                        )
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        enabled = currentMessage.isNotBlank() && !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Enviar",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            checkAndRequestPermissions()
                        }
                    ) {
                        GalleryIcon()
                    }
                }
            }
        }
    }

    // Diálogo para elegir fuente de imagen
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Selecciona una opción") },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = {
                            showImageSourceDialog = false
                            val photoFile = createImageFile()
                            if (photoFile != null) {
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    context.packageName + ".provider",
                                    photoFile
                                )
                                cameraImageUri = uri
                                cameraLauncher.launch(uri)
                            }
                        }) {
                            CameraIcon()
                        }
                        Text("Cámara", color = Color.White, fontSize = 13.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = {
                            showImageSourceDialog = false
                            imagePickerLauncher.launch("image/*")
                        }) {
                            GalleryIcon()
                        }
                        Text("Galería", color = Color.White, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage
) {
    val isUser = message.isUser
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // Avatar del asistente
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logoia),
                    contentDescription = "Asistente",
                    modifier = Modifier.size(20.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) 
                    Color.White.copy(alpha = 0.2f) 
                else 
                    Color.White.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
            }
        }
        
        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // Avatar del usuario
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Usuario",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
} 

@Composable
fun GalleryIcon(modifier: Modifier = Modifier) {
    Canvas(modifier.size(32.dp)) {
        // Marco de la foto
        drawRect(
            color = Color.White,
            size = size,
            style = Stroke(width = 2f)
        )
        // Montaña
        drawLine(
            color = Color.White,
            start = Offset(size.width * 0.2f, size.height * 0.7f),
            end = Offset(size.width * 0.5f, size.height * 0.4f),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.White,
            start = Offset(size.width * 0.5f, size.height * 0.4f),
            end = Offset(size.width * 0.8f, size.height * 0.7f),
            strokeWidth = 2f
        )
        // Sol
        drawCircle(
            color = Color.White,
            radius = size.minDimension * 0.12f,
            center = Offset(size.width * 0.7f, size.height * 0.3f)
        )
    }
}

@Composable
fun CameraIcon(modifier: Modifier = Modifier) {
    Canvas(modifier.size(32.dp)) {
        // Cuerpo de la cámara
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(size.width * 0.15f, size.height * 0.3f),
            size = Size(size.width * 0.7f, size.height * 0.5f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        // Lente
        drawCircle(
            color = Color.White,
            radius = size.minDimension * 0.15f,
            center = Offset(size.width * 0.5f, size.height * 0.55f)
        )
        // Flash
        drawRect(
            color = Color.White,
            topLeft = Offset(size.width * 0.35f, size.height * 0.2f),
            size = Size(size.width * 0.3f, size.height * 0.1f)
        )
    }
} 