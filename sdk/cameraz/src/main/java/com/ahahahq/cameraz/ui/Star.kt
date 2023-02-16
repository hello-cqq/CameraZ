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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.animation.PathInterpolator

internal class Star(val view: StarView, private val maxSize: Int) {
    companion object {
        private const val TAG = "Star"
        private const val DURATION = 2000L
    }

    private var listener: OneSparklingListener? = null

    @Volatile
    private var isAnimStarted = false

    private var valueAnimator: ValueAnimator? = null
    fun init(listener: OneSparklingListener) {
        this.listener = listener
        if (valueAnimator == null) {
            valueAnimator = ValueAnimator.ofFloat(0F, 1F, 0F)
            valueAnimator?.apply {
                addUpdateListener { animation ->
                    val value = animation?.animatedValue as? Float
                    value?.let {
                        val lp = view.layoutParams
                        lp.width = (maxSize * it).toInt()
                        lp.height = (maxSize * it).toInt()
                        view.layoutParams = lp
                        view.alpha = it
                    }
                }
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        isAnimStarted = true
                        this@Star.listener?.onStart()
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        isAnimStarted = false
                        this@Star.listener?.onEnd()
                    }

                })
                interpolator = PathInterpolator(0.3F, 0F, 0.1F, 1F)
                duration = DURATION
            }
        }
    }

    fun start(delay: Long) {
        valueAnimator?.apply {
            startDelay = delay
            if (isPaused) {
                resume()
            } else if (!isAnimStarted) {
                start()
            }
        }
    }

    fun stop(release: Boolean) {
        valueAnimator?.apply {
            if (isStarted) {
                if (release) {
                    removeAllUpdateListeners()
                    removeAllListeners()
                    end()
                } else {
                    pause()
                }
            }
        }
    }

    interface OneSparklingListener {
        fun onStart()
        fun onEnd()
    }
}