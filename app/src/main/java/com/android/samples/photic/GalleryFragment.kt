package com.android.samples.photic

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.android.samples.photic.data.MediaStoreImage
import com.android.samples.photic.databinding.GalleryFragmentBinding
import com.android.samples.photic.viewmodels.GalleryFragmentViewModel
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class GalleryFragment: Fragment(){

    private val viewModel: GalleryFragmentViewModel by activityViewModels()
    private lateinit var binding: GalleryFragmentBinding
    private val buttonClick = AlphaAnimation(0f, 1f)
    private val datedialogFragment = DateDialogFragment()
    private var reqPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = GalleryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val galleryAdapter = GalleryAdapter { image, posi ->
            onImageClick(image, posi)
        }

        binding.gallery.also { view ->
            view.layoutManager = GridLayoutManager(requireContext(), 3)
            view.adapter = galleryAdapter
        }

        if (viewModel.numberImages.value != 0) {
            binding.imageNumber.visibility = View.VISIBLE
            binding.clearSelection.visibility = View.VISIBLE
            binding.imageNumber.text = viewModel.numberImages.value.toString()
        }
        binding.startDate.text = viewModel.startDate.value
        binding.stopDate.text = viewModel.stopDate.value
        initCalendarImage()

        setFragmentResultListener("requestKey") { _, bundle ->
            val justdate = bundle.getString("justDate")
            val finaldate = bundle.getString("finalDate")
            val dateTimeFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss")
            val datetime = dateTimeFormat.parse(finaldate)

            viewModel.setdateSelect(tag = false)
            initCalendarImage()
            if (finaldate != null && justdate != null) swapDates(datetime)
            if (viewModel.startTag.value) binding.startDate.text = justdate else binding.stopDate.text= justdate

            if (finaldate != null && justdate != null) viewModel.setDate(viewModel.startTag.value, datetime, justdate)
            checkConstraints()
        }
        setFragmentResultListener("destroyedDPD") {_, bundle ->
            initCalendarImage()
        }
        setFragmentResultListener("destroyedDD") {_, bundle ->
            initCalendarImage()
        }

        binding.clearSelection.setOnClickListener { onClearClick() }
        binding.startLayout.setOnClickListener { onStartClick() }
        binding.stopLayout.setOnClickListener { onStopClick() }

        if(viewModel.startDate.value!="" && viewModel.stopDate.value!= "" && viewModel.selectedImages.value.count() > 0){
            binding.fab.show()
        }
        else{
            binding.fab.hide()
        }

        binding.fab.setOnClickListener { applyChanges() }


        viewModel.images.observe(viewLifecycleOwner) { images ->
            galleryAdapter.submitList(images)
        }


        if (!haveStoragePermission()) {
            permReqLauncher.launch(reqPermissions)
        } else if (!Environment.isExternalStorageManager()) {
            externalStorageManager()
            showImages()
        }
        else {
            showImages()
        }
    }

    //----------------------------------------------------------------------------------------------------
    //Clickhandler

    private fun onImageClick(image: MediaStoreImage, posi: Int) {
        if(viewModel.byImage.value){
            val justdate = SimpleDateFormat("dd.MM.yy").format(image.dateModified)
            val datetime = image.dateModified

            viewModel.setdateSelect(tag = false)
            initCalendarImage()
            swapDates(datetime)
            if(viewModel.startTag.value) binding.startDate.text=justdate else binding.stopDate.text=justdate

            viewModel.setDate(viewModel.startTag.value, datetime, justdate)
            val recyclerview = binding.gallery
            val holder = recyclerview.findViewHolderForAdapterPosition(posi)
            val imageview = holder!!.itemView.findViewById<ImageView>(R.id.image)
            viewModel.setbyImage(false)

            buttonClick.duration=1000
            buttonClick.fillAfter=true
            buttonClick.startOffset=0
            imageview.startAnimation(buttonClick)
        }
        else{
            viewModel.selectImage(image, posi)
            handleSelection(select = true, posi)
            displayImagenum()
            Log.i("Date_Taken", image.dateTaken.toString())
        }
        checkConstraints()
    }

    private fun onClearClick() {
        handleSelection(false)
        viewModel.deSelectImages()
        displayImagenum()
        checkConstraints()
    }

    private fun onStartClick() {
        viewModel.setTag(tag = true)
        viewModel.setdateSelect(tag = true)
        datedialogFragment.show(parentFragmentManager,"DateDialog_tag")
        binding.StartCalendar.setImageResource(R.drawable.ic_start_calendar_clicked)
    }

    private fun onStopClick() {
        viewModel.setTag(tag = false)
        viewModel.setdateSelect(tag = true)
        datedialogFragment.show(parentFragmentManager,"DateDialog_tag")
        binding.StopCalendar.setImageResource(R.drawable.ic_stop_calendar_clicked)
    }

    //----------------------------------------------------------------------------------------------------
    //Reusable funs
    private fun displayImagenum() {
        val imageNumber= binding.imageNumber
        val intImagesSelected = viewModel.selectedImages.value.size
        val clearSelection = binding.clearSelection


        imageNumber.text = intImagesSelected.toString()
        if (intImagesSelected > 0) {
            imageNumber.visibility = View.VISIBLE
            clearSelection.visibility = View.VISIBLE
        }
        else {
            imageNumber.visibility = View.INVISIBLE
            clearSelection.visibility = View.INVISIBLE
        }
    }

    private fun handleSelection(select: Boolean, position: Int = -1){
        val recyclerview = binding.gallery
        val viewIterator: ListIterator<Int> = viewModel.viewHolds.value.listIterator()

        if (position != -1){
            val holder = recyclerview.findViewHolderForAdapterPosition(position)
            val imageview = holder!!.itemView.findViewById<ImageView>(R.id.image)

            if (viewModel.viewHolds.value.contains(position)){
                imageview.isSelected = true
                imageview.setColorFilter(Color.GRAY, PorterDuff.Mode.SCREEN)
                imageview.setBackgroundResource(R.drawable.rounded_bg)
            }
            else {
                imageview.clearColorFilter()
                imageview.isSelected = false
                imageview.setBackgroundResource(R.color.colorPrimary)
            }
        }
        else {
            while (viewIterator.hasNext()) {
                try{
                    val holder = recyclerview.findViewHolderForAdapterPosition(viewIterator.next())
                    val imageview = holder!!.itemView.findViewById<ImageView>(R.id.image)
                    if (select) {
                        imageview.isSelected = true
                        imageview.setColorFilter(Color.GRAY, PorterDuff.Mode.SCREEN)
                        imageview.setBackgroundResource(R.drawable.rounded_bg)
                    } else {
                        imageview.clearColorFilter()
                        imageview.isSelected = false
                        imageview.setBackgroundResource(R.color.colorPrimary)
                    }
                }
                catch (e: Exception) {

                }

            }
        }
    }

    private fun checkConstraints(){
        if(viewModel.startDate.value!= "" && viewModel.stopDate.value!= "" && viewModel.selectedImages.value.count() > 0)
            binding.fab.show()
        else
            binding.fab.hide()
    }

    private fun applyChanges(){
        val imageIterator: ListIterator<MediaStoreImage> = viewModel.selectedImages.value.listIterator()
        var imageToChange: MediaStoreImage
        val writeExif = WriteExifActivity()
        var fullPathNF: String
        val timeSpan: Long
        var dateToSet: Date
        val startMillis: Long = viewModel.startDateTime.value.time
        val stopMillis: Long = viewModel.stopDateTime.value.time

        if (Environment.isExternalStorageManager()){
            timeSpan = (stopMillis - startMillis)/(viewModel.numberImages.value+1)

            while (imageIterator.hasNext()) {
                dateToSet = Date(startMillis+timeSpan*(imageIterator.nextIndex()+1))
                imageToChange = imageIterator.next()
                fullPathNF = writeExif.apply(imageToChange.rPath, imageToChange.fNumber, dateToSet)
                MediaScannerConnection.scanFile(requireContext(), arrayOf(fullPathNF), arrayOf("image/jpeg"),null)
            }
            onClearClick()
        }
        else {
            externalStorageManager()
        }
    }

    private fun initCalendarImage(){
        if (viewModel.startTag.value)
            if (viewModel.dateSelect.value) binding.StartCalendar.setImageResource(R.drawable.ic_start_calendar_clicked)
            else binding.StartCalendar.setImageResource(R.drawable.ic_start_calendar)
        else
            if (viewModel.dateSelect.value) binding.StopCalendar.setImageResource(R.drawable.ic_stop_calendar_clicked)
            else binding.StopCalendar.setImageResource(R.drawable.ic_stop_calendar)


    }
    private fun swapDates(dateToCheck: Date) {
        if (viewModel.startTag.value && viewModel.stopDate.value!=""){
            if (viewModel.stopDateTime.value.time - dateToCheck.time < 0){
                viewModel.setDate(viewModel.startTag.value, viewModel.stopDateTime.value, viewModel.stopDate.value)
                binding.startDate.text= viewModel.startDate.value
                viewModel.setTag(!viewModel.startTag.value)
            }
        }
        else if(!viewModel.startTag.value && viewModel.startDate.value!=""){
            if (dateToCheck.time - viewModel.startDateTime.value.time < 0){
                viewModel.setDate(viewModel.startTag.value, viewModel.startDateTime.value, viewModel.startDate.value)
                binding.stopDate.text = viewModel.stopDate.value
                viewModel.setTag(!viewModel.startTag.value)
            }
        }
    }

    //----------------------------------------------------------------------------------------------------
    //Galleryadapter

    private inner class GalleryAdapter(val onClick: (MediaStoreImage, Int) -> Unit) :
        ListAdapter<MediaStoreImage, ImageViewHolder>(MediaStoreImage.DiffCallback) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.recyclerview_detail, parent, false)
            return ImageViewHolder(view, onClick)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val mediaStoreImage = getItem(position)
            holder.rootView.tag = mediaStoreImage

            Glide.with(holder.imageView)
                .load(mediaStoreImage.contentUri)
                .thumbnail(0.33f)
                .centerCrop()
                .into(holder.imageView)

            if (viewModel.viewHolds.value.contains(position)) {
                holder.imageView.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SCREEN)
                holder.imageView.isSelected = true
                holder.imageView.setBackgroundResource(R.drawable.rounded_bg)
            } else {
                holder.imageView.clearColorFilter()
                holder.imageView.isSelected = false
                holder.imageView.setBackgroundResource(R.color.colorPrimary)
            }
        }
    }

    private fun showImages() {
        viewModel.loadImages()
    }

    //----------------------------------------------------------------------------------------------------
    //Permission handling

    private fun haveStoragePermission():Boolean {
        return ((ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
    }

    private val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val permissionReqRes = permissions.entries.all {
                it.value
            }
            if (permissionReqRes) {
                if(!Environment.isExternalStorageManager()) {
                    externalStorageManager()
                    showImages()
                }
                else showImages()
            }
            else Toast.makeText(requireContext(),R.string.permission_not_granted, Toast.LENGTH_LONG).show()
        }

    private fun externalStorageManager(){
        val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
        startActivity(
            Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                uri
            )
        )
    }
}