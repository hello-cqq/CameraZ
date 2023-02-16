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

import android.graphics.SurfaceTexture
import android.view.TextureView
import android.widget.FrameLayout
import com.ahahahq.cameraz.model.CameraParams
import com.ahahahq.cameraz.util.CameraLog

internal class TextureViewAdapter(parent: FrameLayout) : PreviewAdapter(parent) {
    companion object {
        private const val TAG = "TextureViewAdapter"
    }

    private var textureView: CameraTextureView = CameraTextureView(parent.context)
    private var listener = TextureStateListener()

    override fun initView() {
        textureView.layoutParams = getLayoutParams()
        textureView.surfaceTextureListener = listener
        parent.removeAllViews()
        parent.addView(textureView)
    }

    override fun applyCameraView(params: CameraParams, needRotation: Boolean) {
        textureView.updateSurface(params.previewSize.width, params.previewSize.height, params.rotation!!, needRotation)
    }

    inner class TextureStateListener : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            CameraLog.d(TAG, "onSurfaceTextureAvailable $surface $width $height")
            surfaceOrTexture = surface
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            CameraLog.d(TAG, "onSurfaceTextureSizeChanged $surface $width $height")
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            CameraLog.d(TAG, "onSurfaceTextureDestroyed")
            surfaceOrTexture = null
            return true
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

        }
    }
}