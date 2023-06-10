package com.android.samples.oNbeat.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// -----------------------------------------------------------------------------------------
// ViewModel to share connection-related data between ServerSocketActivity and GalleryFragment
// -----------------------------------------------------------------------------------------
class FTPClientViewModel : ViewModel() {
    // ---------------------------------------------------------------------------------------------
    // Variables stored inside the ViewModel and the read-only companions that can be accessed by the Fragments
    // ---------------------------------------------------------------------------------------------
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
    private var _hotSpot = MutableLiveData(false)
    val hotspot: LiveData<Boolean> get() = _hotSpot
    private var _connectedDevices = MutableLiveData<MutableList<String>?>(ArrayList())
    val connectedDevices: LiveData<MutableList<String>?> get() = _connectedDevices
    private var _picDownloadOne = MutableLiveData<MutableList<String>?>(ArrayList())
    val picDownloadOne: LiveData<MutableList<String>?> get() = _picDownloadOne
    private var _picDownloadTwo = MutableLiveData<MutableList<String>?>(ArrayList())
    val picDownloadTwo: LiveData<MutableList<String>?> get() = _picDownloadTwo

    // ---------------------------------------------------------------------------------------------
    // Simple setter functions.
    // ---------------------------------------------------------------------------------------------
    fun setHotSpot(status:Boolean){
        _hotSpot.value = status
    }
    fun addDevice(ipAddress: String){
        var currentList = _connectedDevices.value
        if (currentList != null) {
            if (!currentList.contains(ipAddress)) {
                currentList.add(ipAddress)
                _connectedDevices.postValue(currentList)
            }
        } else {
            currentList = listOf(ipAddress).toMutableList()
            _connectedDevices.postValue(currentList)
        }

    }
    fun removeDevice(ipAddress: String){
        val currentList = _connectedDevices.value
        if (currentList != null) {
            if (currentList.contains(ipAddress)){
                currentList.remove(ipAddress)
            }
        }
        _connectedDevices.postValue(currentList)
    }

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
        val currentList = _picDownloadOne.value
        currentList?.add(picName)
        if (ipAddress == hostOne.value){
            _picDownloadOne.postValue(currentList)
        } else {
            _picDownloadTwo.postValue(currentList)
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