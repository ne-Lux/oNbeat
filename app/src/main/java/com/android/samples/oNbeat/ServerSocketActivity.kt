package com.android.samples.oNbeat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.android.samples.oNbeat.viewmodels.FTPClientViewModel
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.util.Timer
import java.util.TimerTask

// -----------------------------------------------------------------------------------------
// ServerSocketActivity is a child activity to MainActivity and handles TCP connections
// -----------------------------------------------------------------------------------------
class ServerSocketActivity : AppCompatActivity() {
    private var serverThread: Thread? = null
    private var serverSocket: ServerSocket? = null
    private var port = 29391
    private val viewModel: FTPClientViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println("ServerSocketActivity")
        startServerThread()

        val receiverFilter = IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED")
        registerReceiver(hotspotReceiver, receiverFilter)

    }

    // -----------------------------------------------------------------------------------------
    // Start TCP server in background thread
    // -----------------------------------------------------------------------------------------
    private fun startServerThread () {
        serverThread = Thread(ServerRunnable())
        serverThread!!.start()
    }

    private val hotspotReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if ("android.net.wifi.WIFI_AP_STATE_CHANGED" == action) {
                val state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)
                if (WifiManager.WIFI_STATE_ENABLED == state % 10) {
                    viewModel.setHotSpot(true)
                } else {
                    viewModel.setHotSpot(false)
                }
            }
        }
    }

    // -----------------------------------------------------------------------------------------
    // Inner Class ServerRunnable establishes one TCP server (for one device to connect)
    // -----------------------------------------------------------------------------------------
    internal inner class ServerRunnable : Runnable {

        override fun run() {
            try {
                serverSocket = ServerSocket(port)
                println(serverSocket?.inetAddress)
                println(serverSocket?.localPort)
            } catch (e: IOException) {
                e.printStackTrace()
                return
            }
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val socket = serverSocket!!.accept()
                    val commThread = CommunicationThread(socket)
                    Thread(commThread).start()
                    Timer().scheduleAtFixedRate(object : TimerTask() {
                        override fun run() {
                            if(socket.inetAddress.isReachable(1000)){
                                socket.inetAddress.hostAddress?.let { viewModel.addDevice(it) }
                            } else {
                                socket.inetAddress.hostAddress?.let { viewModel.removeDevice(it) }
                            }
                        }
                    },5000,5000)
                    val socketIP = socket.inetAddress.hostAddress
                    println("Client connected: $socketIP")
                    viewModel.setIP(socketIP)
                    println(viewModel.hostOne.value)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    // -----------------------------------------------------------------------------------------
    // CommunicationThread handles the incoming TCP messages of a client in a background thread
    // -----------------------------------------------------------------------------------------
    internal inner class CommunicationThread(clientSocket: Socket): Runnable {
        private lateinit var input: BufferedReader
        private var ipAddress: String = clientSocket.inetAddress.hostAddress as String

        init {
            try {
                input = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        override fun run() {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val read = input.readLine()
                    println(read)
                    viewModel.addPic2Download(ipAddress, read)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            serverSocket!!.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            serverThread!!.interrupt()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}