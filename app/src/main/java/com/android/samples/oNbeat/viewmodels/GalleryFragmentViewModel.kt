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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.concurrent.TimeUnit

/*
Shared ViewModel for GalleryFragment, that is also accessed by DateDialogFragment and DateTimePickerFragment
 */
class GalleryFragmentViewModel(application: Application) : AndroidViewModel(application) {
    //Variables stored inside the ViewModel and the read-only companions that can be accessed by the Fragments
    private val _images = MutableLiveData<List<RaceResult>>()
    val images: LiveData<List<RaceResult>> get() = _images
    private var contentObserver: ContentObserver? = null
    private var _selectedImages = MutableStateFlow<MutableList<RaceResult>>(ArrayList())
    val selectedImages: StateFlow<List<RaceResult>> get() = _selectedImages
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

    //fun to store/remove information about a selected/deselected image inside the ViewModel
    fun selectImage(image: RaceResult, view: Int) {
        //image is deselected
        if(_selectedImages.value.contains(image)){
            _selectedImages.value.remove(image)
            _viewHolds.value.remove(Integer.valueOf(view))
        }
        //image is selected
        else{
            _selectedImages.value.add(image)
            _viewHolds.value.add(view)
        }
        //update the number of selected images
        _numberImages.value = _selectedImages.value.size
    }

    //fun to deselect all images (by clear button)
    fun deSelectImages(){
        _selectedImages.value.clear()
        _viewHolds.value.clear()
    }

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

    //----------------------------------------------------------------------------------------------------
    //Load images from storage
    fun loadImages() {
        //Loading images is launched as Coroutine, so that the main thread is not blocked
        viewModelScope.launch {
            val imageList = queryImages()
            //The images provided by the ContentResolver are stored inside the ViewModel
            _images.value = (imageList)

            //Register a ContentObserver,  to Update the imagelist when images are changed
            if (contentObserver == null) {
                contentObserver = getApplication<Application>().contentResolver.registerObserver(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ) {
                    loadImages()
                }
            }
        }
    }

    private suspend fun queryImages(): List<RaceResult> {
        val images = mutableListOf<RaceResult>()

        TODO("Adapt to oNbeat")
        // ImageView.setImageURI(Uri.fromFile(new File("/sdcard/cats.jpg")));
        // ImageView.setImageURI(Uri.parse(new File("/sdcard/cats.jpg").toString()));
        withContext(Dispatchers.IO) {

            //Select all attributes in projection from images
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.RELATIVE_PATH,
                MediaStore.Images.Media.MIME_TYPE
            )
            //Where the MIME type is image/jpeg
            val selection = "${MediaStore.Images.Media.MIME_TYPE} = ?"
            val selectionArgs = arrayOf("image/jpeg")
            //Order by Date_Modified, descending
            val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED}  DESC"

            //Execute the SQL query and return a cursor
            getApplication<Application>().contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->

                //Select the columns with the attributes of interest from the cursor
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateAddedColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val dateTakenColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val dateModifiedColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
                val fileNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE)
                val rPathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)

                //For each element inside the cursor
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val dateAdded =
                        Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateAddedColumn)))
                    val dateTaken =
                        Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateTakenColumn)))
                    val dateModified =
                        Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateModifiedColumn)))
                    val fileName = cursor.getString(fileNameColumn)
                    val rPath = cursor.getString(rPathColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id)

                    //Create a MediaStoreImage with the attributes from the cursor and add it to the list, that will be stored inside the ViewModel
                    val image = RaceResult(id, dateAdded, dateTaken, dateModified, fileName, rPath, contentUri)
                    images += image
                }
            }
        }
        return images
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