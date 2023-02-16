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
package com.ahahahq.scandemo.common.utils

import android.R
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window

object StatusBarUtil {
    private const val TAG = "StatusBarUtil"
    fun initStatusBar(activity: Activity?) {
        if (activity != null) {
            setStatusBarTransparentAndBlackFont(activity, activity.window, true)
        }
    }

    private fun setStatusBarTransparentAndBlackFont(
        context: Context?,
        window: Window?,
        isFullScreen: Boolean
    ) {
        if (context == null || window == null) {
            return
        }
        val decorView = window.decorView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isFullScreen) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = context.getColor(R.color.white)
        }
        var flag = decorView.systemUiVisibility
        window.addFlags(Int.MIN_VALUE)
        if (isNightMode(context)) {
            flag = flag and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            flag = flag and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        } else {
            flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flag or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                //                flag |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            } else {
                flag or 16
            }
        }
        decorView.systemUiVisibility = flag
    }

    fun isNightMode(context: Context): Boolean {
        val configuration = context.resources.configuration
        val currentNightMode = configuration.uiMode and 48
        return 32 == currentNightMode
    }

    fun hideNavigationBar(window: Window?) {
        if (window == null) {
            return
        }
        val decoView = window.decorView
        val flag = (decoView.systemUiVisibility
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        decoView.systemUiVisibility = flag
    }
}