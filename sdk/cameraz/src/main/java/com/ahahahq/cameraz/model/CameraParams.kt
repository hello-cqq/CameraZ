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
package com.ahahahq.cameraz.model

import android.util.Size
import com.ahahahq.cameraz.common.FOCUS_MODE_AUTO

/**
 * Camera parameters, used to preview, take photos, and record.
 * @param zoomRatio Camera zoom scale
 */
data class CameraParams private constructor(
    var focusMode: Int,
    var zoomRatio: Float?,
    var rotation: Int?,
    var previewSize: Size,
    var pictureSize: Size,
    var isCamera2AutoReadImage: Boolean?
) {
    private var isZoomSupported: Boolean = false
    private var maxZoomRatio: Float? = null
    private var minZoomRatio: Float? = null

    companion object {

        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    class Builder {
        var focusMode: Int = FOCUS_MODE_AUTO
        var zoomRatio: Float? = null
        var rotation: Int? = null
        var previewSize: Size = Size(0, 0)
        var pictureSize: Size = Size(0, 0)
        var isCamera2AutoReadImage: Boolean? = null
        fun build() = CameraParams(focusMode, zoomRatio, rotation, previewSize, pictureSize, isCamera2AutoReadImage)
    }

    override fun toString(): String {
        return "CameraParams[" +
                "focusMode: $focusMode, " +
                "zoomRatio: $zoomRatio, " +
                "isZoomSupported: $isZoomSupported, " +
                "maxZoomRatio: $maxZoomRatio, " +
                "minZoomRatio: $minZoomRatio, " +
                "rotation: $rotation, " +
                "previewSize: $previewSize, " +
                "pictureSize: $pictureSize" +
                "isCamera2AutoReadImage: $isCamera2AutoReadImage" +
                "]"
    }

    internal fun setZoomSupport(isSupported: Boolean) {
        isZoomSupported = isSupported
    }

    fun getZoomSupport(): Boolean {
        return isZoomSupported
    }

    internal fun setMaxZoom(ratio: Float) {
        maxZoomRatio = ratio
    }

    fun getMaxZoom(): Float? {
        return maxZoomRatio
    }

    internal fun setMinZoom(ratio: Float) {
        minZoomRatio = ratio
    }

    fun getMinZoom(): Float? {
        return minZoomRatio
    }

    fun clone(): CameraParams {
        val params = this.copy()
        params.isZoomSupported = this.getZoomSupport()
        params.minZoomRatio = this.getMinZoom()
        params.maxZoomRatio = this.getMaxZoom()
        params.previewSize = Size(this.previewSize.width, this.previewSize.height)
        return params
    }

}