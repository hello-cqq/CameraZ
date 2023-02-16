/*
 * Copyright 2023 AhahahQ
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Version: 1.0
 * Date : 2023/2/16
 * Author: hey.cqq@gmail.com
 *
 * ---------------------Revision History: ---------------------
 *  <author>           <data>          <version >       <desc>
 *  AhahahQ            2023/2/16         1.0         build this module
*/
package com.ahahahq.cameraz.core

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.ImageReader
import android.media.MediaActionSound
import android.os.Build
import android.os.Handler
import android.util.Size
import android.view.Surface
import android.view.SurfaceHolder
import androidx.annotation.RequiresApi
import com.ahahahq.cameraz.callback.PictureCallback
import com.ahahahq.cameraz.callback.PreviewCallback
import com.ahahahq.cameraz.controller.Camera2ControllerCompat
import com.ahahahq.cameraz.controller.CompatFactory
import com.ahahahq.cameraz.model.*
import com.ahahahq.cameraz.util.Camera2ConfigUtil
import com.ahahahq.cameraz.util.ImageUtil
import com.ahahahq.cameraz.util.Util
import com.ahahahq.cameraz.util.CameraLog
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque

@RequiresApi(Build.VERSION_CODES.P)
internal class Camera2Proxy(context: Context, handler: Handler) : CameraProxy(context, handler) {
    companion object {
        private const val TAG = "Camera2Proxy"
    }

