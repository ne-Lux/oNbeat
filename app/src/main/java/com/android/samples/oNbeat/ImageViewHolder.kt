package com.android.samples.oNbeat

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.android.samples.oNbeat.data.RaceResult

/*
ImageViewHolder class
 */
class ImageViewHolder(view: View, onClick: (RaceResult, Int) -> Unit) :
    RecyclerView.ViewHolder(view) {
    val rootView = view
    val imageView: ImageView = view.findViewById(R.id.image)

    init {
        //Set OnClickListener
        imageView.setOnClickListener {
            val image = rootView.tag as? RaceResult ?: return@setOnClickListener
            val posi = adapterPosition
            //Return the image and the adapter position
            onClick(image, posi)
        }
    }
}