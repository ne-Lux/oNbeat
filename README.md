
PhotiC
==========================

<div align="center">
<img src="screenshots/PhotiC_launcher.png" height="256" alt="PhotiC Icon"/>
</div>

The Android application PhotiC is used to reorder multiple images at the same time without the need to specify the exact date and time for each image separately.


Introduction
------------

Every time an image is received via an instant messenger, the metadata is deleted. On the receiver's phone the image will pop up in the image gallery at the time where it was received, resulting in a wrongly ordered image gallery. PhotiC is an alternative to the applications out there, where you can specify the exact date and time for one image at a time. If you are looking for a chronologically sorted image gallery without the need to specify the date to the minute, PhotiC is the perfect choice.

Requirements
--------------

- Android SDK 30
- Android Device/Emulator API 30+

Installation
--------------

At the moment PhotiC is not provided via Google Play Store, so you have to install the application by downloading the PhotiC.apk file and executing it on your Android 11+ smartphone or tablet. During the installation you will be asked for your explicit agreement on installing software from an unknown source, that you have to consent.

User Instructions
--------------

To reorder images using PhotiC, you need to have at least one image selected and two dates chosen. 

1. Image selecting: You can select images by simply clicking on them. The number of selected images is displayed in the toolbar. Deselecting of single images works the same way like selecting images - by clicking on them. If you want to deselect all images, you can click the red cancel icon in the toolbar beside the number of images selected.

2. Date selection: You can select a date, by clicking on the calendar icon or the date itself. The date selection by calendar works with a standard date-picker. The according time to the date chosen is always set to 12:00 p.m. When choosing date selection by image, the date and time is taken from the next image that is clicked, whereby the image is not selected. You don't need to worry about the right order of the dates you select. If start date and stop date are reversed by mistake (start date lies beyond the stop date), PhotiC switches these dates automatically.

Once you fulfilled these three conditions, a button to apply the changes is displayed. By clicking the button, the selected images will be copied and their date attributes set, so that all images are equally distributed between the two chosen dates. The original images are deleted afterwards to reduce redundant data.
