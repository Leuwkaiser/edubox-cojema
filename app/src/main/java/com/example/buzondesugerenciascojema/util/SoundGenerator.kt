package com.example.buzondesugerenciascojema.util

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.sin

class SoundGenerator(private val context: Context) {
    private val activeThreads = mutableSetOf<Thread>()
    
    fun playBeep(frequency: Float = 800f, duration: Int = 200) {
        playTone(frequency, duration)
    }
    
    fun playClick() {
        playTone(1000f, 50)
    }
    
    fun playSuccess() {
        playTone(800f, 100)
        Thread.sleep(50)
        playTone(1000f, 100)
        Thread.sleep(50)
        playTone(1200f, 200)
    }
    
    fun playError() {
        playTone(400f, 200)
        Thread.sleep(50)
        playTone(300f, 300)
    }
    
    fun playBounce() {
        playTone(600f, 80)
    }
    
    fun playScore() {
        playTone(1200f, 50)
        Thread.sleep(30)
        playTone(1400f, 50)
        Thread.sleep(30)
        playTone(1600f, 100)
    }
    
    // Efectos específicos para Snake
    fun playSnakeEat() {
        playTone(800f, 60)
        Thread.sleep(20)
        playTone(1000f, 80)
    }
    
    fun playSnakeCrash() {
        playTone(300f, 200)
        Thread.sleep(50)
        playTone(200f, 300)
    }
    
    // Efectos específicos para Tetris
    fun playTetrisLine() {
        playTone(1000f, 40)
        Thread.sleep(20)
        playTone(1200f, 40)
        Thread.sleep(20)
        playTone(1400f, 40)
        Thread.sleep(20)
        playTone(1600f, 80)
    }
    
    fun playTetrisDrop() {
        playTone(400f, 100)
    }
    
    fun playTetrisRotate() {
        playTone(600f, 50)
    }
    
    // Efectos específicos para Space Invaders
    fun playShoot() {
        playTone(1200f, 40)
        Thread.sleep(10)
        playTone(800f, 30)
    }
    
    fun playAlienHit() {
        playTone(800f, 40)
        Thread.sleep(20)
        playTone(600f, 60)
    }
    
    fun playPlayerHit() {
        playTone(200f, 300)
        Thread.sleep(100)
        playTone(150f, 400)
    }
    
    // Efectos específicos para Tres en Raya
    fun playPlaceX() {
        playTone(800f, 60)
    }
    
    fun playPlaceO() {
        playTone(600f, 60)
    }
    
    fun playWin() {
        playTone(800f, 100)
        Thread.sleep(50)
        playTone(1000f, 100)
        Thread.sleep(50)
        playTone(1200f, 200)
    }
    
    // Efectos específicos para Buscaminas
    fun playMineExplosion() {
        playTone(150f, 500)
        Thread.sleep(100)
        playTone(100f, 600)
    }
    
    fun playFlag() {
        playTone(1000f, 40)
    }
    
    fun playReveal() {
        playTone(800f, 30)
    }
    
    // Efectos específicos para Trivia
    fun playCorrectAnswer() {
        playTone(800f, 80)
        Thread.sleep(40)
        playTone(1000f, 80)
        Thread.sleep(40)
        playTone(1200f, 120)
    }
    
    fun playWrongAnswer() {
        playTone(400f, 100)
        Thread.sleep(50)
        playTone(300f, 150)
    }
    
    fun playTimeUp() {
        playTone(300f, 200)
        Thread.sleep(100)
        playTone(200f, 300)
    }
    
    // Efectos específicos para Skeleton Survival
    fun playKill() {
        playTone(800f, 40)
        Thread.sleep(20)
        playTone(600f, 40)
        Thread.sleep(20)
        playTone(400f, 60)
    }
    
    fun playMelee() {
        playTone(300f, 80)
        Thread.sleep(20)
        playTone(200f, 100)
    }
    
    fun playUpgrade() {
        playTone(800f, 60)
        Thread.sleep(30)
        playTone(1000f, 60)
        Thread.sleep(30)
        playTone(1200f, 60)
        Thread.sleep(30)
        playTone(1400f, 120)
    }
    
    fun playSplashSound() {
        // Sonido elegante y sutil para la splash screen
        // Secuencia de notas más suaves y claras
        playSplashTone(600f, 120)  // Nota inicial más baja y suave
        Thread.sleep(60)
        playSplashTone(700f, 150)  // Nota ascendente suave
        Thread.sleep(40)
        playSplashTone(800f, 180)  // Nota culminante clara
        Thread.sleep(50)
        playSplashTone(700f, 140)  // Nota descendente
        Thread.sleep(40)
        playSplashTone(600f, 160)  // Nota final suave
    }
    
    private fun playSplashTone(frequency: Float, durationMs: Int) {
        val thread = Thread {
            try {
                val sampleRate = 44100
                val numSamples = (sampleRate * durationMs / 1000.0).toInt()
                val sample = DoubleArray(numSamples)
                val freqOfTone = frequency.toDouble()
                
                for (i in 0 until numSamples) {
                    sample[i] = sin(2 * Math.PI * i / (sampleRate / freqOfTone))
                }
                
                val generatedSnd = ByteArray(2 * numSamples)
                var idx = 0
                for (dVal in sample) {
                    // Volumen más bajo para el splash (0.3 en lugar de 1.0)
                    val value = (dVal * 32767 * 0.3).toInt().toShort()
                    generatedSnd[idx++] = (value.toInt() and 0x00ff).toByte()
                    generatedSnd[idx++] = (value.toInt() and 0xff00 shr 8).toByte()
                }
                
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_GAME)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                    .setAudioFormat(android.media.AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
                
                audioTrack.play()
                audioTrack.write(generatedSnd, 0, generatedSnd.size)
                audioTrack.stop()
                audioTrack.release()
                
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                activeThreads.remove(Thread.currentThread())
            }
        }
        
        activeThreads.add(thread)
        thread.start()
    }
    
    fun cleanup() {
        activeThreads.forEach { it.interrupt() }
        activeThreads.clear()
    }
    
    private fun playTone(frequency: Float, durationMs: Int) {
        val thread = Thread {
            try {
                val sampleRate = 44100
                val numSamples = (sampleRate * durationMs / 1000.0).toInt()
                val sample = DoubleArray(numSamples)
                val freqOfTone = frequency.toDouble()
                
                for (i in 0 until numSamples) {
                    sample[i] = sin(2 * Math.PI * i / (sampleRate / freqOfTone))
                }
                
                val generatedSnd = ByteArray(2 * numSamples)
                var idx = 0
                for (dVal in sample) {
                    val value = (dVal * 32767).toInt().toShort()
                    generatedSnd[idx++] = (value.toInt() and 0x00ff).toByte()
                    generatedSnd[idx++] = (value.toInt() and 0xff00 shr 8).toByte()
                }
                
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_GAME)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                    .setAudioFormat(android.media.AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
                
                audioTrack.play()
                audioTrack.write(generatedSnd, 0, generatedSnd.size)
                audioTrack.stop()
                audioTrack.release()
                
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                activeThreads.remove(Thread.currentThread())
            }
        }
        
        activeThreads.add(thread)
        thread.start()
    }
} 