package com.example.buzondesugerenciascojema.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.buzondesugerenciascojema.R
import com.example.buzondesugerenciascojema.data.Usuario
import com.example.buzondesugerenciascojema.data.UsuarioManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    navController: NavController,
    usuarioManager: UsuarioManager,
    usuario: Usuario
) {
    var mostrarDialogoEditar by remember { mutableStateOf(false) }
    var mostrarDialogoCambiarFoto by remember { mutableStateOf(false) }
    var mostrarDialogoCambiarEmail by remember { mutableStateOf(false) }
    var mostrarDialogoCambiarPassword by remember { mutableStateOf(false) }
    var nombreEditado by remember { mutableStateOf(usuario.nombreCompleto) }
    var emailEditado by remember { mutableStateOf(usuario.email) }
    var passwordActual by remember { mutableStateOf("") }
    var passwordNueva by remember { mutableStateOf("") }
    var passwordConfirmar by remember { mutableStateOf("") }
    var fotoPerfilUri by remember { mutableStateOf<Uri?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(Unit) { showContent = true }

    if (isLandscape) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Información personal a la izquierda
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Avatar animado
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(800)) + scaleIn(initialScale = 0.7f, animationSpec = tween(800)),
                    exit = fadeOut() + scaleOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE040FB))
                            .border(4.dp, Color.White, CircleShape)
                            .clickable { mostrarDialogoCambiarFoto = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (fotoPerfilUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(fotoPerfilUri)
                                        .crossfade(true)
                                        .build()
                                ),
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (usuario.fotoPerfil.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(usuario.fotoPerfil)
                                        .crossfade(true)
                                        .build()
                                ),
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = usuario.nombreCompleto.split(" ").let { 
                                    it.firstOrNull()?.firstOrNull().toString() + (it.getOrNull(1)?.firstOrNull()?.toString() ?: "") 
                                }.uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 48.sp
                            )
                        }
                        
                        // Icono de editar superpuesto
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(36.dp)
                                .background(
                                    color = Color(0xFF6C63FF),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar foto",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Nombre del usuario
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(1000)),
                    exit = fadeOut()
                ) {
                    Text(
                        text = usuario.nombreCompleto,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Email del usuario
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(1200)),
                    exit = fadeOut()
                ) {
                    Text(
                        text = usuario.email,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Información del perfil
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(1400)),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.15f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Información Personal",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            InfoPerfilItem("Nombre", usuario.nombreCompleto, Icons.Default.Person) {
                                nombreEditado = usuario.nombreCompleto
                                mostrarDialogoEditar = true
                            }
                            
                            InfoPerfilItem("Email", usuario.email, Icons.Default.Email) {
                                emailEditado = usuario.email
                                mostrarDialogoCambiarEmail = true
                            }
                            
                            InfoPerfilItem("Contraseña", "••••••••", Icons.Default.Lock) {
                                mostrarDialogoCambiarPassword = true
                            }
                            
                            InfoPerfilItem("Grado", usuario.grado, Icons.Default.Star)
                            InfoPerfilItem("Grupo", usuario.grupo, Icons.Default.Person)
                            InfoPerfilItem("Rol", if (usuario.esAdmin) "Administrador" else "Estudiante", Icons.Default.Info)
                        }
                    }
                }
            }
            // Botones de acción a la derecha
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Diálogo para editar nombre
                if (mostrarDialogoEditar) {
                    AlertDialog(
                        onDismissRequest = { mostrarDialogoEditar = false },
                        title = { 
                            Text(
                                "Editar Nombre",
                                color = Color(0xFF6C63FF),
                                fontWeight = FontWeight.Bold
                            ) 
                        },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = nombreEditado,
                                    onValueChange = { nombreEditado = it },
                                    label = { Text("Nombre completo") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    scope.launch {
                                        try {
                                            isLoading = true
                                            val usuarioActualizado = usuario.copy(nombreCompleto = nombreEditado)
                                            usuarioManager.actualizarUsuario(usuarioActualizado)
                                            mostrarDialogoEditar = false
                                        } catch (e: Exception) {
                                            error = "Error al actualizar: ${e.message}"
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                },
                                enabled = !isLoading && nombreEditado.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text(
                                        "Guardar",
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    mostrarDialogoEditar = false
                                    nombreEditado = usuario.nombreCompleto
                                }
                            ) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                // Diálogo para cambiar email
                if (mostrarDialogoCambiarEmail) {
                    AlertDialog(
                        onDismissRequest = { mostrarDialogoCambiarEmail = false },
                        title = { 
                            Text(
                                "Cambiar Email",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            ) 
                        },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = emailEditado,
                                    onValueChange = { emailEditado = it },
                                    label = { Text("Nuevo email") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    scope.launch {
                                        try {
                                            isLoading = true
                                            // Aquí iría la lógica para cambiar el email
                                            // Por ahora solo actualizamos el usuario local
                                            val usuarioActualizado = usuario.copy(email = emailEditado)
                                            usuarioManager.actualizarUsuario(usuarioActualizado)
                                            mostrarDialogoCambiarEmail = false
                                        } catch (e: Exception) {
                                            error = "Error al cambiar email: ${e.message}"
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                },
                                enabled = !isLoading && emailEditado.isNotBlank() && emailEditado != usuario.email,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text(
                                        "Cambiar",
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    mostrarDialogoCambiarEmail = false
                                    emailEditado = usuario.email
                                }
                            ) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                // Diálogo para cambiar contraseña
                if (mostrarDialogoCambiarPassword) {
                    AlertDialog(
                        onDismissRequest = { mostrarDialogoCambiarPassword = false },
                        title = { 
                            Text(
                                "Cambiar Contraseña",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            ) 
                        },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = passwordActual,
                                    onValueChange = { passwordActual = it },
                                    label = { Text("Contraseña actual") },
                                    modifier = Modifier.fillMaxWidth(),
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = passwordNueva,
                                    onValueChange = { passwordNueva = it },
                                    label = { Text("Nueva contraseña") },
                                    modifier = Modifier.fillMaxWidth(),
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = passwordConfirmar,
                                    onValueChange = { passwordConfirmar = it },
                                    label = { Text("Confirmar nueva contraseña") },
                                    modifier = Modifier.fillMaxWidth(),
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    scope.launch {
                                        try {
                                            isLoading = true
                                            // Aquí iría la lógica para cambiar la contraseña
                                            // Por ahora solo cerramos el diálogo
                                            mostrarDialogoCambiarPassword = false
                                        } catch (e: Exception) {
                                            error = "Error al cambiar contraseña: ${e.message}"
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                },
                                enabled = !isLoading && passwordActual.isNotBlank() && passwordNueva.isNotBlank() && passwordNueva == passwordConfirmar,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text(
                                        "Cambiar",
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    mostrarDialogoCambiarPassword = false
                                    passwordActual = ""
                                    passwordNueva = ""
                                    passwordConfirmar = ""
                                }
                            ) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                // Diálogo para cambiar foto
                if (mostrarDialogoCambiarFoto) {
                    AlertDialog(
                        onDismissRequest = { mostrarDialogoCambiarFoto = false },
                        title = { 
                            Text(
                                "Cambiar Foto de Perfil",
                                color = Color(0xFF6C63FF),
                                fontWeight = FontWeight.Bold
                            ) 
                        },
                        text = {
                            Text("Función en desarrollo. Próximamente podrás cambiar tu foto de perfil.")
                        },
                        confirmButton = {
                            TextButton(
                                onClick = { mostrarDialogoCambiarFoto = false }
                            ) {
                                Text("Entendido")
                            }
                        }
                    )
                }
            }
        }
    } else {
        // Layout original en Column
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TopBar personalizada
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigateUp() }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(800)),
                    exit = fadeOut()
                ) {
                    Text(
                        text = "Mi Perfil",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            }

            // Contenido principal
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar animado
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(800)) + scaleIn(initialScale = 0.7f, animationSpec = tween(800)),
                    exit = fadeOut() + scaleOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE040FB))
                            .border(4.dp, Color.White, CircleShape)
                            .clickable { mostrarDialogoCambiarFoto = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (fotoPerfilUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(fotoPerfilUri)
                                        .crossfade(true)
                                        .build()
                                ),
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (usuario.fotoPerfil.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(usuario.fotoPerfil)
                                        .crossfade(true)
                                        .build()
                                ),
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = usuario.nombreCompleto.split(" ").let { 
                                    it.firstOrNull()?.firstOrNull().toString() + (it.getOrNull(1)?.firstOrNull()?.toString() ?: "") 
                                }.uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 48.sp
                            )
                        }
                        
                        // Icono de editar superpuesto
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(36.dp)
                                .background(
                                    color = Color(0xFF6C63FF),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar foto",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Nombre del usuario
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(1000)),
                    exit = fadeOut()
                ) {
                    Text(
                        text = usuario.nombreCompleto,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Email del usuario
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(1200)),
                    exit = fadeOut()
                ) {
                    Text(
                        text = usuario.email,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Información del perfil
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(1400)),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.15f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Información Personal",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            InfoPerfilItem("Nombre", usuario.nombreCompleto, Icons.Default.Person) {
                                nombreEditado = usuario.nombreCompleto
                                mostrarDialogoEditar = true
                            }
                            
                            InfoPerfilItem("Email", usuario.email, Icons.Default.Email) {
                                emailEditado = usuario.email
                                mostrarDialogoCambiarEmail = true
                            }
                            
                            InfoPerfilItem("Contraseña", "••••••••", Icons.Default.Lock) {
                                mostrarDialogoCambiarPassword = true
                            }
                            
                            InfoPerfilItem("Grado", usuario.grado, Icons.Default.Star)
                            InfoPerfilItem("Grupo", usuario.grupo, Icons.Default.Person)
                            InfoPerfilItem("Rol", if (usuario.esAdmin) "Administrador" else "Estudiante", Icons.Default.Info)
                        }
                    }
                }
            }
        }
    }

    // Mostrar errores
    if (error != null) {
        LaunchedEffect(error) {
            kotlinx.coroutines.delay(3000)
            error = null
        }
    }
}

@Composable
fun InfoPerfilItem(
    label: String, 
    value: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Editar",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
} 