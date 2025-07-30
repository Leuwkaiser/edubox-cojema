package com.example.buzondesugerenciascojema.model

data class TriviaQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int, // índice de la opción correcta
    val subject: String, // Matemáticas, Castellano, etc.
    val grade: Int // 6 a 11
) 