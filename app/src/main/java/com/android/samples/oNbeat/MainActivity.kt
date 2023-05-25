
package com.android.samples.oNbeat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat

/*
MainActivity class
 */
class MainActivity : AppCompatActivity(){
    private val reqPermissionsStorage = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.MANAGE_EXTERNAL_STORAGE
    )
    private val reqPermissionsNetwork = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE
    )
    private val reqPermissionsHotspot32 = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val reqPermissionsHotspot33 = arrayOf(
        Manifest.permission.NEARBY_WIFI_DEVICES
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!haveNetworkPermission()) {
            permReqLauncher.launch(reqPermissionsNetwork)
        }
        if (!haveStoragePermission()) {
            permReqLauncher.launch(reqPermissionsStorage)
        }
        startServer()
    }
    //----------------------------------------------------------------------------------------------------
    //Permission handling

    //Simple permission check
    private fun haveStoragePermission():Boolean {
        return ((ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
        //The result of externalStorageManager is not checked immediately. It is checked every time an image is about to be changed.
    }
    private fun haveNetworkPermission():Boolean {
        return ((ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED))
        //The result of externalStorageManager is not checked immediately. It is checked every time an image is about to be changed.
    }

    //Permission Request Launcher
    private val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val permissionReqRes = permissions.entries.all {
                it.value
            }
            //Check if the needed permissions are granted
            if (!permissionReqRes) {
                //If the permissions are not granted, show a Toast.
            }
        }
    private fun startServer() {
        val sSA = Intent(this, ServerSocketActivity::class.java)
        startActivity(sSA)
    }
}