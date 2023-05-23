package com.android.samples.oNbeat.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

class FTPClientViewModel(application: Application) : AndroidViewModel(application) {

    private var _ftpPort = MutableStateFlow(21)
    val ftpPort: StateFlow<Int> get() = _ftpPort
    private var _hostOne = MutableStateFlow("")
    val hostOne: StateFlow<String> get() = _hostOne
    private var _hostTwo = MutableStateFlow("")
    val hostTwo: StateFlow<String> get() = _hostTwo
    private var _userName = MutableStateFlow("esp32")
    val userName: StateFlow<String> get() = _userName
    private var _pW = MutableStateFlow("esp32")
    val pW: StateFlow<String> get() = _pW
    private var _picDownloadOne = MutableLiveData<MutableList<String>>(ArrayList())
    val picDownloadOne: LiveData<MutableList<String>> get() = _picDownloadOne
    private var _picDownloadTwo = MutableLiveData<MutableList<String>>(ArrayList())
    val picDownloadTwo: LiveData<MutableList<String>> get() = _picDownloadTwo


// Set the IP address of the ESP32
    fun setIP(ipAddress: String?){
        if (ipAddress != null) {
            if (_hostOne.value == "") {
                _hostOne.value = ipAddress
            } else {
                _hostTwo.value = ipAddress
            }
        }
    }
    fun addPic2Download (ipAddress: String, picName: String){
        if (ipAddress == hostOne.value){
            _picDownloadOne.value?.add(picName)
        } else {
            _picDownloadTwo.value?.add(picName)
        }
    }

    fun downloadCompleted(image: String, ESP32: Boolean){
        if (ESP32){
            _picDownloadOne.value?.remove(image)
        } else {
            _picDownloadTwo.value?.remove(image)
        }
    }
}