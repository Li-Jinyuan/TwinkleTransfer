package com.example.twinkletransfer

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import com.example.twinkletransfer.wifip2p.WiFiDirectBroadcastReceiver
import com.example.twinkletransfer.wifip2p.WifiListViewAdapter
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

    private var receiver = object  : BroadcastReceiver() {
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
                            Toast.makeText( this@MainActivity , "WIFI_P2P_STATE_DISABLED", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }

                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    // Call WifiP2pManager.requestPeers() to get a list of current peers
//                Toast.makeText(activity, "WIFI_P2P_PEERS_CHANGED_ACTION", Toast.LENGTH_SHORT).show()
                    // 获取设备列表
                    deviceList.clear()
                    deviceNameList.clear()
                    manager.requestPeers(channel) { peers ->
                        // TODO 传输peers给deviceList
                        for (device in peers.deviceList) {

                            deviceList.add(device)
                            deviceNameList.add(device.deviceName)
                        }

//                    Toast.makeText(activity, deviceList.toString(), Toast.LENGTH_SHORT).show()
                    }
                    listAdapter?.notifyDataSetChanged()
                }

                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    // Respond to new connection or disconnections
                    Toast.makeText(this@MainActivity, "WIFI_P2P_CONNECTION_CHANGED_ACTION", Toast.LENGTH_SHORT)
                        .show()
                }

                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    // Respond to this device's wifi state changing
                    Toast.makeText(this@MainActivity, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    // 按钮
    private var buttonState = false

    // 设备列表
    private var deviceList = mutableListOf<WifiP2pDevice>()
    private var deviceNameList = mutableListOf<String>()
    private var listAdapter: WifiListViewAdapter? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)
//        receiver = WiFiDirectBroadcastReceiver(this, manager, channel, deviceList, deviceNameList)

        manager.cancelConnect(channel, null)
        manager.removeGroup(channel, null)
        listAdapter = WifiListViewAdapter(this, deviceNameList)




    }

    override fun onResume() {
        super.onResume()
        receiver?.also { receiver ->
            registerReceiver(receiver, intentFilter)
        }
    }

    override fun onPause() {
        super.onPause()
        receiver?.also { receiver ->
            unregisterReceiver(receiver)
        }
    }

    //init view
    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()

        val myButton: Button = findViewById(R.id.myButton)
        myButton.setOnClickListener {
            // 这里是按钮点击后执行的操作
            if (buttonState) {
                buttonState = false
                myButton.text = "开始"
                Toast.makeText(this@MainActivity, "开始", Toast.LENGTH_SHORT).show()
                discoverPeersSuspend()
            } else {
                buttonState = true
                myButton.text = "停止"
                Toast.makeText(this@MainActivity, "停止", Toast.LENGTH_SHORT).show()
            }
        }
        listAdapter?.notifyDataSetChanged()
//        // 关联到listView
        val myListView = findViewById<ListView>(R.id.listView)
        // 设置myListView的适配器
        myListView.adapter = listAdapter

        // 点击事件
        myListView.setOnItemClickListener { _, _, position, _ ->
            // 获取点击的设备
            val device: WifiP2pDevice = deviceList[position]
            // 获取设备的地址
            val deviceAddress: String = device.deviceAddress
            // 创建一个配置
            val config = WifiP2pConfig()
            // 设置要连接的设备的地址
            config.deviceAddress = deviceAddress
            // 设置WPA2的加密方式
            config.wps.setup = WpsInfo.PBC
            // 连接设备
            manager.connect(channel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    // 连接成功
                    Toast.makeText(this@MainActivity, "连接成功", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(reason: Int) {
                    // 连接失败
                    Toast.makeText(this@MainActivity, "连接失败", Toast.LENGTH_SHORT).show()
                }
            })


        }

    }

    private fun discoverPeersSuspend() {
        GlobalScope.launch(Dispatchers.Main) {
            while (isActive) {
                Log.d("MainActivity", "discoverPeers")
                discoverPeers()
                delay(10000)

            }
        }
    }

    //    @SuppressLint("MissingPermission")
    private fun discoverPeers() {



        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 申请权限
            requestPermission()


        }
        val state = manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {

            override fun onSuccess() {
                // 处理成功情况
                Toast.makeText(this@MainActivity, "onSuccess", Toast.LENGTH_SHORT).show()
                listAdapter?.notifyDataSetChanged()


            }

            override fun onFailure(reasonCode: Int) {
                // 处理失败情况
                if (reasonCode == WifiP2pManager.P2P_UNSUPPORTED) {
                    Toast.makeText(this@MainActivity, "P2P_UNSUPPORTED", Toast.LENGTH_SHORT).show()
                } else if (reasonCode == WifiP2pManager.ERROR) {
                    Toast.makeText(this@MainActivity, "ERROR", Toast.LENGTH_SHORT).show()
                } else if (reasonCode == WifiP2pManager.BUSY) {
                    Toast.makeText(this@MainActivity, "BUSY", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "onFailure", Toast.LENGTH_SHORT).show()
                }
            }
        })

    }


    // 申请权限
    // TODO 后面这个地方要重构
    private fun requestPermission() {
        val permissionNeed = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionNeed.add(Manifest.permission.READ_MEDIA_IMAGES)
                permissionNeed.add(Manifest.permission.READ_MEDIA_AUDIO)
                permissionNeed.add(Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                permissionNeed.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            permissionNeed.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionNeed.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionNeed.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        } else {
            permissionNeed.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionNeed.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }


        // 申请权限
        requestPermissions(permissionNeed.toTypedArray(), 1)


    }

}