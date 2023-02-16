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
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Size
import androidx.annotation.RequiresApi

data class DualCamera(val logicalId: String, val physicalId1: String, val physicalId2: String)

object Camera2ConfigUtil {
    private const val TAG = "Camera2ConfigUtil"

    /**
     * camera2 only support level > INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY
     * Determine whether the current device supports #HARDWARE_LEVEL.
     */
    fun isHardwareLevelSupported(characteristics: CameraCharacteristics, requiredLevel: Int): Boolean {
        val sortedLevels = intArrayOf(
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY,
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL,
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED,
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL,
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3,
        )
        val deviceLevel = characteristics[CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL]
        CameraLog.i(TAG, "isHardwareLevelSupported deviceLevel: $deviceLevel")
        if (requiredLevel == deviceLevel) {
            return true
        }
        for (sortedLevel in sortedLevels) {
            if (requiredLevel == sortedLevel) {
                return true
            } else if (deviceLevel == sortedLevel) {
                return false
            }
        }
        return false
    }

    @JvmStatic
    fun getFacingMultiCameraList(cameraManager: CameraManager, facing: Int? = CameraCharacteristics.LENS_FACING_BACK): List<String> {
        val idList: ArrayList<String> = arrayListOf()
        cameraManager.cameraIdList.map {
            Pair(cameraManager.getCameraCharacteristics(it), it)
        }.filter {
            facing == null || it.first.get(CameraCharacteristics.LENS_FACING) == facing
        }.filter {
            it.first.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)!!.contains(
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA
            )
        }.forEach {
            idList.add(it.second)
        }
        return idList
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun getPhysicalCameraIds(cameraCharacteristics: CameraCharacteristics): List<String> {
        return cameraCharacteristics.physicalCameraIds.toList()
    }

    fun getBackCameraList(cameraManager: CameraManager): List<String> {
        val idList: ArrayList<String> = arrayListOf()
        val cameraIdList = cameraManager.cameraIdList
        if (cameraIdList.isEmpty()) {
            return idList
        }
        for (id in cameraIdList) {
            if (isSpecificCameraType(cameraManager, id, CameraCharacteristics.LENS_FACING_BACK)) {
                idList.add(id)
            }
        }
        return idList
    }

    fun getFrontCameraList(cameraManager: CameraManager): List<String> {
        val idList: ArrayList<String> = arrayListOf()
        val cameraIdList = cameraManager.cameraIdList
        if (cameraIdList.isEmpty()) {
            return idList
        }
        for (id in cameraIdList) {
            if (isSpecificCameraType(cameraManager, id, CameraCharacteristics.LENS_FACING_FRONT)) {
                idList.add(id)
            }
        }
        return idList
    }

    fun isSpecificCameraType(cameraManager: CameraManager, id: String, type: Int): Boolean {
        val cameraCharacteristics = cameraManager.getCameraCharacteristics(id)
        if (cameraCharacteristics[CameraCharacteristics.LENS_FACING] == type) {
            return true
        }
        return false
    }

    /**
     * Calculate the angle that the image taken by the camera needs to be rotated when it is normally displayed on the screen.
     */
    fun getRotationFromDisplayToCamera(context: Context, cameraCharacteristics: CameraCharacteristics): Int {
        val cwRotationFromNaturalToDisplay = Util.getRotationFromNaturalToDisplay(context)
        val cwRotationFromNaturalToCamera = cameraCharacteristics[CameraCharacteristics.SENSOR_ORIENTATION]!!
        val result = if (cameraCharacteristics[CameraCharacteristics.LENS_FACING] == CameraCharacteristics.LENS_FACING_FRONT) {
            (360 - (cwRotationFromNaturalToCamera + cwRotationFromNaturalToDisplay) % 360) % 360
        } else {
            (cwRotationFromNaturalToCamera - cwRotationFromNaturalToDisplay + 360) % 360
        }
        CameraLog.i(TAG, "getRotationFromDisplayToCamera: $result")
        return result
    }

    /**
     * Calculate the best camera preview resolution.
     */
    fun getBestFullScreenCameraResolution(
        context: Context,
        cameraCharacteristics: CameraCharacteristics,
        format: Int,
        isRightAngle: Boolean
    ): Size {
        val screenResolution = Util.getScreenResolution(context)
        val screenSize = Size(screenResolution.x, screenResolution.y)
        return getBestCameraResolution(cameraCharacteristics, format, isRightAngle, screenSize, true)
    }

