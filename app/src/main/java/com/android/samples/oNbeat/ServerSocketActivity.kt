package com.android.samples.oNbeat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.samples.oNbeat.viewmodels.FTPClientViewModel
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket

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
        viewModel.setHotSpot(checkHotspot())
    }
    private fun startServerThread () {
        serverThread = Thread(ServerRunnable())
        serverThread!!.start()
    }

    private fun checkHotspot(): Boolean {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiPasspointEnabled
    }

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
                    viewModel.setHotSpot(checkHotspot())

                    val socket = serverSocket!!.accept()
                    val commThread = CommunicationThread(socket)
                    Thread(commThread).start()
                    // Thread { ClientHandler(socket).run() }
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

    internal inner class CommunicationThread(clientSocket: Socket): Runnable {
        private var input: BufferedReader
        private var ipAddress: String = clientSocket.inetAddress.hostAddress

        init {
            try {
                input = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            } catch (e: IOException) {
                TODO("Bei exception connection closen?")
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