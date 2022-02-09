package com.android.samples.photic

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.android.samples.photic.databinding.GalleryFragmentBinding
import com.android.samples.photic.viewmodels.GalleryFragmentViewModel
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*


private const val WRITE_EXTERNAL_STORAGE_REQUEST = 0x0815

class GalleryFragment: Fragment(){

    private val viewModel: GalleryFragmentViewModel by activityViewModels()
    private lateinit var binding: GalleryFragmentBinding
    private val buttonClick = AlphaAnimation(0f, 1f)
    private val datedialogFragment = DateDialogFragment()
    var PERMISSIONS = arrayOf(
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

        if (viewModel.numberImages != 0) {
            binding.imageNumber.visibility = View.VISIBLE
            binding.clearSelection.visibility = View.VISIBLE
            binding.imageNumber.text = viewModel.numberImages.toString()
        }
        binding.startDate.text = viewModel.startDate
        binding.stopDate.text = viewModel.stopDate
        initCalendarImage()

        setFragmentResultListener("requestKey") { requestKey, bundle ->
            val justdate = bundle.getString("justDate")
            val finaldate = bundle.getString("finalDate")
            val dateTimeFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss")
            val datetime = dateTimeFormat.parse(finaldate)

            viewModel.setdateSelect(tag = false)
            initCalendarImage()
            if (finaldate != null && justdate != null) swapDates(datetime)
            if (viewModel.startTag) binding.startDate.text = justdate else binding.stopDate.text= justdate

            if (finaldate != null && justdate != null) viewModel.setDate(viewModel.startTag, datetime, justdate)
            checkConstraints()
        }

        binding.clearSelection.setOnClickListener { onClearClick() }
        binding.startLayout.setOnClickListener { onStartClick() }
        binding.stopLayout.setOnClickListener { onStopClick() }

        if(viewModel.startDate!="" && viewModel.stopDate != "" && viewModel.selectedImages.count() > 0){
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
            permReqLauncher.launch(PERMISSIONS)
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
        if(viewModel.byImage){
            val justdate = SimpleDateFormat("dd.MM.yy").format(image.dateModified)
            val datetime = image.dateModified

            viewModel.setdateSelect(tag = false)
            initCalendarImage()
            swapDates(datetime)
            if(viewModel.startTag) binding.startDate.text=justdate else binding.stopDate.text=justdate

            viewModel.setDate(viewModel.startTag, datetime, justdate)
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
        val intImagesSelected = viewModel.selectedImages.size
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
        val viewIterator: ListIterator<Int> = viewModel.viewHolds.listIterator()

        if (position != -1){
            val holder = recyclerview.findViewHolderForAdapterPosition(position)
            val imageview = holder!!.itemView.findViewById<ImageView>(R.id.image)

            if (viewModel.viewHolds.contains(position)){
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
        if(viewModel.startDate != "" && viewModel.stopDate != "" && viewModel.selectedImages.count() > 0)
            binding.fab.show()
        else
            binding.fab.hide()
    }

    private fun applyChanges(){
        val imageIterator: ListIterator<MediaStoreImage> = viewModel.selectedImages.listIterator()
        var imageToChange: MediaStoreImage
        val writeExif = WriteExifActivity()
        var fullPathNF: String
        val timeSpan: Long
        var dateToSet: Date
        val startMillis: Long = viewModel.startDateTime.time
        val stopMillis: Long = viewModel.stopDateTime.time

        if (Environment.isExternalStorageManager()){
            if(viewModel.numberImages > 1) timeSpan = (stopMillis - startMillis)/(viewModel.numberImages+1) else timeSpan = 0

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
        if (viewModel.startTag)
            if (viewModel.dateSelect) binding.StartCalendar.setImageResource(R.drawable.ic_start_calendar_clicked)
            else binding.StartCalendar.setImageResource(R.drawable.ic_start_calendar)
        else
            if (viewModel.dateSelect) binding.StopCalendar.setImageResource(R.drawable.ic_stop_calendar_clicked)
            else binding.StopCalendar.setImageResource(R.drawable.ic_stop_calendar)


    }
    private fun swapDates(dateToCheck: Date) {
        if (viewModel.startTag && viewModel.stopDate !=""){
            if (viewModel.stopDateTime.time - dateToCheck.time < 0){
                viewModel.setDate(viewModel.startTag, viewModel.stopDateTime, viewModel.stopDate)
                binding.startDate.text= viewModel.startDate
                viewModel.setTag(!viewModel.startTag)
            }
        }
        else if(!viewModel.startTag && viewModel.startDate !=""){
            if (dateToCheck.time - viewModel.startDateTime.time < 0){
                viewModel.setDate(viewModel.startTag, viewModel.startDateTime, viewModel.startDate)
                binding.stopDate.text = viewModel.stopDate
                viewModel.setTag(!viewModel.startTag)
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

            if (viewModel.viewHolds.contains(position)) {
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
                it.value == true
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