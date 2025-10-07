package com.example.tetrisgame

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object SoundManager {
    private var bgmPlayer: MediaPlayer? = null
    private var effectPlayer: MediaPlayer? = null
    private var isInitialized = false

    var bgmVolume by mutableStateOf(0.5f)
        private set
    var effectVolume by mutableStateOf(0.7f)
        private set

    private const val PREFS_NAME = "tetris_sound"
    private const val KEY_BGM_VOLUME = "bgm_volume"
    private const val KEY_EFFECT_VOLUME = "effect_volume"

    fun loadSettings(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        bgmVolume = prefs.getFloat(KEY_BGM_VOLUME, 0.5f)
        effectVolume = prefs.getFloat(KEY_EFFECT_VOLUME, 0.7f)
    }

    fun saveSettings(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putFloat(KEY_BGM_VOLUME, bgmVolume)
            putFloat(KEY_EFFECT_VOLUME, effectVolume)
            apply()
        }
    }

    fun setBgmVolume(volume: Float, context: Context) {
        bgmVolume = volume.coerceIn(0f, 1f)
        bgmPlayer?.setVolume(bgmVolume, bgmVolume)
        saveSettings(context)
    }

    fun setEffectVolume(volume: Float, context: Context) {
        effectVolume = volume.coerceIn(0f, 1f)
        saveSettings(context)
    }

    // Khởi tạo và bắt đầu nhạc nền (gọi 1 lần duy nhất từ MainActivity)
    fun initialize(context: Context, resourceId: Int = R.raw.background_music) {
        if (!isInitialized) {
            loadSettings(context)
            startBgm(context, resourceId)
            isInitialized = true
        }
    }

    // Khởi động nhạc nền (loop liên tục)
    fun startBgm(context: Context, resourceId: Int = R.raw.background_music) {
        try {
            if (bgmPlayer == null) {
                bgmPlayer = MediaPlayer.create(context, resourceId)
                bgmPlayer?.isLooping = true
                bgmPlayer?.setVolume(bgmVolume, bgmVolume)
            }
            if (bgmPlayer?.isPlaying == false) {
                bgmPlayer?.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Dừng nhạc nền
    fun pauseBgm() {
        bgmPlayer?.pause()
    }

    // Tiếp tục nhạc nền
    fun resumeBgm() {
        bgmPlayer?.start()
    }

    // Dừng hoàn toàn và giải phóng
    fun stopBgm() {
        bgmPlayer?.stop()
        bgmPlayer?.release()
        bgmPlayer = null
    }

    // Phát âm thanh hiệu ứng (khi xóa hàng)
    fun playClearEffect(context: Context, resourceId: Int = R.raw.clear_sound) {
        try {
            effectPlayer?.release()
            effectPlayer = MediaPlayer.create(context, resourceId)
            effectPlayer?.setVolume(effectVolume, effectVolume)
            effectPlayer?.setOnCompletionListener { mp ->
                mp.release()
            }
            effectPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Giải phóng tất cả resources
    fun release() {
        stopBgm()
        effectPlayer?.release()
        effectPlayer = null
    }
}