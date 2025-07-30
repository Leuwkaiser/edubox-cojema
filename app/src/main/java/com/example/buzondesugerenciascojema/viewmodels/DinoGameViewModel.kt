package com.example.buzondesugerenciascojema.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class DinoGameViewModel : ViewModel() {
    data class GameState(
        val dinoY: Float = 0f,
        val isJumping: Boolean = false,
        val velocity: Float = 0f,
        val obstacles: List<Float> = emptyList(),
        val score: Int = 0,
        val gameSpeed: Float = 8f,
        val isGameOver: Boolean = false,
        val timeElapsed: Long = 0L
    )

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val groundY = 400f
    private val dinoSize = 60f
    private val gravity = 2.5f
    private val jumpVelocity = -38f
    private val obstacleWidth = 40f
    private val obstacleHeight = 80f
    private val screenWidth = 900f
    private var running = false

    fun startGame() {
        running = true
        viewModelScope.launch {
            while (running && !_state.value.isGameOver) {
                updateGame()
                delay(16L)
            }
        }
    }

    fun jump() {
        if (!_state.value.isJumping && !_state.value.isGameOver) {
            _state.value = _state.value.copy(isJumping = true, velocity = jumpVelocity)
            // TODO: reproducir sonido de salto
        }
    }

    fun restart() {
        _state.value = GameState()
        running = true
        startGame()
    }

    private fun updateGame() {
        val s = _state.value
        var dinoY = s.dinoY
        var velocity = s.velocity
        var isJumping = s.isJumping
        var obstacles = s.obstacles
        var score = s.score
        var gameSpeed = s.gameSpeed
        var isGameOver = s.isGameOver
        var timeElapsed = s.timeElapsed

        // Dino jump physics
        if (isJumping || dinoY < 0f) {
            dinoY += velocity
            velocity += gravity
            if (dinoY >= 0f) {
                dinoY = 0f
                isJumping = false
                velocity = 0f
            }
        }
        // Move obstacles
        obstacles = obstacles.map { it - gameSpeed }
        // Remove obstacles out of screen
        obstacles = obstacles.filter { it + obstacleWidth > 0 }
        // Add new obstacle
        if (obstacles.isEmpty() || (obstacles.last() < screenWidth - 350)) {
            obstacles = obstacles + screenWidth
        }
        // Collision detection
        obstacles.forEach { obsX ->
            if (obsX < 100f + dinoSize && obsX + obstacleWidth > 100f) {
                if (dinoY + dinoSize > groundY - obstacleHeight) {
                    isGameOver = true
                    running = false
                    // TODO: reproducir sonido de choque
                    // TODO: guardar score en ranking
                }
            }
        }
        // Score
        if (!isGameOver) {
            score++
        }
        // Speed up every 5 seconds
        timeElapsed += 16L
        if (timeElapsed % 5000L < 20L) {
            gameSpeed = minOf(gameSpeed + 1f, 30f)
        }
        _state.value = GameState(
            dinoY = dinoY,
            isJumping = isJumping,
            velocity = velocity,
            obstacles = obstacles,
            score = score,
            gameSpeed = gameSpeed,
            isGameOver = isGameOver,
            timeElapsed = timeElapsed
        )
    }
} 