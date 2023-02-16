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
package com.ahahahq.cameraz.controller

import android.graphics.Rect
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest
import com.ahahahq.cameraz.common.FOCUS_MODE_PICTURE

internal open class Camera2ControllerP : Camera2ControllerCompat() {
    override fun initParams(characteristics: CameraCharacteristics) {
        params.focusMode = FOCUS_MODE_PICTURE
        params.zoomRatio = 1F
        val maxRatio = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)
        if (maxRatio != null) {
            params.setZoomSupport(true)
            params.setMaxZoom(maxRatio)
            params.setMinZoom(0F)
        } else {
            params.setZoomSupport(false)
        }
    }

    override fun setZoom(characteristics: CameraCharacteristics, ratio: Float) {
        if (!params.getZoomSupport()) {
            return
        }
        val max = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)
        if ((max != null) && (max > 0)) {
            var zoom = ratio
            if (ratio > max) {
                zoom = max
            }
            if (ratio < 0) {
                zoom = 0F
            }
            params.zoomRatio = zoom
        }
    }

    override fun getFocusMode(): Int {
        return when (params.focusMode) {
            FOCUS_MODE_PICTURE -> CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            else -> CaptureRequest.CONTROL_AF_MODE_AUTO
        }
    }

    override fun applyZoom(characteristics: CameraCharacteristics, requestBuilder: CaptureRequest.Builder) {
        if (!params.getZoomSupport()) {
            return
        }
        val scale = params.zoomRatio
        val activeRect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
        if ((activeRect != null) && (scale != null)) {
            val diffWidth = (activeRect.width() * (scale - 1) / 2).toInt()
            val diffHeight = (activeRect.height() * (scale - 1) / 2).toInt()
            val zoomRect = Rect(
                activeRect.left + diffWidth,
                activeRect.top + diffHeight,
                activeRect.right - diffWidth,
                activeRect.bottom - diffHeight
            )
            requestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomRect)
        }
    }

}