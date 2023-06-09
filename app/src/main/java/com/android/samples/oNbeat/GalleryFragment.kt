package com.android.samples.oNbeat

import android.content.ContentResolver
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
import java.io.FileOutputStream
import java.io.FilenameFilter
import java.io.OutputStream
import java.nio.file.Files
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name


/*
GalleryFragment that displays the main fragment and handles the associated button-clicks
 */
class GalleryFragment: Fragment(), ObjectDetectionFragment.DetectorListener, FTPClient.FileListener{

    //Initiate ViewModel, binding, permissions and other variables
    private val viewModel: GalleryFragmentViewModel by activityViewModels()
    private val ftpViewModel: FTPClientViewModel by activityViewModels()

    private var ftpThread: Thread? = null
    private lateinit var ftpClient: FTPClient

    private val directoryPath: String = "/storage/emulated/0/DCIM/oNbeat/SampleData/"
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
                    directoryPath,
                    this
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
                    directoryPath,
                    this
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

        val devicesObserver = Observer<MutableList<String>?> { devicesConnected ->
            if (binding.devicesConnected.text.toString().trim().toInt() > devicesConnected.count()) {
                binding.devicesConnected.setTextColor(Color.RED)

                AlertDialog.Builder(requireContext())
                    .setTitle("Device disconnected")
                    .setMessage("An ESP32 disconnected. Please save the current results and restart the application!")
                    .setCancelable(true)
                    .create()
                    .show()
            }
            binding.devicesConnected.text = devicesConnected.count().toString()
        }

        val raceResultObserver = Observer<MutableList<RaceResult>?> { resultList ->
            galleryAdapter.submitList(resultList)
            galleryAdapter.notifyDataSetChanged()
            if(resultList.isNotEmpty()){
                binding.icClear.visibility = View.VISIBLE
            } else {
                binding.icClear.visibility = View.INVISIBLE
            }
        }

        // -----------------------------------------------------------------------------------------
        // Observer Registration
        // -----------------------------------------------------------------------------------------
        ftpViewModel.picDownloadOne.observe(viewLifecycleOwner, esp1Observer)
        ftpViewModel.picDownloadTwo.observe(viewLifecycleOwner, esp2Observer)
        ftpViewModel.hotspot.observe(viewLifecycleOwner, hotspotObserver)
        ftpViewModel.connectedDevices.observe(viewLifecycleOwner, devicesObserver)
        viewModel.results.observe(viewLifecycleOwner, raceResultObserver)

