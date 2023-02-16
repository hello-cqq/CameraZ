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
import android.graphics.Outline
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.widget.FrameLayout
import com.ahahahq.cameraz.R
import com.ahahahq.cameraz.core.CameraClient
import com.ahahahq.cameraz.model.CameraParams
import com.ahahahq.cameraz.common.FloatPoint
import com.ahahahq.cameraz.common.MODE_SURFACE_VIEW
import com.ahahahq.cameraz.util.CameraLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class CameraView : FrameLayout {
    companion object {
        private const val TAG = "CameraView"
        const val MODE_DEFAULT = MODE_SURFACE_VIEW
        const val MAX_DELAY_FOR_PREVIEW = 5 * 1000L
    }

    var viewMode: Int = MODE_DEFAULT
    var autoZoom: Boolean = false
    var pinchToZoom: Boolean = false
    var draggable: Boolean = false
    var dragArea: Rect = Rect()
    private var radius = 0F
    private var previewAdapter: PreviewAdapter? = null
    private var client: CameraClient? = null
    private var pinchHolder: PinchEventHolder? = null
    private var dragHolder: DragEventHolder = DragEventHolder(FloatPoint(0F, 0F))

    constructor (context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CameraView,
            defStyleAttr, defStyleRes
        )
        viewMode = typedArray.getInteger(R.styleable.CameraView_viewMode, MODE_DEFAULT)
        autoZoom = typedArray.getBoolean(R.styleable.CameraView_autoZoom, false)
        pinchToZoom = typedArray.getBoolean(R.styleable.CameraView_pinchToZoom, false)
        draggable = typedArray.getBoolean(R.styleable.CameraView_draggable, false)
        radius = typedArray.getDimension(R.styleable.CameraView_radius, 0F)
        typedArray.recycle()
        initDragArea()
        initView()
    }

    private fun initView() {
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline?) {
                if (radius <= 0F) {
                    radius = 0F
                }
                outline?.setRoundRect(0, 0, view.measuredWidth, view.measuredHeight, radius)
            }
        }
        clipToOutline = true
        if (previewAdapter == null) {
            previewAdapter = PreviewFactory().create(this, viewMode)
        }
        previewAdapter!!.initView()
    }

    private fun initClient(client: CameraClient) {
        this.client = client
        pinchHolder = PinchEventHolder()
    }

    fun applyCamera(client: CameraClient, params: CameraParams, needRotation: Boolean) {
        GlobalScope.launch(Dispatchers.Main) {
            CameraLog.d(TAG, "applyCamera: ${params.previewSize} ${params.rotation} $needRotation")
            previewAdapter?.applyCameraView(params, needRotation)
            initClient(client)
        }
    }

    suspend fun waitSurfaceAvailable(): Any? {
        CameraLog.d(TAG, "waitSurfaceAvailable: begin")
        previewAdapter ?: return null
        var totalDelay = 0
        while (!previewAdapter!!.isAvailable()) {
            delay(10)
            totalDelay += 10
            if (totalDelay >= MAX_DELAY_FOR_PREVIEW) {
                break
            }
        }
        CameraLog.d(TAG, "waitSurfaceAvailable: end")
        return previewAdapter?.surfaceOrTexture
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val isPinch = (event.pointerCount == 2) && pinchToZoom && (client != null) && (client!!.cameraState == CameraClient.CameraState.OPENED) && client!!.getParams().getZoomSupport()
        val isDraggable = (event.pointerCount == 1) && draggable && (client != null) && (client!!.cameraState == CameraClient.CameraState.OPENED)
        if (isPinch) {
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_UP -> pinchHolder?.resetPoint()
                MotionEvent.ACTION_MOVE -> {
                    zoom(event)
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    val oldPoint1 = FloatPoint(event.getX(0), event.getY(0))
                    val oldPoint2 = FloatPoint(event.getX(1), event.getY(1))
                    pinchHolder?.updatePoint(oldPoint1, oldPoint2)
                }
            }
        }
        if (isDraggable) {
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> dragHolder.update(event.x, event.y)
                MotionEvent.ACTION_MOVE -> {
                    move(event)
                }
            }
        }
        return true
    }

    private fun zoom(event: MotionEvent) {
        client?.let {
            val newPoint1 = FloatPoint(event.getX(0), event.getY(0))
            val newPoint2 = FloatPoint(event.getX(1), event.getY(1))
            val diff = pinchHolder!!.calculateDiff(newPoint1, newPoint2)
            pinchHolder!!.updatePoint(newPoint1, newPoint2)
            val viewW = width
            val viewH = height
            val viewLen = sqrt((viewW * viewW + viewH * viewH).toDouble()).toFloat()
            val zoomRange = it.getParams().getMaxZoom()!! - it.getParams().getMinZoom()!!
            val newRatio = it.getParams().zoomRatio!! + diff / viewLen * zoomRange * 0.4F
//            logd(TAG, "zoom diff $diff viewLen $viewLen zoomRange $zoomRange newRatio $newRatio")
            it.zoomTo(newRatio)
        }
    }

    private fun move(event: MotionEvent) {
        val xDistance = event.x - dragHolder.getX()
        val yDistance = event.y - dragHolder.getY()
        var l = (left + xDistance).toInt()
        var r = l + width
        var t = (top + yDistance).toInt()
        var b = t + height
        if (l < dragArea.left) {
            l = dragArea.left
            r = l + width
        } else if (r > dragArea.right) {
            r = dragArea.right
            l = r - width
        }
        if (t < dragArea.top) {
            t = dragArea.top
            b = t + height
        } else if (b > dragArea.bottom) {
            b = dragArea.bottom
            t = b - height
        }
        layout(l, t, r, b)
    }

    private fun initDragArea() {
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val screenDisplay = manager.defaultDisplay
        dragArea = Rect(50, 50, screenDisplay.width - 50, screenDisplay.height - 50)
    }

    fun setRadius(r: Float) {
        if (r <= 0F) {
            return
        }
        radius = r
        invalidateOutline()
    }
}