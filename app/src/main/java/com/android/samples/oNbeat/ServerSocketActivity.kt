package com.android.samples.oNbeat

import android.app.Activity
import android.os.Bundle
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket

class ServerSocketActivity : Activity() {
    private var serverThread: Thread? = null
    private var serverSocket: ServerSocket? = null
    private var port = 29391

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    internal inner class CommunicationThread(clientSocket: Socket) :
        Runnable {
        private var input: BufferedReader? = null

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
                    // TODO("Handle the received message")
                    val read = input!!.readLine()
                    println(read)

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