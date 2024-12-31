// com/example/androidapp/sensors/ProximitySensor.kt

package com.example.androidapp.sensors

import android.app.Application
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
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
fun ProximitySensor(
    modifier: Modifier = Modifier,
    themeViewModel: ThemeViewModel,
    size: Dp = 80.dp // Dimensiune implicită mai mică
) {
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
        targetValue = if (proximitySensorViewModel.uiState) 1.2f else 1f,
        animationSpec = tween(durationMillis = 500)
    )
    val rotation by animateFloatAsState(
        targetValue = if (proximitySensorViewModel.uiState) 360f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    Column(
        modifier = modifier
            .size(size) // Aplică dimensiunea personalizată
            .background(backgroundColor)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier
                .size(size / 2) // Ajustează dimensiunea iconiței
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    rotationZ = rotation
                )
        )

        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall // Ajustează dimensiunea textului
        )
    }
}
