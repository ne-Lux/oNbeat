package com.android.samples.oNbeat

import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import java.util.*
import kotlin.concurrent.thread


fun main(args: Array<String>) {
    val server = ServerSocket(9999)
    println("Server is running on port ${server.localPort}")

    while (true) {
        val client = server.accept()
        println("Client connected: ${client.inetAddress.hostAddress}")

        // Run client in it's own thread.
        thread { ClientHandler(client).run() }
    }

}

class ClientHandler(socket: Socket) {
    // rename client -> socket
    private val socket: Socket = socket
    private val reader: Scanner = Scanner(socket.getInputStream())
    private val writer: OutputStream = socket.getOutputStream()
    private var running: Boolean = false

    fun run() {
        running = true
        // Welcome message
        write("Welcome to the server!")

        // Alternativer Ansatz
        dis = DataInputStream(socket.getInputStream())

        while (running) {
            try {
                // Alternativer Ansatz
                // -----------------------------------------------------------------------------------------------
                if (dis.available() > 0) {
                    // ToDo "Bufferedreader?"
                }



                // -----------------------------------------------------------------------------------------------
                val text = reader.nextLine()
                if (text == "EXIT"){
                    shutdown()
                    continue
                }
                write("acknowledged")
            } catch (ex: Exception) {
                // TODO: Implement exception handling
                shutdown()
            } finally {

            }

        }
    }

    private fun write(message: String) {
        writer.write((message + '\n').toByteArray(Charset.defaultCharset()))
    }

    private fun shutdown() {
        running = false
        socket.close()
        println("${client.inetAddress.hostAddress} closed the connection")
    }

}