    fun getBestCameraResolution(
        cameraCharacteristics: CameraCharacteristics,
        format: Int,
        isRightAngle: Boolean,
        targetSize: Size,
        autoAdapt: Boolean = true
    ): Size {
        val streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val supportedSizes = streamConfigurationMap?.getOutputSizes(format)
        supportedSizes ?: throw IllegalStateException("Parameters contained no preview size!")
        // Some devices support camera2, However, the supported preview resolution can only be consistent with the ratio of the sensor
        // to ensure that there will be no preview stretching problems. It may be that the vendor's adaptation to camera2 is not friendly enough,
        // Maybe only HAL supports INFO_SUPPORTED_HARDWARE_LEVEL_FULL in order to be used normally.
        // Therefore, the expected preview size is recalculated to the size consistent with the sensor ratio here.
        var expectSize = targetSize
        if (autoAdapt) {
            val activeArraySize = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
            activeArraySize?.let {
//            val results = MathUtil.getSameSlopSize(supportedSizes, Size(activeArraySize.width(), activeArraySize.height()), 0.05F)
//            if (results.isNotEmpty()) {
//                val step1Result = results[0]
//                logd(TAG, "getBestCameraResolution step1 sensor size, result: $step1Result")
//                return step1Result
//            }
                val slop = if (isRightAngle) {
                    activeArraySize.width().toFloat() / activeArraySize.height().toFloat()
                } else {
                    activeArraySize.height().toFloat() / activeArraySize.width().toFloat()
                }
                expectSize = MathUtil.transToSlopSize(expectSize, slop)
            }
        }
        val result = Util.getNearestSize(supportedSizes, expectSize, isRightAngle)
        result ?: throw IllegalStateException("Parameters contained no preview size!")
        CameraLog.i(TAG, "getBestCameraResolution step2 target size, target: $targetSize expect: $expectSize result: [$result] ")
        return result
    }

    /**
     * quote: {@link https://medium.com/androiddevelopers/getting-the-most-from-the-new-multi-camera-api-5155fb3d77d9}
     * Find the pair of physical cameras that support dual opening with the longest focal length and the shortest focal length.
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun findShortLongCameraPair(manager: CameraManager, facing: Int? = CameraCharacteristics.LENS_FACING_BACK): DualCamera? {

        return findDualCameras(manager, facing).map {
            val characteristics1 = manager.getCameraCharacteristics(it.physicalId1)
            val characteristics2 = manager.getCameraCharacteristics(it.physicalId2)

            // 查询每个物理摄像头公布的焦距
            val focalLengths1 = characteristics1.get(
                CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS
            ) ?: floatArrayOf(0F)
            val focalLengths2 = characteristics2.get(
                CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS
            ) ?: floatArrayOf(0F)

            // 计算相机之间最小焦距和最大焦距之间的最大差异
            val focalLengthsDiff1 = focalLengths2.maxOrNull()!! - focalLengths1.minOrNull()!!
            val focalLengthsDiff2 = focalLengths1.maxOrNull()!! - focalLengths2.minOrNull()!!

            // 返回相机 ID 和最小焦距与最大焦距之间的差值
            if (focalLengthsDiff1 < focalLengthsDiff2) {
                Pair(DualCamera(it.logicalId, it.physicalId1, it.physicalId2), focalLengthsDiff1)
            } else {
                Pair(DualCamera(it.logicalId, it.physicalId2, it.physicalId1), focalLengthsDiff2)
            }

            // 只返回差异最大的对，如果没有找到对，则返回 null
        }.sortedBy { it.second }.reversed().lastOrNull()?.first
    }

    /**
     * quote: {@link https://medium.com/androiddevelopers/getting-the-most-from-the-new-multi-camera-api-5155fb3d77d9}
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun findDualCameras(manager: CameraManager, facing: Int? = CameraCharacteristics.LENS_FACING_BACK): Array<DualCamera> {
        val dualCameras = ArrayList<DualCamera>()

        // 遍历所有可用的摄像头特征
        manager.cameraIdList.map {
            Pair(manager.getCameraCharacteristics(it), it)
        }.filter {
            // 通过摄像头的方向这个请求参数进行过滤
            (facing == null) || it.first.get(CameraCharacteristics.LENS_FACING) == facing
        }.filter {
            // 逻辑摄像头过滤
            it.first.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)!!.contains(
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA
            )
        }.forEach {
            // 物理摄像头列表中的所有可能对都是有效结果
            // 注意：可能有 N 个物理摄像头作为逻辑摄像头分组的一部分
            val physicalCameras = it.first.physicalCameraIds.toTypedArray()
            for (idx1 in physicalCameras.indices) {
                for (idx2 in (idx1 + 1) until physicalCameras.size) {
                    dualCameras.add(
                        DualCamera(
                            it.second, physicalCameras[idx1], physicalCameras[idx2]
                        )
                    )
                }
            }
        }

        return dualCameras.toTypedArray()
    }

    fun findMaxOrMinSensorId(manager: CameraManager, isMax: Boolean, facing: Int? = CameraCharacteristics.LENS_FACING_BACK): String? {
        val list = manager.cameraIdList.map {
            Pair(manager.getCameraCharacteristics(it), it)
        }.filter {
            (facing == null) || it.first.get(CameraCharacteristics.LENS_FACING) == facing
        }.sortedBy {
            it.first.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)?.width
        }
        return if (isMax) {
            list.lastOrNull()?.second
        } else {
            list.firstOrNull()?.second
        }
    }

    fun findMaxOrMinFocalId(manager: CameraManager, isMax: Boolean, facing: Int? = CameraCharacteristics.LENS_FACING_BACK): String? {
        val list = manager.cameraIdList.map {
            Pair(manager.getCameraCharacteristics(it), it)
        }.filter {
            (facing == null) || it.first.get(CameraCharacteristics.LENS_FACING) == facing
        }.sortedBy {
            it.first.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.maxOrNull()
        }
        return if (isMax) {
            list.lastOrNull()?.second
        } else {
            list.firstOrNull()?.second
        }
    }
}