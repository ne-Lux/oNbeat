package com.android.samples.oNbeat

import android.content.ContentResolver
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.android.samples.oNbeat.data.RaceResult
import com.android.samples.oNbeat.databinding.GalleryFragmentBinding
import com.android.samples.oNbeat.viewmodels.FTPClientViewModel
import com.android.samples.oNbeat.viewmodels.GalleryFragmentViewModel
import com.bumptech.glide.Glide
import org.tensorflow.lite.task.vision.detector.Detection
import java.io.File
import java.text.SimpleDateFormat
import java.util.concurrent.Executor


/*
GalleryFragment that displays the main fragment and handles the associated button-clicks
 */
class GalleryFragment: Fragment(), ObjectDetectionFragment.DetectorListener{

    //Initiate ViewModel, binding, permissions and other variables
    private val viewModel: GalleryFragmentViewModel by activityViewModels()
    private val ftpViewModel: FTPClientViewModel by activityViewModels()

    private var ftpThread: Thread? = null
    private lateinit var ftpClient: FTPClient
    private val executor: Executor? = null


    private lateinit var binding: GalleryFragmentBinding
    private lateinit var contentResolver: ContentResolver
    private val buttonClick = AlphaAnimation(0f, 1f)
    private lateinit var odf:  ObjectDetectionFragment

    //Initiate the binding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = GalleryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    //fun onViewCreated defines onClickListener and restores the Fragment according to the ViewModel-Variables
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // -----------------------------------------------------------------------------------------
        // Initiate Object Detection
        // -----------------------------------------------------------------------------------------
        odf = ObjectDetectionFragment(context = requireContext(), objectDetectorListener = this)

        // -----------------------------------------------------------------------------------------
        // Attach the galleryAdapter
        // -----------------------------------------------------------------------------------------
        contentResolver = requireContext().contentResolver
        val galleryAdapter = GalleryAdapter { result, posi ->
            onImageClick(result, posi)
        }

        //Bind the GalleryAdapter to the RecyclerView-Grid
        binding.gallery.also { viewX ->
            viewX.layoutManager = GridLayoutManager(requireContext(), 1)
            viewX.adapter = galleryAdapter
        }
        // -----------------------------------------------------------------------------------------
        // Observers Functions
        // -----------------------------------------------------------------------------------------
        val esp1Observer = Observer<MutableList<String>?> { imagesToDownload ->
            println("fired")
            if (imagesToDownload.isNotEmpty()) {
                println("Incoming Picture")
                ftpClient = FTPClient(
                    true,
                    ftpViewModel.hostOne.value,
                    ftpViewModel.ftpPort.value,
                    ftpViewModel.userName.value,
                    ftpViewModel.pW.value,
                    imagesToDownload,
                    requireContext().filesDir.path
                )
                ftpThread = Thread(ftpClient)
                ftpThread!!.start()
                println("Download completed!")
            }
        }

        val esp2Observer = Observer<MutableList<String>?> { imagesToDownload ->
            if (imagesToDownload.isNotEmpty()) {
                println("Incoming Picture")
                ftpClient = FTPClient(
                    false,
                    ftpViewModel.hostTwo.value,
                    ftpViewModel.ftpPort.value,
                    ftpViewModel.userName.value,
                    ftpViewModel.pW.value,
                    imagesToDownload,
                    requireContext().filesDir.path
                )
                ftpThread = Thread(ftpClient)
                ftpThread!!.start()
                println("Downloading....")
            }
        }

        val hotspotObserver = Observer<Boolean> { hotspotActive ->
            if (hotspotActive) {
                binding.icHotspot.setImageResource(R.drawable.ic_hotspot)
            } else {
                binding.icHotspot.setImageResource(R.drawable.ic_hotspot_off)
            }
        }

        val devicesObserver = Observer<Int> { devicesConnected ->
            binding.devicesConnected.text = devicesConnected.toString()
        }

        val raceResultObserver = Observer<MutableList<RaceResult>> { resultList ->
            galleryAdapter.submitList(resultList)
        }

        // -----------------------------------------------------------------------------------------
        // Observer Registration
        // -----------------------------------------------------------------------------------------
        ftpViewModel.picDownloadOne.observe(viewLifecycleOwner, esp1Observer)
        ftpViewModel.picDownloadTwo.observe(viewLifecycleOwner, esp2Observer)
        ftpViewModel.hotspot.observe(viewLifecycleOwner, hotspotObserver)
        ftpViewModel.connectedDevices.observe(viewLifecycleOwner, devicesObserver)
        viewModel.results.observe(viewLifecycleOwner, raceResultObserver)

        // ToDo: Ist an dieser Stelle nur zum ausprobieren
        //odf.detectObjects("Teststring")

