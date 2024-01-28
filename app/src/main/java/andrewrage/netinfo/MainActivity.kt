package andrewrage.netinfo

import andrewrage.netinfo.ui.theme.NetInfoTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp


class MainActivity : ComponentActivity() {

    private val wifiInfo = mutableStateOf("")
    private var registerNetworkCallback: NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        wifiInfo.value = getString(R.string.loading)

        setContent {
            NetInfoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NetworkInfoContent(wifiInfo)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        registerNetworkCallback?.unregister()
        registerNetworkCallback = registerNetworkCallback(this) { callback, info, _ ->
            val value = format(this, callback.connectivityManager, info)

            updateWidgets(this, value)


            wifiInfo.value = value
        }
    }

    override fun onDestroy() {
        registerNetworkCallback?.unregister()

        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkInfoContent(wifiInfo: MutableState<String>) {
    val info by wifiInfo

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
            title = {
                Text(text = stringResource(id = R.string.app_name))
            }
        )
        Text(
            text = stringResource(R.string.disclaimer),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(16.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = info,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}