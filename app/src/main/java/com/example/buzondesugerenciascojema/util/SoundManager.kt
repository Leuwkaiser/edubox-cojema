package com.example.buzondesugerenciascojema.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

class SoundManager(private val context: Context) {
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<String, Int>()
    
    init {
        initializeSoundPool()
    }
    
    private fun initializeSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()
    }
    
    fun loadSound(soundName: String, resourceId: Int) {
        try {
            val soundId = soundPool?.load(context, resourceId, 1) ?: return
            soundMap[soundName] = soundId
        } catch (e: Exception) {
            Log.e("SoundManager", "Error loading sound: $soundName", e)
        }
    }
    
    fun playSound(soundName: String, volume: Float = 1.0f) {
        val soundId = soundMap[soundName] ?: return
        soundPool?.play(soundId, volume, volume, 1, 0, 1.0f)
    }
    
    fun playSoundWithPitch(soundName: String, pitch: Float = 1.0f, volume: Float = 1.0f) {
        val soundId = soundMap[soundName] ?: return
        soundPool?.play(soundId, volume, volume, 1, 0, pitch)
    }
    
    fun release() {
        soundPool?.release()
        soundPool = null
        soundMap.clear()
    }
} 