package com.example.androidapp.core.ui

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.androidapp.MyApplication
import com.example.androidapp.todo.data.ItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val repository: ItemRepository =
        (context.applicationContext as MyApplication).container.itemRepository

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val pendingMovies = repository.getPendingSyncMovies()
                for (movie in pendingMovies) {
                    if (movie._id.isNotEmpty()) {
                        repository.update(movie.copy(isPendingSync = false))
                    } else {
                        repository.save(movie.copy(isPendingSync = false))
                    }
                    repository.markAsSynced(movie._id)
                }

                // Notificarea opțională
                repository.showNotification("Sync Completed", "Your changes have been synced successfully.")

                Result.success()
            } catch (e: Exception) {
                Log.e("SyncWorker", "Sync failed: ${e.localizedMessage}")
                Result.retry()
            }
        }
    }
}
