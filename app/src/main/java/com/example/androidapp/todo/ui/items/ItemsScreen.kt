package com.example.androidapp.todo.ui.items

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
    themeViewModel: ThemeViewModel  // Adăugare ThemeViewModel ca parametru
) {
    Log.d("ItemsScreen", "recompose")
    val itemsViewModel: ItemsViewModel = viewModel(factory = ItemsViewModel.Factory)
    val itemsUiState by itemsViewModel.uiState.collectAsStateWithLifecycle(initialValue = listOf())

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
            FloatingActionButton(
                onClick = {
                    Log.d("ItemsScreen", "add")
                    onAddItem()
                },
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            MyNetworkStatus()
            MyJobs()

            // Integrarea ProximitySensor pentru schimbarea temei
            ProximitySensor(
                modifier = Modifier.padding(paddingValues),
                themeViewModel = themeViewModel  // Pasarea ThemeViewModel
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
