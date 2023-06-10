package com.android.samples.oNbeat

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.samples.oNbeat.data.RaceResult

// -----------------------------------------------------------------------------------------
// ViewHolder for a race result
// -----------------------------------------------------------------------------------------
class ResultViewHolder(view: View, onClick: (RaceResult) -> Unit) :
    RecyclerView.ViewHolder(view) {
    val rootView = view
    val raceNumber: TextView = view.findViewById(R.id.label_race_number)
    val totalTime: TextView = view.findViewById(R.id.label_total_time)
    val startTime: TextView = view.findViewById(R.id.label_start_time)
    val finishTime: TextView = view.findViewById(R.id.label_finish_time)
    val startImage: ImageView = view.findViewById(R.id.image_start)
    val finishImage: ImageView = view.findViewById(R.id.image_finish)

    init {
        startImage.setOnClickListener {
            val image = rootView.tag as? RaceResult ?: return@setOnClickListener
            onClick(image)
        }
        finishImage.setOnClickListener {
            val image = rootView.tag as? RaceResult ?: return@setOnClickListener
            onClick(image)
        }
    }
}