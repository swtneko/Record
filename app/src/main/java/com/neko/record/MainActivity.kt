package com.neko.record

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.neko.record.ui.navigation.NekoRecordNavHost
import com.neko.record.ui.theme.NekoRecordTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NekoRecordRoot()
        }
    }
}

@Composable
private fun NekoRecordRoot() {
    NekoRecordTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NekoRecordNavHost()
        }
    }
}
