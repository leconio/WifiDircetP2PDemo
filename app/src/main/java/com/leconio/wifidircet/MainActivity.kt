package com.leconio.wifidircet

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_wifi.view.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class MainActivity : AppCompatActivity() {

    var mChannel: WifiP2pManager.Channel? = null
    var mReceiver: WiFiDirectBroadcastReceiver? = null
    lateinit var mAdapter: MyAdapter

    private val mWifiP2pManager: WifiP2pManager by lazy(LazyThreadSafetyMode.NONE) {
        getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }

    private val mIntentFilter: IntentFilter by lazy(LazyThreadSafetyMode.NONE) {
        IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mChannel = mWifiP2pManager.initialize(this, mainLooper, null)
        mReceiver = WiFiDirectBroadcastReceiver(mWifiP2pManager, mChannel, this)
        listview.layoutManager = LinearLayoutManager(this)
        mAdapter = MyAdapter()
        listview.adapter = mAdapter
    }

    @NeedsPermission(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
    )
    fun scanWifiTo() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mWifiP2pManager.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Toast.makeText(this@MainActivity, "开始扫描P2P设备", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(reasonCode: Int) {
                Toast.makeText(this@MainActivity, "不支持的P2P设备", Toast.LENGTH_SHORT).show()

            }
        })
    }

    fun scanWifi(view: View) {
        scanWifiToWithPermissionCheck()
    }


    override fun onResume() {
        super.onResume()
        mReceiver?.also { receiver ->
            registerReceiver(receiver, mIntentFilter)
        }
    }

    override fun onPause() {
        super.onPause()
        mReceiver?.also { receiver ->
            unregisterReceiver(receiver)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    fun setList(peers: WifiP2pDeviceList?) {
        mAdapter.setList(peers)
    }

    @SuppressLint("MissingPermission")
    private fun connectTo(deviceAddress: String?) {
        deviceAddress?.let {
            val config = WifiP2pConfig()
            config.deviceAddress = it
            mWifiP2pManager.connect(mChannel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {}

                override fun onFailure(reason: Int) {}
            })
        }
    }


    class MyAdapter : RecyclerView.Adapter<MyHolder>() {

        private var list: Collection<WifiP2pDevice>? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_wifi, parent, false)
            return MyHolder(view)
        }

        override fun onBindViewHolder(holder: MyHolder, position: Int) {
            list?.let { devices ->
                for (device in devices) {
                    holder.itemView.name.text = device.deviceName
                    holder.itemView.mac.text = device.deviceAddress
                    holder.itemView.setOnClickListener {
                        (holder.itemView.context as MainActivity).connectTo(device.deviceAddress)
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return list?.size ?: 0
        }

        fun setList(peers: WifiP2pDeviceList?) {
            list = peers?.deviceList
            notifyDataSetChanged()
        }

    }

    class MyHolder(view: View) : RecyclerView.ViewHolder(view) {}

}