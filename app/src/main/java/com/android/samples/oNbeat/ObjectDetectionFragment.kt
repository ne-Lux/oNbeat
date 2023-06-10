/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.samples.oNbeat


import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import android.graphics.BitmapFactory
import androidx.fragment.app.Fragment

// -----------------------------------------------------------------------------------------
// ObjectDetectionFragment performs the object detection on a given image
// -----------------------------------------------------------------------------------------
class ObjectDetectionFragment(
    private var threshold: Float = 0.5f,
    private var numThreads: Int = 4,
    private var maxResults: Int = 4,
    private var currentDelegate: Int = 0,
    private val context: Context,
    val objectDetectorListener: DetectorListener?
) : Fragment() {


    // -----------------------------------------------------------------------------------------
    // Public function to be called from GalleryFragment
    // -----------------------------------------------------------------------------------------
    fun detectObjects(imagePath: String) {
        val imageBitmap = BitmapFactory.decodeFile(imagePath)
        detect(imageBitmap, filePath = imagePath)
    }

    private var objectDetector: ObjectDetector? = null

    // -----------------------------------------------------------------------------------------
    // Initiate the object detector including hardware options and model binding
    // -----------------------------------------------------------------------------------------
    private fun setupObjectDetector() {
        val optionsBuilder =
            ObjectDetector.ObjectDetectorOptions.builder()
                .setScoreThreshold(threshold)
                .setMaxResults(maxResults)
        val baseOptionsBuilder = BaseOptions.builder().setNumThreads(numThreads)
        when (currentDelegate) {
            DELEGATE_CPU -> {
                // Default
            }
            DELEGATE_GPU -> {
                if (CompatibilityList().isDelegateSupportedOnThisDevice) {
                    baseOptionsBuilder.useGpu()
                } else {
                    objectDetectorListener?.onError("GPU is not supported on this device")
                }
            }
            DELEGATE_NNAPI -> {
                baseOptionsBuilder.useNnapi()
            }
        }

        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        val modelName = "v18_lite4_dyn.tflite"

        try {
            objectDetector =
                ObjectDetector.createFromFileAndOptions(context, modelName, optionsBuilder.build())
        } catch (e: IllegalStateException) {
            objectDetectorListener?.onError(
                "Object detector failed to initialize. See error logs for details"
            )
            Log.e("Test", "TFLite failed to load model with error: " + e.message)
        }
    }

    // -----------------------------------------------------------------------------------------
    // Perform object detection
    // -----------------------------------------------------------------------------------------
    private fun detect(image: Bitmap, imageRotation: Int = 0, filePath: String) {
        if (objectDetector == null) {
            setupObjectDetector()
        }

        val imageProcessor =
            ImageProcessor.Builder()
                .add(Rot90Op(-imageRotation / 90))
                .build()

        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(image))

        val results = objectDetector?.detect(tensorImage)
        objectDetectorListener?.onResults(
            results,
            filePath,
            tensorImage.height,
            tensorImage.width)
    }

    // -----------------------------------------------------------------------------------------
    // Interface to pass the results back to GalleryFragment
    // -----------------------------------------------------------------------------------------
    interface DetectorListener {
        fun onError(error: String)
        fun onResults(
            results: MutableList<Detection>?,
            filePath: String,
            imageHeight: Int,
            imageWidth: Int
        )
    }

    companion object {
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val DELEGATE_NNAPI = 2
    }

}


