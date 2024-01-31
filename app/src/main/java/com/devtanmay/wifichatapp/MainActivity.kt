package com.devtanmay.wifichatapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PermissionChecker.PERMISSION_DENIED
import androidx.core.content.PermissionChecker.checkCallingOrSelfPermission
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() ,sendDataInterface {

lateinit var connectionStatus:TextView
    lateinit var messageTextView:TextView
    lateinit var listView:ListView
    lateinit var typeMsg:EditText
    lateinit var sendButton: ImageButton
    lateinit var aSwitch: ImageButton
    lateinit var discoverButton:ImageButton
    lateinit var locationButton:ImageButton

lateinit var messageRecyclerView: RecyclerView

    lateinit var wifiP2pManager: WifiP2pManager
    lateinit var channel:WifiP2pManager.Channel
    lateinit var broadcastReceiver: BroadcastReceiver
    lateinit var intentFilter: IntentFilter
var senderDeviceName=""

    lateinit var adapter:ArrayAdapter<WifiP2pDevice>
 lateinit var messageAdapter:CustomAdapter


    private val peers = mutableListOf<WifiP2pDevice>()
   val messageList = mutableListOf<messageModel>()

//    var peers:ArrayList<WifiP2pDevice> = ArrayList<WifiP2pDevice>()
//    var deviceNameArray:ArrayList<String> = arrayListOf()
//
//   var deviceArray: ArrayList<WifiP2pDevice> = arrayListOf()

    val MESSAGE_READ = 1
//lateinit var socket: Socket //error
var serverClass:ServerClass?=null
 var clientClass: ClientClass?=null
var isHost=false
    private val PERMISSIONS_REQUEST_CODE = 123
    private val PERMISSION_REQUEST = 10
    private var permissionEnabled = false
    private var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.SEND_SMS)
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (checkCallingOrSelfPermission(applicationContext,permissions[0]) == PERMISSION_DENIED
            || checkCallingOrSelfPermission(applicationContext,permissions[1])== PERMISSION_DENIED
            || checkCallingOrSelfPermission(applicationContext,permissions[2])== PERMISSION_DENIED)
        {

//            requestPermissions(permissions, 10)
        }


            if (checkPermission(permissions)) {
              //  Toast.makeText(applicationContext,"tru",Toast.LENGTH_SHORT).show()
                permissionEnabled = true
            } else {
              //  Toast.makeText(applicationContext,"fasle",Toast.LENGTH_SHORT).show()
                requestPermissions(permissions, PERMISSION_REQUEST)
            }



        init()
        adapter=ArrayAdapter(applicationContext,android.R.layout.simple_list_item_1,peers)
        listView.adapter=adapter


