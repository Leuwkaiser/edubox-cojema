package com.example.buzondesugerenciascojema.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.buzondesugerenciascojema.data.Usuario
import com.example.buzondesugerenciascojema.data.AuthService
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import com.example.buzondesugerenciascojema.util.SoundGenerator

@Composable
fun DrawerContent(
    navController: NavController,
    usuario: Usuario?,
    authService: AuthService,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val soundGenerator = remember { SoundGenerator(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            item {
                // Encabezado del menú con información del usuario
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(bottom = 8.dp),
                        tint = Color(0xFF6C63FF)
                    )
                    
                    Text(
                        text = usuario?.nombreCompleto ?: "Usuario",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 4.dp),
                        color = Color(0xFF6C63FF)
                    )
                    
                    Text(
                        text = "${usuario?.grado}° ${usuario?.grupo}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6C63FF)
                    )
                    
                    // Mostrar rol del usuario
                    if (usuario?.esAdmin == true) {
                        Text(
                            text = "Administrador",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Opción Editar Perfil
            item {
                NavigationDrawerItem(
                    icon = { 
                        Icon(
                            Icons.Default.Person, 
                            contentDescription = "Editar Perfil",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    },
                    label = { 
                        Text(
                            "Editar Perfil",
                            color = Color(0xFF6C63FF)
                        ) 
                    },
                    selected = false,
                    onClick = {
                        soundGenerator.playClick()
                        navController.navigate("perfil")
                    },
                    modifier = Modifier.padding(vertical = 4.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color.White,
                        unselectedContainerColor = Color.White,
                        selectedIconColor = Color(0xFF6C63FF),
                        unselectedIconColor = Color(0xFF6C63FF),
                        selectedTextColor = Color(0xFF6C63FF),
                        unselectedTextColor = Color(0xFF6C63FF)
                    )
                )
            }

            // Botón de cerrar sesión
            item {
                NavigationDrawerItem(
                    icon = { 
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp, 
                            contentDescription = "Cerrar Sesión",
                            tint = MaterialTheme.colorScheme.error
                        ) 
                    },
                    label = { 
                        Text(
                            "Cerrar Sesión",
                            color = MaterialTheme.colorScheme.error
                        ) 
                    },
                    selected = false,
                    onClick = {
                        soundGenerator.playClick()
                        scope.launch {
                            // Eliminar token FCM de Firestore antes de cerrar sesión
                            val currentEmail = authService.currentUser?.email
                            authService.removeFcmTokenFromFirestore(currentEmail)
                            authService.signOut()
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.padding(vertical = 4.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color.White,
                        unselectedContainerColor = Color.White,
                        selectedIconColor = Color(0xFF6C63FF),
                        unselectedIconColor = Color(0xFF6C63FF),
                        selectedTextColor = Color(0xFF6C63FF),
                        unselectedTextColor = Color(0xFF6C63FF)
                    )
                )
            }
        }
        
        // Botón Acerca de EduBox fijo en la parte inferior
        Spacer(modifier = Modifier.weight(1f))
        
        Divider(
            color = Color(0xFF6C63FF).copy(alpha = 0.3f), 
            thickness = 1.dp, 
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        NavigationDrawerItem(
            icon = {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Acerca de EduBox",
                    tint = Color(0xFF6C63FF)
                )
            },
            label = {
                Text(
                    "Acerca de EduBox",
                    color = Color(0xFF6C63FF)
                )
            },
            selected = false,
            onClick = {
                soundGenerator.playClick()
                navController.navigate("acerca-edubox")
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = Color.White,
                unselectedContainerColor = Color.White,
                selectedIconColor = Color(0xFF6C63FF),
                unselectedIconColor = Color(0xFF6C63FF),
                selectedTextColor = Color(0xFF6C63FF),
                unselectedTextColor = Color(0xFF6C63FF)
            )
        )
    }
}