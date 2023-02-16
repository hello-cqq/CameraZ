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
package com.ahahahq.scandemo.activity

import android.os.Bundle
import com.ahahahq.scandemo.R
import com.ahahahq.scandemo.common.BaseAppCompatActivity
import com.ahahahq.scandemo.common.tools.DemoLog
import com.ahahahq.scandemo.databinding.ActivityDoublePreviewBinding
import com.ahahahq.cameraz.callback.CameraStateCallback
import com.ahahahq.cameraz.core.CameraClient
import com.ahahahq.cameraz.core.CameraZ
import com.ahahahq.cameraz.view.CameraView

class DoublePreviewActivity : BaseAppCompatActivity() {

    companion object {
        private const val TAG = "DoublePreviewActivity"
        private const val BACK_ID = "0"
        private const val FONT_ID = "1"
    }

    private lateinit var cameraZ: CameraZ
    private var fontClient: CameraClient? = null
    private var backClient: CameraClient? = null
    private lateinit var fontView: CameraView
    private lateinit var backView: CameraView
    private lateinit var binding: ActivityDoublePreviewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoublePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar(resources.getString(R.string.double_module))
        fontView = binding.cameraViewFont
        backView = binding.cameraViewBack
        cameraZ = CameraZ.getInstance(this.applicationContext)
    }

    override fun onResume() {
        super.onResume()
        cameraZ.open(BACK_ID, object : CameraStateCallback {
            override fun onOpened(client: CameraClient) {
                DemoLog.d(TAG, "onOpened")
                backClient = client
                client.bind(this@DoublePreviewActivity)
                client.startPreview(backView, "back")
            }
        }, 2)
        cameraZ.open(FONT_ID, object : CameraStateCallback {
            override fun onOpened(client: CameraClient) {
                DemoLog.d(TAG, "onOpened")
                fontClient = client
                client.bind(this@DoublePreviewActivity)
                val size = resources.getDimensionPixelSize(R.dimen.camera_font_size)
                client.setPreviewSize(size, size)
                client.startPreview(fontView, "font")
            }
        }, 2)
    }

    override fun onPause() {
        super.onPause()
        fontClient?.close()
        backClient?.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        fontClient?.unbind(this)
        backClient?.unbind(this)
        cameraZ.closeAll()
    }
}