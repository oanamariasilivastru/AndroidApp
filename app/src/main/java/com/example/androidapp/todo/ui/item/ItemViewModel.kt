package com.example.androidapp.todo.ui.item

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.androidapp.MyApplication
import com.example.androidapp.core.Result
import com.example.androidapp.core.TAG
import com.example.androidapp.todo.data.ItemRepository
import com.example.androidapp.todo.data.Product
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class ItemUiState(
    val itemId: String? = null,
    val item: Product = Product(),
    var loadResult: Result<Product>? = null,
    var submitResult: Result<Product>? = null,
)

class ItemViewModel(
    private val itemId: String?,
    private val itemRepository: ItemRepository
) : ViewModel() {

    var uiState: ItemUiState by mutableStateOf(ItemUiState(loadResult = Result.Loading))
        private set

    init {
        Log.d(TAG, "ViewModel initialized")
        if (itemId != null) {
            loadItem()
        } else {
            uiState = uiState.copy(loadResult = Result.Success(Product()))
        }
    }

    private fun loadItem() {
        viewModelScope.launch {
            itemRepository.productStream.collect { items ->
                if (!(uiState.loadResult is Result.Loading)) {
                    return@collect
                }
                val item = items.find { it._id == itemId } ?: Product()
                uiState = uiState.copy(item = item, loadResult = Result.Success(item))
            }
        }
    }

    fun saveOrUpdateItem(
        name: String,
        category: String,
        price: Double,
        inStock: Boolean,
        imageUri: String? = null // <--- Adăugat parametrul imageUri
    ) {
        viewModelScope.launch {
            Log.d(TAG, "saveOrUpdateItem started...")
            try {
                // Validare locală a datelor
                if (name.isBlank() || category.isBlank() || price < 0) {
                    Log.e(TAG, "Validation failed: Invalid input data")
                    uiState = uiState.copy(
                        submitResult = Result.Error(
                            IllegalArgumentException("All fields must be valid. Price must be non-negative.")
                        )
                    )
                    return@launch
                }

                uiState = uiState.copy(submitResult = Result.Loading)

                // Construirea obiectului `Product`
                val item = uiState.item.copy(
                    name = name,
                    category = category,
                    price = price,
                    inStock = inStock,
                    imageUri = imageUri // <--- Adăugat
                )

                // Salvare sau actualizare
                val savedItem: Product = if (itemId == null) {
                    Log.d(TAG, "Creating new item...")
                    itemRepository.save(item)
                } else {
                    Log.d(TAG, "Updating existing item...")
                    itemRepository.update(item)
                }

                Log.d(TAG, "saveOrUpdateItem succeeded")
                uiState = uiState.copy(
                    submitResult = Result.Success(savedItem),
                    item = savedItem
                )

            } catch (e: Exception) {
                Log.e(TAG, "saveOrUpdateItem failed", e)
                uiState = uiState.copy(submitResult = Result.Error(e))
            }
        }
    }

    companion object {
        fun Factory(itemId: String?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                ItemViewModel(itemId, app.container.itemRepository)
            }
        }
    }
}
