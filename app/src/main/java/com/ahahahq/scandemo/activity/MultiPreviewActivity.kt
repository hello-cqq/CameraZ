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

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import com.ahahahq.scandemo.R
import com.ahahahq.scandemo.common.BaseAppCompatActivity
import com.ahahahq.scandemo.common.tools.DemoLog
import com.ahahahq.scandemo.databinding.ActivityMultiPreviewBinding
import com.ahahahq.cameraz.callback.CameraStateCallback
import com.ahahahq.cameraz.core.CameraClient
import com.ahahahq.cameraz.core.CameraZ
import com.ahahahq.cameraz.model.Preview
import com.ahahahq.cameraz.util.Camera2ConfigUtil
import com.ahahahq.cameraz.util.DualCamera
import com.ahahahq.cameraz.view.CameraView

class MultiPreviewActivity : BaseAppCompatActivity() {
    companion object {
        private const val TAG = "MultiPreviewActivity"
    }

    private lateinit var cameraZ: CameraZ
    private var cameraClient: CameraClient? = null
    private var dualCamera: DualCamera? = null
    private lateinit var binding: ActivityMultiPreviewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cameraZ = CameraZ.getInstance(this.applicationContext)
        initCameraId()
    }

    override fun onResume() {
        super.onResume()
        dualCamera?.logicalId?.let {
            cameraZ.open(it, object : CameraStateCallback {

                override fun onOpened(client: CameraClient) {
                    DemoLog.d(TAG, "onOpened")
                    cameraClient = client
                    client.bind(this@MultiPreviewActivity)
                    val backView1 = findViewById<CameraView>(R.id.preview_view_back1)
                    val backView2 = findViewById<CameraView>(R.id.preview_view_back2)
                    val multiPreview1 = Preview(backView1, "multi-1")
                    val multiPreview2 = Preview(backView2, "multi-2")
                    multiPreview2.physicalId = dualCamera?.physicalId2
                    val width = resources.getDimensionPixelSize(R.dimen.camera_multi_width)
                    val height = resources.getDimensionPixelSize(R.dimen.camera_multi_height)
                    client.setPreviewSize(width, height)
                    client.startPreview(multiPreview1, multiPreview2)
                }
            }, 2)
        }
    }

    override fun onPause() {
        super.onPause()
        if (cameraClient?.cameraState == CameraClient.CameraState.OPENED) {
            cameraClient!!.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraClient?.unbind(this)
        cameraZ.closeAll()
    }

    private fun initCameraId() {
        val manager =
            getSystemService(Context.CAMERA_SERVICE) as CameraManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            dualCamera = Camera2ConfigUtil.findShortLongCameraPair(manager)
        }
        DemoLog.d(
            TAG,
            "initCameraId: ${dualCamera?.logicalId}-(${dualCamera?.physicalId1} ${dualCamera?.physicalId2})"
        )
    }
}