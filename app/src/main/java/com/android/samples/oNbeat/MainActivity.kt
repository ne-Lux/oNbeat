
package com.android.samples.oNbeat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat


/*
MainActivity class
 */
class MainActivity : AppCompatActivity(){
    private val reqPermissionsOnbeat = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.MANAGE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!haveNecessaryPermission()) {
            val u = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, u)
            startActivity(intent)
            permReqLauncher.launch(reqPermissionsOnbeat)
        }
        startServer()
    }
    //----------------------------------------------------------------------------------------------------
    //Permission handling

    //Simple permission check
    private fun haveNecessaryPermission():Boolean {
        return ((ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED))
        //The result of externalStorageManager is not checked immediately. It is checked every time an image is about to be changed.
    }

    //Permission Request Launcher
    private val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val permissionReqRes = permissions.entries.all {
                it.value
            }
            //Check if the needed permissions are granted
            if (permissionReqRes) {
                //Check the External Storage Manager Permission
                if (!Environment.isExternalStorageManager()) {
                    //Show the External Storage Manager Request, if there is no permission
                    externalStorageManager()
                }
            }
        }

    private fun externalStorageManager(){
        val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
        startActivity(
            Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                uri
            )
        )
    }
    private fun startServer() {
        val sSA = Intent(this, ServerSocketActivity::class.java)
        startActivity(sSA)
    }
}