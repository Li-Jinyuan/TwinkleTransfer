package com.example.twinkletransfer.wifip2p

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.twinkletransfer.R

class WifiListViewAdapter(private val context: Context, private val deviceList: MutableList<String>) :
    BaseAdapter() {
    override fun getCount(): Int {
        return deviceList.size
    }

    override fun getItem(position: Int): Any {
        return deviceList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // 重写该方法，该方法返回的View将作为列表框
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        if (convertView == null) {
            view = View.inflate(context, R.layout.item_wifi_list, null)
        } else {
            view = convertView
        }
        val deviceName = view.findViewById<TextView>(R.id.device_name)
        deviceName.text = deviceList[position]
        return view
    }
}