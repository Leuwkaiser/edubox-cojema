package com.example.buzondesugerenciascojema.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buzondesugerenciascojema.model.TriviaQuestion
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed class TriviaGameState {
    object NotStarted : TriviaGameState()
    object Loading : TriviaGameState()
    data class Playing(
        val question: TriviaQuestion,
        val questionNumber: Int,
        val totalQuestions: Int,
        val score: Int,
        val timeLeft: Int
    ) : TriviaGameState()
    data class Finished(val score: Int, val maxScore: Int) : TriviaGameState()
}

class TriviaViewModel : ViewModel() {
    private val _gameState = MutableStateFlow<TriviaGameState>(TriviaGameState.NotStarted)
    val gameState: StateFlow<TriviaGameState> = _gameState

    private var questions: List<TriviaQuestion> = emptyList()
    private var currentIndex = 0
    private var score = 0
    private var timerJob: kotlinx.coroutines.Job? = null
    private val timePerQuestion = 10 // segundos

    fun startGame(questionsPool: List<TriviaQuestion>) {
        questions = questionsPool.shuffled().take(20) // 20 preguntas por partida
        currentIndex = 0
        score = 0
        nextQuestion()
    }

    private fun nextQuestion() {
        if (currentIndex >= questions.size) {
            _gameState.value = TriviaGameState.Finished(score, questions.size)
            return
        }
        val question = questions[currentIndex]
        _gameState.value = TriviaGameState.Playing(
            question = question,
            questionNumber = currentIndex + 1,
            totalQuestions = questions.size,
            score = score,
            timeLeft = timePerQuestion
        )
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var time = timePerQuestion
            while (time > 0) {
                delay(1000)
                time--
                val state = _gameState.value
                if (state is TriviaGameState.Playing) {
                    _gameState.value = state.copy(timeLeft = time)
                }
            }
            // Si se acaba el tiempo, termina el juego
            _gameState.value = TriviaGameState.Finished(score, questions.size)
        }
    }

    fun answerQuestion(answerIndex: Int) {
        val state = _gameState.value
        if (state is TriviaGameState.Playing) {
            timerJob?.cancel()
            if (answerIndex == state.question.correctAnswer) {
                score++
                currentIndex++
                nextQuestion()
            } else {
                _gameState.value = TriviaGameState.Finished(score, questions.size)
            }
        }
    }

    fun resetGame() {
        _gameState.value = TriviaGameState.NotStarted
    }
} 