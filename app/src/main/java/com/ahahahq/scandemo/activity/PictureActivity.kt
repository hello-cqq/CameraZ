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
 * File: PictureActivity
 * Description:
 * Version: 1.0
 * Date : 2023/2/16
 * Author: hey.cqq@gmail.com
 *
 * ---------------------Revision History: ---------------------
 *  <author>           <data>          <version >       <desc>
 *  AhahahQ            2023/2/16         1.0         build this module
*/
package com.ahahahq.scandemo.activity

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.CameraManager
import android.media.MediaScannerConnection
import android.os.*
import android.provider.MediaStore
import android.util.Size
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.ahahahq.scandemo.BuildConfig
import com.ahahahq.scandemo.R
import com.ahahahq.scandemo.common.BaseAppCompatActivity
import com.ahahahq.scandemo.common.tools.DemoLog
import com.ahahahq.scandemo.common.tools.SensorClient
import com.ahahahq.scandemo.common.tools.runAsync
import com.ahahahq.scandemo.common.utils.EXJumpUtil
import com.ahahahq.scandemo.databinding.ActivityPictureBinding
import com.ahahahq.cameraz.callback.CameraStateCallback
import com.ahahahq.cameraz.callback.PictureCallback
import com.ahahahq.cameraz.callback.PreviewCallback
import com.ahahahq.cameraz.core.CameraClient
import com.ahahahq.cameraz.core.CameraZ
import com.ahahahq.cameraz.ui.RotateImageView
import com.ahahahq.cameraz.util.Camera2ConfigUtil
import com.ahahahq.cameraz.view.CameraView
import com.ahahahq.barcode.Decoder
import com.ahahahq.barcode.base.BarcodeOptions
import com.ahahahq.cameraz.util.CameraLog
import com.ahahahq.barcode.base.Result
import com.ahahahq.cameraz.util.ImageUtil
import java.io.*

class PictureActivity : BaseAppCompatActivity(), SensorClient.SensorClientListener {

    companion object {
        private const val TAG = "PictureActivity"
        private const val REQUEST_CODE_ALBUM = 1
        private const val CODE_REQUEST_PERMISSION = 1
    }

    private lateinit var sensorClient: SensorClient
    private lateinit var cameraZ: CameraZ
    private var cameraClient: CameraClient? = null
    private lateinit var decoder: Decoder
    private var backCameraId: String? = null

    @Volatile
    private var isRecognizing = false
    private var isSensorStatic = false
    private var cameraView: CameraView? = null
    private lateinit var shotBtn: RotateImageView
    private lateinit var albumBtn: RotateImageView
    private lateinit var binding: ActivityPictureBinding
    private var previewCallback: PreviewCallback? = object : PreviewCallback {

        override fun onPreviewFrame(data: ByteArray?, size: Size, format: Int) {
            if (isRecognizing) {
                return
            }
            if (isSensorStatic) {
                recognizeWhenShot(data, size, format, false)
                return
            }
            decodeCode(data, size, format)
        }
    }

