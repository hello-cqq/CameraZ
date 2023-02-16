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
import android.graphics.Point
import android.util.Size
import android.view.Surface
import android.view.WindowManager
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt


object Util {
    private const val TAG = "Util"
    private const val SCALE_SIZE = 1.5

    fun getRotationFromNaturalToDisplay(context: Context): Int {
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        val result = when (val displayRotation = display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else ->         // Have seen this return incorrect values like -90
                if (displayRotation % 90 == 0) {
                    (360 + displayRotation) % 360
                } else {
                    throw IllegalArgumentException("Bad rotation: $displayRotation")
                }
        }
        CameraLog.i(TAG, "getRotationFromNaturalToDisplay: $result")
        return result
    }

    fun getScreenResolution(context: Context): Point {
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        val screenResolution = Point()
        display.getSize(screenResolution)
        CameraLog.i(TAG, "Screen resolution in current orientation: $screenResolution")
        return screenResolution
    }

    fun getNearestSize(sampleArr: Array<Size>, data: Size, isRightAngle: Boolean): Size? {
        CameraLog.i(TAG, "getNearestSize screenSize: [$data]")
        var logStr = "getNearestSize sampleArr:"
        for (s in sampleArr) {
            logStr += " [$s]"
        }
        CameraLog.i(TAG, logStr)
        if (sampleArr.isEmpty()) {
            return null
        }
        var dataWidth = data.width
        var dataHeight = data.height
        if (isRightAngle) {
            dataWidth = data.height
            dataHeight = data.width
        }
        val tmpData = Size(dataWidth, dataHeight)
        val dataAspectRatio = dataWidth.toDouble() / dataHeight.toDouble()
        val aspectSize = mutableListOf<Size>()
        Arrays.sort(sampleArr) { p1, p2 -> if (calculate2VectorDistance(p1, tmpData) > calculate2VectorDistance(p2, tmpData)) 1 else -1 }
        for (s in sampleArr) {
            val x = s.width
            val y = s.height
            if ((x == dataWidth) && (y == dataHeight)) {
                return s
            }
            if ((x >= dataWidth) && (y >= dataHeight) && (x <= SCALE_SIZE * dataWidth) && (y <= SCALE_SIZE * dataHeight)) {
                aspectSize.add(s)
            }
        }
        if (aspectSize.isEmpty()) {
            return sampleArr[0]
        }
        aspectSize.sortWith { p1, p2 -> if (abs(p1.width.toDouble() / p1.height.toDouble() - dataAspectRatio) > abs(p2.width.toDouble() / p2.height.toDouble() - dataAspectRatio)) 1 else -1 }
        return aspectSize[0]
    }

    fun calculate2VectorDistance(a: Size, b: Size): Double {
        return calculateVectorLength(Size(a.width - b.width, a.height - b.height))
    }

    fun calculateVectorLength(size: Size): Double {
        val widthSquare = size.width.toDouble().pow(2)
        val heightSquare = size.height.toDouble().pow(2)
        return sqrt(widthSquare + heightSquare)
    }

    fun isRightAngle(rotation: Int): Boolean {
        return (rotation / 90) % 2 != 0
    }

}