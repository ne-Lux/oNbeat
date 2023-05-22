package com.android.samples.oNbeat

import android.R
import android.app.Activity
import androidx.activity.viewModels
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.android.samples.oNbeat.viewmodels.FTPClientViewModel
import com.android.samples.oNbeat.viewmodels.GalleryFragmentViewModel
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.Scanner

class ServerSocketActivity : AppCompatActivity() {
    private val viewModel: FTPClientViewModel by viewModels()
    private var serverThread: Thread? = null
    private var serverSocket: ServerSocket? = null
    private var port = 29391


    fun startServerThread () {
        serverThread = Thread(ServerRunnable())
        serverThread!!.start()
    }

    fun OnDataReceive (data: String) {
        println(data)
        println("hallo")
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
            println("Nummer 1")
            while (!Thread.currentThread().isInterrupted) {
            // while (true) {
                println("Nummer 2")
                try {
                    println("Nummer 3")
                    // TODO("Handle the received message")
                    val read = input.read()
                    println("Nummer 4")
                    runOnUiThread(Runnable { this@ServerSocketActivity.OnDataReceive(read.toString()) })

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

/// ------------------------------------------------------------------------------------------------
/// Alternative zur Class CommunicationThread
class ClientHandler(client: Socket) {
    private val reader: Scanner = Scanner(client.getInputStream())
    private var running: Boolean = false

    fun run() {
        running = true
        println("running started")
        // Welcome message

        while (running) {
            println("running")
            try {
                val text = reader.nextLine()
                if (text == "EXIT") {
                    continue
                }

                println(text)
            } catch (ex: Exception) {
                // TODO: Implement exception handling
            } finally {

            }

        }
    }
}