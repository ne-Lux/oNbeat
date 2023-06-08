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

package com.android.samples.oNbeat.data

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil
import java.util.*

/*
Data Class that holds an Image of the MediaStore provided by the ContentResolver
 */
data class RaceResult(
    var raceNumber: Int,
    var startTime: Long = 0,
    var startImage: String = "",
    var contentUriStart: Uri = Uri.parse(""),
    var finishTime: Long = 0,
    var finishImage: String = "",
    var contentUriFinish: Uri = Uri.parse(""),
    var totalTime: Long = 0,
) {
    //Calculate Updates for RecyclerView Adapter
    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<RaceResult>() {
            override fun areItemsTheSame(oldItem: RaceResult, newItem: RaceResult) =
                (oldItem.raceNumber == newItem.raceNumber) && (oldItem.startTime == newItem.startTime) && (oldItem.finishTime == newItem.finishTime)

            override fun areContentsTheSame(oldItem: RaceResult, newItem: RaceResult) =
                (oldItem.raceNumber == newItem.raceNumber) && (oldItem.startTime == newItem.startTime) && (oldItem.finishTime == newItem.finishTime)
        }
    }
}