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

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest
import android.os.Build
import androidx.annotation.RequiresApi
import com.ahahahq.cameraz.common.FOCUS_MODE_PICTURE

@RequiresApi(Build.VERSION_CODES.R)
internal class Camera2ControllerR : Camera2ControllerP() {

    override fun initParams(characteristics: CameraCharacteristics) {
        params.focusMode = FOCUS_MODE_PICTURE
        val max = characteristics.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)?.upper
        val min = characteristics.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)?.lower
        if ((max == null) || (min == null)) {
            super.initParams(characteristics)
        } else {
            params.setZoomSupport(true)
            params.zoomRatio = 1F
            params.setMaxZoom(max)
            params.setMinZoom(min)
        }
    }

    override fun setZoom(characteristics: CameraCharacteristics, ratio: Float) {
        if (!params.getZoomSupport()) {
            return
        }
        val min = characteristics.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)!!.lower
        val max = characteristics.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)!!.upper
        if ((max == null) || (min == null)) {
            super.setZoom(characteristics, ratio)
        } else {
            var zoom = ratio
            if (max <= 0) {
                return
            }
            if (ratio > max) {
                zoom = max
            }
            if (ratio < min) {
                zoom = min
            }
            params.zoomRatio = zoom
        }
    }

    override fun applyZoom(characteristics: CameraCharacteristics, requestBuilder: CaptureRequest.Builder) {
        if (!params.getZoomSupport()) {
            return
        }
        val min = characteristics.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)!!.lower
        val max = characteristics.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)!!.upper
        if ((max == null) || (min == null)) {
            super.applyZoom(characteristics, requestBuilder)
        } else {
            params.zoomRatio?.let { requestBuilder.set(CaptureRequest.CONTROL_ZOOM_RATIO, it) }
        }
    }

}