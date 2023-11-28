package com.example.twinkletransfer

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
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

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

//  Indicates a change in the Wi-Fi P2P status.
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var manager: WifiP2pManager

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



        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)

        receiver = WiFiDirectBroadcastReceiver( this, manager, channel)


    }

    override fun onResume() {
        super.onResume()
        receiver?.also { receiver ->
            registerReceiver(receiver, intentFilter)
            discoverPeers()
        }
    }
    override fun onPause() {
        super.onPause()
        receiver?.also { receiver ->
            unregisterReceiver(receiver)
        }
    }


    // 多线程启动discover peers
    private fun discoverPeers() {
        manager.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Toast.makeText(this@MainActivity, "removeGroup onSuccess", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(reason: Int) {
                Toast.makeText(this@MainActivity, "removeGroup onFailure", Toast.LENGTH_SHORT).show()
            }
        })
//        val job = GlobalScope.launch(Dispatchers.Default) {
//            while (isActive) {

                // check permission
                if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
                }

                val state = manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        // 处理成功情况
                        Toast.makeText(this@MainActivity, "onSuccess", Toast.LENGTH_SHORT).show()

                    }

                    override fun onFailure(reasonCode: Int) {
                        // 处理失败情况
                        if(reasonCode == WifiP2pManager.P2P_UNSUPPORTED) {
                            Toast.makeText(this@MainActivity, "P2P_UNSUPPORTED", Toast.LENGTH_SHORT).show()
                        } else if (reasonCode == WifiP2pManager.ERROR) {
                            Toast.makeText(this@MainActivity, "ERROR", Toast.LENGTH_SHORT).show()
                        } else if (reasonCode == WifiP2pManager.BUSY) {
                            Toast.makeText(this@MainActivity, "BUSY", Toast.LENGTH_SHORT).show()
                        }else {
                            Toast.makeText(this@MainActivity, "onFailure", Toast.LENGTH_SHORT).show()
                        }
                    }
                })

//                // 等待一段时间后再次执行发现操作（比如每隔一段时间）
//                delay(5000) // 10秒钟
//            }
//        }
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


