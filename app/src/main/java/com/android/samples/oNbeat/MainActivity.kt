
package com.android.samples.oNbeat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext

/*
MainActivity class
 */
class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startServer()
    }
    private fun startServer() {
        val sSA = Intent(this, ServerSocketActivity::class.java)
        startActivity(sSA)
    }
}