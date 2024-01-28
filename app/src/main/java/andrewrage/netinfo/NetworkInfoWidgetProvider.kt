package andrewrage.netinfo

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit


class NetworkInfoWidgetProvider : AppWidgetProvider() {

    private var wifiInfo: String? = null

    override fun onReceive(context: Context, intent: Intent?) {
        wifiInfo = intent?.getStringExtra(WIFI_INFO)

        super.onReceive(context, intent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

        enqueue(context)

        if (wifiInfo == null) {
            context.sendBroadcast(Intent(context, UpdateBroadcast::class.java))
        }
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, wifiInfo, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetText: String?,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.network_info_widget)

        if (appWidgetText != null) {
            views.setTextViewText(
                R.id.appwidget_text,
                appWidgetText
            )
        }

        setOpenAppClickListener(context, views)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun setOpenAppClickListener(context: Context, views: RemoteViews) {
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
//                action = Intent.ACTION_MAIN
//                addCategory(Intent.CATEGORY_LAUNCHER)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
            PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.appwidget_view, pendingIntent)
    }

    private fun setUpdateClickListener(context: Context, views: RemoteViews) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, UpdateBroadcast::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.appwidget_view, pendingIntent)
    }

    private fun enqueue(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<NetworkWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        ).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)

        WorkManager.getInstance(context)
            .cancelUniqueWork(TAG)
    }

    companion object {
        const val TAG = "NetworkInfoWidgetProvider"
        const val WIFI_INFO = "wifi_info"
    }
}