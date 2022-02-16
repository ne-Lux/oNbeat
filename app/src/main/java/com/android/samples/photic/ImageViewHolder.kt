package com.android.samples.photic

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.android.samples.photic.data.MediaStoreImage

/*
ImageViewHolder class
 */
class ImageViewHolder(view: View, onClick: (MediaStoreImage, Int) -> Unit) :
    RecyclerView.ViewHolder(view) {
    val rootView = view
    val imageView: ImageView = view.findViewById(R.id.image)

    init {
        //Set OnClickListener
        imageView.setOnClickListener {
            val image = rootView.tag as? MediaStoreImage ?: return@setOnClickListener
            val posi = adapterPosition
            //Return the image and the adapter position
            onClick(image, posi)
        }
    }
}