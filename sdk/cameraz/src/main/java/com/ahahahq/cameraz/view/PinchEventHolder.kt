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
package com.ahahahq.cameraz.view

import com.ahahahq.cameraz.common.FloatPoint
import com.ahahahq.cameraz.util.MathUtil

class PinchEventHolder {
    var point1: FloatPoint = FloatPoint(0F, 0F)
    var point2: FloatPoint = FloatPoint(0F, 0F)


    fun updatePoint(p1: FloatPoint, p2: FloatPoint) {
        point1.x = p1.x
        point1.y = p1.y
        point2.x = p2.x
        point2.y = p2.y
    }

    fun resetPoint() {
        point1.reset()
        point2.reset()
    }

    fun calculateDiff(newPoint1: FloatPoint, newPoint2: FloatPoint): Float {
        val oldDist = MathUtil.calculateDistance(point1, point2)
        val newDist = MathUtil.calculateDistance(newPoint1, newPoint2)
        return newDist - oldDist
    }
}