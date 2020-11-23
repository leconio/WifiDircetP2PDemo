package com.leconio.wifidircet

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast


class WiFiDirectBroadcastReceiver(
    private val mManager: WifiP2pManager?,
    private val mChannel: WifiP2pManager.Channel?,
    private val mActivity: MainActivity
) : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION == action) {
            if (wifiManager.wifiState == WifiManager.WIFI_STATE_DISABLED) {
                //关闭状态则打开
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mActivity.startActivity(Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY))
                    Toast.makeText(context, "请打开Wifi", Toast.LENGTH_SHORT).show()
                } else {
                    wifiManager.isWifiEnabled = true
                    Toast.makeText(context, "wifi已打开", Toast.LENGTH_SHORT).show()
                }
            } else {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                when (state) {
                    WifiP2pManager.WIFI_P2P_STATE_ENABLED -> {
                        Toast.makeText(context,"Wifi P2P is enabled",Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(context,"Wi-Fi P2P is not enabled",Toast.LENGTH_SHORT).show()
                    }
                }

            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION == action) {
            mManager?.requestPeers(mChannel) { peers ->
                for (device in peers.deviceList) {
//                    connectToDevice()
                }
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION == action) {
            val networkInfo = intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected) {

            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION == action) {

        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: WifiP2pDevice) {
        val config = WifiP2pConfig()
        config.deviceAddress = device.deviceAddress
        mManager?.connect(mChannel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Toast.makeText(mActivity, "连接成功", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(reason: Int) {}
        })
    }

}