package com.android.samples.photic

import android.annotation.SuppressLint
import android.app.Activity
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.io.path.Path


class WriteExifActivity : Activity() {
    private val storage = "/storage/emulated/0/"
    private var format = ""
    private var fullPath: String = ""
    private var fullPathNF: String = ""

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class, ParseException::class)
    fun apply(path: String, filename: String, dateToSet: Date): String {

        if(Files.exists(Path("$storage$path$filename.jpg"))) format = ".jpg"
        else if (Files.exists(Path("$storage$path$filename.jpeg"))) format = ".jpeg"

        val dateTimeFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss")
        val date = dateTimeFormat.format(dateToSet)
        fullPath = storage + path + filename + format
        if(path=="DCIM/Changed/") {
            if (filename.takeLast(2) == "_1")
                fullPathNF = storage + "DCIM/Changed/" + filename.take(filename.length - 2) + format
            else
                fullPathNF = storage + "DCIM/Changed/" + filename + "_1" + format
        }
        else fullPathNF = storage + "DCIM/Changed/" + filename + "_1" + format

        //---------------------------------------------------------------------------------------------------------------------
        //Copy existing File
        if (!Files.exists(Path(storage+"DCIM/Changed"))) Files.createDirectory(Path(storage+"DCIM/Changed"))
        Files.copy(Path(fullPath),Path(fullPathNF), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING)

        //---------------------------------------------------------------------------------------------------------------------
        //Date Taken by EXIF
        val exifInterface = ExifInterface(fullPathNF)

        exifInterface.setAttribute("DateTime", date)
        exifInterface.setAttribute("DateTimeOriginal", date)
        exifInterface.setAttribute("DateTimeDigitized", date)
        exifInterface.saveAttributes()

        val fileToChange = File(fullPathNF)
        fileToChange.setLastModified(dateToSet.time)

        Files.delete(Path(fullPath))
        return fullPathNF
    }
}