// com/example/androidapp/todo/ui/items/ItemsScreen.kt

package com.example.androidapp.todo.ui.items

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidapp.R
import com.example.androidapp.core.ui.MyJobs
import com.example.androidapp.core.ui.MyNetworkStatus
import com.example.androidapp.sensors.ProximitySensor
import com.example.androidapp.sensors.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(
    onItemClick: (id: String?) -> Unit,
    onAddItem: () -> Unit,
    onLogout: () -> Unit,
    themeViewModel: ThemeViewModel
) {
    Log.d("ItemsScreen", "recompose")
    val itemsViewModel: ItemsViewModel = viewModel(factory = ItemsViewModel.Factory)
    val itemsUiState by itemsViewModel.uiState.collectAsStateWithLifecycle(initialValue = listOf())

    var isClicked by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isClicked) 1.2f else 1f,
        animationSpec = tween(durationMillis = 300)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.items)) },
                actions = {
                    Button(onClick = onLogout) { Text("Logout") }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = itemsUiState.isNotEmpty(),  // Butonul apare doar dacă există iteme
                enter = scaleIn(tween(300)) + fadeIn(tween(300)),
                exit = scaleOut(tween(300)) + fadeOut(tween(300))
            ) {
                FloatingActionButton(
                    onClick = {
                        Log.d("ItemsScreen", "add")
                        isClicked = !isClicked
                        onAddItem()
                    },
                    modifier = Modifier.graphicsLayer(
                        scaleX = scale,
                        scaleY = scale
                    )
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add")
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            MyNetworkStatus()
            MyJobs()

            ProximitySensor(
                modifier = Modifier.padding(paddingValues),
                themeViewModel = themeViewModel
            )

            ItemList(
                itemList = itemsUiState,
                onItemClick = onItemClick,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

// Preview pentru ItemsScreen
@Preview(showBackground = true)
@Composable
fun PreviewItemsScreen() {
    val previewThemeViewModel = ThemeViewModel()  // Instanță temporară pentru preview
    ItemsScreen(
        onItemClick = {},
        onAddItem = {},
        onLogout = {},
        themeViewModel = previewThemeViewModel  // Adăugare în preview
    )
}
