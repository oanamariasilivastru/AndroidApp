package com.example.androidapp.todo.data


import android.util.Log
import com.example.androidapp.todo.data.remote.ItemService
import com.example.androidapp.core.Result
import com.example.androidapp.core.TAG
import com.example.androidapp.core.data.remote.Api
import com.example.androidapp.todo.data.local.ProductDao
import com.example.androidapp.todo.data.remote.ItemEvent
import com.example.androidapp.todo.data.remote.ItemWsClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class ItemRepository(private val productService: ItemService, private val productWsClient: ItemWsClient, private val productDao: ProductDao) {
//    private var products: List<Product> = listOf()
//
//    private var productsFlow: MutableSharedFlow<Result<List<Product>>> = MutableSharedFlow(
//        replay = 1,
//        onBufferOverflow = BufferOverflow.DROP_OLDEST
//    )
//
//    val productStream: Flow<Result<List<Product>>> = productsFlow

    val productStream by lazy { productDao.getAll() }

    init {
        Log.d(TAG, "ProductRepository initialized")
    }

    suspend fun refresh() {
        Log.d(TAG, "refresh started")
        try {
            val products = productService.find(authorization = getBearerToken())
            productDao.deleteAll()
            products.forEach{productDao.insert(it)}
            Log.d(TAG, "refresh succeeded")
        } catch (e: Exception) {
            Log.w(TAG, "refresh failed", e)
        }
    }

    suspend fun openWsClient() {
        Log.d(TAG, "openWsClient")
        withContext(Dispatchers.IO) {
            getProductEvents().collect {
                Log.d(TAG, "Product event collected $it")
                if (it is Result.Success) {
                    val productEvent = it.data
                    when (productEvent.type) {
                        "created" -> handleProductCreated(productEvent.payload)
                        "updated" -> handleProductUpdated(productEvent.payload)
                        "deleted" -> {
                            val productId = productEvent.payload._id
                            if (productId != null) {
                                handleProductDeleted(productId)
                            } else {
                                Log.w(TAG, "Product ID is null in deleted event")
                            }
                        }

                    }
                }
            }
        }
    }

    suspend fun closeWsClient() {
        Log.d(TAG, "closeWsClient")
        withContext(Dispatchers.IO) {
            productWsClient.closeSocket()
        }
    }

    suspend fun getProductEvents(): Flow<Result<ItemEvent>> = callbackFlow {
        Log.d(TAG, "getProductEvents started")
        productWsClient.openSocket(
            onEvent = {
                Log.d(TAG, "onEvent $it")
                if (it != null) {
                    Log.d(TAG, "onEvent trySend $it")
                    trySend(Result.Success(it))
                }
            },
            onClosed = { close() },
            onFailure = { close() }
        )
        awaitClose { productWsClient.closeSocket() }
    }

    suspend fun update(product: Product): Product {
        Log.d(TAG, "update $product...")
        val updatedProduct = productService.update(authorization = getBearerToken(), product._id, product)
        Log.d(TAG, "update $product succeeded")
        handleProductUpdated(updatedProduct)
        return updatedProduct
    }

    suspend fun save(product: Product): Product {
        Log.d(TAG, "save $product...")
        val createdProduct = productService.create(authorization = getBearerToken(), product)
        Log.d(TAG, "save $product succeeded")
        handleProductCreated(createdProduct)
        return createdProduct
    }

    private suspend fun handleProductDeleted(productId: String) {
        Log.d(TAG, "handleProductDeleted: $productId")
        productDao.deleteById(productId)
    }

    private suspend fun handleProductUpdated(product: Product) {
        Log.d(TAG, "handleProductUpdated: $product")
        productDao.update(product)
    }

    private suspend fun handleProductCreated(product: Product) {
        Log.d(TAG, "handleProductCreated: $product")
        productDao.insert(product)
    }

    fun setToken(token: String) {
        productWsClient.authorize(token)
    }

    private fun getBearerToken() = "Bearer ${Api.tokenInterceptor.token}"
}
