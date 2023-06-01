package com.android.samples.oNbeat

import androidx.fragment.app.Fragment
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import org.tensorflow.lite.task.vision.detector.Detection
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.InetAddress
import java.net.SocketException
import java.nio.file.Files
import kotlin.io.path.Path


class FTPClient(private val firstESP32: Boolean,
                private val host: String,
                private val port: Int,
                private val user: String,
                private val pw: String,
                private val downloadList: List<String>,
                private val directoryPath: String,
                private val fileListener: FileListener?): Runnable {

    private var ftpClient = FTPClient()
    private var isConnected = false

    init {
        println("init")
    }
    override fun run() {
        println("run")
        connect(host, port)
        login(user, pw)
        println("logged in")
        for (file in downloadList) {
            val fileName: String = if (firstESP32) {
                "start_$file.jpg"
            } else {
                "finish_$file.jpg"
            }
            val destFilePath = directoryPath + fileName
            if (!Files.exists(Path(directoryPath))) Files.createDirectory(Path(directoryPath))
            val newFile = File(destFilePath)
            newFile.createNewFile()
            val oFile = FileOutputStream(newFile, false)

            val success = downloadFile(fileName, oFile )
            oFile.close()
            if (success) {
                //ftpViewModel.downloadCompleted(file, firstESP32)
                deleteFile(fileName)
            } else {
                newFile.delete()
            }
        }
        logout()
        disconnect()
    }

    private fun connect(hostAddress: String, port: Int) {
        try {
            val address: InetAddress = InetAddress.getByName(hostAddress)
            ftpClient.connect(address, port)
            val replyCode = ftpClient.replyCode
            isConnected = FTPReply.isPositiveCompletion(replyCode)
            if (!isConnected) {
                println("Disconnecting")
                ftpClient.disconnect()
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    private fun login(username: String, password: String) {
        try {
            ftpClient.login(username, password)

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
            ftpClient.enterLocalPassiveMode()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    private fun downloadFile(fileName: String, outputStream: OutputStream): Boolean {
        return try {
            println(fileName)
            val downloaded = ftpClient.retrieveFile(fileName, outputStream)
            println("File downloaded: $downloaded")
            downloaded
        } catch (ex: IOException) {
            ex.printStackTrace()
            false
        } catch (ex: SocketException) {
            ex.printStackTrace()
            false
        }
    }

    private fun deleteFile(fileName: String) {
        try {
            val success = ftpClient.deleteFile(fileName)
            println("File exists and deleted: $success")
            if (success) {
                fileListener?.onDownloaded(fileName)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
    }

    private fun logout() {
        try {
            ftpClient.logout()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    private fun disconnect() {
        try {
            ftpClient.disconnect()
            isConnected = false
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    interface FileListener {
        fun onDownloaded(
            fileName: String
        )
    }

    /*fun getFiles(path: String = ""): Array<FTPFile> {
        var files = emptyArray<FTPFile>()
        try {
            files = ftpClient.listFiles(path)
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: SocketException) {
            ex.printStackTrace()
        } finally {
            println("Code ${ftpClient.replyCode} Reply ${ftpClient.replyStrings.joinToString("")}")
        }
        return files
    }*/

    /*fun getFileNames(path: String = "") : Array<String> {
        var files = emptyArray<String>()
        try {
            files = ftpClient.listNames(path)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return files
    }*/
}