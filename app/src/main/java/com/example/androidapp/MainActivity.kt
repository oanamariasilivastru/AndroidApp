package com.example.androidapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.androidapp.ui.theme.AndroidAppTheme
import com.example.androidapp.core.TAG
import com.example.androidapp.core.ui.createNotificationChannel
import com.example.androidapp.sensors.ThemeViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel("MyTestChannel", this)
        val themeViewModel by viewModels<ThemeViewModel>()
        setContent {
            Log.d(TAG, "onCreate")
            MyApp {
                MyAppNavHost(themeViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            (application as MyApplication).container.itemRepository.openWsClient()
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            (application as MyApplication).container.itemRepository.closeWsClient()
        }
    }
}

@Composable
fun MyApp(content: @Composable () -> Unit) {
    Log.d("MyApp", "recompose")
    AndroidAppTheme {
        Surface {
            content()
        }
    }
}

@Preview
@Composable
fun PreviewMyApp() {
    val fakeThemeViewModel = ThemeViewModel()
    MyApp {
        MyAppNavHost(themeViewModel = fakeThemeViewModel)
    }
}
