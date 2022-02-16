
package com.android.samples.photic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/*
MainActivity class
 */
class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Display the activity_main layout
        setContentView(R.layout.activity_main)
    }
}