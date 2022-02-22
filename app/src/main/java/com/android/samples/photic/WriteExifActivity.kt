package com.android.samples.photic

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.io.path.Path

/*
Class to change image attributes
 */
class WriteExifActivity : Activity() {
    //Initiate the variables
    private val storage = "/storage/emulated/0/"
    private var format = ""
    private var fullPath: String = ""
    private var fullPathNF: String = ""

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class, ParseException::class)

    //Fun to change image attributes
    fun apply(path: String, filename: String, dateToSet: Date): String {
        //check if the file extension is .jpg or .jpeg
        if(Files.exists(Path("$storage$path$filename.jpg"))) format = ".jpg"
        else if (Files.exists(Path("$storage$path$filename.jpeg"))) format = ".jpeg"

        //Create DateTimeFormatter and format the target date
        val dateTimeFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss")
        val date = dateTimeFormat.format(dateToSet)

        //Construct the full path of the original image
        fullPath = storage + path + filename + format

        try {
            //If the image had been changed before with PhotiC, just copying the file to DCIM/Changed/ won't create a new image
            //As a solution, add a _1 to the filename
            if (path == "DCIM/Changed/") {
                //If the filename ends with _1
                if (filename.takeLast(2) == "_1")
                //Remove the _1
                    fullPathNF =
                        storage + "DCIM/Changed/" + filename.take(filename.length - 2) + format
                else
                //Add _1
                    fullPathNF = storage + "DCIM/Changed/" + filename + "_1" + format
            }
            //If the image had not been changed before with PhotiC, just take the DCIM/Changed + original filename as new path
            else fullPathNF = storage + "DCIM/Changed/" + filename + "_1" + format

            //---------------------------------------------------------------------------------------------------------------------
            //Check if the target directory exists and create it, if it does not exist
            if (!Files.exists(Path(storage + "DCIM/Changed"))) Files.createDirectory(Path(storage + "DCIM/Changed"))
            //Copy existing File
            Files.copy(
                Path(fullPath),
                Path(fullPathNF),
                StandardCopyOption.COPY_ATTRIBUTES,
                StandardCopyOption.REPLACE_EXISTING
            )

            //---------------------------------------------------------------------------------------------------------------------
            //Change all EXIF attributes with the EXIF-Interface
            val exifInterface = ExifInterface(fullPathNF)
            exifInterface.setAttribute("DateTime", date)
            exifInterface.setAttribute("DateTimeOriginal", date)
            exifInterface.setAttribute("DateTimeDigitized", date)
            exifInterface.saveAttributes()

            //Change the LastModifiedDate of the file
            val fileToChange = File(fullPathNF)
            fileToChange.setLastModified(dateToSet.time)

            //Delete the original file and return the new path to be scanned by the MediaScanner
            Files.delete(Path(fullPath))
        }
        catch (e: Exception){
            Toast.makeText(applicationContext, "File not found!", Toast.LENGTH_LONG).show()
        }
        return fullPathNF
    }
}