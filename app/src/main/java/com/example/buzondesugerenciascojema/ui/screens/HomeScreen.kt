package com.example.buzondesugerenciascojema.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.buzondesugerenciascojema.R
import com.example.buzondesugerenciascojema.data.UsuarioManager
import com.example.buzondesugerenciascojema.ui.components.DrawerContent
import com.example.buzondesugerenciascojema.ui.components.FloatingAssistantButton
import com.example.buzondesugerenciascojema.viewmodels.NotificacionViewModel
import com.example.buzondesugerenciascojema.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import com.example.buzondesugerenciascojema.util.SoundGenerator

// Data class para los elementos del menú
data class MenuItem(
    val title: String,
    val icon: Any, // ImageVector o Painter
    val color: Color,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    usuarioManager: UsuarioManager,
    notificacionViewModel: NotificacionViewModel,
    mantenerSesion: Boolean
) {
    val currentUser by usuarioManager.currentUser.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current
    val soundGenerator = remember { SoundGenerator(context) }

    // Estados para animaciones
    var showContent by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf<String?>(null) }
    
    // Animaciones mejoradas
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Efecto para mostrar contenido con animación
    LaunchedEffect(Unit) {
        showContent = true
    }

    // Cargar notificaciones
    LaunchedEffect(currentUser) {
        currentUser?.email?.let { email ->
            notificacionViewModel.actualizarContadorNoLeidas(email)
        }
    }

    // Configuración de pantalla
    DisposableEffect(configuration) {
        onDispose { }
    }

    val drawerContent = @Composable {
        DrawerContent(
            navController = navController,
            usuario = currentUser,
            authService = usuarioManager.authService
        )
    }

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = Color.White,
                drawerContentColor = Color(0xFF6C63FF)
            ) {
                drawerContent()
            }
        },
        drawerState = drawerState,
        gesturesEnabled = true
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = GradientePrimario
                    )
                )
        ) {
            // TopAppBar mejorada
            TopAppBar(
                modifier = Modifier.padding(top = 8.dp),
                title = {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(animationSpec = tween(800)) + slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = tween(800)
                        ),
                        exit = fadeOut() + slideOutHorizontally(
                            targetOffsetX = { -it },
                            animationSpec = tween(800)
                        )
                    ) {
                        Text(
                            text = "EduBox COJEMA",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            soundGenerator.playClick()
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menú",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Badge de notificaciones
                    currentUser?.email?.let { email ->
                        val cantidadNoLeidas by notificacionViewModel.cantidadNoLeidas.collectAsState()
                        Box(contentAlignment = Alignment.TopEnd) {
                            IconButton(onClick = { 
                                soundGenerator.playClick()
                                navController.navigate("notificaciones") 
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.notificacion),
                                    contentDescription = "Notificaciones",
                                    tint = Color.White
                                )
                            }
                            if (cantidadNoLeidas > 0) {
                                Badge(
                                    containerColor = RojoError,
                                    contentColor = Color.White,
                                    modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                                ) {
                                    Text(
                                        text = if (cantidadNoLeidas > 99) "99+" else cantidadNoLeidas.toString(),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(top = 96.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo animado mejorado
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(1000)) + scaleIn(
                        initialScale = 0.3f,
                        animationSpec = tween(1000, easing = EaseOutBack)
                    ),
                    exit = fadeOut() + scaleOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Perfil",
                            modifier = Modifier.size(100.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Mensaje de bienvenida personalizado
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(1200)) + slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(1200)
                    ),
                    exit = fadeOut() + slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(1200)
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¡Bienvenido a EduBox!",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            textAlign = TextAlign.Center
                        )
                        currentUser?.let { user ->
                            Text(
                                text = user.nombreCompleto,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${user.grado}° ${user.grupo}",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Grid de opciones mejorado
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(1400)) + slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(1400)
                    ),
                    exit = fadeOut() + slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(1400)
                    )
                ) {
                    val menuItems = listOf(
                        MenuItem(
                            title = if (currentUser?.esAdmin == true) "Panel de Administración" else "Buzón de Sugerencias",
                            icon = if (currentUser?.esAdmin == true) Icons.Filled.Settings else painterResource(id = R.drawable.buzon),
                            color = if (currentUser?.esAdmin == true) VerdeExito else AzulPrincipal,
                            onClick = {
                                if (currentUser != null &&
                                    !currentUser!!.grado.isNullOrBlank() &&
                                    !currentUser!!.grupo.isNullOrBlank() &&
                                    !currentUser!!.email.isNullOrBlank()
                                ) {
                                    if (currentUser!!.esAdmin) {
                                        navController.navigate("admin/${currentUser!!.grado}/${currentUser!!.grupo}")
                                    } else {
                                        navController.navigate("buzon/${currentUser!!.grado}/${currentUser!!.grupo}/${currentUser!!.email}")
                                    }
                                } else {
                                    showError = "No se pudo obtener todos los datos del usuario. Intenta de nuevo en unos segundos."
                                }
                            }
                        ),
                        MenuItem(
                            title = "Biblioteca Virtual",
                            icon = painterResource(id = R.drawable.libro),
                            color = VerdeExito,
                            onClick = { navController.navigate("biblioteca") }
                        ),
                        MenuItem(
                            title = "Calendario",
                            icon = painterResource(id = R.drawable.calendario),
                            color = AmarilloDestacado,
                            onClick = { navController.navigate("calendario") }
                        ),
                        MenuItem(
                            title = "Minijuegos",
                            icon = painterResource(id = R.drawable.gamepad),
                            color = RojoError,
                            onClick = { navController.navigate("minijuegos") }
                        )
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.height(400.dp)
                    ) {
                        items(menuItems) { item ->
                            MenuCard(
                                item = item,
                                soundGenerator = soundGenerator,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            )
                        }
                    }
                }

                // Mensaje de error
                if (showError != null) {
                    AnimatedVisibility(
                        visible = showError != null,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
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
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = showError!!,
                                    color = RojoError,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = { 
                                        soundGenerator.playClick()
                                        showError = null 
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Cerrar",
                                        tint = RojoError
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Botón flotante del asistente
            FloatingAssistantButton(
                onClick = { navController.navigate("assistant") },
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
fun MenuCard(
    item: MenuItem,
    soundGenerator: SoundGenerator,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .clickable {
                soundGenerator.playClick()
                item.onClick()
                // Reset del estado después de un breve delay
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    kotlinx.coroutines.delay(150)
                    isPressed = false
                }
            }
            .graphicsLayer(scaleX = if (isPressed) 0.95f else 1f, scaleY = if (isPressed) 0.95f else 1f),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            item.color.copy(alpha = 0.1f),
                            item.color.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (item.icon) {
                    is androidx.compose.ui.graphics.vector.ImageVector -> {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = item.color,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    is androidx.compose.ui.graphics.painter.Painter -> {
                        Icon(
                            painter = item.icon,
                            contentDescription = item.title,
                            tint = item.color,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.title,
                    color = TextoPrimario,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}