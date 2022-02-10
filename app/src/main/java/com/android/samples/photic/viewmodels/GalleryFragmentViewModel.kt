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

package com.android.samples.photic.viewmodels

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
import com.android.samples.photic.GalleryFragment
import com.android.samples.photic.data.MediaStoreImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.concurrent.TimeUnit

class GalleryFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val _images = MutableLiveData<List<MediaStoreImage>>()
    val images: LiveData<List<MediaStoreImage>> get() = _images

/*
    private val _images = MutableStateFlow<List<MediaStoreImage>>(ArrayList())
    val images: StateFlow<List<MediaStoreImage>> get() = _images
*/
    private var contentObserver: ContentObserver? = null

    private var _selectedImages = MutableStateFlow<MutableList<MediaStoreImage>>(ArrayList())
    val selectedImages: StateFlow<List<MediaStoreImage>> get() = _selectedImages
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
    private var _byCalendar = MutableStateFlow(false)
    val byCalendar: StateFlow<Boolean> get() = _byCalendar
    private var _dateSelect = MutableStateFlow(false)
    val dateSelect: StateFlow<Boolean> get() = _dateSelect
    init {

    }

    //----------------------------------------------------------------------------------------------------
    //Interface funs for fragments to set variables

    fun selectImage(image: MediaStoreImage, view: Int) {
        if(_selectedImages.value.contains(image)){
            _selectedImages.value.remove(image)
            _viewHolds.value.remove(Integer.valueOf(view))
        }
        else{
            _selectedImages.value.add(image)
            _viewHolds.value.add(view)
        }
        _numberImages.value = _selectedImages.value.size
    }

    fun deSelectImages(){
        _selectedImages.value.clear()
        _viewHolds.value.clear()
    }

    fun setTag(tag: Boolean){
        _startTag.value = tag
    }

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

    fun setbyImage(tag: Boolean){
        _byImage.value = tag
    }

    fun setbyCalendar(tag: Boolean){
        _byCalendar.value = tag
    }

    fun setdateSelect(tag:Boolean){
        _dateSelect.value = tag
    }

    //----------------------------------------------------------------------------------------------------
    //Load images from storage
    fun loadImages() {
        viewModelScope.launch {
            val imageList = queryImages()
            _images.value = (imageList)

            if (contentObserver == null) {
                contentObserver = getApplication<Application>().contentResolver.registerObserver(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ) {
                    loadImages()
                }
            }
        }
    }

    private suspend fun queryImages(): List<MediaStoreImage> {
        val images = mutableListOf<MediaStoreImage>()

        withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.RELATIVE_PATH
            )
            val selection = "${MediaStore.Images.Media._ID} <> ?"
            val selectionArgs = arrayOf("")
            val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED}  DESC"

            getApplication<Application>().contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->

                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateAddedColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val dateTakenColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val dateModifiedColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
                val fnumberColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE)
                val rPathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val dateAdded =
                        Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateAddedColumn)))
                    val dateTaken =
                        Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateTakenColumn)))
                    val dateModified =
                        Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateModifiedColumn)))
                    val fNumber = cursor.getString(fnumberColumn)
                    val rPath = cursor.getString(rPathColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id)

                    val image = MediaStoreImage(id, dateAdded, dateTaken, dateModified, fNumber, rPath, contentUri)
                    images += image
                }
            }
        }
        return images
    }

    override fun onCleared() {
        contentObserver?.let {
            getApplication<Application>().contentResolver.unregisterContentObserver(it)
        }
    }
}

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