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

import android.graphics.SurfaceTexture
import android.util.Size
import android.view.SurfaceHolder
import androidx.lifecycle.LifecycleOwner
import com.ahahahq.cameraz.callback.PreviewCallback
import com.ahahahq.cameraz.callback.PictureCallback
import com.ahahahq.cameraz.model.*
import com.ahahahq.cameraz.view.CameraView
import com.ahahahq.cameraz.util.CameraLog
import kotlinx.coroutines.*

/**
 * Camera client, perform actions such as previewing, taking pictures, etc.
 * Get through {@link CameraManager.open}
 */
class CameraClient internal constructor(private val proxy: CameraProxy) {
    var cameraState: CameraState = CameraState.CLOSED
    private var lifecycleObserver: LifecycleCamera = LifecycleCamera(this)
    private var previewJob: Job? = null

    /**
     * optional
     * Binding the lifecycle of the camera to the specified lifecycle to facilitate the automatic release of camera resources.
     */
    fun bind(lifecycleOwner: LifecycleOwner) {
        GlobalScope.launch(Dispatchers.Main) {
            lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        }
    }

    /**
     * start camera preview
     * @param view Use the packaged preview interface provided internally
     * @param id Required parameters, used to mark a unique preview
     * @param callback Preview frame callback, used for real-time code scanning, etc.
     */
    @JvmOverloads
    fun startPreview(view: CameraView, id: String, callback: PreviewCallback? = null) {
        if (checkJobActive()) {
            return
        }
        CameraLog.i(TAG, "startPreview")
        previewJob = GlobalScope.launch {
            val preview = createPreviewWithSurface(view, callback, id)
            preview?.let { proxy.preview(it) }
        }
    }

    /**
     * Start multiple preview screens through a series of previews
     */
    fun startPreview(vararg previewArr: Preview) {
        if (checkJobActive()) {
            return
        }
        CameraLog.i(TAG, "startPreview")
        previewJob = GlobalScope.launch {
            val configurationList = arrayListOf<Preview>()
            for (preview in previewArr) {
                if (preview.surface is CameraView) {
                    val previewWithSurface = createPreviewWithSurface(preview.surface, preview)
                    previewWithSurface?.let {
                        it.physicalId = preview.physicalId
                        configurationList.add(it)
                    }
                } else {
                    configurationList.add(preview)
                }
            }
            if (configurationList.isNotEmpty()) {
                proxy.preview(configurationList)
            }
        }
    }

    private fun checkJobActive(): Boolean {
        return previewJob?.isActive ?: false
    }

    private suspend fun createPreviewWithSurface(view: CameraView, preview: Preview): Preview? {
        return createPreviewWithSurface(view, preview.callback, preview.id)
    }

    private suspend fun createPreviewWithSurface(
        view: CameraView,
        callback: PreviewCallback?,
        id: String
    ): Preview? {
        CameraLog.i(TAG, "createPreviewWithSurface")
        val holderOrTexture = view.waitSurfaceAvailable()
        if (holderOrTexture == null) {
            /*throw CameraException(
                CameraException.ERR_RUNTIME,
                "Current surface has not created. Check if stopped."
            )*/
            CameraLog.e(TAG, "Current surface has not created. Check if stopped.")
            return null
        } else {
            try {
                checkAvailable(holderOrTexture)
                val params = proxy.getParams()
                view.applyCamera(this, params, proxy is Camera2Proxy)
                return Preview(holderOrTexture, id, callback)
            } catch (e: CameraException) {
                CameraLog.e(TAG, "createPreviewWithSurface $e")
                return null
            }
        }
    }

    private fun checkAvailable(holderOrTexture: Any?) {
        if (cameraState != CameraState.OPENED) {
            throw CameraException(
                CameraException.ERR_RUNTIME,
                "Current camera is not opened. Check it please."
            )
        }
        if ((holderOrTexture == null) || !((holderOrTexture is SurfaceHolder) || (holderOrTexture is SurfaceTexture))) {
            throw CameraException(
                CameraException.ERR_ARG,
                "holderOrTexture must be SurfaceHolder or SurfaceTexture!"
            )
        }
    }

    fun stopPreview() {
        proxy.setState(CameraProxy.STATE_STOP)
    }

    fun restartPreview() {
        proxy.setState(CameraProxy.STATE_PREVIEWING)
    }

    @JvmOverloads
    fun takePicture(cameraId: String, callback: PictureCallback) {
        if (checkJobActive()) {
            return
        }
        proxy.picture(cameraId, callback)
    }

    fun unbind(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
    }

    fun close() {
        proxy.close()
    }

    fun getParams(): CameraParams {
        return proxy.getParams()
    }

    fun zoomTo(ratio: Float) {
        proxy.setParams(CameraParams.build { zoomRatio = ratio })
    }

    fun setPreviewSize(width: Int, height: Int) {
        proxy.setParams(CameraParams.build { previewSize = Size(width, height) })
    }

    fun setRotation(rotation: Int) {
        proxy.setParams(CameraParams.build { this.rotation = rotation })
    }

    fun setCamera2AutoReadImageEnabled(enable: Boolean) {
        proxy.setParams(CameraParams.build { this.isCamera2AutoReadImage = enable })
    }

    enum class CameraState {
        OPENING,
        OPENED,
        DISCONNECTED,
        ERROR,
        CLOSED
    }

    companion object {
        private const val TAG = "CameraClient"
    }
}