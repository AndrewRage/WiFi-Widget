package andrewrage.netinfo

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.core.content.ContextCompat.getSystemService
import java.net.Inet4Address
import java.net.Inet6Address

private val FAKE = BuildConfig.DEBUG && false
private val fakeInfo = StringBuilder().apply {
    append("IPv4:\u00A0192.168.0.101/24").append("\n")
    append("IPv6:\u00A0fec0::863f:1a:69ef:89d1:d449/64").append("\n")
    append("IPv6:\u00A0fec0::6086:4635:8e2d:ae81:51f0/64").append("\n")
    append("DHCP:\u00A0192.168.0.1").append("\n")
    append("DNS:\u00A08.8.8.8").append("\n")
    append("DNS:\u00A08.8.4.4")
}.toString()

internal fun getConnectivityManager(context: Context) =
    getSystemService(context, ConnectivityManager::class.java)

internal fun getActiveNetworkInfo(context: Context) = getConnectivityManager(context)?.run {
    activeNetwork?.let { format(context, this, it) }
} ?: context.getString(R.string.no_wifi_information)

internal fun getWifiNetworkInfo(context: Context, wifiInfoCallback: (String) -> Unit) {
    if (isWifiEnabled(context)) {
        registerNetworkCallback(context) { callback, network, _ ->
            wifiInfoCallback(format(context, callback.connectivityManager, network))
            callback.unregister()
        }
    } else {
        wifiInfoCallback(context.getString(R.string.no_wifi_information))
    }
}

internal fun format(context: Context, connectivityManager: ConnectivityManager, network: Network) =
    if (FAKE) fakeInfo else
    connectivityManager.run {
        getLinkProperties(network)?.let { properties ->
            val addresses = properties.linkAddresses
            val result = mutableListOf<String>().apply {
                addresses
                    .filter { it.address is Inet4Address }
                    .map { "IPv4:\u00A0$it" }
                    .takeIf { it.isNotEmpty() }
                    ?.let { addAll(it) }
                addresses
                    .filter { it.address is Inet6Address }
                    .map { "IPv6:\u00A0$it" }
                    .takeIf { it.isNotEmpty() }
                    ?.let { addAll(it) }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val dhcpServerAddress = properties.dhcpServerAddress
                    add("DHCP:\u00A0$dhcpServerAddress".replace("/", ""))
                }
                addAll(properties.dnsServers.map { "DNS:\u00A0$it".replace("/", "") })
            }.map { it.replace("/", "\u2215") }

            result.joinToString(separator = "\n")
        } ?: context.getString(R.string.no_wifi_information)
    }

internal fun isWifiEnabled(context: Context) = getConnectivityManager(context)?.run {
    getNetworkCapabilities(activeNetwork)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
} ?: false

internal data class NetworkCallback(
    val connectivityManager: ConnectivityManager,
    val callback: (NetworkCallback, Network, Boolean) -> Unit
): ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        callback(this, network, true)
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        callback(this, network, false)
    }

    fun getLinkProperties(network: Network) = connectivityManager.getLinkProperties(network)

    fun unregister() {
        connectivityManager.unregisterNetworkCallback(this)
    }
}

internal fun registerNetworkCallback(
    context: Context,
    callback: (NetworkCallback, Network, Boolean) -> Unit
): NetworkCallback? {

    return getConnectivityManager(context)?.let { connectivityManager ->

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        val networkCallback = NetworkCallback(connectivityManager, callback)

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        return networkCallback
    }
}

internal fun updateWidgets(context: Context, wifiInfo: String) = AppWidgetManager
    .getInstance(context).apply {
        val appWidgetIds = getAppWidgetIds(
            ComponentName(context, NetworkInfoWidgetProvider::class.java)
        )
        context.sendBroadcast(
            Intent(context, NetworkInfoWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                putExtra(NetworkInfoWidgetProvider.WIFI_INFO, wifiInfo)
            }
        )
    }