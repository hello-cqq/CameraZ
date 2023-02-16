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

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.ahahahq.scandemo.R
import com.ahahahq.scandemo.common.BaseAppCompatActivity
import com.ahahahq.scandemo.common.tools.DemoLog
import com.ahahahq.scandemo.common.tools.runAsync
import com.ahahahq.scandemo.common.tools.runMain
import com.ahahahq.scandemo.common.utils.EXJumpUtil
import com.ahahahq.scandemo.databinding.ActivityDecodeBinding
import com.ahahahq.barcode.Decoder

class DecodeActivity : BaseAppCompatActivity() {
    companion object {
        private const val TAG = "DecodeActivity"
        const val REQUEST_CODE_ALBUM = 1
    }

    private var imageUri: Uri? = null
    private lateinit var decoder: Decoder
    private lateinit var decodeBinding: ActivityDecodeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        decodeBinding = ActivityDecodeBinding.inflate(layoutInflater)
        setContentView(decodeBinding.root)
        initToolbar(resources.getString(R.string.decode_module))
        decoder = Decoder()
        decodeBinding.decodeImage.setOnClickListener {
            EXJumpUtil.jumpAlbum(this, REQUEST_CODE_ALBUM)
        }
        decodeBinding.decodeBtn.setOnClickListener {
            decode()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_ALBUM -> {
                    data?.data?.let {
                        updateUI(it)
                    }
                }

                else -> Toast.makeText(this, "refuse", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(uri: Uri) {
        imageUri = uri
        decodeBinding.decodeImage.setImageURI(uri)
    }

    private fun decode() {
        runAsync {
            imageUri ?: return@runAsync
            val start = System.currentTimeMillis()
            val results = decoder.decode(this@DecodeActivity, imageUri!!)
            val end = System.currentTimeMillis()
            if ((results == null) || results.isEmpty()) {
                runMain { decodeBinding.recognizeResult.text = null }
            } else {
                DemoLog.i(TAG, "decode uri cost: ${end - start}")
                runMain {
                    decodeBinding.recognizeResult.text =
                        "[条码类型: ${results[0].codeType}, 码值: ${results[0].text}]\n"
                }
            }
        }
    }
}