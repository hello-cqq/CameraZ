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

import android.graphics.Point
import android.util.Size
import com.ahahahq.cameraz.common.FloatPoint
import kotlin.math.abs
import kotlin.math.sqrt

object MathUtil {

    fun calculateDistance(point1: FloatPoint, point2: FloatPoint): Float {
        val diffX = point1.x - point2.x
        val diffY = point1.y - point2.y
        return sqrt((diffX * diffX + diffY * diffY).toDouble()).toFloat()
    }

    fun calculateGeometricCenter(points: Array<Point>): Point {
        var centerX = 0
        var centerY = 0
        for (point in points) {
            centerX += point.x
            centerY += point.y
        }
        centerX /= points.size
        centerY /= points.size
        return Point(centerX, centerY)
    }

    fun calculateScaleRatio(size1: Size, size2: Size): Float {
        val len1 = sqrt((size1.width * size1.width + size1.height * size1.height).toDouble())
        val len2 = sqrt((size2.width * size2.width + size2.height * size2.height).toDouble())
        return (len2 / len1).toFloat()
    }

    fun transToSlopSize(origin: Size, slop: Float): Size {
        val x0 = origin.width
        val y0 = origin.height
        val x = sqrt((x0 * x0 + y0 * y0).toFloat() / (1 + slop * slop))
        val y = x * slop
        return Size(x.toInt(), y.toInt())
    }

    fun getSameSlopSize(sampleArr: Array<Size>, data: Size, e: Float): Array<Size> {
        val sizeList = ArrayList<Size>()
        val ratioData = data.height.toFloat() / data.width.toFloat()
        for (size in sampleArr) {
            val ratioSample = size.height.toFloat() / size.width.toFloat()
            val diff = abs(ratioSample - ratioData)
            if ((diff == 0F) || (diff < e)) {
                sizeList.add(size)
            }
        }
        return sizeList.toTypedArray()
    }
}