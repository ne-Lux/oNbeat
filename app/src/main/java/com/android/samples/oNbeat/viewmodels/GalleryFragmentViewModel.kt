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
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.samples.oNbeat.data.RaceResult
import kotlinx.coroutines.flow.*

// ---------------------------------------------------------------------------------------------
// Shared ViewModel for GalleryFragment that is also accessed by DialogFragments
// ---------------------------------------------------------------------------------------------
class GalleryFragmentViewModel(application: Application) : AndroidViewModel(application) {
    
    // ---------------------------------------------------------------------------------------------
    // Variables stored inside the ViewModel and the read-only companions that can be accessed by the Fragments
    // ---------------------------------------------------------------------------------------------
    private val _results = MutableLiveData<MutableList<RaceResult>?>()
    val results: LiveData<MutableList<RaceResult>?> get() = _results
    private var _startTag = MutableStateFlow(false)
    val startTag: StateFlow<Boolean> get() = _startTag
    private var _trackMode = MutableStateFlow(true)
    val trackMode: StateFlow<Boolean> get() = _trackMode
    private var _chosenFile = MutableLiveData<String>()
    val chosenFile: LiveData<String> get() = _chosenFile

    // ---------------------------------------------------------------------------------------------
    // Simple setter functions.
    // ---------------------------------------------------------------------------------------------
    fun chooseFile(chosenFile: String){
        _chosenFile.value= chosenFile
    }
    fun setMode(){
        _trackMode.value = !_trackMode.value
    }

    fun importResults (importedResults: List<RaceResult>) {
        _results.value = importedResults.toMutableList()
    }

    fun clearResults(){
        _results.value = listOf<RaceResult>().toMutableList()
    }

    // ---------------------------------------------------------------------------------------------
    // Add a result. Either register a new race number or add it to an existing record.
    // ---------------------------------------------------------------------------------------------
    
