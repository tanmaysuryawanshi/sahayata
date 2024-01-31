package com.devtanmay.wifichatapp

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Looper
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.logging.Handler

class WifiDirectBroadcastReceiver( private val manager: WifiP2pManager,
                                   private val channel: WifiP2pManager.Channel,
                                   private val activity: MainActivity):BroadcastReceiver() {



    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
val action: String? = intent?.action
        when (action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                // Check to see if Wi-Fi is enabled and notify appropriate activity
            }

            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                if (manager!=null){
                    manager.requestPeers(channel,activity.peerListListener)
                }

            }

            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                // Respond to new connection or disconnections



                if(manager!=null){
                    val newtorkInfo:NetworkInfo?=intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)
                    if (newtorkInfo != null) {
                        if(newtorkInfo.isConnected()){
                            manager.requestConnectionInfo(channel,activity.connectionListner)
                        }
                        else{

                            activity.connectionStatus.text="Not Connected"
                        }
                    }

                }

            }
//            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
//                // Respond to this device's wifi state changing
//            }
        }

    }



}