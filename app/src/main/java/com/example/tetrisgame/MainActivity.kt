package com.example.tetrisgame

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.tetrisgame.ui.theme.TetrisGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TetrisGameTheme {
                val context = LocalContext.current
                LaunchedEffect(Unit) {
                    SoundManager.initialize(context)
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph()
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        // Tiếp tục phát nhạc khi app quay lại foreground
        SoundManager.resumeBgm()
    }

    override fun onPause() {
        super.onPause()
        // Tạm dừng nhạc khi app vào background
        SoundManager.pauseBgm()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Giải phóng resources khi app bị đóng hoàn toàn
        SoundManager.release()
    }
}