        // -----------------------------------------------------------------------------------------
        // Onclick Listeners
        // -----------------------------------------------------------------------------------------
        binding.icOpen.setOnClickListener {onOpenClick()}
        binding.icSave.setOnClickListener { onSaveClick() }
        binding.icHotspot.setOnClickListener { onHotspotClick() }
        binding.devicesConnected.setOnClickListener { onDevicesClick() }

        // -----------------------------------------------------------------------------------------
        // Initial States
        // -----------------------------------------------------------------------------------------
        if (ftpViewModel.hotspot.value!!){
            binding.icHotspot.setImageResource(R.drawable.ic_hotspot)
        } else {
            binding.icHotspot.setImageResource(R.drawable.ic_hotspot_off)
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Read/Write .csv data
    // ---------------------------------------------------------------------------------------------
    private fun readCsv(inputFile: File): List<RaceResult> {
        //Todo("Pass directorypath to FTPClient. Redundant data atm."
        val directoryPath: String = "/storage/emulated/0/DCIM/oNbeat/SampleData/"
        val reader = inputFile.bufferedReader()
        val header = reader.readLine()
        return reader.lineSequence()
            .filter { it.isNotBlank() }
            .map {
                val (raceNumber,
                    startTime,
                    startImage,
                    finishTime,
                    finishImage) = it.split(',', ignoreCase = true, limit = 6)
                RaceResult(raceNumber.trim().toInt(),
                    startTime.trim().toLong(),
                    startImage.trim(),
                    Uri.parse("file://"+directoryPath+startImage.trim()),
                    finishTime.trim().toLong(),
                    finishImage.trim(),
                    Uri.parse("file://"+directoryPath+finishImage.trim()),
                    finishTime.trim().toLong()-startTime.trim().toLong())
            }.toList()
    }

    // ---------------------------------------------------------------------------------------------
    // Clickhandler
    // ---------------------------------------------------------------------------------------------

    //ClickHandler for image
    private fun onImageClick(raceResult: RaceResult, posi: Int) {
        val correctRNFragment = CorrectRaceNumberFragment()
        val args = Bundle()
        args.putInt("raceNumber", raceResult.raceNumber)
        correctRNFragment.arguments = args
        correctRNFragment.show(parentFragmentManager, "CorrectRN_tag")
    //If Date by image selection is active
        /*
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

         */
    }

    private fun onOpenClick() {
        val externalPath: String = "/storage/emulated/0/DCIM/oNbeat/SampleData"
        {
            TODO("Auswahl des Files über Dialog")
        }
        if (File(externalPath+"/ux47aW_20230528.csv").exists()) {
            val importResults = readCsv(File(externalPath+"/ux47aW_20230528.csv"))
            viewModel.importResults(importResults)
        }

    }

    private fun onSaveClick(){
        TODO()
    }

    private fun onHotspotClick(){
        Toast.makeText(requireContext(),"Hotspot is activated: ${ftpViewModel.hotspot}",Toast.LENGTH_LONG).show()
    }

    private fun onDevicesClick(){
        Toast.makeText(requireContext(),"Devices connected: ${ftpViewModel.connectedDevices}",Toast.LENGTH_LONG).show()
    }

    // ---------------------------------------------------------------------------------------------
    // Other functions. To be DELETED
    // ---------------------------------------------------------------------------------------------

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


    // ---------------------------------------------------------------------------------------------
    // Binding of a race result to Recyclerview Gallery
    // ---------------------------------------------------------------------------------------------
    private inner class GalleryAdapter(val onClick: (RaceResult, Int) -> Unit) :
        ListAdapter<RaceResult, ResultViewHolder>(RaceResult.DiffCallback) {

        //Create a new ViewHolder for RecyclerView
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            //Inflate the recyclerview_detail (ImageView)
            val view = layoutInflater.inflate(R.layout.race_result, parent, false)
            return ResultViewHolder(view, onClick)
        }

        //Update content of RecyclerView
        override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
            val raceResult = getItem(position)
            holder.rootView.tag = raceResult
            holder.raceNumber.text = raceResult.raceNumber.toString()

            if (raceResult.startImage.isNotEmpty()) {
                Glide.with(holder.startImage)
                    .load(raceResult.contentUriStart)
                    .thumbnail(0.33f)
                    .centerCrop()
                    .into(holder.startImage)
                holder.startTime.text = raceResult.startTime.toString()
            }

            if (raceResult.finishImage.isNotEmpty()){
                Glide.with(holder.finishImage)
                    .load(raceResult.contentUriFinish)
                    .thumbnail(0.33f)
                    .centerCrop()
                    .into(holder.finishImage)
                holder.finishTime.text = raceResult.finishTime.toString()
            }

            if ( raceResult.startImage.isNotEmpty() && raceResult.finishImage.isNotEmpty()){
                holder.totalTime.text = raceResult.totalTime.toString()
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Fragment Listener for Object Detection
    // ---------------------------------------------------------------------------------------------

    override fun onError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResults(results: MutableList<Detection>?, imageHeight: Int, imageWidth: Int) {
        TODO("Not yet implemented")
    }
}