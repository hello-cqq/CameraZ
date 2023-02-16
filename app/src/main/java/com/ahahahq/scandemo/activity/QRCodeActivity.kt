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

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import com.ahahahq.scandemo.R
import com.ahahahq.scandemo.common.BaseAppCompatActivity
import com.ahahahq.scandemo.common.tools.runAsync
import com.ahahahq.scandemo.common.tools.runMain
import com.ahahahq.scandemo.databinding.ActivityQrcodeBinding
import com.ahahahq.barcode.qrcode.ErrorLevel
import com.ahahahq.barcode.qrcode.QRCodeParams
import com.ahahahq.barcode.qrcode.QRCodeUtil

class QRCodeActivity : BaseAppCompatActivity() {
    private lateinit var binding: ActivityQrcodeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.codeBtn.setOnClickListener {
            createQRCode()
        }
        init()
    }

    private fun createQRCode() {
        val editText = binding.editText.text.toString()
        when {
            editText.isEmpty() -> {
                Toast.makeText(applicationContext, "请输入文本", Toast.LENGTH_SHORT).show()
            }

            else -> {
                runAsync {
                    val bitmap = QRCodeUtil.createQRCode(QRCodeParams.build {
                        text = editText
                        width = resources.getDimensionPixelSize(R.dimen.qrcode_size)
                        height = resources.getDimensionPixelSize(R.dimen.qrcode_size)
                        errLevel = ErrorLevel.H
                        logo = BitmapFactory.decodeResource(resources, R.drawable.icon_app)
                        scale = 0.2F
                    })
                    runMain {
                        binding.qrImage.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }

    private fun init() {
        initToolbar(resources.getString(R.string.qrcode_module))
        binding.editText.setText("this is one test qrcode")
        createQRCode()
    }
}