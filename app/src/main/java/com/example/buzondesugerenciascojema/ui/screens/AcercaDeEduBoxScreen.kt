package com.example.buzondesugerenciascojema.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcercaDeEduBoxScreen(onBackPressed: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acerca de EduBox", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6C63FF))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "EduBox",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6C63FF),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Fundadores de Digital Dreamer:",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF6C63FF),
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            Text(
                text = "• Jorge Ramos\n• Vidal Herrera\n• Samuel Narváez",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Divider(color = Color(0xFF6C63FF), thickness = 1.dp)
            Text(
                text = "Política de la aplicación:",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF6C63FF),
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
            Text(
                text = "EduBox respeta la privacidad de los usuarios, protege sus datos y promueve un ambiente seguro y educativo. No compartimos información personal con terceros y fomentamos el uso responsable de la tecnología en el entorno escolar.",
                fontSize = 15.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Divider(color = Color(0xFF6C63FF), thickness = 1.dp)
            Text(
                text = "Objetivo:",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF6C63FF),
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
            Text(
                text = "Brindar a la comunidad educativa una plataforma integral para sugerencias, juegos, biblioteca virtual y asistencia inteligente, fortaleciendo la participación, el aprendizaje y la innovación en el colegio.",
                fontSize = 15.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Divider(color = Color(0xFF6C63FF), thickness = 1.dp)
            Text(
                text = "Visión:",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF6C63FF),
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
            Text(
                text = "Ser la aplicación educativa líder en Colombia, reconocida por su innovación, seguridad y capacidad de transformar la experiencia escolar a través de la tecnología y la inteligencia artificial.",
                fontSize = 15.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Divider(color = Color(0xFF6C63FF), thickness = 1.dp)
            Text(
                text = "Misión:",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF6C63FF),
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
            Text(
                text = "Facilitar la comunicación, el aprendizaje y la gestión escolar mediante herramientas digitales accesibles, seguras y adaptadas a las necesidades de estudiantes, docentes y directivos.",
                fontSize = 15.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Divider(color = Color(0xFF6C63FF), thickness = 1.dp)
            Text(
                text = "Sobre EduBox:",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF6C63FF),
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
            Text(
                text = "EduBox es una aplicación desarrollada por Digital Dreamer para el Colegio COJEMA, integrando sugerencias, biblioteca, juegos y un asistente virtual inteligente. Su propósito es empoderar a la comunidad educativa y fomentar la innovación en el aprendizaje.",
                fontSize = 15.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
} 