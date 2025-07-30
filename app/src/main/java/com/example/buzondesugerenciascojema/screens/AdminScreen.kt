package com.example.buzondesugerenciascojema.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.buzondesugerenciascojema.viewmodels.AdminViewModel
import com.example.buzondesugerenciascojema.components.SugerenciaItem
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AdminScreen(
    navController: NavHostController,
    grado: String,
    grupo: String,
    viewModel: AdminViewModel = viewModel()
) {
    val sugerencias by viewModel.sugerencias.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(grado, grupo) {
        viewModel.cargarSugerencias(grado, grupo)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sugerencias) { sugerencia ->
            SugerenciaItem(
                sugerencia = sugerencia,
                onVotar = { esLike ->
                    viewModel.votarSugerencia(sugerencia.id, currentUserId, esLike)
                },
                onEliminar = {
                    viewModel.borrarSugerencia(sugerencia.id, currentUserId)
                },
                esAdmin = true
            )
        }
    }
} 