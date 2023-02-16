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
package com.ahahahq.barcode.zxing

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.ahahahq.barcode.qrcode.BarcodeGenerator
import com.ahahahq.barcode.qrcode.ErrorLevel
import com.ahahahq.barcode.qrcode.QRCodeParams
import com.ahahahq.barcode.util.ImageUtil
import com.ahahahq.barcode.util.BarcodeLog
import java.util.HashMap

internal class ZXingGenerator : BarcodeGenerator() {
    companion object {
        private const val TAG = "BarcodeGenerator"
    }

    override fun createQRCode(params: QRCodeParams): Bitmap? {
        return if ((params.logo == null) || (params.scale <= 0)) {
            createQRCodeNoLogo(params, params.text, params.width, params.height, params.foreColor, params.backColor, params.errLevel)
        } else {
            createQRCodeWithLogo(
                params,
                params.text,
                params.width,
                params.height,
                params.foreColor,
                params.backColor,
                params.errLevel,
                params.logo!!,
                params.scale
            )
        }
    }

    private fun createQRCodeNoLogo(params: QRCodeParams, text: String, width: Int, height: Int, foreColor: Int, backColor: Int, level: ErrorLevel): Bitmap? {
        val errLevel = transErrLevelFormat(level)

        val hints: MutableMap<EncodeHintType?, Any?> = HashMap()
        hints[EncodeHintType.CHARACTER_SET] = DEFAULT_CHARSET_UTF_8
        hints[EncodeHintType.ERROR_CORRECTION] = errLevel
        val qrCodeWriter = QRCodeWriter()
        try {
            val bitMatrix = qrCodeWriter.encode(
                text, BarcodeFormat.QR_CODE, width, height,
                hints
            )
            params.setQRRatio(qrCodeWriter.ratio)
            val mWidth = bitMatrix.width
            val mHeight = bitMatrix.height
            val pixels = IntArray(mWidth * mHeight)
            for (y in 0 until mHeight) {
                for (x in 0 until mWidth) {
                    if (bitMatrix[x, y]) {
                        pixels[y * mWidth + x] = foreColor
                    } else {
                        pixels[y * mWidth + x] = backColor
                    }
                }
            }
            val bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
            bitmap?.setPixels(pixels, 0, mWidth, 0, 0, mWidth, mHeight)
            return bitmap
        } catch (e: WriterException) {
            BarcodeLog.e(TAG, "createQRCodeNoLogo: $e")
        }
        return null
    }

    private fun createQRCodeWithLogo(
        params: QRCodeParams,
        text: String,
        width: Int,
        height: Int,
        foreColor: Int,
        backColor: Int,
        level: ErrorLevel,
        logo: Bitmap,
        scale: Float
    ): Bitmap? {
        val src = createQRCodeNoLogo(params, text, width, height, foreColor, backColor, level)
        return ImageUtil.addLogo(src, logo, scale)
    }


    private fun transErrLevelFormat(level: ErrorLevel): ErrorCorrectionLevel {
        return when (level) {
            ErrorLevel.L -> ErrorCorrectionLevel.L
            ErrorLevel.M -> ErrorCorrectionLevel.M
            ErrorLevel.Q -> ErrorCorrectionLevel.Q
            ErrorLevel.H -> ErrorCorrectionLevel.H
        }
    }
}