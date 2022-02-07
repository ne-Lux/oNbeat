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
    val storage = "/storage/emulated/0/"
    val format = ".jpg"
    var fullPath: String = ""
    var fullPathNF: String = ""
    // TODO: 03.02.2022 Umgang jpg/jpeg

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class, ParseException::class)
    fun apply(path: String, filename: String, dateToSet: Date): String {

        val dateTimeFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss")
        val date = dateTimeFormat.format(dateToSet)
        fullPath = storage + path + filename + format
        if(path=="DCIM/Changed/") {
            if (filename.takeLast(2) == "_1")
                fullPathNF = storage + "DCIM/Changed/" + filename.take(filename.length - 2) + format
            else
                fullPathNF = storage + "DCIM/Changed/" + filename + "_1" + format
        }

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
        fileToChange.setLastModified(dateTimeFormat.parse(date).time)

        Files.delete(Path(fullPath))
        return fullPathNF
    }
}