        // -----------------------------------------------------------------------------------------
        // Onclick Listeners
        // -----------------------------------------------------------------------------------------
        binding.icOpen.setOnClickListener {onOpenClick()}
        binding.icSave.setOnClickListener { onSaveClick() }
        binding.icHotspot.setOnClickListener { onHotspotClick() }
        binding.devicesConnected.setOnClickListener { onDevicesClick() }
        binding.icMode.setOnClickListener { onModeClick() }
        binding.icClear.setOnClickListener { onClearClick() }
    }

    // ---------------------------------------------------------------------------------------------
    // Read/Write .csv data
    // ---------------------------------------------------------------------------------------------
    private fun readCsv(inputFile: File): List<RaceResult> {
        //Todo("Pass directorypath to FTPClient. Redundant data atm."
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
                    if (startImage != ""){
                        Uri.parse("file://"+directoryPath+startImage.trim())
                    } else {
                       Uri.parse("")
                    },
                    finishTime.trim().toLong(),
                    finishImage.trim(),
                    if (finishImage != ""){
                        Uri.parse("file://"+directoryPath+finishImage.trim())
                    } else {
                       Uri.parse("")
                    },
                    finishTime.trim().toLong()-startTime.trim().toLong())
            }.toList()
    }
    fun OutputStream.writeData() {
        val writer = bufferedWriter()
        writer.write(""""raceNumber", "startTime", "startImage", "finishTime", "finishImage", "totalTime"""")
        writer.newLine()
        viewModel.results.value!!.forEach {
            writer.write("${it.raceNumber},${it.startTime},${it.startImage},${it.finishTime},${it.finishImage},${it.totalTime}")
            writer.newLine()
        }
        writer.flush()
    }
    private fun writeCsv(): String {
        val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
        val currentDT = LocalDateTime.now().format(dateTimeFormat)
        val outputFileName: String = "results_$currentDT.csv"
        val destFilePath = directoryPath + outputFileName

        if (!Files.exists(Path(directoryPath))) Files.createDirectory(Path(directoryPath))
        val newFile = File(destFilePath)
        newFile.createNewFile()
        val oFile = FileOutputStream(newFile, false)

        oFile.apply{ writeData() }
        oFile.close()
        return outputFileName
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
    }

    private fun onOpenClick() {

        val files = Path(directoryPath).listDirectoryEntries("*.csv")
        val fileNames: ArrayList<String> = ArrayList()
        for (file in files) {
            fileNames.add(file.name)
        }

        val resultPickerFragment = ResultPickerFragment()
        val args = Bundle()
        args.putStringArrayList("fileList", fileNames)
        resultPickerFragment.arguments = args
        resultPickerFragment.show(parentFragmentManager, "pickResult_tag")


        if (File(directoryPath+"/results_2023-06-08_14-12-50.csv").exists()) {
            val importResults = readCsv(File(directoryPath+"/results_2023-06-08_14-12-50.csv"))
            viewModel.importResults(importResults)
        }
    }

    private fun onModeClick(){
        viewModel.setMode()
        if (viewModel.trackMode.value){
            binding.icMode.setImageResource(R.drawable.ic_track)
        } else {
            binding.icMode.setImageResource(R.drawable.ic_loop)
        }
    }

    private fun onClearClick(){
        viewModel.clearResults()
    }

    private fun onSaveClick(){
        if (viewModel.results.value != null) {
            val outputFileName = writeCsv()
            Toast.makeText(
                requireContext(),
                "Saved current list to $outputFileName.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                requireContext(),
                "No results to save.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun onHotspotClick(){
        Toast.makeText(requireContext(),"Hotspot is activated: ${ftpViewModel.hotspot}",Toast.LENGTH_LONG).show()
    }

    private fun onDevicesClick(){
        Toast.makeText(requireContext(),"Devices connected: ${ftpViewModel.connectedDevices}",Toast.LENGTH_LONG).show()
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
            } else {
                holder.startImage.setImageResource(0)
                holder.startTime.text = ""
            }

            if (raceResult.finishImage.isNotEmpty()){
                Glide.with(holder.finishImage)
                    .load(raceResult.contentUriFinish)
                    .thumbnail(0.33f)
                    .centerCrop()
                    .into(holder.finishImage)
                holder.finishTime.text = raceResult.finishTime.toString()
            } else {
                holder.finishImage.setImageResource(0)
                holder.finishTime.text = ""
            }

            if ( raceResult.startImage.isNotEmpty() && raceResult.finishImage.isNotEmpty()){
                holder.totalTime.text = raceResult.totalTime.toString()
            } else {
                holder.totalTime.text = ""
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Listener for Object Detection
    // ---------------------------------------------------------------------------------------------

    override fun onError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResults(results: MutableList<Detection>?, filePath: String, imageHeight: Int, imageWidth: Int) {
        if (results != null) {
            var numberString = ""
            var bounding: Float
            var label = ""
            var score: Float
            val numberList = hashMapOf<Float, String>()
            for (number in results) {
                score = 0F
                bounding = number.boundingBox.left
                for (category in number.categories) {
                    if (category.score > score) {
                        score = category.score
                        label = category.label
                    }
                }
                numberList[bounding] = label
            }
            val sortedList = numberList.toSortedMap()
            for (char in sortedList) {
                numberString += char.value
            }
            val completeNumber = if (numberString != "") {
                numberString.toInt()
            } else {
                0
            }
            val start: Boolean = filePath.substringAfterLast("/").startsWith("s",true)
            val time: String = filePath.substringAfterLast("_").substringBeforeLast(".")
            viewModel.registerRaceNumber(completeNumber, filePath, start,time.toLong())
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Listener for FTP download
    // ---------------------------------------------------------------------------------------------
    override fun onDownloaded(firstESP32: Boolean, number: String, destFilePath: String) {
        odf.detectObjects(destFilePath)
        ftpViewModel.downloadCompleted(number, firstESP32)
    }
}
