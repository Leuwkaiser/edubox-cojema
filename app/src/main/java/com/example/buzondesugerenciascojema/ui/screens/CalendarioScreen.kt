package com.example.buzondesugerenciascojema.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.buzondesugerenciascojema.data.EventoService
import com.example.buzondesugerenciascojema.model.Evento
import com.example.buzondesugerenciascojema.data.Usuario
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.graphicsLayer
import android.widget.Toast
import android.content.Context
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioScreen(
    navController: NavController,
    usuario: Usuario?
) {
    val eventoService = remember { EventoService() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var eventos by remember { mutableStateOf<List<Evento>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var eventoEditando by remember { mutableStateOf<Evento?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Evento?>(null) }
    var selectedDate by remember {
        mutableStateOf(Calendar.getInstance())
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDayEvents by remember { mutableStateOf<Pair<Date, List<Evento>>?>(null) }
    
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale("es", "ES")) }
    val dayFormat = remember { SimpleDateFormat("d", Locale.getDefault()) }
    
    val hoy = Calendar.getInstance()
    val currentYear = selectedDate.get(Calendar.YEAR)

    // Cargar eventos
    LaunchedEffect(Unit) {
        eventos = eventoService.obtenerEventos().sortedBy { it.fecha }
    }

    // Generar días del mes
    val daysInMonth = selectedDate.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = Calendar.getInstance().apply {
        set(currentYear, selectedDate.get(Calendar.MONTH), 1)
    }.get(Calendar.DAY_OF_WEEK)
    
    // Ajustar para que la semana empiece en lunes (como en la imagen)
    // Calendar.DAY_OF_WEEK: 1=Dom, 2=Lun, ..., 7=Sáb
    val adjustedFirstDay = if (firstDayOfMonth == Calendar.SUNDAY) 6 else firstDayOfMonth - 2
    
    val calendarDays = mutableListOf<CalendarDay>()
    
    // Agregar días vacíos al inicio
    repeat(adjustedFirstDay) {
        calendarDays.add(CalendarDay.Empty)
    }
    
    // Agregar días del mes
    for (day in 1..daysInMonth) {
        val calendar = Calendar.getInstance().apply {
            set(currentYear, selectedDate.get(Calendar.MONTH), day)
        }
        val date = calendar.time
        val dayEvents = eventos.filter { 
            dateFormat.format(it.fecha) == dateFormat.format(date)
        }
        calendarDays.add(CalendarDay.Day(day, date, dayEvents))
    }

    // Aviso de evento hoy o mañana
    val eventoHoy = eventos.find { dateFormat.format(it.fecha) == dateFormat.format(hoy.time) }
    val eventoManana = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.let { manana ->
        eventos.find { dateFormat.format(it.fecha) == dateFormat.format(manana.time) }
    }
    
    if (eventoHoy != null) {
        LaunchedEffect(eventoHoy.titulo) {
            if (usuario != null && !eventoHoy.notificados.contains(usuario.email)) {
                eventoService.marcarNotificado(eventoHoy.id, usuario.email)
            }
        }
        SnackbarHost(hostState = remember { SnackbarHostState() }) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = Color(0xFF6C63FF),
                contentColor = Color.White
            ) { Text("¡Hoy hay un evento: ${eventoHoy.titulo}!") }
        }
    } else if (eventoManana != null) {
        LaunchedEffect(eventoManana.titulo) {
            if (usuario != null && !eventoManana.notificados.contains(usuario.email)) {
                eventoService.marcarNotificado(eventoManana.id, usuario.email)
            }
        }
        SnackbarHost(hostState = remember { SnackbarHostState() }) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = Color(0xFF42A5F5),
                contentColor = Color.White
            ) { Text("¡Mañana hay un evento: ${eventoManana.titulo}!") }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendario", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = com.example.buzondesugerenciascojema.R.drawable.calendario),
                            contentDescription = "Volver",
                            modifier = Modifier.size(28.dp),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6C63FF))
            )
        },
        floatingActionButton = {
            if (usuario?.esAdmin == true) {
                FloatingActionButton(
                    onClick = { 
                        eventoEditando = null
                        showDialog = true 
                    }, 
                    containerColor = Color(0xFF6C63FF)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar evento", tint = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Header del calendario con navegación
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newDate = Calendar.getInstance()
                            newDate.time = selectedDate.time
                            newDate.add(Calendar.MONTH, -1)
                            selectedDate = newDate
                        },
                        modifier = Modifier
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Mes anterior")
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = monthFormat.format(selectedDate.time).replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF6C63FF)
                        )
                        Text(
                            text = selectedDate.get(Calendar.YEAR).toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.DarkGray
                        )
                    }
                    IconButton(
                        onClick = {
                            val newDate = Calendar.getInstance()
                            newDate.time = selectedDate.time
                            newDate.add(Calendar.MONTH, 1)
                            selectedDate = newDate
                        },
                        modifier = Modifier
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Mes siguiente")
                    }
                }
            }

            // Días de la semana
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cuadrícula del calendario
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                items(calendarDays.size) { index ->
                    val calendarDay = calendarDays[index]
                    when (calendarDay) {
                        is CalendarDay.Empty -> {
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .background(Color(0xFFF5F5F5))
                            )
                        }
                        is CalendarDay.Day -> {
                            val isToday = dateFormat.format(calendarDay.date) == dateFormat.format(hoy.time)
                            val isCurrentMonth = calendarDay.date.month == selectedDate.get(Calendar.MONTH)
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .background(
                                        when {
                                            isToday -> Color(0xFF8E24AA)
                                            isCurrentMonth -> Color.White
                                            else -> Color(0xFFF0F0F0)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFFE0E0E0)
                                    )
                                    .clickable {
                                        if (usuario?.esAdmin == true) {
                                            eventoEditando = null
                                            showDialog = true
                                        }
                                    }
                                    .padding(4.dp)
                            ) {
                                Column {
                                    Text(
                                        text = calendarDay.day.toString(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            isToday -> Color.White
                                            isCurrentMonth -> Color.Black
                                            else -> Color.Gray
                                        }
                                    )
                                    if (calendarDay.events.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(Color(0xFF42A5F5), CircleShape)
                                                .align(Alignment.Center)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                // Mostrar eventos del día en un diálogo
                                                selectedDayEvents = Pair(calendarDay.date, calendarDay.events)
                                            }
                                    ) {
                                        Text(
                                            text = "${calendarDay.events.size} evento${if (calendarDay.events.size > 1) "s" else ""}",
                                            fontSize = 8.sp,
                                            color = Color(0xFF42A5F5),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de eventos del mes seleccionado
            Text(
                text = "Eventos de ${monthFormat.format(selectedDate.time).replaceFirstChar { it.uppercase() }}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Calcular eventos del mes fuera del LazyColumn
            val eventosDelMes = eventos.filter {
                it.fecha.month == selectedDate.get(Calendar.MONTH) && it.fecha.year + 1900 == currentYear
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(eventosDelMes.size) { index ->
                    val evento = eventosDelMes[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (dateFormat.format(evento.fecha) == dateFormat.format(hoy.time))
                                Color(0xFF6C63FF) else Color(0xFFF5F5F5)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = evento.titulo,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dateFormat.format(evento.fecha) == dateFormat.format(hoy.time))
                                        Color.White else Color.Black
                                )
                                Text(
                                    text = evento.descripcion,
                                    color = if (dateFormat.format(evento.fecha) == dateFormat.format(hoy.time))
                                        Color.White else Color.DarkGray
                                )
                                Text(
                                    text = "Fecha: ${dateFormat.format(evento.fecha)}",
                                    color = if (dateFormat.format(evento.fecha) == dateFormat.format(hoy.time))
                                        Color.White else Color.Gray
                                )
                            }
                            if (usuario?.esAdmin == true) {
                                IconButton(onClick = { eventoEditando = evento; showDialog = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }
                                IconButton(onClick = { showDeleteDialog = evento }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo para agregar/editar evento
    if (showDialog) {
        var titulo by remember { mutableStateOf(eventoEditando?.titulo ?: "") }
        var descripcion by remember { mutableStateOf(eventoEditando?.descripcion ?: "") }
        var fecha by remember { mutableStateOf<Date?>(eventoEditando?.fecha) }
        var showDatePickerDialog by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (eventoEditando == null) "Agregar evento" else "Editar evento") },
            text = {
                Column {
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        label = { Text("Título") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = fecha?.let { dateFormat.format(it) } ?: "",
                        onValueChange = {},
                        label = { Text("Fecha") },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showDatePickerDialog = true }) {
                                Icon(
                                    painter = painterResource(id = com.example.buzondesugerenciascojema.R.drawable.calendario),
                                    contentDescription = "Seleccionar fecha"
                                )
                            }
                        },
                        placeholder = { Text("Seleccione una fecha") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    when {
                        titulo.isBlank() -> {
                            Toast.makeText(context, "Por favor ingrese un título", Toast.LENGTH_SHORT).show()
                        }
                        descripcion.isBlank() -> {
                            Toast.makeText(context, "Por favor ingrese una descripción", Toast.LENGTH_SHORT).show()
                        }
                        fecha == null -> {
                            Toast.makeText(context, "Por favor seleccione una fecha", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            scope.launch {
                                if (eventoEditando == null) {
                                    eventoService.agregarEvento(Evento(titulo = titulo, descripcion = descripcion, fecha = fecha!!, creadoPor = usuario?.email ?: ""))
                                } else {
                                    eventoService.editarEvento(eventoEditando!!.copy(titulo = titulo, descripcion = descripcion, fecha = fecha!!))
                                }
                                eventos = eventoService.obtenerEventos().sortedBy { it.fecha }
                                showDialog = false
                            }
                        }
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
        
        // DatePicker simple
        if (showDatePickerDialog) {
            var selectedYear by remember { mutableStateOf(fecha?.year?.plus(1900)?.toString() ?: "") }
            var selectedMonth by remember { mutableStateOf((fecha?.month?.plus(1) ?: "").toString()) }
            var selectedDay by remember { mutableStateOf(fecha?.date?.toString() ?: "") }
            
            AlertDialog(
                onDismissRequest = { showDatePickerDialog = false },
                title = { Text("Seleccionar fecha") },
                text = {
                    Column {
                        // Selector de año
                        OutlinedTextField(
                            value = selectedYear,
                            onValueChange = { selectedYear = it },
                            label = { Text("Año") },
                            placeholder = { Text("Ej: 2024") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Selector de mes
                        OutlinedTextField(
                            value = selectedMonth,
                            onValueChange = { selectedMonth = it },
                            label = { Text("Mes (1-12)") },
                            placeholder = { Text("Ej: 12") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Selector de día
                        OutlinedTextField(
                            value = selectedDay,
                            onValueChange = { selectedDay = it },
                            label = { Text("Día") },
                            placeholder = { Text("Ej: 25") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        // Mostrar fecha seleccionada
                        if (selectedYear.isNotBlank() && selectedMonth.isNotBlank() && selectedDay.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            val year = selectedYear.toIntOrNull()
                            val month = selectedMonth.toIntOrNull()?.minus(1)
                            val day = selectedDay.toIntOrNull()
                            
                            if (year != null && month != null && month in 0..11 && day != null) {
                                val maxDays = Calendar.getInstance().apply { 
                                    set(year, month, 1) 
                                }.getActualMaximum(Calendar.DAY_OF_MONTH)
                                
                                if (day in 1..maxDays) {
                                    val previewDate = Calendar.getInstance().apply {
                                        set(year, month, day)
                                    }
                                    Text(
                                        text = "Fecha seleccionada: ${dateFormat.format(previewDate.time)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF6C63FF),
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "Día inválido para este mes",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Red
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val year = selectedYear.toIntOrNull()
                        val month = selectedMonth.toIntOrNull()?.minus(1)
                        val day = selectedDay.toIntOrNull()
                        
                        if (year != null && month != null && month in 0..11 && day != null) {
                            val maxDays = Calendar.getInstance().apply { 
                                set(year, month, 1) 
                            }.getActualMaximum(Calendar.DAY_OF_MONTH)
                            
                            if (day in 1..maxDays) {
                                val calendar = Calendar.getInstance()
                                calendar.set(year, month, day)
                                fecha = calendar.time
                                showDatePickerDialog = false
                            } else {
                                Toast.makeText(context, "Fecha inválida", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Por favor complete todos los campos correctamente", Toast.LENGTH_SHORT).show()
                        }
                    }) { Text("Aceptar") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePickerDialog = false }) { Text("Cancelar") }
                }
            )
        }
    }

    // Diálogo de confirmación para eliminar
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("¿Eliminar evento?") },
            text = { Text("¿Estás seguro de que deseas eliminar este evento?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        eventoService.eliminarEvento(showDeleteDialog!!.id)
                        eventos = eventoService.obtenerEventos().sortedBy { it.fecha }
                        showDeleteDialog = null
                    }
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo para mostrar eventos del día
    selectedDayEvents?.let { (date, events) ->
        AlertDialog(
            onDismissRequest = { selectedDayEvents = null },
            title = {
                Text(
                    text = "Eventos del ${dateFormat.format(date)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF6C63FF)
                )
            },
            text = {
                LazyColumn {
                    items(events.size) { index ->
                        val evento = events[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F5F5)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = evento.titulo,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF6C63FF)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = evento.descripcion,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.DarkGray
                                )
                                if (usuario?.esAdmin == true) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(
                                            onClick = {
                                                eventoEditando = evento
                                                showDialog = true
                                                selectedDayEvents = null
                                            }
                                        ) {
                                            Text("Editar")
                                        }
                                        TextButton(
                                            onClick = {
                                                showDeleteDialog = evento
                                                selectedDayEvents = null
                                            }
                                        ) {
                                            Text("Eliminar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedDayEvents = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

sealed class CalendarDay {
    object Empty : CalendarDay()
    data class Day(val day: Int, val date: Date, val events: List<Evento>) : CalendarDay()
}