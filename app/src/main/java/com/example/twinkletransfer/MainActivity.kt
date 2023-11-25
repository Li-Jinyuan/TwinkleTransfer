package com.example.twinkletransfer

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.twinkletransfer.ui.theme.TwinkleTransferTheme
import com.example.twinkletransfer.wifip2p.WiFiDirectBroadcastReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    //
    private val intentFilter = IntentFilter()

//  Indicates a change in the Wi-Fi P2P status.
    private val manager: WifiP2pManager? by lazy(LazyThreadSafetyMode.NONE) {
        getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
    }
    private var channel: WifiP2pManager.Channel? = null
    private var receiver: BroadcastReceiver? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TwinkleTransferTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        channel = manager?.initialize(this, mainLooper, null)
        receiver = WiFiDirectBroadcastReceiver(manager!!, channel!!, this)

        // check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        }
//        manager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
//            override fun onSuccess() {
//                // Code for when the discovery initiation is successful goes here.
//                // No services have actually been discovered yet, so this method
//                // can often be left blank.  Code for peer discovery goes in the
//                // onReceive method, detailed below.
//                Toast.makeText(this@MainActivity, "onSuccess", Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onFailure(reasonCode: Int) {
//                // Code for when the discovery initiation fails goes here.
//                // Alert the user that something went wrong.
//                Toast.makeText(this@MainActivity, "onFailure", Toast.LENGTH_SHORT).show()
//
//            }
//        })
        val job = GlobalScope.launch(Dispatchers.Default) {
            while (isActive) {
                // 发现操作
                manager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        // 处理成功情况
                        Toast.makeText(this@MainActivity, "onSuccess", Toast.LENGTH_SHORT).show()

                    }

                    override fun onFailure(reasonCode: Int) {
                        // 处理失败情况
                        Toast.makeText(this@MainActivity, "onFailure", Toast.LENGTH_SHORT).show()
                    }
                })

                // 等待一段时间后再次执行发现操作（比如每隔一段时间）
                delay(3000) // 10秒钟
            }
        }


    }

    override fun onResume() {
        super.onResume()
        receiver?.let { receiver ->
            registerReceiver(receiver, intentFilter)
        }
    }

    override fun onPause() {
        super.onPause()
        receiver?.let { receiver ->
            unregisterReceiver(receiver)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TwinkleTransferTheme {
        Greeting("Android")
    }
}


