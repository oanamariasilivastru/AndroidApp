package com.example.androidapp.core.ui

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit.SECONDS

class MyWorker(
    context: Context,
    val workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        Log.d("MyWorker", "Work started")
        var s = 0
        val to = workerParams.inputData.getInt("to", 1)
        Log.d("MyWorker", "to: $to")
        for (i in 1..to) {
            if (isStopped) {
                Log.d("MyWorker", "Work stopped")
                break
            }
            SECONDS.sleep(1)
            Log.d("MyWorker", "progress: $i")
            setProgressAsync(workDataOf("progress" to i))
            s += i
        }
        Log.d("MyWorker", "Work completed with result: $s")
        return Result.success(workDataOf("result" to s))
    }
}
