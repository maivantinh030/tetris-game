package com.example.tetrisgame

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.tetrisgame.ui.theme.TetrisGameTheme

class MainActivity : ComponentActivity() {
    lateinit var mediaPlayer: MediaPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoundSettings.init(this)
        mediaPlayer = MediaPlayer.create(this, R.raw.musicsound)
        mediaPlayer.isLooping = true
        updateMusicState()
        enableEdgeToEdge()
        setContent {
            TetrisGameTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(
                        onMusicToggle = { onMusicToggle() },
                        onSoundToggle = { onSoundToggle() }
                    )
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        updateMusicState()
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer.isPlaying) mediaPlayer.pause()
    }

    private fun updateMusicState() {
        if (SoundSettings.isMusicOn) {
            if (!mediaPlayer.isPlaying) mediaPlayer.start()
        } else {
            if (mediaPlayer.isPlaying) mediaPlayer.pause()
        }
    }

    // Call this from your Composable via a callback or use a ViewModel
    fun onMusicToggle() {
        SoundSettings.isMusicOn = !SoundSettings.isMusicOn
        updateMusicState()
    }
    fun onSoundToggle() {
        SoundSettings.isSoundOn = !SoundSettings.isSoundOn
        updateMusicState()
    }
}
