package com.example.onechat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.onechat.ui.theme.MyApp
import com.example.onechat.ui.theme.OneChatTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()
        var keepSplashScreen = true

        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { keepSplashScreen }
        lifecycleScope.launch {
            delay(3000)
            keepSplashScreen = false
        }

        enableEdgeToEdge()
        setContent {
            OneChatTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