exqListner()
        sendButton.setOnClickListener {

            val executor=Executors.newSingleThreadExecutor()
            val msg=typeMsg.text.toString()
            messageList.add(messageModel(msg,"self"))
            Log.d("sendButtonTanmay", "messageListlength: "+messageList.size.toString())
            messageAdapter.notifyDataSetChanged()
            executor.execute(Runnable {

                run {
                    if (isHost){
                        Log.d("tanmay", "server started")
                        serverClass?.write(msg.toByteArray())
                        Log.d("tanmay", "server started :bytearray"+msg.toString())

                    }

                    else if(!isHost){
                        Log.d("tanmay", "client started")
                        clientClass?.write(msg.toByteArray())
                        Log.d("tanmay", "client started :bytearray"+msg.toByteArray().toString())
                    }
                }

            })

            typeMsg.text.clear()

        }




    }

    fun DisplayText(tempMsg:String) {
        messageTextView.text = tempMsg
    }


    public class ClientClass(hostAddress: InetAddress, context: Context ): Thread(){
      var c=context
             var socket: Socket?= null
        var hostAdd: String?
        private var callback: sendDataInterface? = null

        // Set the callback
        fun setCallback(callback: sendDataInterface) {
            this.callback = callback
        }

        // Simulate receiving data from the server
        fun receiveDataFromServer(msg:String) {

            callback?.sendData(msg)
        }

        init {
            hostAdd =hostAddress.hostAddress
        }
        lateinit var  inputStream: InputStream
        lateinit var outputStream: OutputStream
        fun write(bytes:ByteArray){
            try {
                outputStream.write(bytes)
            }
            catch (e:Exception){
                Log.d("tanmay", "write: client"+e.toString())
            }

        }
        override fun run() {
            socket= Socket()
            socket!!.keepAlive = true
            //try catch client
            socket!!.connect(InetSocketAddress(hostAdd, 2323), 5000)

            inputStream= socket!!.getInputStream()
            outputStream= socket!!.getOutputStream()

            var listening = true
            val executorService= Executors.newSingleThreadExecutor()
            val handler = Handler(Looper.getMainLooper())

            executorService.execute(
                Runnable {
                   run {
                        val buffer= ByteArray(1024)
                        var bytes:Int
                        outloop@while (socket != null){

                            try {

                                bytes=inputStream.read(buffer)
                                if(bytes>0){
                                    val finalBytes=bytes
                                    handler.post{
                                        val tempMSG=String(buffer,0,finalBytes)
                                        receiveDataFromServer(tempMSG)
                                        Toast.makeText(c,"new message:"+tempMSG.toString(),Toast.LENGTH_SHORT).show()
                                    //   (c as MainActivity).messageTextView.text=tempMSG  //error cannot be cast to mainactivity
                                    }




                                }
                            }
                            catch (e: Exception){
                                Log.d("tanmay", "run: "+e.toString())
                            }
                        }

                    }
                }

            )
        }




    }
    public class ServerClass(context: Context) : Thread(){
        var socket: Socket? = null
        var serverSocket: ServerSocket? = null
        lateinit var inputStream: InputStream
        lateinit var outputStream: OutputStream
val c=context

        private var callback: sendDataInterface? = null

        // Set the callback
        fun setCallback(callback: sendDataInterface) {
            this.callback = callback
        }

        // Simulate receiving data from the server
        fun receiveDataFromServer(msg:String) {

            callback?.sendData(msg)
        }

        fun write(bytes:ByteArray){
            try {
                outputStream.write(bytes)
            }
            catch (e:Exception){
                Log.d("tanmay", "write: server"+e.toString())
            }


        }



        override fun run() {
            try {


                serverSocket = ServerSocket(2323)
                serverSocket!!.reuseAddress = true
                socket= serverSocket!!.accept()
                val executor=Executors.newSingleThreadExecutor()
                inputStream=socket!!.getInputStream()
                outputStream=socket!!.getOutputStream()

                executor.execute(Runnable {

                    run {
                        val buffer= ByteArray(1024)
                        var bytes:Int
                        outloop@while (socket != null){

                            try {

                                bytes=inputStream.read(buffer)
                                if(bytes>0){
                                    val finalBytes=bytes
                                    android.os.Handler(Looper.getMainLooper()).post {
                                        val tempMSG=String(buffer,0,finalBytes)

receiveDataFromServer(tempMSG)
                                        Toast.makeText(c,"new message: "+tempMSG.toString(),Toast.LENGTH_SHORT).show()
                                        //(c as MainActivity).messageTextView.text=tempMSG

                                    }


                                }
                            }
                            catch (e: Exception){
                                Log.d("tanmay", "run: server"+e.toString())
                            }
                        }

                    }

                })

            } catch (se:Exception) {
                Log.d("tanmay", "run: server outside"+se.toString())

            }
        }


    }

    val connectionListner=WifiP2pManager.ConnectionInfoListener {
        val groupOwnerAddress=it.groupOwnerAddress
        if(it.groupFormed && it.isGroupOwner){
            connectionStatus.text="Host"
            messageRecyclerView.visibility=View.VISIBLE
            listView.visibility=View.GONE
            isHost=true
            serverClass= ServerClass(context = applicationContext
            )
            serverClass!!.setCallback(this)

            serverClass!!.start()
        }
        else if (it.groupFormed ){
            connectionStatus.text="Client"
            messageRecyclerView.visibility=View.VISIBLE
            listView.visibility=View.GONE
isHost=false
clientClass= ClientClass(groupOwnerAddress,applicationContext)
            clientClass!!.setCallback(this)
            clientClass!!.start()
        }
    }


 val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        val refreshedPeers = peerList.deviceList
        if (refreshedPeers != peers) {
            peers.clear()
            peers.addAll(refreshedPeers)

            // If an AdapterView is backed by this data, notify it
            // of the change. For instance, if you have a ListView of
            // available peers, trigger an update.
            adapter.notifyDataSetChanged()

            // Perform any other updates needed based on the new list of
            // peers connected to the Wi-Fi P2P network.
        }

        if (peers.isEmpty()) {
            connectionStatus.text="No device found"
            return@PeerListListener
        }
    }




