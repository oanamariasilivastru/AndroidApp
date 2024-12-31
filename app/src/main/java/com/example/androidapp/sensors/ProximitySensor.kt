package com.example.androidapp.sensors

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProximitySensorViewModel(application: Application) : AndroidViewModel(application) {
    var uiState by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            ProximitySensorMonitor(getApplication()).isNear.collect {
                uiState = it
            }
        }
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ProximitySensorViewModel(application)
            }
        }
    }
}

class ThemeViewModel : ViewModel() {
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    fun setDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
    }
}

@Composable
fun ProximitySensor(modifier: Modifier, themeViewModel: ThemeViewModel) {
    val proximitySensorViewModel = viewModel<ProximitySensorViewModel>(
        factory = ProximitySensorViewModel.Factory(
            LocalContext.current.applicationContext as Application
        )
    )

    LaunchedEffect(proximitySensorViewModel.uiState) {
        themeViewModel.setDarkTheme(proximitySensorViewModel.uiState)
    }

    val backgroundColor = if (proximitySensorViewModel.uiState) Color(0xFF1EB980) else Color(0xFFFFD54F)
    val iconColor = if (proximitySensorViewModel.uiState) Color.White else Color.Black
    val text = if (proximitySensorViewModel.uiState) "Near" else "Away"

    val scale by animateFloatAsState(
        targetValue = if (proximitySensorViewModel.uiState) 1.5f else 1f,
        animationSpec = tween(durationMillis = 500)
    )
    val rotation by animateFloatAsState(
        targetValue = if (proximitySensorViewModel.uiState) 360f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    Column(
        modifier = modifier
            .size(200.dp)
            .background(backgroundColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    rotationZ = rotation
                )
        )

        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: android.os.Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val themeViewModel = ThemeViewModel()
//
//        setContent {
//            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState(initial = false)
//
//            val colors = if (isDarkTheme) {
//                darkColorScheme(
//                    primary = Color.Black,
//                    secondary = Color.DarkGray
//                )
//            } else {
//                lightColorScheme(
//                    primary = Color.White,
//                    secondary = Color.LightGray
//                )
//            }
//
//            MaterialTheme(colorScheme = colors) {
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    ProximitySensor(
//                        modifier = Modifier,
//                        onThemeChange = { themeViewModel.setDarkTheme(it) }
//                    )
//                }
//            }
//        }
//    }
//}
