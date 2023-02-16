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

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Handler
import android.util.Size
import android.view.SurfaceHolder
import com.ahahahq.cameraz.callback.PictureCallback
import com.ahahahq.cameraz.controller.Camera1ControllerCompat
import com.ahahahq.cameraz.controller.CompatFactory
import com.ahahahq.cameraz.model.CameraParams
import com.ahahahq.cameraz.model.Preview
import com.ahahahq.cameraz.util.CameraConfigUtil
import com.ahahahq.cameraz.util.Util
import com.ahahahq.cameraz.util.CameraLog

internal class Camera1Proxy(context: Context, handler: Handler) : CameraProxy(context, handler) {
    @Volatile
    private var camera: Camera? = null
    private var cameraId: Int? = null
    private var cameraInfo: Camera.CameraInfo? = null
    private var previewCallback: com.ahahahq.cameraz.callback.PreviewCallback? = null
    private var controller: Camera1ControllerCompat = CompatFactory.getCamera1Controller()

    override fun open(id: String, stateCallback: CameraProxyStateCallback) {
        var cId = id.toInt()
        val numCameras = Camera.getNumberOfCameras()
        if (numCameras == 0) {
            throw CameraException(CameraException.ERR_DEVICE, "No cameras in current device!")
        }
        if ((camera != null) && (cameraId == cId)) {
            CameraLog.i(TAG, "camera #$cId has opened.")
            return
        }
        cameraStateCallback = stateCallback
        CameraLog.i(TAG, "opening camera #$cId")

        val cameraInfo = Camera.CameraInfo()
        Camera.getCameraInfo(cId, cameraInfo)
        camera = Camera.open(cId)
        if (camera != null) {
            cameraId = cId
            this.cameraInfo = cameraInfo
            initParams()
            handler.post { cameraStateCallback?.onOpened(this) }
        } else {
            handler.post {
                cameraStateCallback?.onError(
                    CameraException(
                        CameraException.ERR_DEVICE,
                        "Open camera failed."
                    )
                )
            }
        }
    }

    override fun getId(): String {
        return cameraId!!.toString()
    }

    override fun setState(state: Int) {
        CameraLog.i(TAG, "setState pre: $previewState current: $state")
        when (state) {
            STATE_DESTROY -> stopPreview()
            STATE_PREVIEWING -> {
                if (previewState != STATE_PREVIEWING) {
                    camera?.let {
                        previewState = STATE_PREVIEWING
                        it.startPreview()
                        requestFrame()
                    }
                }
            }
            STATE_STOP -> {
                if (previewState == STATE_PREVIEWING) {
                    camera?.let {
                        previewState = STATE_STOP
                        it.stopPreview()
                    }
                }
            }
            STATE_PICTURE -> {
                previewState = STATE_PICTURE
            }
        }
    }

    override fun setParams(params: CameraParams) {
        val parameters = camera?.parameters
        params.isCamera2AutoReadImage?.let { controller.params.isCamera2AutoReadImage = it }
        parameters?.let {
            params.zoomRatio?.let { it1 -> controller.setZoom(it, it1) }
            params.rotation?.let { it1 -> controller.params.rotation = it1 }
            var previewSize = Size(params.previewSize.width, params.previewSize.height)
            if ((previewSize.width > 0) && (previewSize.height > 0)) {
                controller.params.previewSize = CameraConfigUtil.getBestCameraResolution(
                    camera!!,
                    Util.isRightAngle(controller.params.rotation!!),
                    previewSize
                )
            }
            var pictureSize = Size(params.pictureSize.width, params.pictureSize.height)
            if ((pictureSize.width > 0) && (pictureSize.height > 0)) {
                controller.params.pictureSize = CameraConfigUtil.getBestCameraResolution(
                    camera!!,
                    Util.isRightAngle(controller.params.rotation!!),
                    pictureSize,
                    1
                )
            }
            applyParameters()
        }
    }

