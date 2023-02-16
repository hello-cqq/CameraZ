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

import android.content.Context
import android.util.AttributeSet
import android.util.Size
import android.view.SurfaceView
import com.ahahahq.cameraz.util.CameraLog

internal class CameraSurfaceView : SurfaceView {

    companion object {
        private const val TAG = "CameraSurfaceView"
    }

    private var ratioWidth = 0
    private var ratioHeight = 0

    constructor (context: Context) : this(context, null) {

    }

    constructor (context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {

    }

    constructor (context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {

    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param previewWidth  camera preview width
     * @param previewHeight camera preview height
     */
    fun updateSurface(previewWidth: Int, previewHeight: Int, rotation: Int) {
        require((previewWidth > 0) && (previewHeight > 0)) { "Size cannot be negative." }
        redrawCameraView(previewWidth, previewHeight, rotation)
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param previewWidth  camera preview width
     * @param previewHeight camera preview height
     */
    private fun redrawCameraView(previewWidth: Int, previewHeight: Int, rotation: Int) {
        if ((previewWidth < 0) || (previewHeight < 0)) {
            throw IllegalArgumentException("Size cannot be negative.")
        }
        if (rotation % 180 != 0) {
            ratioWidth = previewHeight
            ratioHeight = previewWidth
        } else {
            ratioWidth = previewWidth
            ratioHeight = previewHeight
        }
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if ((ratioWidth == 0) || (ratioHeight == 0)) {
            setMeasuredDimension(width, height)
        } else {
            val size = getAvailableSize1()
            setMeasuredDimension(size.width, size.height)
            CameraLog.d(TAG, "onMeasure available result: ${size.width}, ${size.height}")
        }
    }

    private fun getAvailableSize1(): Size {
        val pRatio = ratioWidth.toDouble() / ratioHeight.toDouble()
        val maxWidth: Int
        val maxHeight: Int
        if (ratioWidth.toDouble() / width.toDouble() > ratioHeight.toDouble() / height.toDouble()) {
            maxWidth = (height * pRatio).toInt()
            maxHeight = height
        } else {
            maxWidth = width
            maxHeight = (width / pRatio).toInt()
        }
        return Size(maxWidth, maxHeight)
    }
}