package com.android.samples.oNbeat

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
import androidx.recyclerview.widget.ListAdapter
import com.android.samples.oNbeat.data.RaceResult
import com.android.samples.oNbeat.databinding.GalleryFragmentBinding
import com.android.samples.oNbeat.viewmodels.GalleryFragmentViewModel
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*
import java.net.*

/*
GalleryFragment that displays the main fragment and handles the associated button-clicks
 */
class TransceiverFragment: Fragment(){

    //Initiate ViewModel, binding, permissions and other variables
    private val localIP = InetAddress.getByName("127.0.0.1")
    private val viewModel: GalleryFragmentViewModel by activityViewModels()
    private lateinit var binding: GalleryFragmentBinding
    private val buttonClick = AlphaAnimation(0f, 1f)
    private val datedialogFragment = DateDialogFragment()
    private var reqPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    //Initiate the binding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = GalleryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    //fun onViewCreated defines onClickListener and restores the Fragment according to the ViewModel-Variables
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        server()

        //Set onClickListener for images inside the gallery

        /*
        val galleryAdapter = GalleryAdapter { image, posi ->
            onImageClick(image, posi)
        }
        //Set onClickListener for all buttons
        binding.clearSelection.setOnClickListener { onClearClick() }
        binding.startLayout.setOnClickListener { onStartStopClick(tag = true) }
        binding.stopLayout.setOnClickListener { onStartStopClick(tag = false) }
        binding.fab.setOnClickListener { applyChanges() }

        //Bind the GalleryAdapter to the RecyclerView-Grid
        binding.gallery.also { viewX ->
            viewX.layoutManager = GridLayoutManager(requireContext(), 3)
            viewX.adapter = galleryAdapter
        }

        //Restore the number of already selected images icon and the clear button, if there are already selected images
        if (viewModel.numberImages.value != 0) {
            binding.imageNumber.visibility = View.VISIBLE
            binding.clearSelection.visibility = View.VISIBLE
            binding.imageNumber.text = viewModel.numberImages.value.toString()
        }

        //Display start-and stopdate, if already selected
        binding.startDate.text = viewModel.startDate.value
        binding.stopDate.text = viewModel.stopDate.value
        initCalendarImage()

        //Display the Floating Action Button, if all constraints are already fulfilled
        if(viewModel.startDate.value!="" && viewModel.stopDate.value!= "" && viewModel.selectedImages.value.count() > 0){
            binding.fab.show()
        }
        else{
            binding.fab.hide()
        }

        //FragmentResultListener for the DateTimePickerFragment. This Code is executed, once a date is selected
        setFragmentResultListener("requestKey") { _, bundle ->
            val justdate = bundle.getString("justDate")!!
            val finaldate = bundle.getString("finalDate")!!
            val dateTimeFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss")
            val datetime = dateTimeFormat.parse(finaldate)!!

            //No date selection mode any more
            viewModel.setdateSelect(tag = false)
            //Restore ic_start/stop_calendar (not clicked)
            initCalendarImage()
            //Call swapDates to swap start- and stopdate, if stopdate is earlier than startdate
            swapDates(datetime)
            //Write the date to the specified label
            if (viewModel.startTag.value) binding.startDate.text = justdate else binding.stopDate.text= justdate
            //Store the date inside the ViewModel
            viewModel.setDate(viewModel.startTag.value, datetime, justdate)
            //Check if constraints to display the fab are already fulfilled
            checkConstraints()
        }

        //FragmentResultListener for the DateTimePickerFragment. This Code is executed once the view is canceled.
        setFragmentResultListener("destroyedDPD") {_, _ ->
            //Restore ic_start/stop_calendar (not clicked)
            initCalendarImage()
        }
        //FragmentResultListener for the DateDialogFragment. This Code is executed once the view is canceled.
        setFragmentResultListener("destroyedDD") {_, _ ->
            //Restore ic_start/stop_calendar (not clicked)
            initCalendarImage()
        }

        //Observe the imagelist and hand it to the GalleryAdapter, so that a new image list is displayed
        viewModel.images.observe(viewLifecycleOwner) { images ->
            galleryAdapter.submitList(images)
        }

        //Check permission
        if (!haveStoragePermission()) {
            //Show the Permission Request Launcher, if there is no permission
            permReqLauncher.launch(reqPermissions)
            //Check the External Storage Manager Permission
        } else if (!Environment.isExternalStorageManager()) {
            //Show the External Storage Manager Request, if there is no permission
            externalStorageManager()
            showImages()
        }
        //Every permission is granted
        else {
            showImages()
        }

         */
    }

    //----------------------------------------------------------------------------------------------------
    // Start TCP Server
    fun server() {
        val server = ServerSocket(29391, 2, localIP)

        val client = server.accept()
        // TBD: val output = PrintWriter(client.getOutputStream(), true)
        // TBD: val input = BufferedReader(InputStreamReader(client.inputStream))

        //TBD: output.println("${input.readLine()} back")
    }

    //----------------------------------------------------------------------------------------------------
    //Clickhandler

    //ClickHandler for image
    private fun onImageClick(image: RaceResult, posi: Int) {
        //If Date by image selection is active
        if(viewModel.byImage.value && viewModel.dateSelect.value){
            //Format the dateModified of the image
            val justdate = SimpleDateFormat("dd.MM.yy").format(image.dateModified)
            val datetime = image.dateModified

            //No date selection mode any more
            viewModel.setdateSelect(tag = false)
            viewModel.setbyImage(false)

            //Restore ic_start/stop_calendar (not clicked)
            initCalendarImage()
            //Call swapDates to swap start- and stopdate, if stopdate is earlier than startdate
            swapDates(datetime)
            //Write the date to the specified label
            if(viewModel.startTag.value) binding.startDate.text=justdate else binding.stopDate.text=justdate
            //Store the date inside the ViewModel
            viewModel.setDate(viewModel.startTag.value, datetime, justdate)

            //Display an animation to give a feedback to the user, that the image click was recognized
            val recyclerview = binding.gallery
            val holder = recyclerview.findViewHolderForAdapterPosition(posi)
            val imageview = holder!!.itemView.findViewById<ImageView>(R.id.image)
            buttonClick.duration=1000
            buttonClick.fillAfter=true
            buttonClick.startOffset=0
            imageview.startAnimation(buttonClick)
        }
        //If date selection is not active
        else{
            //Store/Remove the selected image in/from the ViewModel variable
            viewModel.selectImage(image, posi)
            //Highlight/Remove Highlighting for selected image
            handleSelection(select = true, posi)
            //Update the number of selected images
            displayImagenum()
        }
        //Check if constraints to display the fab are already fulfilled
        checkConstraints()
    }

    //ClickHandler for clear button
    private fun onClearClick() {
        //Remove highlighting from all images
        handleSelection(false)
        //Clear the image storage variables inside the ViewModel
        viewModel.deSelectImages()
        //Update the number of selected images
        displayImagenum()
        //Check if constraints to display the fab are already fulfilled
        checkConstraints()
    }

    //ClickHandler for start/stopdate
    private fun onStartStopClick(tag: Boolean){
        //Set date selection mode for start/stopdate
        viewModel.setTag(tag = tag)
        viewModel.setdateSelect(tag = true)
        //Show the DateDialogFragment
        datedialogFragment.show(parentFragmentManager,"DateDialog_tag")
        //Restore ic_start/stop_calendar (clicked)
        initCalendarImage()
    }

    //ClickHandler for fab
    private fun applyChanges(){
        val imageIterator: ListIterator<RaceResult> = viewModel.selectedImages.value.listIterator()
        var imageToChange: RaceResult
        val writeExif = WriteExifActivity()
        var fullPathNF: String
        val timeSpan: Long
        var dateToSet: Date
        val startMillis: Long = viewModel.startDateTime.value.time
        val stopMillis: Long = viewModel.stopDateTime.value.time

        //If External Storage Permission is granted (needed to change images)
        if (Environment.isExternalStorageManager()){
            //Calculate the timespan between start- and stopdate
            timeSpan = (stopMillis - startMillis)/(viewModel.numberImages.value+1)

            //Iterate over all images to change
            while (imageIterator.hasNext()) {
                //Calculate the target date for n-th images as: startdate + n*timespan
                dateToSet = Date(startMillis+timeSpan*(imageIterator.nextIndex()+1))
                imageToChange = imageIterator.next()
                //Change the attributes of the image
                fullPathNF = writeExif.apply(imageToChange.rPath, imageToChange.fileName, dateToSet)
                //Scan the new image, so that it is displayed in the image gallery
                MediaScannerConnection.scanFile(requireContext(), arrayOf(fullPathNF), arrayOf("image/jpeg"),null)
            }
            //Deselect all images
            onClearClick()
        }
        //If External Storage Permission is not granted (needed to change images)
        else {
            //Show the dialog to grant the permission
            externalStorageManager()
        }
    }

    //----------------------------------------------------------------------------------------------------
    //Reusable funs

    //Display/Hide the number of selected images as well as the clear selection button
    private fun displayImagenum() {
        val imageNumber= binding.imageNumber
        val intImagesSelected = viewModel.selectedImages.value.size
        val clearSelection = binding.clearSelection

        //Write the number of selected images to the label
        imageNumber.text = intImagesSelected.toString()
        //If there is an image selected
        if (intImagesSelected > 0) {
            //Display the number of selected images as well as the clear selection button
            imageNumber.visibility = View.VISIBLE
            clearSelection.visibility = View.VISIBLE
        }
        else {
            //Hide the number of selected images as well as the clear selection button
            imageNumber.visibility = View.INVISIBLE
            clearSelection.visibility = View.INVISIBLE
        }
    }

    //Set background and colorfilter depending on selection status
    private fun handleSelection(select: Boolean, position: Int = -1){ //attribute select is not needed yet. It is introduced to be used for a range selection
        val recyclerview = binding.gallery
        val viewIterator: ListIterator<Int> = viewModel.viewHolds.value.listIterator()

        //If a specific position is given
        if (position != -1){
            val holder = recyclerview.findViewHolderForAdapterPosition(position)
            val imageview = holder!!.itemView.findViewById<ImageView>(R.id.image)

            //if the variable inside the ViewModel contains this position == the image should be highlighted as selected
            if (viewModel.viewHolds.value.contains(position)){
                imageview.isSelected = true
                //Set a colorfilter
                imageview.setColorFilter(Color.GRAY, PorterDuff.Mode.SCREEN)
                //Add round edges to the image
                imageview.setBackgroundResource(R.drawable.rounded_bg)
            }
            else {
                imageview.isSelected = false
                //Remove colorfilter
                imageview.clearColorFilter()
                //Remove round edges
                imageview.setBackgroundResource(R.color.colorPrimary)
            }
        }
        //If no specific position is given, iterate over all views stored inside the ViewModel
        else {
            while (viewIterator.hasNext()) {
                try{
                    val holder = recyclerview.findViewHolderForAdapterPosition(viewIterator.next())
                    val imageview = holder!!.itemView.findViewById<ImageView>(R.id.image)
                    //If the image should be selected (unused at the moment - can be used for range selection)
                    if (select) {
                        imageview.isSelected = true
                        imageview.setColorFilter(Color.GRAY, PorterDuff.Mode.SCREEN)
                        imageview.setBackgroundResource(R.drawable.rounded_bg)
                    } else {
                        imageview.isSelected = false
                        //Remove colorfilter
                        imageview.clearColorFilter()
                        //Remove round edges
                        imageview.setBackgroundResource(R.color.colorPrimary)
                    }
                }
                //Catch Exception - occurs if the viewHolder for this position is not inside the view range (because you scrolled up/down)
                catch (e: Exception) {
                    //Do nothing. The removal of the highlighting will then be made by onBindViewHolder
                }

            }
        }
    }

    //Fun to check, if all constraints to display the Floating Action Button are fulfilled:
    //StartDate is set, StopDate is set, at least one image is selected
    private fun checkConstraints(){
        if(viewModel.startDate.value!= "" && viewModel.stopDate.value!= "" && viewModel.selectedImages.value.count() > 0)
            binding.fab.show()
        else
            binding.fab.hide()
    }

    //Fun to display the ic_start/stop_calendar image, based on the date selection mode
    private fun initCalendarImage(){
        //For startdate
        if (viewModel.startTag.value)
        //If dateselection mode is active, show ic_start_calendar_clicked
            if (viewModel.dateSelect.value) binding.StartCalendar.setImageResource(R.drawable.ic_start_calendar_clicked)
            //else show ic_start_calendar
            else binding.StartCalendar.setImageResource(R.drawable.ic_start_calendar)
        //For stopdate
        else
        //If dateselection mode is active, show ic_stop_calendar_clicked
            if (viewModel.dateSelect.value) binding.StopCalendar.setImageResource(R.drawable.ic_stop_calendar_clicked)
            //else show ic_stop_calendar
            else binding.StopCalendar.setImageResource(R.drawable.ic_stop_calendar)
    }

    //Fun to swap start- and stopdate, if stopdate is before startdate
    private fun swapDates(dateToCheck: Date) {
        //If startdate should be chosen, and stopdate is already selected
        if (viewModel.startTag.value && viewModel.stopDate.value!=""){
            //If the difference between these dates is negative (should be positive) --> swap dates
            if (viewModel.stopDateTime.value.time - dateToCheck.time < 0){
                //Set the already chosen stopdate as startdate
                viewModel.setDate(viewModel.startTag.value, viewModel.stopDateTime.value, viewModel.stopDate.value)
                binding.startDate.text= viewModel.startDate.value
                //Swap tag
                viewModel.setTag(!viewModel.startTag.value)
            }
        }
        //If stopdate should be chosen, and startdate is already selected
        else if(!viewModel.startTag.value && viewModel.startDate.value!=""){
            //If the difference between these dates is negative (should be positive) --> swap dates
            if (dateToCheck.time - viewModel.startDateTime.value.time < 0){
                //Set the already chosen startdate as stopdate
                viewModel.setDate(viewModel.startTag.value, viewModel.startDateTime.value, viewModel.startDate.value)
                binding.stopDate.text = viewModel.stopDate.value
                //Swap tag
                viewModel.setTag(!viewModel.startTag.value)
            }
        }
    }

    //----------------------------------------------------------------------------------------------------
    //Galleryadapter
    private inner class GalleryAdapter(val onClick: (RaceResult, Int) -> Unit) :
        ListAdapter<RaceResult, ImageViewHolder>(RaceResult.DiffCallback) {

        //Create a new ViewHolder for RecyclerView
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            //Inflate the recyclerview_detail (ImageView)
            val view = layoutInflater.inflate(R.layout.recyclerview_detail, parent, false)
            return ImageViewHolder(view, onClick)
        }

        //Update content of RecyclerView
        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val mediaStoreImage = getItem(position)
            holder.rootView.tag = mediaStoreImage

            //Bind the image as a thumbnail into the ImageView using Glide
            Glide.with(holder.imageView)
                .load(mediaStoreImage.contentUri)
                .thumbnail(0.33f)
                .centerCrop()
                .into(holder.imageView)

            //If the image is already selected
            if (viewModel.viewHolds.value.contains(position)) {
                holder.imageView.isSelected = true
                //Set the colorfilter
                holder.imageView.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SCREEN)
                //Add round edges
                holder.imageView.setBackgroundResource(R.drawable.rounded_bg)
            } else {
                holder.imageView.isSelected = false
                //Remove the colorfilter
                holder.imageView.clearColorFilter()
                //Remove round edges
                holder.imageView.setBackgroundResource(R.color.colorPrimary)
            }
        }
    }

    private fun showImages() {
        viewModel.loadImages()
    }

    //----------------------------------------------------------------------------------------------------
    //Permission handling

    //Simple permission check
    private fun haveStoragePermission():Boolean {
        return ((ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
        //The result of externalStorageManager is not checked immediately. It is checked every time an image is about to be changed.
    }

    //Permission Request Launcher
    private val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val permissionReqRes = permissions.entries.all {
                it.value
            }
            //Check if the needed permissions are granted
            if (permissionReqRes) {
                //Check the External Storage Manager Permission
                if(!Environment.isExternalStorageManager()) {
                    //Show the External Storage Manager Request, if there is no permission
                    externalStorageManager()
                    showImages()
                }
                else showImages()
            }
            //If the permissions are not granted, show a Toast.
            else Toast.makeText(requireContext(),R.string.permission_not_granted, Toast.LENGTH_LONG).show()
        }

    //Show the view to grant the External Storage Manager Permission
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