    private fun applyParameters() {
        val parameters = camera?.parameters
        parameters?.let {
            controller.params.apply {
                if ((previewSize.width > 0) && (previewSize.height > 0)) {
                    it.setPreviewSize(previewSize.width, previewSize.height)
                }
                if ((pictureSize.width > 0) && (pictureSize.height > 0)) {
                    it.setPictureSize(pictureSize.width, pictureSize.height)
                }
                it.jpegQuality = 100
                rotation?.let { it1 -> it.setRotation(it1) }
                val focusMode = controller.getFocusMode()
                if (it.supportedFocusModes.contains(focusMode)) {
                    it.focusMode = focusMode
                }
                if ((zoomRatio != null) && getZoomSupport()) {
                    it.zoom = zoomRatio!!.toInt()
                }
                CameraLog.d(TAG, "applyParameters $this")
            }
            try {
                camera?.parameters = it
            } catch (e: Exception) {
                CameraLog.e(TAG, "applyParameters failed: $e")
            }
        }
    }

    override fun getParams(): CameraParams {
        return controller.params.clone()
    }

    private fun initParams() {
        camera ?: throw CameraException(CameraException.ERR_RUNTIME, "camera is null!")
        controller.initParams(camera!!.parameters)
        controller.params.apply {
            rotation = CameraConfigUtil.getRotationFromDisplayToCamera(context, cameraInfo!!)
            previewSize = CameraConfigUtil.getBestFullScreenCameraResolution(
                context,
                camera!!,
                Util.isRightAngle(rotation!!)
            )
            pictureSize = CameraConfigUtil.getBestFullScreenCameraResolution(
                context,
                camera!!,
                Util.isRightAngle(rotation!!),
                1
            )
        }
    }

    private fun stopPreview() {
        if (previewState == STATE_DESTROY) {
            CameraLog.w(TAG, "preview has already destroyed")
            return
        }
        try {
            previewState = STATE_DESTROY
            camera?.stopPreview()
        } catch (e: RuntimeException) {

        }
    }

    override fun picture(id: String, callback: PictureCallback) {
        camera?.let {
            if (previewState != STATE_DESTROY && previewState != STATE_PICTURE) {
                CameraLog.i(TAG, "picture")
                setState(STATE_PICTURE)
                it.takePicture({ CameraLog.d(TAG, "onShutter") }, null, { data, c ->
                    val parameters = it.parameters
                    val format = parameters.pictureFormat
                    val size = Size(parameters.pictureSize.width, parameters.pictureSize.height)
                    callback.onPictureTaken(data, size, format)
                    setState(STATE_PREVIEWING)
                })
            }
        }
    }

    override fun close() {
        CameraLog.i(TAG, "close")
        if (camera == null) {
            return
        }
        stopPreview()
        try {
            camera?.release()
        } catch (e: Exception) {

        }
        camera = null
        handler.post { cameraStateCallback?.onClosed() }
    }

    override fun preview(preview: Preview) {
        camera?.let {
            if (previewState == STATE_PREVIEWING) {
                return
            }
            try {
                applyParameters()
                camera?.setDisplayOrientation(controller.params.rotation!!)
                when (preview.surface) {
                    is SurfaceHolder -> it.setPreviewDisplay(preview.surface)
                    is SurfaceTexture -> it.setPreviewTexture(preview.surface)
                    else -> throw CameraException(
                        CameraException.ERR_ARG,
                        "T must SurfaceHolder or SurfaceTexture!"
                    )
                }
                setState(STATE_PREVIEWING)
                previewCallback = preview.callback
                requestFrame()
            } catch (e: Exception) {
                CameraLog.e(TAG, "preview err. ${e.message}")
            }
        }
    }

    override fun preview(previewList: List<Preview>) {
        if (previewList.isEmpty()) {
            return
        }
        preview(previewList[0])
    }

    fun requestFrame() {
        if (previewState == STATE_PREVIEWING) {
            camera?.setOneShotPreviewCallback(PreviewCallback())
        }
    }

    private inner class PreviewCallback : Camera.PreviewCallback {

        override fun onPreviewFrame(data: ByteArray?, c: Camera?) {
            camera?.let {
                try {
                    val parameters = it.parameters
                    val format = parameters.previewFormat
                    val size = Size(parameters.previewSize.width, parameters.previewSize.height)
                    previewCallback?.onPreviewFrame(data, size, format)
                    requestFrame()
                } catch (e: Exception) {

                }
            }
        }
    }

    companion object {
        private const val TAG = "Camera1Proxy"
    }
}