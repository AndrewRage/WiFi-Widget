package andrewrage.netinfo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class UpdateBroadcast: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        getWifiNetworkInfo(context) { wifiInfo ->
            updateWidgets(context, wifiInfo)
        }
    }
}