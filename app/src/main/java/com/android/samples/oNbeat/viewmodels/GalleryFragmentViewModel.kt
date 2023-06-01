/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.samples.oNbeat.viewmodels

import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.samples.oNbeat.data.RaceResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Path
import java.util.Date
import kotlin.io.path.Path

/*
Shared ViewModel for GalleryFragment, that is also accessed by DateDialogFragment and DateTimePickerFragment
 */
class GalleryFragmentViewModel(application: Application) : AndroidViewModel(application) {
    //Variables stored inside the ViewModel and the read-only companions that can be accessed by the Fragments
    private val _results = MutableLiveData<MutableList<RaceResult>?>()
    val results: LiveData<MutableList<RaceResult>?> get() = _results

    private var contentObserver: ContentObserver? = null

    private var _viewHolds = MutableStateFlow<MutableList<Int>>(ArrayList())
    val viewHolds: StateFlow<List<Int>> get() = _viewHolds
    private var _numberImages = MutableStateFlow(0)
    val numberImages: StateFlow<Int> get() = _numberImages
    private var _startTag = MutableStateFlow(false)
    val startTag: StateFlow<Boolean> get() = _startTag
    private var _startDate = MutableStateFlow("")
    val startDate: StateFlow<String> get() = _startDate
    private var _stopDate = MutableStateFlow("")
    val stopDate: StateFlow<String> get() = _stopDate
    private var _startDateTime = MutableStateFlow(Date(0))
    val startDateTime: StateFlow<Date> get() = _startDateTime
    private var _stopDateTime = MutableStateFlow(Date(0))
    val stopDateTime: StateFlow<Date> get() = _stopDateTime
    private var _byImage = MutableStateFlow(false)
    val byImage: StateFlow<Boolean> get() = _byImage
    private var _dateSelect = MutableStateFlow(false)
    val dateSelect: StateFlow<Boolean> get() = _dateSelect

    //----------------------------------------------------------------------------------------------------
    //Interface funs for fragments to set variables


    //fun to define, if start- or stopdate will be selected
    fun setTag(tag: Boolean){
        _startTag.value = tag
    }

    //fun to store a selcted date as start- or stopdate. The Date and the formatted String (DD.mm.YYYY) is stored
    fun setDate(tag: Boolean, datetime: Date, justdate: String){
        if(tag){
            _startDate.value = justdate
            _startDateTime.value = datetime
        }
        else {
            _stopDate.value = justdate
            _stopDateTime.value= datetime
        }
    }

    //fun to store the information, that the next image clicked will not be selected but used to retreive the date from it
    fun setbyImage(tag: Boolean){
        _byImage.value = tag
    }

    //fun to store the information, that the app is in the date-selection routine --> show ic_start/stop_calendar_clicked
    fun setdateSelect(tag:Boolean){
        _dateSelect.value = tag
    }

    fun importResults (importedResults: List<RaceResult>) {
        _results.value = importedResults.toMutableList()
    }

    fun registerRaceNumber (raceNumber: Int, imagePath: String, start: Boolean, time: Long){
        val uri = Uri.parse("file://"+imagePath)
        if (start) {
            val result = RaceResult(raceNumber = raceNumber, startTime = time, startImage = imagePath, contentUriStart = uri)
            var currentList = _results.value
            if (currentList!= null){
                currentList.add(result)
            } else {
                currentList= listOf(result).toMutableList()
            }
            _results.postValue(currentList)
        }
        else {
            val filteredRaceNumber = _results.value!!.indexOfFirst { it.raceNumber == raceNumber}
            if (filteredRaceNumber != -1){
                with(_results.value!![filteredRaceNumber]) {
                    finishTime = time
                    finishImage = imagePath
                    contentUriFinish = uri
                    totalTime = time - startTime
                }
            }
            else {
                val result = RaceResult(raceNumber = raceNumber, finishTime = time, finishImage = imagePath, contentUriFinish = uri)
                val currentList = _results.value
                currentList?.add(result)
                _results.postValue(currentList)
            }
        }
    }
    //unregister the ContentObserver when the ViewModel is destroyed (e.g. the application is closed)
    override fun onCleared() {
        contentObserver?.let {
            getApplication<Application>().contentResolver.unregisterContentObserver(it)
        }
    }
}

//fun to register a ContentObserver
private fun ContentResolver.registerObserver(
    uri: Uri,
    observer: (selfChange: Boolean) -> Unit
): ContentObserver {
    val contentObserver = object : ContentObserver(Handler(Looper.myLooper()!!)) {
        override fun onChange(selfChange: Boolean) {
            observer(selfChange)
        }
    }
    registerContentObserver(uri, true, contentObserver)
    return contentObserver
}