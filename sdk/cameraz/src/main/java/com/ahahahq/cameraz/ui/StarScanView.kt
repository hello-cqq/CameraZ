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
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import com.ahahahq.cameraz.R
import com.ahahahq.cameraz.common.DISPLAY_AREA_ELLIPSE
import com.ahahahq.cameraz.common.DISPLAY_AREA_RECT
import com.ahahahq.cameraz.util.CameraLog
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.sqrt

class StarScanView : FrameLayout {
    companion object {
        private const val TAG = "StarScanView"
        private const val MAX_STAR_NUM = 20
        private const val STAR_SIZE = 20
        private const val DELAY_START = 1000
    }

    private val starList: ArrayList<Star> = ArrayList()
    private var isPlaying = false
    private val random = Random()

    private var displayArea: Int = DISPLAY_AREA_RECT
    private var starSize = STAR_SIZE
    private var starCount = MAX_STAR_NUM

    constructor (context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.StarScanView,
            defStyleAttr, defStyleRes
        )
        displayArea = typedArray.getInteger(R.styleable.StarScanView_displayArea, DISPLAY_AREA_RECT)
        starCount = typedArray.getInteger(R.styleable.StarScanView_starCount, MAX_STAR_NUM)
        starSize = typedArray.getDimension(R.styleable.StarScanView_starSize, STAR_SIZE.toFloat()).toInt()
        typedArray.recycle()
        initView()
    }

    private fun initView() {
        starList.clear()
        removeAllViews()
        CameraLog.d(TAG, "initView $width $height")
        for (i in 0 until starCount) {
            val starView = StarView(context)
            val lp = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            starView.layoutParams = lp
            val star = Star(starView, starSize)
            star.init(object : Star.OneSparklingListener {
                override fun onStart() {

                }

                override fun onEnd() {
                    if (isPlaying) {
                        move(starView)
                        star.start(random.nextInt(DELAY_START).toLong())
                    }
                }

            })
            starList.add(i, star)
            addView(starView, i)
        }
    }

    fun play() {
        if (!isPlaying) {
            isPlaying = true
            for (star in starList) {
                move(star.view)
                star.start(random.nextInt(DELAY_START).toLong())
            }
        }
    }

    fun stop(release: Boolean) {
        isPlaying = false
        for (star in starList) {
            star.stop(release)
        }
    }

    private fun move(view: StarView) {
        val w = width - STAR_SIZE
        val h = height - STAR_SIZE
        val moveX = random.nextInt(w).toFloat()
        view.x = moveX
        if (displayArea == DISPLAY_AREA_ELLIPSE) {
            val bool = random.nextBoolean()
            val yArea = (h * sqrt(abs(moveX / width - (moveX * moveX) / (width * width)))).toInt()
            val moveY = if (yArea == 0) {
                h / 2
            } else {
                if (bool) {
                    h / 2 + random.nextInt(yArea)
                } else {
                    h / 2 - random.nextInt(yArea)
                }
            }
            view.y = moveY.toFloat()
        } else {
            view.y = random.nextInt(h).toFloat()
        }
    }
}