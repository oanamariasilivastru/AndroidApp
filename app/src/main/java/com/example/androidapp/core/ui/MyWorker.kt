package com.example.androidapp.core.ui

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit.SECONDS

class MyWorker (
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        return try {
            var s = 0

            while(!isStopped) {
                SECONDS.sleep(1)
                setProgressAsync(workDataOf("progress" to s))
                s++
            }

//            for (i in 1..workerParams.inputData.getInt("to", 1)) {
//                if (isStopped) {
//                    break
//                }
//                SECONDS.sleep(1)
//                Log.d("MyWorker", "progress: $i")
//                setProgressAsync(workDataOf("progress" to i))
//                s += i
//            }

            Log.d("LoginTimeWorker", "Logged in time: $s seconds")

            return Result.success(workDataOf("result" to s))
        } catch (e: Exception) {
            Log.e("LoginTimeWorker", "Error counting login time", e)
            Result.retry()
        }
    }
}