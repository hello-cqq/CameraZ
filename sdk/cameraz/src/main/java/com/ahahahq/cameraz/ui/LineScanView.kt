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
package com.ahahahq.cameraz.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.ahahahq.cameraz.R
import com.ahahahq.cameraz.common.LINE_SCAN_TYPE_BLUE
import com.ahahahq.cameraz.common.LINE_SCAN_TYPE_GREEN
import com.ahahahq.cameraz.common.LINE_SCAN_TYPE_RED

class LineScanView : View {
    companion object {
        private const val TAG = "LineScanView"
        private const val SCAN_LINE_HEIGHT = 80
        private const val SPEED_DISTANCE_DEFAULT = 6
    }

    private var isFirst = true
    private var isPlaying = false
    private var lineBitmap: Bitmap? = null
    private val paint: Paint = Paint()
    private val bitmapRect = Rect()
    private var slideTop = 0
    private var speed = SPEED_DISTANCE_DEFAULT
    private var type = LINE_SCAN_TYPE_GREEN

    constructor (context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LineScanView,
            defStyleAttr, defStyleRes
        )
        speed = typedArray.getInteger(R.styleable.LineScanView_speed, SPEED_DISTANCE_DEFAULT)
        type = typedArray.getInteger(R.styleable.LineScanView_type, LINE_SCAN_TYPE_GREEN)
        lineBitmap = when (type) {
            LINE_SCAN_TYPE_RED -> BitmapFactory.decodeResource(resources, R.drawable.ic_scan_line_red)
            LINE_SCAN_TYPE_BLUE -> BitmapFactory.decodeResource(resources, R.drawable.ic_scan_line_blue)
            else -> BitmapFactory.decodeResource(resources, R.drawable.ic_scan_line_green)
        }
        paint.apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            isDither = true
        }
        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas?) {
        if (!isPlaying) {
            return
        }
        if (isFirst) {
            isFirst = false
            setLayerType(LAYER_TYPE_HARDWARE, null)
        }
        if (slideTop + SCAN_LINE_HEIGHT <= height) {
            bitmapRect.apply {
                left = 0
                top = slideTop
                right = this@LineScanView.width
                bottom = (slideTop + SCAN_LINE_HEIGHT)
            }
            lineBitmap?.let { canvas?.drawBitmap(it, null, bitmapRect, paint) }
            slideTop += speed
        } else {
            slideTop = 0
        }
        postInvalidate()
    }

    fun play() {
        if (!isPlaying) {
            isPlaying = true
            postInvalidate()
        }
    }

    fun stop() {
        isPlaying = false
    }
}