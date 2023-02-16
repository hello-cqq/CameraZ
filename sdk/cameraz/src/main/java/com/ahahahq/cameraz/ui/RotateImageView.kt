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
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.AppCompatImageView
import com.ahahahq.cameraz.common.ANGLE_180
import com.ahahahq.cameraz.common.ANGLE_360

/**
 * A @{code ImageView} which can rotate it's content.
 */
open class RotateImageView : AppCompatImageView {
    private var mCurrentDegree = 0 // [0, 359]
    private var mStartDegree = 0
    protected var degree = 0
    private var mbClockwise = false
    private var mbEnableAnimation = true
    private var mAnimationStartTime: Long = 0
    private var mAnimationEndTime: Long = 0

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {}

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return false
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return false
    }

    // Rotate the view counter-clockwise
    fun setOrientation(degree: Int, animation: Boolean) {
        var degree = degree
        mbEnableAnimation = if (visibility == View.VISIBLE) {
            animation
        } else {
            false
        }

        // make sure in the range of [0, 359]
        degree = if (degree >= 0) degree % ANGLE_360 else degree % ANGLE_360 + ANGLE_360
        if (degree == this.degree) {
            return
        }
        this.degree = degree
        if (mbEnableAnimation) {
            mStartDegree = mCurrentDegree
            mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis()
            var diff = this.degree - mCurrentDegree
            diff = if (diff >= 0) diff else ANGLE_360 + diff // make it in range [0, 359]

            // Make it in range [-179, 180]. That's the shorted distance between the two angles
            diff = if (diff > ANGLE_180) diff - ANGLE_360 else diff
            mbClockwise = diff >= 0
            mAnimationEndTime = mAnimationStartTime + Math.abs(diff) * ONE_SECOND / ANIMATION_SPEED
        } else {
            mCurrentDegree = this.degree
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val drawable: Drawable = drawable ?: return
        val bounds = drawable.bounds
        val w = bounds.right - bounds.left
        val h = bounds.bottom - bounds.top
        if (w == 0 || h == 0) {
            return  // nothing to draw
        }
        if (mCurrentDegree != degree) {
            val time = AnimationUtils.currentAnimationTimeMillis()
            if (time < mAnimationEndTime) {
                val deltaTime = (time - mAnimationStartTime).toInt()
                var degree = mStartDegree + ANIMATION_SPEED * (if (mbClockwise) deltaTime else -deltaTime) / 1000
                degree = if (degree >= 0) degree % ANGLE_360 else degree % ANGLE_360 + ANGLE_360
                mCurrentDegree = degree
                invalidate()
            } else {
                mCurrentDegree = degree
            }
        }
        val left: Int = paddingLeft
        val top: Int = paddingTop
        val right: Int = paddingRight
        val bottom: Int = paddingBottom
        val width: Int = width - left - right
        val height: Int = height - top - bottom
        val saveCount = canvas.saveCount

        // Scale down the image first if required.
        if (scaleType == ScaleType.FIT_CENTER && (width < w || height < h)) {
            val ratio = (width.toFloat() / w).coerceAtMost(height.toFloat() / h)
            canvas.scale(ratio, ratio, width / SCALE_TWO, height / SCALE_TWO)
        }
        canvas.translate(left + width / SCALE_TWO, top + height / SCALE_TWO)
        canvas.rotate(-mCurrentDegree.toFloat())
        canvas.translate(-w / SCALE_TWO, -h / SCALE_TWO)
        drawable.draw(canvas)
        canvas.restoreToCount(saveCount)
    }

    companion object {
        private const val TAG = "RotateImageView"
        private const val SCALE_TWO = 2.0f
        private const val ONE_SECOND = 1000
        private const val ANIMATION_SPEED = 270 // 270 deg/sec
    }
}