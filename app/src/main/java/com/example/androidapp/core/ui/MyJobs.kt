package com.example.androidapp.core.ui

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit

data class MyJobUiState(val isRunning: Boolean = false, val progress: Int = 0, val result: Int = 0)

class MyJobsViewModel(application: Application) : AndroidViewModel(application) {
    var uiState by mutableStateOf(MyJobUiState())
        private set
    private var workManager: WorkManager = WorkManager.getInstance(getApplication())
    private var workId: UUID? = null

    init {
        startJob()
    }

    private fun startJob() {
        viewModelScope.launch {
            val constraints = Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val inputData = Data.Builder()
                .putInt("to", 10)
                .build()

//            val myWork = PeriodicWorkRequestBuilder<MyWorker>(10, TimeUnit.MINUTES)
//                .setConstraints(constraints)
//                .setInputData(inputData)
//                .build()

            val myWork = OneTimeWorkRequest.Builder(MyWorker::class.java)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()

            workId = myWork.id
            uiState = uiState.copy(isRunning = true)

            workManager.apply {
                enqueue(myWork)
                getWorkInfoByIdLiveData(workId!!).asFlow().collect {
                    if (it != null) {
                        Log.d("MyJobsViewModel", "$it")
                        uiState = uiState.copy(
                            isRunning = !it.state.isFinished,
                            progress = it.progress.getInt("progress", 0),
                        )
                        if (it.state.isFinished) {
                            uiState = uiState.copy(
                                result = it.outputData.getInt("result", 0),
                            )
                        }
                    }
                }
            }

//            workManager.enqueueUniquePeriodicWork(
//                "LoginTimeWork",
//                ExistingPeriodicWorkPolicy.UPDATE,
//                myWork
//            )
//
//            workManager.getWorkInfoByIdLiveData(workId!!).observeForever { workInfo ->
//                if (workInfo != null) {
//                    if (workInfo.state == WorkInfo.State.ENQUEUED) {
//                        Log.d("MyJobs", "here1")
//                        val secondsLoggedIn = workInfo.progress.getInt("secondsLoggedIn", 0)
//                        uiState = uiState.copy(secondsLoggedIn = secondsLoggedIn)
//                    } else if (workInfo.state == WorkInfo.State.FAILED || workInfo.state == WorkInfo.State.CANCELLED) {
//                        val channelId = "My Channel"
//                        Log.d("MyJobs", "here2")
//                        showSimpleNotification(
//                            getApplication<Application>().applicationContext,
//                            channelId,
//                            "Time Update Stopped",
//                            "You are offline! The time update stopped."
//                        )
//                        uiState = uiState.copy(isRunning = false)
//                    }
//                }
//                Log.d("MyJobs", "here3")
//            }
        }
    }

    fun cancelJob() {
        workManager.cancelWorkById(workId!!)
//        uiState = uiState.copy(isRunning = false)
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MyJobsViewModel(application)
            }
        }
    }
}

@Composable
fun MyJobs() {
    val myJobsViewModel = viewModel<MyJobsViewModel>(
        factory = MyJobsViewModel.Factory(
            LocalContext.current.applicationContext as Application
        )
    )

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Background Task: ${if (myJobsViewModel.uiState.isRunning) "Running" else "Stopped"}",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(end = 16.dp)
            )
            Button(
                onClick = { myJobsViewModel.cancelJob() },
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text("Cancel")
            }
        }
        Text(
            text = "Logged In for ${myJobsViewModel.uiState.progress} seconds",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
