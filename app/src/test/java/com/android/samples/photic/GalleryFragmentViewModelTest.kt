package com.android.samples.photic

import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.samples.photic.data.MediaStoreImage
import com.android.samples.photic.viewmodels.GalleryFragmentViewModel
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.*


@RunWith(AndroidJUnit4::class)
class GalleryFragmentViewModelTest{
    @Test
    fun displayStartDate(){
        val galleryFragmentViewModel = GalleryFragmentViewModel(ApplicationProvider.getApplicationContext())

        galleryFragmentViewModel.setDate(true, Date(0),"01.01.1970")

        assertThat(galleryFragmentViewModel.startDate.value, `is`("01.01.1970"))
        assertThat(galleryFragmentViewModel.startDateTime.value, `is`(Date(0)))
    }

    @Test
    fun displayStopDate(){
        val galleryFragmentViewModel = GalleryFragmentViewModel(ApplicationProvider.getApplicationContext())

        galleryFragmentViewModel.setDate(false, Date(0),"01.01.1970")

        assertThat(galleryFragmentViewModel.stopDate.value, `is`("01.01.1970"))
        assertThat(galleryFragmentViewModel.stopDateTime.value, `is`(Date(0)))
    }

    @Test
    fun setTag(){
        val galleryFragmentViewModel = GalleryFragmentViewModel(ApplicationProvider.getApplicationContext())

        galleryFragmentViewModel.setTag(tag = true)

        assertThat(galleryFragmentViewModel.startTag.value, `is`(true))
    }

    @Test
    fun setbyImage(){
        val galleryFragmentViewModel = GalleryFragmentViewModel(ApplicationProvider.getApplicationContext())

        galleryFragmentViewModel.setbyImage(tag = true)

        assertThat(galleryFragmentViewModel.byImage.value, `is`(true))
    }

    @Test
    fun setdateSelect(){
        val galleryFragmentViewModel = GalleryFragmentViewModel(ApplicationProvider.getApplicationContext())

        galleryFragmentViewModel.setdateSelect(tag = true)

        assertThat(galleryFragmentViewModel.dateSelect.value, `is`(true))
    }

    @Test
    fun selectImage(){
        val galleryFragmentViewModel = GalleryFragmentViewModel(ApplicationProvider.getApplicationContext())

        galleryFragmentViewModel.selectImage(MediaStoreImage(0,Date(0),Date(0),Date(0),
            "0","/TestPath", contentUri = Uri.fromFile(File("/storage/emulated/0/")
        )),0)

        assertThat(galleryFragmentViewModel.selectedImages.value.count(), `is`(1))
        assertThat(galleryFragmentViewModel.viewHolds.value.count(), `is`(1))

        galleryFragmentViewModel.selectImage(MediaStoreImage(0,Date(0),Date(0),Date(0),
            "0","/TestPath", contentUri = Uri.fromFile(File("/storage/emulated/0/")
            )),0)

        assertThat(galleryFragmentViewModel.selectedImages.value.count(), `is`(0))
        assertThat(galleryFragmentViewModel.viewHolds.value.count(), `is`(0))

        galleryFragmentViewModel.selectImage(MediaStoreImage(0,Date(0),Date(0),Date(0),
            "0","/TestPath", contentUri = Uri.fromFile(File("/storage/emulated/0/")
            )),0)

        assertThat(galleryFragmentViewModel.selectedImages.value.count(), `is`(1))
        assertThat(galleryFragmentViewModel.viewHolds.value.count(), `is`(1))

        galleryFragmentViewModel.deSelectImages()

        assertThat(galleryFragmentViewModel.selectedImages.value.count(), `is`(0))
        assertThat(galleryFragmentViewModel.viewHolds.value.count(), `is`(0))
    }
}