package com.android.samples.oNbeat

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.android.samples.oNbeat.viewmodels.FTPClientViewModel
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket

class ServerSocketActivity : AppCompatActivity() {
    private val viewModel: FTPClientViewModel by viewModels()
    private var serverThread: Thread? = null
    private var serverSocket: ServerSocket? = null
    private var port = 29391


    fun startServerThread () {
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
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    internal inner class CommunicationThread(clientSocket: Socket): Runnable {
        private var input: BufferedReader
        private var ipAdress: String = clientSocket.inetAddress.toString()

        init {
            try {
                input = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                println("Bufferedreader gestartet")
            } catch (e: IOException) {
                TODO("Bei exception connection closen?")
                e.printStackTrace()
            }
        }

        override fun run() {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val read = input.read().toString()
                    println(read)
                    viewModel.addPic2Download(ipAdress, read)
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