//    val peerListListener=  WifiP2pManager.PeerListListener {
//        if(!it.deviceList.equals(peers)){
//            peers.clear()
//            peers.addAll(it.deviceList)
//
//            for (item in it.deviceList){
//                deviceNameArray.add(item.deviceName)
//                deviceArray.add(item)
//            }
//
//            adapter.notifyDataSetChanged()
//
//            if(peers.size==0){
//                connectionStatus.text="No device found"
//
//                return@PeerListListener
//            }
//        }
//    }
//


    @SuppressLint("MissingPermission")
    private fun exqListner() {


      aSwitch.setOnClickListener{
         val intent: Intent =Intent(Settings.ACTION_WIFI_SETTINGS)
          startActivityForResult(intent,1)
      }

locationButton.setOnClickListener {
    val intent: Intent =Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    startActivityForResult(intent,2)
}

        discoverButton.setOnClickListener {

messageRecyclerView.visibility=View.VISIBLE
            messageList.clear()
            messageAdapter.notifyDataSetChanged()
            listView.visibility=View.VISIBLE


            wifiP2pManager.discoverPeers(channel,object:WifiP2pManager.ActionListener{
                override fun onSuccess() {
                 connectionStatus.text="Discovery Started"
                }

                override fun onFailure(p0: Int) {
                    if (p0==2){
                        connectionStatus.text="Turn on wifi again"
                    }
                    else if (p0==0){
                        connectionStatus.text="Turn on location again"
                    }
                    else{
                        connectionStatus.text="Discovery Not Started:$p0"
                    }

                }

            })



        }

        listView.setOnItemClickListener { adapterView, view, i, l ->
            val device= peers[i]
            val config=WifiP2pConfig()
            config.deviceAddress=device.deviceAddress
            wifiP2pManager.connect(channel,config, object : WifiP2pManager.ActionListener {

                @SuppressLint("SetTextI18n")
                override fun onSuccess() {
                    connectionStatus.text="Connected to "+device.deviceName
                    senderDeviceName=device.deviceName
                    listView.visibility= View.GONE
                    messageRecyclerView.visibility=View.VISIBLE
                  //  messageList.clear()
                   // messageAdapter.notifyDataSetChanged()
                }

                override fun onFailure(reason: Int) {
                    //failure logic
                    connectionStatus.text="Not Connected to "+device.deviceName

                    listView.visibility= View.VISIBLE
                  //  messageRecyclerView.visibility=View.GONE
                 //   messageList.clear()
                  //  messageAdapter.notifyDataSetChanged()
                }
            })


        }
    }

    private fun init() {

        messageTextView = findViewById(R.id.message)
        listView = findViewById(R.id.listView)
messageRecyclerView=findViewById(R.id.recyclerView)
      messageAdapter = CustomAdapter(messageList)
        typeMsg = findViewById(R.id.editText)
        sendButton = findViewById(R.id.sendButton)
        aSwitch = findViewById(R.id.aSwitch)
        discoverButton = findViewById(R.id.discover)
        connectionStatus = findViewById(R.id.connectionStatus)



        locationButton=findViewById(R.id.locationButton)
        wifiP2pManager = getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager
        channel = wifiP2pManager.initialize(this, mainLooper, null)
        broadcastReceiver =
            WifiDirectBroadcastReceiver(manager = wifiP2pManager, channel = channel, this)
        intentFilter = IntentFilter()
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        messageRecyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter=CustomAdapter(messageList)
        messageRecyclerView.adapter=messageAdapter
    }




    override fun onResume() {
        super.onResume()
        registerReceiver(broadcastReceiver,intentFilter)

    }


    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcastReceiver)
    }



    private fun checkPermission(permissionArray: Array<String>): Boolean {
        var allSuccess = true
        for (i in permissionArray.indices) {
            if (checkCallingOrSelfPermission(permissionArray[i]) == PackageManager.PERMISSION_DENIED)
                allSuccess = false
        }
        return allSuccess
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if(permissionEnabled)
        {
            if (requestCode == PERMISSION_REQUEST) {
                var allSuccess = true
                for (i in permissions.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        allSuccess = false
                        val requestAgain = shouldShowRequestPermissionRationale(permissions[i])
                        if (requestAgain) {
                            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Go to settings and enable the permission", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                if (allSuccess)
                    permissionEnabled = true

            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun sendData(str: String?) {
        Log.d("tanmay", "sendData: deviceName"+senderDeviceName)
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            messageList.add(messageModel(str,senderDeviceName))
            messageAdapter.notifyDataSetChanged()
            messageTextView.text=str}


    }


}