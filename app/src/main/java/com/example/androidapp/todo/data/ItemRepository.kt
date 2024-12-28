package com.example.androidapp.todo.data


import android.util.Log
import com.example.androidapp.MyApplication
import com.example.androidapp.todo.data.remote.ItemService
import com.example.androidapp.core.Result
import com.example.androidapp.core.TAG
import com.example.androidapp.core.data.remote.Api
import com.example.androidapp.core.ui.showSimpleNotificationWithTapAction
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
            syncPendingChanges();
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

//    suspend fun update(product: Product): Product {
//        Log.d(TAG, "update $product...")
//        val updatedProduct = productService.update(authorization = getBearerToken(), product._id, product)
//        Log.d(TAG, "update $product succeeded")
//        handleProductUpdated(updatedProduct)
//        return updatedProduct
//    }

    suspend fun update(product: Product): Product {
        Log.d(TAG, "update $product...")
        return try {
            // 1. Încearcă să faci update pe server
            val updatedProduct = productService.update(authorization = getBearerToken(), product._id, product)
            Log.d(TAG, "update $product succeeded on server")

            // 2. Marchează-l local ca sincronizat
            val syncedProduct = updatedProduct.copy(isPendingSync = false)
            handleProductUpdated(syncedProduct)

            // 3. Returnează produsul updatat
            syncedProduct
        } catch (e: java.net.ConnectException) {
            // 4. Dacă nu există rețea, doar actualizează local
            Log.w(TAG, "Network unreachable, updating locally.", e)

            val offlineProduct = product.copy(isPendingSync = true)
            handleProductUpdated(offlineProduct)

            // 5. Returnează produsul actualizat local (fără confirmare server)
            offlineProduct
        } catch (e: Exception) {
            Log.e(TAG, "update failed with other error", e)
            throw e
        }
    }



    suspend fun save(product: Product): Product {
        Log.d(TAG, "save $product...")
        return try {
            // 1. Încearcă să creezi pe server
            val createdProduct = productService.create(getBearerToken(), product)
            Log.d(TAG, "save $product succeeded on server")

            // 2. Marchează-l local ca sincronizat
            val syncedProduct = createdProduct.copy(isPendingSync = false)
            handleProductCreated(syncedProduct)

            syncedProduct
        } catch (e: java.net.ConnectException) {
            // 3. Dacă nu există rețea, salvează local cu isPendingSync = true
            Log.w(TAG, "Network unreachable, saving locally.", e)
            val offlineProduct = product.copy(isPendingSync = true)
            handleProductCreated(offlineProduct)
            offlineProduct
        } catch (e: Exception) {
            Log.e(TAG, "save failed with other error", e)
            throw e
        }
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

    suspend fun getPendingSyncMovies(): List<Product> {
        return productDao.getPendingSyncProducts()
    }

    suspend fun markAsSynced(id: String) {
        return productDao.markAsSynced(id)
    }
    fun setToken(token: String) {
        productWsClient.authorize(token)
    }

    suspend fun syncPendingChanges() {
        val pendingProducts = productDao.getPendingSyncProducts()  // Obține produsele nesincronizate

        pendingProducts.forEach { product ->
            if (product.isPendingSync) {
                try {
                    if (product._id.isBlank()) {
                        save(product.copy(isPendingSync = false))  // Salvează ca nou dacă nu are ID
                    } else {
                        update(product.copy(isPendingSync = false))  // Actualizează dacă are ID
                    }
                    productDao.markAsSynced(product._id)  // Marchează ca sincronizat
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to sync product ${product.name}", e)
                }
            }
        }
    }

    fun showNotification(title: String, content: String) {
        val context = MyApplication.appContext
        val channelId = "My Channel"
        showSimpleNotificationWithTapAction(context, channelId, 1, title, content)
    }

    private fun getBearerToken() = "Bearer ${Api.tokenInterceptor.token}"

}
