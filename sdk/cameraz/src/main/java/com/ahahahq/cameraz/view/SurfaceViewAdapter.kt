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

import android.view.SurfaceHolder
import android.widget.FrameLayout
import com.ahahahq.cameraz.model.CameraParams
import com.ahahahq.cameraz.util.CameraLog

internal class SurfaceViewAdapter(parent: FrameLayout) : PreviewAdapter(parent) {

    companion object {
        private const val TAG = "SurfaceViewAdapter"
    }
    private var surfaceView: CameraSurfaceView = CameraSurfaceView(parent.context)
    private var callback = HolderStateCallback()

    override fun initView() {
        surfaceView.layoutParams = getLayoutParams()
        surfaceView.holder.addCallback(callback)
        parent.removeAllViews()
        parent.addView(surfaceView)
    }

    override fun applyCameraView(params: CameraParams, needRotation: Boolean) {
        surfaceView.updateSurface(params.previewSize.width, params.previewSize.height, params.rotation!!)
    }

    inner class HolderStateCallback : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            CameraLog.d(TAG, "surfaceCreated $holder")
            surfaceOrTexture = holder
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            CameraLog.d(TAG, "surfaceChanged $holder $width $height")
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            CameraLog.d(TAG, "surfaceDestroyed")
            surfaceOrTexture = null
        }
    }
}