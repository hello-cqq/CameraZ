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
import android.graphics.ImageFormat
import android.graphics.Point
import android.os.Bundle
import android.util.Size
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import com.ahahahq.barcode.Decoder
import com.ahahahq.barcode.base.CodeType
import com.ahahahq.barcode.base.Result
import com.ahahahq.cameraz.callback.CameraStateCallback
import com.ahahahq.cameraz.callback.PreviewCallback
import com.ahahahq.cameraz.core.CameraClient
import com.ahahahq.cameraz.core.CameraZ
import com.ahahahq.cameraz.util.CameraConfigUtil
import com.ahahahq.cameraz.util.MathUtil
import com.ahahahq.cameraz.view.CameraView
import com.ahahahq.scandemo.R
import com.ahahahq.scandemo.common.BaseAppCompatActivity
import com.ahahahq.scandemo.common.tools.DemoLog
import com.ahahahq.scandemo.databinding.ActivityScanBinding

class ScanActivity : BaseAppCompatActivity() {
    companion object {
        private const val TAG = "ScanActivity"
    }

    private lateinit var binding: ActivityScanBinding
    private lateinit var cameraZ: CameraZ
    private var cameraClient: CameraClient? = null
    private lateinit var decoder: Decoder
    private var backCameraId: String? = null

    @Volatile
    private var hasDecoded = false
    private var cameraView: CameraView? = null
    private lateinit var resultLayout: RelativeLayout
    private lateinit var maskLayout: FrameLayout
    private var imageList: ArrayList<ImageView> = ArrayList()
    private var previewCallback: PreviewCallback? = object : PreviewCallback {
        override fun onPreviewFrame(data: ByteArray?, size: Size, format: Int) {
            if (hasDecoded) {
                return
            }
            decodeCode(data, size, format)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar(resources.getString(R.string.scan_module))
        cameraZ = CameraZ.getInstance(this.applicationContext)
        decoder = Decoder()
        backCameraId = CameraConfigUtil.getBackCameraList()[0].toString()
        resultLayout = findViewById(R.id.result_layout)
        maskLayout = findViewById(R.id.mask_layout)

        binding.cancel.setOnClickListener { resetView() }
    }

    override fun onResume() {
        super.onResume()
        resetView()
        cameraZ.open(
            backCameraId!!,
            object : CameraStateCallback {
                override fun onOpened(client: CameraClient) {
                    DemoLog.d(TAG, "onOpened")
                    cameraView = findViewById(R.id.camera_view)
                    cameraClient = client
                    client.bind(this@ScanActivity)
                    client.startPreview(cameraView!!, TAG, previewCallback)
                    binding.scanLine.play()
                }
            },
            1
        )
    }

    override fun onPause() {
        super.onPause()
        if (cameraClient?.cameraState == CameraClient.CameraState.OPENED) {
            cameraClient!!.close()
        }
        binding.scanLine.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraClient?.unbind(this)
        cameraZ.closeAll()
    }

    private fun decodeCode(data: ByteArray?, size: Size, format: Int) {
        val start = System.currentTimeMillis()
        val results = data?.let {
            if (format == ImageFormat.NV21) {
                decoder.decode(it, format, size, null, null)
            } else {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                decoder.decode(bitmap)
            }
        }
        val end = System.currentTimeMillis()
        if (results.isNullOrEmpty()) {
            return
        }
        DemoLog.i(TAG, "decodeCode data cost ${end - start}")
        handleDecodeResults(results)
    }

    private fun handleDecodeResults(results: Array<Result>) {
        var resultStr = ""
        for (i in results.indices) {
            resultStr += "\n$i: ${results[i]}$"
        }
        DemoLog.d(TAG, "handleDecodeResults $resultStr")
        runOnUiThread {
            if (hasDecoded) {
                return@runOnUiThread
            }
            val cameraParams = cameraClient?.getParams()
            cameraParams?.let {
                val degree = cameraParams.rotation!!
                val previewSize = cameraParams.previewSize
                for (result in results) {
                    if ((result.points == null) || result.points!!.isEmpty() || (result.codeType != CodeType.QR_CODE)) {
                        parse(result.text, result.codeType)
                        return@let
                    }
                    result.points?.let {
                        val centerPointInImage = MathUtil.calculateGeometricCenter(it)
                        val scale = MathUtil.calculateScaleRatio(
                            previewSize,
                            Size(cameraView!!.width, cameraView!!.height)
                        )
                        val image = drawMask(centerPointInImage, degree, scale, previewSize)
                        maskLayout.addView(image)
                        imageList.add(image)
                        image.setOnClickListener {
                            parse(result.text, result.codeType)
                        }
                    }
                }
                hasDecoded = true
                cameraClient?.stopPreview()
                resultLayout.visibility = View.VISIBLE
                binding.scanLine.visibility = View.INVISIBLE
            }
        }
    }

    private fun drawMask(point: Point, degree: Int, scale: Float, originPoint: Size): ImageView {
        DemoLog.d(TAG, "drawMask: $point $degree $scale")
        val newPoint = Point(point.x, point.y)
        if (degree == 90) {
            newPoint.x = originPoint.height - point.y
            newPoint.y = point.x
        }
        newPoint.x = (newPoint.x * scale).toInt()
        newPoint.y = (newPoint.y * scale).toInt()
        val imageView = ImageView(this)
        imageView.setImageDrawable(getDrawable(R.drawable.dot))
        val size = resources.getDimension(R.dimen.hand_size).toInt()
        val params = FrameLayout.LayoutParams(size, size)
        params.marginStart = newPoint.x - size / 2
        params.topMargin = newPoint.y - size / 2
        imageView.layoutParams = params
        DemoLog.d(TAG, "drawMask result: $newPoint")
        return imageView
    }

    private fun parse(content: String?, codeType: CodeType?) {
        content ?: return
        codeType ?: return
        Toast.makeText(this, "scan result: $content", Toast.LENGTH_SHORT).show()
    }

    private fun resetView() {
        resultLayout.visibility = View.INVISIBLE
        binding.scanLine.visibility = View.VISIBLE
        cameraView?.visibility = View.VISIBLE
        for (image in imageList) {
            maskLayout.removeView(image)
        }
        imageList.clear()
        cameraClient?.restartPreview()
        hasDecoded = false
    }
}