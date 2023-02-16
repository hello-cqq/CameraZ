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
package com.ahahahq.scandemo.common.widget

import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.Interpolator
import android.view.animation.PathInterpolator
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout

open class SoftCandyCard : FrameLayout {

    private var mPressAnimationInterpolator: Interpolator? = null
    private var mPressedFeedbackAnimation: ScaleAnimation? = null
    private var mPressedAnimator: ValueAnimator? = null

    private var mPressValue: Float = 0f

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private fun init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPressAnimationInterpolator =
                PathInterpolator(INTERPOLATOR_PATH_X[0], INTERPOLATOR_PATH_Y[0], INTERPOLATOR_PATH_X[1], INTERPOLATOR_PATH_Y[1])
        }
        mPressedAnimator = ValueAnimator.ofFloat(DEFAULT_CARD_START_SCALE, DEFAULT_CARD_FINAL_VALUE)
        mPressedAnimator?.apply {
            duration = NORMAL_ANIMATOR_DURATION
            interpolator = mPressAnimationInterpolator
            addUpdateListener { animation ->
                mPressValue = animation?.animatedValue as Float
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                animateToPressed()
            }

            MotionEvent.ACTION_UP -> {
                animateToNormal()
            }

            MotionEvent.ACTION_CANCEL -> {
                animateToNormal()
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onAnimationEnd() {
        super.onAnimationEnd()
        cancelAnimator()
    }

    private fun animateToPressed() {
        cancelAnimator()
        mPressedFeedbackAnimation = ScaleAnimation(
            DEFAULT_CARD_START_SCALE,
            DEFAULT_CARD_FINAL_VALUE,
            DEFAULT_CARD_START_SCALE,
            DEFAULT_CARD_FINAL_VALUE,
            (width / 2).toFloat(),
            (height / 2).toFloat()
        )
        mPressedFeedbackAnimation?.apply {
            duration = PRESS_ANIMATOR_DURATION
            interpolator = mPressAnimationInterpolator
            fillAfter = true
        }
        mPressedAnimator?.start()
        startAnimation(mPressedFeedbackAnimation)
    }

    private fun animateToNormal() {
        cancelAnimator()
        mPressedFeedbackAnimation = ScaleAnimation(
            mPressValue,
            DEFAULT_CARD_START_SCALE,
            mPressValue,
            DEFAULT_CARD_START_SCALE,
            (width / 2).toFloat(),
            (height / 2).toFloat()
        )
        mPressedFeedbackAnimation?.apply {
            duration = NORMAL_ANIMATOR_DURATION
            interpolator = mPressAnimationInterpolator
        }
        startAnimation(mPressedFeedbackAnimation)
    }

    private fun cancelAnimator() {
        if (mPressedAnimator?.isRunning!!) {
            mPressedAnimator!!.cancel()
        }
    }

    companion object {
        private const val TAG = "SoftCandyCard"
        private const val PRESS_ANIMATOR_DURATION = 200L
        private const val NORMAL_ANIMATOR_DURATION = 200L
        private const val DEFAULT_CARD_START_SCALE = 1.0f
        private const val DEFAULT_CARD_FINAL_VALUE = 0.9f
        private val INTERPOLATOR_PATH_X = floatArrayOf(0.4F, 0.2F)
        private val INTERPOLATOR_PATH_Y = floatArrayOf(0F, 1F)
    }
}