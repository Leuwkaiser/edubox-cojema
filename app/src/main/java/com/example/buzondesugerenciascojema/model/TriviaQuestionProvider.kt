package com.example.buzondesugerenciascojema.model

import kotlin.random.Random

object TriviaQuestionProvider {
    private val subjectsByGrade = mapOf(
        6 to listOf("Matemáticas", "Castellano", "Biología", "Inglés", "Sociales"),
        7 to listOf("Matemáticas", "Castellano", "Biología", "Inglés", "Sociales"),
        8 to listOf("Matemáticas", "Castellano", "Biología", "Inglés", "Sociales"),
        9 to listOf("Matemáticas", "Castellano", "Biología", "Inglés", "Sociales"),
        10 to listOf("Matemáticas", "Castellano", "Biología", "Inglés", "Economía", "Política"),
        11 to listOf("Matemáticas", "Castellano", "Biología", "Inglés", "Economía", "Política")
    )

    fun getQuestionsForGrade(grade: Int): List<TriviaQuestion> {
        val subjects = subjectsByGrade[grade] ?: return emptyList()
        val questions = mutableListOf<TriviaQuestion>()
        for (subject in subjects) {
            repeat(100) { i ->
                val (q, opts, correct) = generateQuestion(subject, grade, i)
                questions.add(
                    TriviaQuestion(
                        id = "$grade-$subject-$i",
                        question = q,
                        options = opts,
                        correctAnswer = correct,
                        subject = subject,
                        grade = grade
                    )
                )
            }
        }
        return questions.shuffled()
    }

    private fun generateQuestion(subject: String, grade: Int, index: Int): Triple<String, List<String>, Int> {
        // Generador simple de preguntas aleatorias por asignatura
        return when(subject) {
            "Matemáticas" -> {
                val a = Random.nextInt(1, 100)
                val b = Random.nextInt(1, 100)
                val correct = a + b
                val options = listOf(correct, correct+1, correct-1, correct+2).shuffled()
                Triple("¿Cuánto es $a + $b?", options.map { it.toString() }, options.indexOf(correct))
            }
            "Castellano" -> {
                val words = listOf("casa", "perro", "gato", "árbol", "escuela", "libro", "mesa", "silla")
                val word = words.random()
                val correct = word.uppercase()
                val options = listOf(correct, word.lowercase(), word.capitalize(), word.reversed()).shuffled()
                Triple("¿Cómo se escribe '$word' en mayúsculas?", options, options.indexOf(correct))
            }
            "Biología" -> {
                val animals = listOf("perro", "gato", "elefante", "león", "tigre", "ratón", "conejo", "jirafa")
                val animal = animals.random()
                val correct = "mamífero"
                val options = listOf("mamífero", "ave", "pez", "reptil").shuffled()
                Triple("¿A qué grupo pertenece el $animal?", options, options.indexOf(correct))
            }
            "Inglés" -> {
                val pairs = listOf(
                    "dog" to "perro",
                    "cat" to "gato",
                    "house" to "casa",
                    "book" to "libro",
                    "tree" to "árbol",
                    "table" to "mesa",
                    "chair" to "silla",
                    "school" to "escuela"
                )
                val (eng, esp) = pairs.random()
                val options = listOf(esp, "libro", "perro", "gato").shuffled()
                Triple("¿Qué significa '$eng'?", options, options.indexOf(esp))
            }
            "Sociales" -> {
                val countries = listOf("Colombia", "Argentina", "Brasil", "México", "Chile")
                val capitals = mapOf("Colombia" to "Bogotá", "Argentina" to "Buenos Aires", "Brasil" to "Brasilia", "México" to "Ciudad de México", "Chile" to "Santiago")
                val country = countries.random()
                val correct = capitals[country] ?: ""
                val options = listOf(correct, "Lima", "Quito", "Caracas").shuffled()
                Triple("¿Cuál es la capital de $country?", options, options.indexOf(correct))
            }
            "Economía" -> {
                val q = "¿Qué es la inflación?"
                val options = listOf(
                    "Aumento general de precios",
                    "Disminución de precios",
                    "Aumento de la población",
                    "Reducción de salarios"
                ).shuffled()
                Triple(q, options, options.indexOf("Aumento general de precios"))
            }
            "Política" -> {
                val q = "¿Quién es el jefe de Estado en Colombia?"
                val options = listOf(
                    "El Presidente",
                    "El Congreso",
                    "El Alcalde",
                    "El Gobernador"
                ).shuffled()
                Triple(q, options, options.indexOf("El Presidente"))
            }
            else -> Triple("Pregunta genérica $subject $grade $index", listOf("A", "B", "C", "D"), 0)
        }
    }
} 