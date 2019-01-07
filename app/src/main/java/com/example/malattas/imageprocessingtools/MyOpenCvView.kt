package com.example.malattas.imageprocessingtools

import java.io.FileOutputStream

import org.opencv.android.JavaCameraView

import android.content.Context
import android.hardware.Camera
// import android.hardware.Camera
import android.hardware.Camera.PictureCallback
import android.hardware.Camera.Size
import android.util.AttributeSet
import android.util.Log

class MyOpenCvView(context: Context, attrs: AttributeSet) : JavaCameraView(context, attrs), PictureCallback {
    private var mPictureFileName: String? = null

    val effectList: List<String>
        get() = mCamera.parameters.supportedColorEffects

    val isEffectSupported: Boolean
        get() = mCamera.parameters.colorEffect != null

    var effect: String
        get() = mCamera.parameters.colorEffect
        set(effect) {
            val params = mCamera.parameters
            params.colorEffect = effect
            mCamera.parameters = params
        }

    val resolutionList: List<Size>
        get() = mCamera.parameters.supportedPreviewSizes

    var resolution: Size
        get() = mCamera.parameters.previewSize
        set(resolution) {
            disconnectCamera()
            mMaxHeight = resolution.height
            mMaxWidth = resolution.width
            connectCamera(width, height)
        }

    fun takePicture(fileName: String) {
        Log.i(TAG, "Taking picture")
        this.mPictureFileName = fileName
        // Postview and jpeg are sent in the same buffers if the queue is not empty when performing a capture.
        // Clear up buffers to avoid mCamera.takePicture to be stuck because of a memory issue
        //mCamera.setPreviewCallback(null);

        // PictureCallback is implemented by the current class
        mCamera.takePicture(null, null, this)
    }

    override fun onPictureTaken(data: ByteArray, camera: Camera) {
        Log.i(TAG, "Saving a bitmap to file")
        // The camera preview was automatically stopped. Start it again.
        mCamera.startPreview()
        mCamera.setPreviewCallback(this)

        // Write the image in a file (in jpeg format)
        try {
            val fos = FileOutputStream(mPictureFileName!!)

            fos.write(data)
            fos.close()

        } catch (e: java.io.IOException) {
            Log.e("PictureDemo", "Exception in photoCallback", e)
        }

    }

    companion object {
        private val TAG = "Sample::Tutorial3View"
    }
}
