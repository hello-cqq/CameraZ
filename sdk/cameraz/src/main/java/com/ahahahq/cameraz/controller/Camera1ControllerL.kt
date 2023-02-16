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

import android.hardware.Camera
import com.ahahahq.cameraz.common.FOCUS_MODE_PICTURE

internal open class Camera1ControllerL : Camera1ControllerCompat() {
    override fun initParams(parameters: Camera.Parameters) {
        params.focusMode = FOCUS_MODE_PICTURE
        params.zoomRatio = parameters.zoom.toFloat()
        if (parameters.isZoomSupported) {
            params.setZoomSupport(true)
            params.setMaxZoom(parameters.maxZoom.toFloat())
            params.setMinZoom(0F)
        } else {
            params.setZoomSupport(false)
        }
    }

    override fun setZoom(parameters: Camera.Parameters, ratio: Float) {
        if (params.getZoomSupport()) {
            val max = parameters.maxZoom
            if (max <= 0) {
                return
            }
            var zoom = ratio
            if (ratio > max) {
                zoom = max.toFloat()
            }
            if (ratio < 0) {
                zoom = 0F
            }
            params.zoomRatio = zoom
        }
    }

    override fun getFocusMode(): String {
        return when (params.focusMode) {
            FOCUS_MODE_PICTURE -> Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            else -> Camera.Parameters.FOCUS_MODE_AUTO
        }
    }
}