    fun registerRaceNumber (raceNumber: Int, imagePath: String, start: Boolean, time: Long){
        val uri = Uri.parse("file://$imagePath")
        if (start) {
            var filteredRaceNumber: Int = -1
            if (_results.value != null){
                filteredRaceNumber = _results.value!!.indexOfFirst { it.raceNumber == raceNumber}
            }
            if (!trackMode.value && filteredRaceNumber != -1){
                val currentList = _results.value
                with(currentList!![filteredRaceNumber]) {
                    finishTime = time
                    finishImage = imagePath
                    contentUriFinish = uri
                    totalTime = time - startTime
                }
                _results.postValue(currentList)
            } else {
                val result = RaceResult(raceNumber = raceNumber, startTime = time, startImage = imagePath, contentUriStart = uri)
                var currentList = _results.value
                if (currentList!= null){
                    currentList.add(result)
                } else {
                    currentList= listOf(result).toMutableList()
                }
                _results.postValue(currentList)
            }
        }
        else {
            val filteredRaceNumber = _results.value!!.indexOfFirst { it.raceNumber == raceNumber}
            if (filteredRaceNumber != -1){
                with(_results.value!![filteredRaceNumber]) {
                    //check if this is functional
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
    
    // ---------------------------------------------------------------------------------------------
    // Correct a falsely detected result.
    // ---------------------------------------------------------------------------------------------
    fun correctRaceNumber (wrongRaceNumber: Int, rightRaceNumber: Int, start: Boolean){
        val wrongTime: Long
        val wrongImage: String
        val wrongContentUri: Uri
        
        val currentList = _results.value
        val filteredNewRaceNumber = currentList!!.indexOfFirst { it.raceNumber == rightRaceNumber}
        val filteredRaceNumber = currentList.indexOfFirst { it.raceNumber == wrongRaceNumber}
        if (filteredRaceNumber != -1){
            /// If start has to be corrected
            ///     If there is just the start value and the new start number is not registered yet
            ///     Else if there is also a finish value and the new start number is not registered yet
            ///     Else if the new start number is already registered
            /// If finish has to be corrected
            ///     If there is just the finish value and the new start number is not registered yet
            ///     Else if there is also a start value and the new start number is not registered yet
            ///     Else if the new start number is already registered

            if(start){
                wrongTime = _results.value!![filteredRaceNumber].startTime
                wrongImage = _results.value!![filteredRaceNumber].startImage
                wrongContentUri = _results.value!![filteredRaceNumber].contentUriStart

                // If there is just the start time and there is no record for the correct race number yet
                if((currentList[filteredRaceNumber].finishTime == 0L) && (filteredNewRaceNumber == -1)) {
                    // Just change the start number
                    currentList[filteredRaceNumber].raceNumber = rightRaceNumber

                // If there are two times but there is no record for the correct race number yet
                } else if ((currentList[filteredRaceNumber].finishTime != 0L) && (filteredNewRaceNumber == -1)) {
                    // Register the new race number
                    val result = RaceResult(raceNumber = rightRaceNumber, startTime = wrongTime, startImage = wrongImage, contentUriStart = wrongContentUri)
                    currentList.add(result)
                    if (!trackMode.value) {
                        with(currentList[filteredRaceNumber]) {
                            startTime = finishTime
                            startImage = finishImage
                            contentUriStart = contentUriFinish
                            finishTime = 0
                            finishImage = ""
                            contentUriFinish = Uri.parse("")
                            totalTime = 0
                        }
                    } else {
                        with(currentList[filteredRaceNumber]) {
                            startTime = 0
                            startImage = ""
                            contentUriStart = Uri.parse("")
                            totalTime = 0
                        }
                    }

                // LOOP MODE only: If there is already a record for the correct race number
                } else if (filteredNewRaceNumber != -1 && !trackMode.value) {

                    // If the existing record for the correct race number is not complete yet or has the right order
                    if (currentList[filteredNewRaceNumber].startTime < wrongTime){
                        with(currentList[filteredNewRaceNumber]) {
                            finishTime = wrongTime
                            finishImage = wrongImage
                            contentUriFinish = wrongContentUri
                            totalTime = wrongTime - startTime
                        }
                        // Drop the old record, if it has no additional values
                        if (currentList[filteredRaceNumber].finishTime == 0L) {
                            currentList.removeAt(filteredRaceNumber)
                        } else {
                            with(currentList[filteredRaceNumber]) {
                                startTime = finishTime
                                startImage = finishImage
                                contentUriStart = contentUriFinish
                                finishTime = 0
                                finishImage = ""
                                contentUriFinish = Uri.parse("")
                                totalTime = 0
                            }
                        }

                    // Existing record accidentally has finish mapped to start
                    } else {
                        with(currentList[filteredNewRaceNumber]) {
                            finishTime = startTime
                            finishImage = startImage
                            contentUriFinish = contentUriStart
                            startTime = wrongTime
                            startImage = wrongImage
                            contentUriStart = wrongContentUri
                            totalTime = finishTime - startTime
                        }
                        currentList.removeAt(filteredRaceNumber)
                    }
                // TRACK MODE only: Base case: there is already a record for the correct race number
                } else {
                    if (currentList[filteredNewRaceNumber].startTime > wrongTime) {
                        with(currentList[filteredNewRaceNumber]) {
                            startTime = wrongTime
                            startImage = wrongImage
                            contentUriStart = wrongContentUri
                            totalTime = finishTime - startTime
                        }
                    }
                    if (currentList[filteredRaceNumber].finishTime != 0L) {
                        with(currentList[filteredRaceNumber]) {
                            startTime = 0
                            startImage = ""
                            contentUriStart = Uri.parse("")
                            totalTime = 0
                        }
                    } else {
                        currentList.removeAt(filteredRaceNumber)
                    }
                }
            // FINISH
            } else {
                wrongTime = _results.value!![filteredRaceNumber].finishTime
                wrongImage = _results.value!![filteredRaceNumber].finishImage
                wrongContentUri = _results.value!![filteredRaceNumber].contentUriFinish

                // If there is just the start time and there is no record for the correct race number yet
                if((currentList[filteredRaceNumber].startTime == 0L) && (filteredNewRaceNumber == -1)) {
                    // Just change the start number
                    currentList[filteredRaceNumber].raceNumber = rightRaceNumber

                // If there are two times but there is no record for the correct race number yet
                } else if ((currentList[filteredRaceNumber].startTime != 0L) && (filteredNewRaceNumber == -1)) {
                    // Register the new race number
                    if (!trackMode.value) {
                        val result = RaceResult(raceNumber = rightRaceNumber, startTime = wrongTime, startImage = wrongImage, contentUriStart = wrongContentUri)
                        currentList.add(result)
                    } else {
                        val result = RaceResult(raceNumber = rightRaceNumber, finishTime = wrongTime, finishImage = wrongImage, contentUriFinish = wrongContentUri)
                        currentList.add(result)
                    }
                    with(currentList[filteredRaceNumber]) {
                        finishTime = 0
                        finishImage = ""
                        contentUriFinish = Uri.parse("")
                        totalTime = 0
                    }

                // LOOP MODE only: If there is already a record for the correct race number
                } else if (filteredNewRaceNumber != -1 && !trackMode.value) {

                    // If the existing record for the correct race number is not complete yet or has the right order
                    if ((currentList[filteredNewRaceNumber].finishTime == 0L) || (currentList[filteredNewRaceNumber].startTime < wrongTime)){
                        with(currentList[filteredNewRaceNumber]) {
                            finishTime = wrongTime
                            finishImage = wrongImage
                            contentUriFinish = wrongContentUri
                            totalTime = wrongTime - startTime
                        }
                    // Existing record accidentally has finish mapped to start
                    } else {
                        with(currentList[filteredNewRaceNumber]) {
                            finishTime = startTime
                            finishImage = startImage
                            contentUriFinish = contentUriStart
                            startTime = wrongTime
                            startImage = wrongImage
                            contentUriStart = wrongContentUri
                            totalTime = finishTime - startTime
                        }
                    }
                    with(currentList[filteredRaceNumber]) {
                        finishTime = 0
                        finishImage = ""
                        contentUriFinish = Uri.parse("")
                        totalTime = 0
                    }
                // TRACK MODE only: Base case: there is already a record for the correct race number
                } else {
                    // If there are two finish times for this race number, take the earlier one.
                    if (currentList[filteredNewRaceNumber].finishTime > wrongTime) {
                        with(currentList[filteredNewRaceNumber]) {
                            finishTime = wrongTime
                            finishImage = wrongImage
                            contentUriFinish = wrongContentUri
                            totalTime = finishTime - startTime
                        }
                    }
                    // If the old race number has a start time, set finish to zero. Else, drop the record.
                    if (currentList[filteredRaceNumber].startTime != 0L) {
                        with(currentList[filteredRaceNumber]) {
                            finishTime = 0
                            finishImage = ""
                            contentUriFinish = Uri.parse("")
                            totalTime = 0
                        }
                    } else {
                        currentList.removeAt(filteredRaceNumber)
                    }
                }
            }
        }
        _results.postValue(currentList)
    }
}
