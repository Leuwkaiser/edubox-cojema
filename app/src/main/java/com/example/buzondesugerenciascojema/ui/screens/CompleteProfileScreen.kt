package com.example.buzondesugerenciascojema.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.buzondesugerenciascojema.R
import com.example.buzondesugerenciascojema.data.GoogleAuthService
import com.example.buzondesugerenciascojema.data.Usuario
import com.example.buzondesugerenciascojema.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteProfileScreen(
    user: Usuario,
    googleAuthService: GoogleAuthService,
    onProfileComplete: (Usuario) -> Unit
) {
    var nombre by remember { mutableStateOf(user.nombreCompleto) }
    var grupo by remember { mutableStateOf("") }
    var grado by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showGradoSelector by remember { mutableStateOf(false) }
    var showGrupoSelector by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Cambiar color de la status bar
    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = view.context as android.app.Activity
        activity.window.statusBarColor = AzulOscuro.toArgb()
        WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars = false
    }
    
    // Listas de opciones
    val grados = listOf("6°", "7°", "8°", "9°", "10°", "11°")
    val grupos = listOf("A", "B", "C", "D", "E", "F")
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = GradientePrimario
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo y título
            Spacer(modifier = Modifier.height(60.dp))
            
            Text(
                text = "¡Bienvenido a EduBox!",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Completa tu perfil para continuar",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Formulario
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Nombre
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre completo") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzulPrincipal,
                            unfocusedBorderColor = GrisClaro
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = AzulPrincipal
                            )
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Grado
                    OutlinedTextField(
                        value = grado,
                        onValueChange = { },
                        label = { Text("Grado") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showGradoSelector = true },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzulPrincipal,
                            unfocusedBorderColor = GrisClaro
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = AzulPrincipal
                            )
                        },
                        readOnly = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Grupo
                    OutlinedTextField(
                        value = grupo,
                        onValueChange = { },
                        label = { Text("Grupo") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showGrupoSelector = true },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzulPrincipal,
                            unfocusedBorderColor = GrisClaro
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = AzulPrincipal
                            )
                        },
                        readOnly = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Código
                    OutlinedTextField(
                        value = codigo,
                        onValueChange = { codigo = it },
                        label = { Text("Código de estudiante") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzulPrincipal,
                            unfocusedBorderColor = GrisClaro
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = AzulPrincipal
                            )
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Mensaje de error
                    if (error != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = RojoError.copy(alpha = 0.1f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RojoError)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = RojoError,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = error!!,
                                    color = RojoError,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Botón de completar perfil
                    Button(
                        onClick = {
                            if (nombre.isBlank() || grado.isBlank() || grupo.isBlank() || codigo.isBlank()) {
                                error = "Por favor completa todos los campos"
                                return@Button
                            }
                            
                            scope.launch {
                                isLoading = true
                                error = null
                                
                                try {
                                    val result = googleAuthService.updateUserProfile(
                                        user.email,
                                        nombre,
                                        grupo,
                                        grado,
                                        codigo
                                    )
                                    
                                    if (result.isSuccess) {
                                        val updatedUser = user.copy(
                                            nombreCompleto = nombre,
                                            grupo = grupo,
                                            grado = grado,
                                            codigoGrado = codigo
                                        )
                                        onProfileComplete(updatedUser)
                                    } else {
                                        error = "Error al guardar el perfil: ${result.exceptionOrNull()?.message}"
                                    }
                                } catch (e: Exception) {
                                    error = "Error: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AzulPrincipal
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "Completar Perfil",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
        
        // Selector de grado
        if (showGradoSelector) {
            AlertDialog(
                onDismissRequest = { showGradoSelector = false },
                title = { Text("Seleccionar Grado") },
                text = {
                    Column {
                        grados.forEach { gradoOption ->
                            TextButton(
                                onClick = {
                                    grado = gradoOption
                                    showGradoSelector = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(gradoOption)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showGradoSelector = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        // Selector de grupo
        if (showGrupoSelector) {
            AlertDialog(
                onDismissRequest = { showGrupoSelector = false },
                title = { Text("Seleccionar Grupo") },
                text = {
                    Column {
                        grupos.forEach { grupoOption ->
                            TextButton(
                                onClick = {
                                    grupo = grupoOption
                                    showGrupoSelector = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(grupoOption)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showGrupoSelector = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
} 