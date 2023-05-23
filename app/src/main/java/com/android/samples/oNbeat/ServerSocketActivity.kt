package com.android.samples.oNbeat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var viewModel: FTPClientViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println("Created")
        viewModel = ViewModelProvider(this)[FTPClientViewModel::class.java]
        startServerThread()

    }

    private fun startServerThread () {
        serverThread = Thread(ServerRunnable())
        serverThread!!.start()
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