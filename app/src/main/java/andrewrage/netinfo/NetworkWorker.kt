package andrewrage.netinfo

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters

class NetworkWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        applicationContext.sendBroadcast(Intent(applicationContext, UpdateBroadcast::class.java))

        return Result.success()
    }
}