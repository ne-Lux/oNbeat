package com.android.samples.oNbeat

import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.InetAddress
import java.net.SocketException
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.outputStream


class FTPClient(private val host: String, private val port: Int, private val user: String, private val pw: String, private val downloadList: List<String>): Runnable {

    private var ftpClient = FTPClient()
    private val directoryPath: String = "/storage/emulated/0/Android/data/oNbeat/"
    var isConnected = false
        private set

    init {
        println("init")
    }
    override fun run() {
        println("run")
        connect(host, port)
        login(user, pw)
        println("logged in")
        for (file in downloadList) {
            val destFilePath = "$directoryPath$file.jpg"
            if (!Files.exists(Path(directoryPath))) Files.createDirectory(Path(directoryPath))
            val newFile = File(destFilePath)
            newFile.createNewFile()
            val oFile = FileOutputStream(newFile, false)

            downloadFile("$file.jpg", oFile )

            oFile.close()

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

    /*fun getDirectories(path: String = ""): Array<FTPFile> {
        var files = emptyArray<FTPFile>()
        try {
            files = ftpClient.listDirectories(path)
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: SocketException) {
            ex.printStackTrace()
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

    private fun logout() {
        try {
            ftpClient.logout()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    /*fun changeDir(path: String, goToParent: Boolean = false) {
        try {
            if (goToParent) {
                ftpClient.changeToParentDirectory()
            } else {
                ftpClient.changeWorkingDirectory(path)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
    }*/

    fun getCurrentPath(): String {
        var result = ""
        try {
            result = ftpClient.printWorkingDirectory()
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
        return result
    }

    fun deleteFile(fileName: String) {
        try {
            val exists = ftpClient.deleteFile(fileName)
            println("File exists and deleted $exists")
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
    }

    /*fun createDir(dirName: String) {
        try {
            val created = ftpClient.makeDirectory(dirName)
            println("Dir created ${created}")
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
    }*/

    /*fun removeDir(dirName: String) {
        try {
            val removed = ftpClient.removeDirectory(dirName)
            println("Dir removed ${removed}")
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
    }*/

    private fun downloadFile(file: String, outputStream: OutputStream) {
        try {
            val fileName = file + ".jpg"
            val downloaded = ftpClient.retrieveFile(fileName, outputStream)
            println("File downloaded $downloaded")
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
    }

    /*fun uploadFile(fileName: String, inputStream: InputStream) {
        try {
            val uploaded = ftpClient.appendFile(fileName, inputStream)
            println("File uploaded $uploaded")
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
    }*/

    private fun disconnect() {
        try {
            ftpClient.disconnect()
            isConnected = false
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
}