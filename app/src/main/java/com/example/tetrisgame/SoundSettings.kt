package com.example.tetrisgame

import android.content.Context
import android.content.SharedPreferences

object SoundSettings {
    private const val PREF_NAME = "sound_prefs"
    private const val KEY_SOUND_ON = "isSoundOn"
    private const val KEY_MUSIC_ON = "isMusicOn"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }

    var isSoundOn: Boolean
        get() = prefs?.getBoolean(KEY_SOUND_ON, true) ?: true
        set(value) { prefs?.edit()?.putBoolean(KEY_SOUND_ON, value)?.apply() }

    var isMusicOn: Boolean
        get() = prefs?.getBoolean(KEY_MUSIC_ON, true) ?: true
        set(value) { prefs?.edit()?.putBoolean(KEY_MUSIC_ON, value)?.apply() }
}