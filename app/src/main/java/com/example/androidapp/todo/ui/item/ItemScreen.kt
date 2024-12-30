package com.example.androidapp.todo.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidapp.core.Result
import com.example.androidapp.todo.ui.item.ItemViewModel
import com.example.androidapp.R
import com.example.androidapp.camera.MyPhotos
import com.example.androidapp.core.ui.createNotificationChannel
import com.example.androidapp.core.ui.showSimpleNotificationWithTapAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(itemId: String?, onClose: () -> Unit) {
    val itemViewModel = viewModel<ItemViewModel>(factory = ItemViewModel.Factory(itemId))
    val itemUiState = itemViewModel.uiState

    var name by remember { mutableStateOf(itemUiState.item.name) }
    var category by remember { mutableStateOf(itemUiState.item.category) }
    var price by remember { mutableStateOf(itemUiState.item.price.toString()) }
    var inStock by remember { mutableStateOf(itemUiState.item.inStock) }
    var imageUri by remember { mutableStateOf(itemUiState.item.imageUri ?: "") } // <--- Adăugat

    Log.d("ItemScreen", "Recompose: name=$name, category=$category, price=$price, inStock=$inStock, imageUri=$imageUri")

    val context = LocalContext.current
    val channelId = "MyTestChannel"
    val notificationId = 0

    LaunchedEffect(Unit) {
        createNotificationChannel(channelId, context)
    }

    LaunchedEffect(itemUiState.submitResult) {
        if (itemUiState.submitResult is Result.Success) {
            Log.d("ItemScreen", "Closing screen after submit success")
            onClose()
        }
    }

    LaunchedEffect(itemId, itemUiState.loadResult) {
        if (itemId == null || itemUiState.loadResult !is Result.Loading) {
            val item = itemUiState.item
            name = item.name
            category = item.category
            price = item.price.toString()
            inStock = item.inStock
            imageUri = item.imageUri ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.item)) },
                actions = {
                    Button(onClick = {
                        val parsedPrice = price.toDoubleOrNull()
                        if (parsedPrice == null || parsedPrice < 0) {
                            Log.d("ItemScreen", "Invalid price entered")
                        } else {
                            // Salvează item-ul
                            itemViewModel.saveOrUpdateItem(
                                name = name,
                                category = category,
                                price = parsedPrice,
                                inStock = inStock,
                                imageUri = imageUri // <--- Transmis către ViewModel
                            )

                            // Afișează notificarea după ce item-ul a fost salvat cu succes
                            showSimpleNotificationWithTapAction(
                                context,
                                channelId,
                                notificationId,
                                "Product updated",
                                "Your product has been updated successfully."
                            )
                        }
                    }) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (val loadResult = itemUiState.loadResult) {
                is Result.Loading -> {
                    LoadingView()
                }
                is Result.Error -> {
                    ErrorView(message = loadResult.exception?.message)
                }
                is Result.Success -> {
                    ItemForm(
                        name = name,
                        onNameChange = { name = it },
                        category = category,
                        onCategoryChange = { category = it },
                        price = price,
                        onPriceChange = { price = it },
                        inStock = inStock,
                        onInStockChange = { inStock = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Adaugă MyPhotos și actualizează imageUri
                    MyPhotos(
                        modifier = Modifier.fillMaxWidth(),
                        onImageUriChanged = { uri ->
                            imageUri = uri ?: ""
                        }
                    )
                }
                else -> {
                    FallbackView()
                }
            }

            // Gestionarea erorii la submit
            if (itemUiState.submitResult is Result.Error) {
                SubmitErrorView(message = (itemUiState.submitResult as Result.Error).exception?.message)
            }
        }
    }
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorView(message: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Error loading item: ${message ?: "Unknown error"}")
    }
}

@Composable
fun FallbackView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Unexpected state!")
    }
}

@Composable
fun SubmitErrorView(message: String?) {
    Text(
        text = "Failed to submit item: ${message ?: "Unknown error"}",
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.error
    )
}

@Composable
fun ItemForm(
    name: String,
    onNameChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    price: String,
    onPriceChange: (String) -> Unit,
    inStock: Boolean,
    onInStockChange: (Boolean) -> Unit
) {
    Column {
        TextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = category,
            onValueChange = onCategoryChange,
            label = { Text("Category") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = price,
            onValueChange = onPriceChange,
            label = { Text("Price") },
            modifier = Modifier.fillMaxWidth(),
            isError = price.toDoubleOrNull() == null || price.toDoubleOrNull() ?: 0.0 < 0,
            supportingText = {
                if (price.toDoubleOrNull() == null || price.toDoubleOrNull() ?: 0.0 < 0) {
                    Text(text = "Invalid price")
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("In Stock")
            Checkbox(
                checked = inStock,
                onCheckedChange = onInStockChange,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Preview
@Composable
fun PreviewItemScreen() {
    ItemScreen(itemId = null, onClose = {})
}
