package com.example.twinkletransfer.wifip2p

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pManager
import android.widget.Toast

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
class WiFiDirectBroadcastReceiver( private val activity: Activity,
                                     private val manager: WifiP2pManager,
                                      private val channel: WifiP2pManager.Channel,
                                    private val deviceList: MutableList<String>
) : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
//        val action: String = intent.action!!
        when (intent.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                when (state) {
                    WifiP2pManager.WIFI_P2P_STATE_ENABLED -> {
                        // Wifi P2P is enabled
//                        Toast.makeText(activity, "WIFI_P2P_STATE_ENABLED", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // Wi-Fi P2P is not enabled
                        Toast.makeText(activity, "WIFI_P2P_STATE_DISABLED", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                Toast.makeText(activity, "WIFI_P2P_PEERS_CHANGED_ACTION", Toast.LENGTH_SHORT).show()
                // 获取设备列表
                manager.requestPeers(channel) { peers ->
                    deviceList.clear()
                    peers.deviceList.forEach {
                        deviceList.add(it.deviceName)
                    }
                    Toast.makeText(activity, deviceList.toString(), Toast.LENGTH_SHORT).show()
                }
            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                // Respond to new connection or disconnections
                Toast.makeText(activity, "WIFI_P2P_CONNECTION_CHANGED_ACTION", Toast.LENGTH_SHORT).show()
            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                // Respond to this device's wifi state changing
                Toast.makeText(activity, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION", Toast.LENGTH_SHORT).show()
            }
        }
    }
}