    private var pictureCallback: PictureCallback = object : PictureCallback {

        override fun onPictureTaken(data: ByteArray?, size: Size, format: Int) {
            if (isRecognizing) {
                return
            }
            recognizeWhenShot(data, size, format)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPictureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar(resources.getString(R.string.pic_module))
        CameraLog.init(BuildConfig.DEBUG)
        cameraZ = CameraZ.getInstance(this.applicationContext)
        sensorClient = SensorClient(this)
        sensorClient.addListener(this)
        sensorClient.start()
        decoder = Decoder()
        val manager =
            getSystemService(Context.CAMERA_SERVICE) as CameraManager
        backCameraId = Camera2ConfigUtil.getBackCameraList(manager)[0]
        albumBtn = findViewById(com.ahahahq.cameraz.R.id.iv_album)
        shotBtn = findViewById(com.ahahahq.cameraz.R.id.iv_shot)
        shotBtn.setOnClickListener { clickShotBtn() }
        albumBtn.setOnClickListener { EXJumpUtil.jumpAlbum(this, REQUEST_CODE_ALBUM) }
    }

    override fun onResume() {
        super.onResume()
        requestPermission()
        cameraZ.open(
            backCameraId!!,
            object : CameraStateCallback {
                override fun onOpened(client: CameraClient) {
                    DemoLog.d(TAG, "onOpened")
                    cameraView = binding.cameraView
                    cameraClient = client
                    client.bind(this@PictureActivity)
                    client.startPreview(cameraView!!, TAG, previewCallback)
                    runOnUiThread { binding.scanStar.play() }
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
        binding.scanStar.stop(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraClient?.unbind(this)
        cameraZ.closeAll()
        sensorClient.removeListener(this)
        sensorClient.stop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_ALBUM -> {
                    data?.data?.let { uri ->
                        val bitmap =
                            BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                        bitmap?.let { decodeBitmap(it) }
                    }
                }

                else -> Toast.makeText(this, "refuse", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun decodeBitmap(bitmap: Bitmap) {
        runAsync {
            val results = decoder.decode(bitmap)
            if (results.isNullOrEmpty()) {
                return@runAsync
            }
            DemoLog.d(TAG, "decodeBitmap")
            handleDecodeResults(results)
        }
    }


    private fun decodeCode(data: ByteArray?, size: Size, format: Int) {
        data ?: return
        val results = if (format == ImageFormat.NV21) {
            decoder.decode(data, format, size, null, BarcodeOptions.build { saveBitmap = true })
        } else {
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            decoder.decode(bitmap)
        }
        if (results.isNullOrEmpty()) {
            return
        }
        DemoLog.d(TAG, "decodeCode data: $data size: $size, format: $format")
        handleDecodeResults(results)
    }

    private fun handleDecodeResults(results: Array<Result>) {
        var resultStr = ""
        for (i in results.indices) {
            resultStr += "\n$i: ${results[i]}$"
        }
        DemoLog.d(TAG, "handleDecodeResults $resultStr")
    }

    private fun recognizeWhenShot(
        data: ByteArray?,
        size: Size,
        format: Int,
        isTaken: Boolean = true
    ) {
        DemoLog.d(TAG, "recognizeWhenShot data: $data size: $size, format: $format")
        data ?: return
        val bitmap = if (format == ImageFormat.NV21) {
            val image = YuvImage(data, format, size.width, size.height, null)
            ImageUtil.createBitmapFromYUVImage(image, 45)
        } else {
            val option = BitmapFactory.Options()
            option.inSampleSize = 2
            BitmapFactory.decodeByteArray(data, 0, data.size, option)
        }
        bitmap?.let {
            decodeBitmap(bitmap)
            if (isTaken) {
                saveBitmap(bitmap)
            }
        }
    }


    private fun clickShotBtn() {
        cameraClient?.let {
            it.takePicture(TAG, pictureCallback)
        }
    }

    override fun onMoving() {
        DemoLog.d(TAG, "onMoving")
        isSensorStatic = false
    }

    override fun onStatic() {
        DemoLog.d(TAG, "onStatic")
        isSensorStatic = true
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasCameraPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasCameraPermission) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.READ_MEDIA_IMAGES
                        ),
                        CODE_REQUEST_PERMISSION
                    )
                }
            }
        } else {
            val hasCameraPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasCameraPermission) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ),
                        CODE_REQUEST_PERMISSION
                    )
                }
            }
        }
    }

    private fun saveBitmap(bitmap: Bitmap) {
        val imageMediaPath = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "ScanDemo"
            ).path
        } else {
            Environment.DIRECTORY_DCIM + "/ScanDemo"
        }
        val fileName = "ScanDemo_${System.currentTimeMillis()}.png"
        runAsync {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                try {
                    var outputFile = File(imageMediaPath)
                    if (!outputFile.exists()) {
                        outputFile.mkdir()
                    }
                    outputFile = File("$imageMediaPath/$fileName")
                    val fos = FileOutputStream(outputFile)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                    DemoLog.i(TAG, "saveBitmap outputFile: ${outputFile.path}")
                    fos.close()
                } catch (e: FileNotFoundException) {
                    DemoLog.e(TAG, "saveBitmap FileNotFoundException: $e")
                } catch (e: Exception) {
                    DemoLog.e(TAG, "saveBitmap Exception: $e")
                }
            } else {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                values.put(MediaStore.Images.Media.RELATIVE_PATH, imageMediaPath)
                val external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val insertUri = contentResolver.insert(external, values)
                DemoLog.i(TAG, "saveBitmap insertUri: ${insertUri.toString()}")
                var os: OutputStream? = null
                try {
                    if (insertUri != null) {
                        os = contentResolver.openOutputStream(insertUri)
                    }
                    if (os != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
                    }
                } catch (e: Exception) {
                    DemoLog.e(TAG, "saveBitmap Exception: $e")
                } finally {
                    os?.close()
                }
            }
            MediaScannerConnection.scanFile(
                this@PictureActivity,
                arrayOf("$imageMediaPath/$fileName"),
                arrayOf("image/jpeg", "image/png", "image/jpg"),
                null
            )
        }
    }
}