    private val cameraManager: CameraManager by lazy { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    private var cameraCharacteristics: CameraCharacteristics? = null
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var previewBuilder: CaptureRequest.Builder? = null
    private val sessionMap: ConcurrentHashMap<String, Camera2Session> = ConcurrentHashMap()
    private val captureResults: BlockingQueue<CaptureResult?> = LinkedBlockingDeque()
    private val executor = Executors.newFixedThreadPool(3)

    private var controller: Camera2ControllerCompat = CompatFactory.getCamera2Controller()

    private val mediaActionSound: MediaActionSound = MediaActionSound()

    @SuppressLint("MissingPermission")
    override fun open(cameraId: String, stateCallback: CameraProxyStateCallback) {
        var cId = cameraId
        val cameraIdList = cameraManager.cameraIdList
        if (cameraIdList.isEmpty()) {
            throw CameraException(CameraException.ERR_DEVICE, "No cameras in current device!")
        }
        val characteristics = cameraManager.getCameraCharacteristics(cId)
        if (!Camera2ConfigUtil.isHardwareLevelSupported(characteristics, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY)) {
            throw CameraException(
                CameraException.ERR_DEVICE,
                "Current device level not support camera2 #INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY when cameraId = $cId."
            )
        }
        if ((cameraDevice != null) && (cameraDevice!!.id == cId)) {
            CameraLog.i(TAG, "camera #$cId has opened.")
            return
        }
        cameraStateCallback = stateCallback
        cameraManager.openCamera(cId, StateCallback(), handler)
    }

    override fun getId(): String {
        return cameraDevice!!.id
    }

    override fun setState(state: Int) {
        CameraLog.i(TAG, "setState pre: $previewState current: $state")
        when (state) {
            STATE_DESTROY -> stopPreview()
            STATE_PREVIEWING -> {
                if (captureSession == null) {
                    CameraLog.e(TAG, "current session has not created, please execute preview first!")
                    return
                }
                if (previewState != STATE_PREVIEWING) {
                    requestPreview()
                }
            }
            STATE_STOP -> {
                if (previewState == STATE_PREVIEWING) {
                    previewState = STATE_STOP
                    captureSession?.stopRepeating()
                }
            }
            STATE_PICTURE -> {
                previewState = STATE_PICTURE
            }
        }
    }

    override fun setParams(params: CameraParams) {
        val characteristics = cameraCharacteristics
        params.isCamera2AutoReadImage?.let { controller.params.isCamera2AutoReadImage = it }
        if (characteristics != null) {
            params.zoomRatio?.let { controller.setZoom(characteristics, it) }
            params.rotation?.let { controller.params.rotation = it }
            var previewSize = Size(params.previewSize.width, params.previewSize.height)
            if ((previewSize.width > 0) && (previewSize.height > 0)) {
                controller.params.previewSize = Camera2ConfigUtil.getBestCameraResolution(
                    cameraCharacteristics!!,
                    ImageFormat.YUV_420_888,
                    Util.isRightAngle(controller.params.rotation!!),
                    previewSize,
                    false
                )
            }
            requestPreview()
        }
    }

    override fun getParams(): CameraParams {
        return controller.params.clone()
    }

    private fun initParams() {
        controller.initParams(cameraCharacteristics!!)
        controller.params.apply {
            rotation = Camera2ConfigUtil.getRotationFromDisplayToCamera(context, cameraCharacteristics!!)
            previewSize = Camera2ConfigUtil.getBestFullScreenCameraResolution(
                context,
                cameraCharacteristics!!,
                ImageFormat.YUV_420_888,
                Util.isRightAngle(rotation!!)
            )
            pictureSize = Size(previewSize.width, previewSize.height)
        }
    }

    private fun stopPreview() {
        if (previewState == STATE_DESTROY) {
            CameraLog.w(TAG, "preview has already destroyed")
            return
        }
        captureSession?.stopRepeating()
        captureSession?.close()

        previewState = STATE_DESTROY
        captureSession = null
    }

    @Synchronized
    override fun picture(previewId: String, callback: PictureCallback) {
        cameraDevice?.let {
            if (previewState != STATE_DESTROY && previewState != STATE_PICTURE) {
                CameraLog.i(TAG, "picture")
                val captureBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, controller.getFocusMode())
                captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, controller.params.rotation)
                captureBuilder.set(CaptureRequest.JPEG_QUALITY, 100.toByte())
                sessionMap[previewId] ?: throw RuntimeException("current id is not in previewing!")
                val imageReader = sessionMap[previewId]!!.imageReader
                val listener = sessionMap[previewId]!!.imageReaderListener
                sessionMap[previewId]!!.viewSurface?.let { it1 -> captureBuilder.addTarget(it1) }
                imageReader?.surface?.let { it1 -> captureBuilder.addTarget(it1) }
                val captureRequest = captureBuilder.build()
                captureSession?.let {
                    captureSession?.stopRepeating()
                    it.captureSingleRequest(captureRequest, executor, object : CameraCaptureSession.CaptureCallback() {
                        override fun onCaptureStarted(session: CameraCaptureSession, request: CaptureRequest, timestamp: Long, frameNumber: Long) {
                            super.onCaptureStarted(session, request, timestamp, frameNumber)
                            CameraLog.d(TAG, "onCaptureStarted")
                            listener?.pictureCallback = callback
                            handler.post { mediaActionSound.play(MediaActionSound.SHUTTER_CLICK) }
                        }

                        override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                            CameraLog.d(TAG, "onCaptureCompleted")
                            captureResults.put(result)
                            setState(STATE_PREVIEWING)
                        }

                        override fun onCaptureFailed(session: CameraCaptureSession, request: CaptureRequest, failure: CaptureFailure) {
                            super.onCaptureFailed(session, request, failure)
                            CameraLog.d(TAG, "onCaptureFailed")
                            captureResults.put(null)
                            setState(STATE_PREVIEWING)
                        }
                    })
                }
                setState(STATE_PICTURE)
            }
        }
    }

    override fun close() {
        CameraLog.i(TAG, "close")
        if (cameraDevice == null) {
            return
        }
        for ((_, value) in sessionMap.entries) {
            value.release()
        }
        sessionMap.clear()
        stopPreview()
        cameraDevice?.close()
        cameraDevice = null
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun preview(preview: Preview) {
        val previewList = arrayListOf<Preview>()
        previewList.add(preview)
        preview(previewList)
    }

    override fun preview(previewList: List<Preview>) {
        cameraDevice?.let {
            if (previewState == STATE_PREVIEWING) {
                return
            }
            previewBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            val outputs = mutableListOf<OutputConfiguration>()
            for (preview in previewList) {
                val camera2Session = Camera2Session()
                val surfaceList = mutableListOf<Surface>()
                val imageReader = createImageReader(ImageFormat.YUV_420_888, controller.params.previewSize)
                val listener = InnerImageAvailableListener()
                listener.previewCallback = preview.callback
                imageReader.setOnImageAvailableListener(listener, handler)
                val imageSurface = imageReader.surface
                surfaceList.add(imageSurface)
                camera2Session.imageReader = imageReader
                camera2Session.imageReaderListener = listener
                val viewSurface = when (preview.surface) {
                    is SurfaceHolder -> preview.surface.surface
                    is SurfaceTexture -> Surface(preview.surface)
                    else -> throw CameraException(CameraException.ERR_ARG, "T must SurfaceHolder or SurfaceTexture!")
                }
                surfaceList.add(viewSurface)
                camera2Session.viewSurface = viewSurface
                for (surface in surfaceList) {
                    val outputConfiguration = OutputConfiguration(surface)
                    if (preview.physicalId != null) {
                        outputConfiguration.setPhysicalCameraId(preview.physicalId)
                    }
                    outputs.add(outputConfiguration)
                    previewBuilder?.addTarget(surface)
                }
                sessionMap[preview.id] = camera2Session
            }
            preview(outputs)
        }
    }

    private fun createImageReader(imageFormat: Int, size: Size): ImageReader {
        val streamConfigurationMap = cameraCharacteristics!![CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]

        return if (streamConfigurationMap?.isOutputSupportedFor(imageFormat) == true) {
            ImageReader.newInstance(size.width, size.height, imageFormat, 2)
        } else {
            throw CameraException(CameraException.ERR_RUNTIME, "Current device doesn't support YUV_420_888!")
        }
    }

    private fun preview(outputs: MutableList<OutputConfiguration>) {
        val callback = object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                CameraLog.d(TAG, "onConfigured")
                captureSession = session
                requestPreview()
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                session.close()
                previewState = STATE_DESTROY
                captureSession = null
            }
        }
        cameraDevice!!.createCaptureSession(
            SessionConfiguration(
                SessionConfiguration.SESSION_REGULAR,
                outputs,
                executor,
                callback
            )
        )
    }

    private fun requestPreview() {
        previewBuilder?.let {
            previewBuilder?.set(CaptureRequest.JPEG_ORIENTATION, controller.params.rotation)
            val supportedFocus = cameraCharacteristics!!.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE)
            if ((supportedFocus != null) && (supportedFocus > 0)) {
                previewBuilder?.set(CaptureRequest.CONTROL_AF_MODE, controller.getFocusMode())
            }
            controller.applyZoom(cameraCharacteristics!!, previewBuilder!!)
            val previewRequest = previewBuilder?.build()
            val session = captureSession
            if ((previewRequest != null) && (session != null)) {
                previewState = STATE_PREVIEWING
                session.setRepeatingRequest(previewRequest, null, handler)
            }
        }
    }

    private inner class Camera2Session {
        var imageReader: ImageReader? = null
        var imageReaderListener: InnerImageAvailableListener? = null
        var viewSurface: Surface? = null

        fun release() {
            imageReader?.close()
            imageReaderListener = null
            imageReader = null
        }
    }

    private inner class StateCallback : CameraDevice.StateCallback() {
        @RequiresApi(Build.VERSION_CODES.P)
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraDevice!!.id)
            initParams()
            cameraStateCallback?.onOpened(this@Camera2Proxy)
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraStateCallback?.onDisconnected()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraStateCallback?.onError(error)
        }

        override fun onClosed(camera: CameraDevice) {
            cameraStateCallback?.onClosed()
        }
    }

    private inner class InnerImageAvailableListener : ImageReader.OnImageAvailableListener {
        var previewCallback: PreviewCallback? = null
        var pictureCallback: PictureCallback? = null
        override fun onImageAvailable(reader: ImageReader) {
            val isAutoRead = controller.params.isCamera2AutoReadImage
            if (isAutoRead != false) {
                val image = reader.acquireLatestImage()
                image?.let {
                    val size = Size(it.width, it.height)
                    var format = it.format
                    val data: ByteArray? = try {
                        when (format) {
                            ImageFormat.JPEG -> {
                                CameraLog.d(TAG, "onImageAvailable JPEG")
                                val byteBuffer = image.planes[0].buffer
                                val d = ByteArray(byteBuffer.remaining())
                                byteBuffer.get(d)
                                d
                            }
                            ImageFormat.YUV_420_888 -> {
                                format = ImageFormat.NV21
                                ImageUtil.getDataFromYUVImage(image, ImageUtil.COLOR_FORMAT_NV21)
                            }
                            else -> null
                        }
                    } catch (e: Exception) {
                        CameraLog.e(TAG, "onImageAvailable transcoding failed!")
                        null
                    } finally {
                        try {
                            it.close()
                        } catch (e: Exception) {
                            CameraLog.w(TAG, "onImageAvailable image close exception. ${e.message}")
                        }
                    }
                    if (previewState == STATE_PICTURE) {
                        if (pictureCallback != null) {
                            pictureCallback?.onPictureTaken(data, size, format)
                            pictureCallback = null
                        } else {

                        }
                    } else {
                        previewCallback?.onPreviewFrame(data, size, format)
                    }
                }
            } else {
                if (previewState == STATE_PICTURE) {
                    if (pictureCallback != null) {
                        pictureCallback?.onPictureTaken(reader)
                        pictureCallback = null
                    }
                } else {
                    previewCallback?.onPreviewFrame(reader)
                }
            }
        }
    }
}