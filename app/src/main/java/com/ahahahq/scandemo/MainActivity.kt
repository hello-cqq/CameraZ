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
package com.ahahahq.scandemo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.ahahahq.scandemo.common.BaseAppCompatActivity
import com.ahahahq.scandemo.common.tools.DemoLog
import com.ahahahq.scandemo.databinding.ActivityMainBinding
import com.ahahahq.scandemo.activity.DoublePreviewActivity
import com.ahahahq.scandemo.activity.MultiPreviewActivity
import com.ahahahq.scandemo.activity.PictureActivity
import com.ahahahq.scandemo.activity.DecodeActivity
import com.ahahahq.scandemo.activity.QRCodeActivity
import com.ahahahq.scandemo.activity.ScanActivity

class MainActivity : BaseAppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
        const val CODE_REQUEST_PERMISSION = 1
        const val CAPTURE_SCAN = 0
        const val CAPTURE_PIC = 1
        const val CAPTURE_DOUBLE = 2
        const val CAPTURE_MULTI = 3
        const val TARGET_DECODE = 4
        const val TARGET_QR_CODE = 5
    }

    private lateinit var mainBinding: ActivityMainBinding
    private var currentType = CAPTURE_SCAN
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        mainBinding.apply {
            openCodeBtn.setOnClickListener {
                clickBtn(TARGET_QR_CODE)
            }
            openDecodeBtn.setOnClickListener {
                clickBtn(TARGET_DECODE)
            }
            openScanBtn.setOnClickListener {
                clickBtn(CAPTURE_SCAN)
            }

            openPicBtn.setOnClickListener {
                clickBtn(CAPTURE_PIC)
            }

            multiPreviewBtn.setOnClickListener {
                clickBtn(CAPTURE_MULTI)
            }
            doublePreviewBtn.setOnClickListener {
                clickBtn(CAPTURE_DOUBLE)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CODE_REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestActivity(currentType)
            }
        }
    }

    private fun clickBtn(type: Int) {
        currentType = type
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasCameraPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), CODE_REQUEST_PERMISSION)
            }
        } else {
            requestActivity(currentType)
        }
    }

    private fun requestActivity(type: Int) {
        DemoLog.d(TAG, "requestCaptureActivity $type")
        when (type) {
            CAPTURE_SCAN -> startActivity(Intent(this, ScanActivity::class.java))
            CAPTURE_PIC -> startActivity(Intent(this, PictureActivity::class.java))
            CAPTURE_DOUBLE -> startActivity(Intent(this, DoublePreviewActivity::class.java))
            CAPTURE_MULTI -> startActivity(Intent(this, MultiPreviewActivity::class.java))
            TARGET_DECODE -> startActivity(Intent(this, DecodeActivity::class.java))
            TARGET_QR_CODE -> startActivity(Intent(this, QRCodeActivity::class.java))
        }
    }
}