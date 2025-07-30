package com.example.buzondesugerenciascojema.util

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.*

class MusicPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    fun playMenuMusic() {
        if (isPlaying) return
        
        try {
            // Crear música de menú generada programáticamente
            playGeneratedMenuMusic()
            isPlaying = true
        } catch (e: Exception) {
            Log.e("MusicPlayer", "Error playing menu music", e)
        }
    }
    
    fun playGameMusic(gameType: String) {
        stopMusic()
        
        try {
            when (gameType) {
                "snake" -> playSnakeMusic()
                "tetris" -> playTetrisMusic()
                "space_invaders" -> playSpaceInvadersMusic()
                "tres_en_raya" -> playTresEnRayaMusic()
                "buscaminas" -> playBuscaminasMusic()
                "pong" -> playPongMusic()
                "trivia" -> playTriviaMusic()
                "skeleton_survival" -> playActionMusic()
                else -> playMenuMusic()
            }
            isPlaying = true
        } catch (e: Exception) {
            Log.e("MusicPlayer", "Error playing game music", e)
        }
    }
    
    fun playActionMusic() {
        scope.launch {
            while (isPlaying) {
                // Música de acción intensa para Skeleton Survival
                playNote(220f, 200) // A3 - Nota base
                delay(50)
                playNote(277f, 200) // C#4
                delay(50)
                playNote(330f, 200) // E4
                delay(50)
                playNote(440f, 400) // A4 - Climax
                delay(100)
                
                if (!isPlaying) break
                
                // Segunda parte más intensa
                playNote(330f, 150) // E4
                delay(30)
                playNote(440f, 150) // A4
                delay(30)
                playNote(554f, 300) // C#5
                delay(80)
                
                if (!isPlaying) break
                
                // Parte rítmica
                playNote(220f, 100) // A3
                delay(20)
                playNote(220f, 100) // A3
                delay(20)
                playNote(277f, 100) // C#4
                delay(20)
                playNote(330f, 200) // E4
                delay(60)
                
                if (!isPlaying) break
                
                // Final intenso
                playNote(440f, 200) // A4
                delay(50)
                playNote(554f, 200) // C#5
                delay(50)
                playNote(659f, 400) // E5
                delay(150)
            }
        }
    }
    
    private fun playGeneratedMenuMusic() {
        scope.launch {
            while (isPlaying) {
                // Melodía calmada y suave para el menú de minijuegos
                // Notas más bajas y pausas más largas para un efecto más relajante
                playNote(261.63f, 800) // C4 (nota más baja)
                delay(200)
                playNote(329.63f, 600) // E4
                delay(150)
                playNote(392.00f, 600) // G4
                delay(150)
                playNote(523.25f, 1000) // C5
                delay(400)
                
                if (!isPlaying) break
                
                // Segunda parte más suave
                playNote(392.00f, 600) // G4
                delay(200)
                playNote(329.63f, 600) // E4
                delay(200)
                playNote(261.63f, 800) // C4
                delay(500)
                
                if (!isPlaying) break
                
                // Tercera parte con variación suave
                playNote(293.66f, 600) // D4
                delay(150)
                playNote(349.23f, 600) // F4
                delay(150)
                playNote(440.00f, 800) // A4
                delay(300)
                
                if (!isPlaying) break
                
                // Regreso suave
                playNote(349.23f, 600) // F4
                delay(200)
                playNote(293.66f, 600) // D4
                delay(200)
                playNote(261.63f, 1000) // C4
                delay(600)
            }
        }
    }
    
    private fun playSnakeMusic() {
        scope.launch {
            while (isPlaying) {
                // Música tensa para Snake
                playNote(440f, 200) // A4
                delay(100)
                playNote(494f, 200) // B4
                delay(100)
                playNote(523f, 400) // C5
                delay(200)
                
                if (!isPlaying) break
                
                playNote(494f, 200) // B4
                delay(100)
                playNote(440f, 400) // A4
                delay(300)
            }
        }
    }
    
    private fun playTetrisMusic() {
        scope.launch {
            while (isPlaying) {
                // Melodía clásica de Tetris (simplificada)
                playNote(659.25f, 200) // E5
                delay(50)
                playNote(587.33f, 200) // D5
                delay(50)
                playNote(523.25f, 200) // C5
                delay(50)
                playNote(493.88f, 400) // B4
                delay(200)
                
                if (!isPlaying) break
                
                playNote(523.25f, 200) // C5
                delay(50)
                playNote(587.33f, 200) // D5
                delay(50)
                playNote(659.25f, 400) // E5
                delay(300)
            }
        }
    }
    
    private fun playSpaceInvadersMusic() {
        scope.launch {
            while (isPlaying) {
                // Música de ciencia ficción
                playNote(220f, 150) // A3
                delay(50)
                playNote(277.18f, 150) // C#4
                delay(50)
                playNote(329.63f, 150) // E4
                delay(50)
                playNote(415.30f, 300) // G#4
                delay(200)
                
                if (!isPlaying) break
                
                playNote(415.30f, 150) // G#4
                delay(50)
                playNote(329.63f, 150) // E4
                delay(50)
                playNote(277.18f, 150) // C#4
                delay(50)
                playNote(220f, 300) // A3
                delay(300)
            }
        }
    }
    
    private fun playTresEnRayaMusic() {
        scope.launch {
            while (isPlaying) {
                // Música relajante para estrategia
                playNote(523.25f, 400) // C5
                delay(100)
                playNote(659.25f, 400) // E5
                delay(100)
                playNote(783.99f, 400) // G5
                delay(100)
                playNote(1046.50f, 800) // C6
                delay(400)
                
                if (!isPlaying) break
                
                playNote(783.99f, 400) // G5
                delay(100)
                playNote(659.25f, 400) // E5
                delay(100)
                playNote(523.25f, 800) // C5
                delay(500)
            }
        }
    }
    
    private fun playBuscaminasMusic() {
        scope.launch {
            while (isPlaying) {
                // Música misteriosa
                playNote(261.63f, 300) // C4
                delay(100)
                playNote(293.66f, 300) // D4
                delay(100)
                playNote(329.63f, 300) // E4
                delay(100)
                playNote(349.23f, 600) // F4
                delay(300)
                
                if (!isPlaying) break
                
                playNote(329.63f, 300) // E4
                delay(100)
                playNote(293.66f, 300) // D4
                delay(100)
                playNote(261.63f, 600) // C4
                delay(400)
            }
        }
    }
    
    private fun playPongMusic() {
        scope.launch {
            while (isPlaying) {
                // Música retro para Pong
                playNote(440f, 200) // A4
                delay(50)
                playNote(494f, 200) // B4
                delay(50)
                playNote(523f, 200) // C5
                delay(50)
                playNote(587f, 400) // D5
                delay(200)
                
                if (!isPlaying) break
                
                playNote(523f, 200) // C5
                delay(50)
                playNote(494f, 200) // B4
                delay(50)
                playNote(440f, 400) // A4
                delay(300)
            }
        }
    }
    
    private fun playTriviaMusic() {
        scope.launch {
            while (isPlaying) {
                // Música intelectual para Trivia
                playNote(523.25f, 250) // C5
                delay(50)
                playNote(587.33f, 250) // D5
                delay(50)
                playNote(659.25f, 250) // E5
                delay(50)
                playNote(698.46f, 500) // F5
                delay(250)
                
                if (!isPlaying) break
                
                playNote(659.25f, 250) // E5
                delay(50)
                playNote(587.33f, 250) // D5
                delay(50)
                playNote(523.25f, 500) // C5
                delay(300)
            }
        }
    }
    
    private suspend fun playNote(frequency: Float, durationMs: Int) {
        withContext(Dispatchers.IO) {
            try {
                val sampleRate = 44100
                val numSamples = (sampleRate * durationMs / 1000.0).toInt()
                val sample = DoubleArray(numSamples)
                
                for (i in 0 until numSamples) {
                    sample[i] = kotlin.math.sin(2 * Math.PI * i / (sampleRate / frequency.toDouble()))
                }
                
                val generatedSnd = ByteArray(2 * numSamples)
                var idx = 0
                for (dVal in sample) {
                    // Volumen más bajo para música más suave (reducido de 16383 a 8192)
                    val value = (dVal * 8192).toInt().toShort()
                    generatedSnd[idx++] = (value.toInt() and 0x00ff).toByte()
                    generatedSnd[idx++] = (value.toInt() and 0xff00 shr 8).toByte()
                }
                
                val audioTrack = android.media.AudioTrack.Builder()
                    .setAudioAttributes(android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_GAME)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                    .setAudioFormat(android.media.AudioFormat.Builder()
                        .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                    .setTransferMode(android.media.AudioTrack.MODE_STREAM)
                    .build()
                
                audioTrack.play()
                audioTrack.write(generatedSnd, 0, generatedSnd.size)
                audioTrack.stop()
                audioTrack.release()
                
            } catch (e: Exception) {
                Log.e("MusicPlayer", "Error playing note", e)
            }
        }
    }
    
    fun stopMusic() {
        isPlaying = false
        scope.cancel()
    }
    
    fun pauseMusic() {
        isPlaying = false
    }
    
    fun resumeMusic() {
        if (!isPlaying) {
            isPlaying = true
            playMenuMusic()
        }
    }
    
    fun release() {
        stopMusic()
        scope.cancel()
    }
} 