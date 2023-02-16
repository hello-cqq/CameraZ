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
package com.ahahahq.cameraz.util

import android.content.Context
import android.hardware.Camera
import android.util.Size

object CameraConfigUtil {
    private const val TAG = "CameraConfigUtil"

    @JvmStatic
    fun getBackCameraList(): List<Int> {
        val idList: ArrayList<Int> = arrayListOf()
        val numCameras = Camera.getNumberOfCameras()
        if (numCameras <= 0) {
            return idList
        }
        for (id in 0 until numCameras) {
            if (isSpecificCameraType(id, Camera.CameraInfo.CAMERA_FACING_BACK)) {
                idList.add(id)
            }
        }
        return idList
    }

    @JvmStatic
    fun getFrontCameraList(): List<Int> {
        val idList: ArrayList<Int> = arrayListOf()
        val numCameras = Camera.getNumberOfCameras()
        if (numCameras <= 0) {
            return idList
        }
        for (id in 0 until numCameras) {
            if (isSpecificCameraType(id, Camera.CameraInfo.CAMERA_FACING_FRONT)) {
                idList.add(id)
            }
        }
        return idList
    }

    @JvmStatic
    fun isSpecificCameraType(id: Int, type: Int): Boolean {
        val cameraInfo = Camera.CameraInfo()
        Camera.getCameraInfo(id, cameraInfo)
        if (cameraInfo.facing == type) {
            return true
        }
        return false
    }

    /**
     * copy form Google API: Camera.java
     */
    @JvmStatic
    fun getRotationFromDisplayToCamera(context: Context, camera: Camera.CameraInfo): Int {
        val cwRotationFromNaturalToDisplay = Util.getRotationFromNaturalToDisplay(context)
        var cwRotationFromNaturalToCamera = camera.orientation
        val result = if (camera.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            (360 - (cwRotationFromNaturalToCamera + cwRotationFromNaturalToDisplay) % 360) % 360
        } else {
            (cwRotationFromNaturalToCamera - cwRotationFromNaturalToDisplay + 360) % 360
        }
        CameraLog.i(TAG, "getRotationFromDisplayToCamera: $result")
        return result
    }

    /**
     * copy from zxing code
     * cwRotationFromNaturalToDisplay: natural orientation, 0 in mobile phone
     * cwRotationFromNaturalToCamera: camera sensor orientation, FACE-BACK is 90 and FACE-FRONT is 270 in mobile phone common.
     * @param resType 0: preview; 1:picture
     */
    @JvmStatic
    @JvmOverloads
    fun getBestFullScreenCameraResolution(context: Context, camera: Camera, isRightAngle: Boolean, resType: Int = 0): Size {
        val screenResolution = Util.getScreenResolution(context)
        val screenSize = Size(screenResolution.x, screenResolution.y)
        return getBestCameraResolution(camera, isRightAngle, screenSize, resType)
    }

    /**
     *
     * @param camera Camera
     * @param isRightAngle Boolean
     * @param targetSize Size
     * @param resType Int 0: preview; 1:picture
     * @return Size
     */
    @JvmStatic
    @JvmOverloads
    fun getBestCameraResolution(camera: Camera, isRightAngle: Boolean, targetSize: Size, resType: Int = 0): Size {
        val parameters = camera.parameters
        val supportedSizes = if (resType == 0) {
            parameters.supportedPreviewSizes
        } else {
            parameters.supportedPictureSizes
        }
        val supportSizeArr = Array(supportedSizes.size) { i -> Size(supportedSizes[i].width, supportedSizes[i].height) }
        val result = Util.getNearestSize(supportSizeArr, targetSize, isRightAngle)
        CameraLog.i(TAG, "getBestCameraResolution res $resType. [$result]")
        return if (result == null) {
            val default = if (resType == 0) {
                parameters.previewSize
            } else {
                parameters.pictureSize
            }
            if (default == null) {
                throw IllegalStateException("Parameters contained no preview size!")
            } else {
                Size(default.width, default.height)
            }
        } else {
            result
        }
    }
}