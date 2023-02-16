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
import android.graphics.Point
import android.graphics.Rect
import android.util.Size
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.ReaderException
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import com.ahahahq.barcode.base.BarcodeDecoder
import com.ahahahq.barcode.base.CodeType
import com.ahahahq.barcode.base.Result
import com.ahahahq.barcode.base.BarcodeOptions
import com.ahahahq.barcode.util.ImageUtil
import java.util.EnumMap
import java.util.EnumSet

internal class ZXingDecoder : BarcodeDecoder() {
    private val multiFormatReader = MultiFormatReader()
    override fun decodeYUV(
        data: ByteArray,
        format: Int,
        size: Size,
        rect: Rect?,
        options: BarcodeOptions?
    ): Array<Result>? {
        updateOptions(options)
        var scanRect: Rect = calculateScanRect(size, rect)
        val source = PlanarYUVLuminanceSource(
            data, size.width, size.height, scanRect.left, scanRect.top,
            scanRect.width(), scanRect.height(), false
        )
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        var decodeResult: com.google.zxing.Result? = null
        try {
            decodeResult = multiFormatReader.decodeWithState(bitmap)
        } catch (re: ReaderException) {
            // continue
        } finally {
            multiFormatReader.reset()
        }
        return if (decodeResult == null) {
            null
        } else {
            val zXingResult: ArrayList<Result> = ArrayList()
            val tResult = transformResult(decodeResult)
            if (tResult != null) {
                tResult.format = format
                if (codeOptions.saveBitmap) {
                    tResult.bitmap = ImageUtil.createBitmapFromYUVImage(data, format, size, 50)
                }
                zXingResult.add(tResult)
                zXingResult.toTypedArray()
            } else {
                null
            }
        }
    }

    override fun decodeRGB(bitmap: Bitmap, options: BarcodeOptions?): Array<Result>? {
        updateOptions(options)
        val width: Int = bitmap.width
        val height: Int = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val source = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(GlobalHistogramBinarizer(source))
        var decodeResult: com.google.zxing.Result? = null
        try {
            decodeResult = multiFormatReader.decodeWithState(binaryBitmap)
        } catch (re: ReaderException) {
            // continue
        } finally {
            multiFormatReader.reset()
        }
        return if (decodeResult == null) {
            null
        } else {
            val zXingResult: ArrayList<Result> = ArrayList()
            val tResult = transformResult(decodeResult)
            if (tResult != null) {
                zXingResult.add(tResult)
                zXingResult.toTypedArray()
            } else {
                null
            }
        }
    }

    private fun transformResult(decodeResult: com.google.zxing.Result): ZXingResult? {
        val zXingResult = ZXingResult()
        zXingResult.text = decodeResult.text
        zXingResult.codeType = transZXingType(decodeResult.barcodeFormat)
        zXingResult.originValue = decodeResult.rawBytes
        zXingResult.points = getPointsFromDecodeResult(decodeResult)
        zXingResult.numBits = decodeResult.numBits
        zXingResult.timestamp = decodeResult.timestamp
        return zXingResult
    }

    private fun updateOptions(options: BarcodeOptions?) {
        codeOptions.apply {
            if ((options?.mode != null) && (options.mode > 0)) {
                mode = options.mode
            }
            saveBitmap = options?.saveBitmap ?: true
        }
        val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java)
        hints[DecodeHintType.POSSIBLE_FORMATS] = transModeToZXing(codeOptions.mode)
        multiFormatReader.setHints(hints)
    }

    private fun transModeToZXing(type: Int): Collection<BarcodeFormat> {
        val decodeFormats = EnumSet.noneOf(BarcodeFormat::class.java)
        if (CodeType.AZTEC_CODE.codeType and type > 0) {
            decodeFormats.add(BarcodeFormat.AZTEC)
        }
        if (CodeType.CODE39.codeType and type > 0) {
            decodeFormats.add(BarcodeFormat.CODE_39)
        }
        if (CodeType.CODE93.codeType and type > 0) {
            decodeFormats.add(BarcodeFormat.CODE_93)
        }
        if (CodeType.CODE128.codeType and type > 0) {
            decodeFormats.add(BarcodeFormat.CODE_128)
        }
        if (CodeType.DM_CODE.codeType and type > 0) {
            decodeFormats.add(BarcodeFormat.DATA_MATRIX)
        }
        if (CodeType.EAN8.codeType and type > 0) {
            decodeFormats.add(BarcodeFormat.EAN_8)
        }
        if (CodeType.EAN13.codeType and type > 0) {
            decodeFormats.add(BarcodeFormat.EAN_13)
        }
        if (CodeType.ITF.codeType and type > 0) {
            decodeFormats.add(BarcodeFormat.ITF)
        }
        if (CodeType.PDF417.codeType and type > 0) {
            decodeFormats.add(BarcodeFormat.PDF_417)
        }
        if (CodeType.QR_CODE.codeType and type > 0) {
            decodeFormats.add(BarcodeFormat.QR_CODE)
        }
        if (CodeType.UPC_A.codeType and type > 0) {
            decodeFormats.add(BarcodeFormat.UPC_A)
        }
        if (CodeType.UPC_A.codeType and type > 0) {
            decodeFormats.add(BarcodeFormat.UPC_A)
        }
        return decodeFormats
    }

    private fun transZXingType(zType: BarcodeFormat): CodeType? {
        return when (zType) {
            BarcodeFormat.AZTEC -> CodeType.AZTEC_CODE
            BarcodeFormat.CODABAR, BarcodeFormat.RSS_14, BarcodeFormat.UPC_EAN_EXTENSION -> CodeType.ONE_CODE
            BarcodeFormat.CODE_39 -> CodeType.CODE39
            BarcodeFormat.CODE_93 -> CodeType.CODE93
            BarcodeFormat.CODE_128 -> CodeType.CODE128
            BarcodeFormat.DATA_MATRIX -> CodeType.DM_CODE
            BarcodeFormat.EAN_8 -> CodeType.EAN8
            BarcodeFormat.EAN_13 -> CodeType.EAN13
            BarcodeFormat.ITF -> CodeType.ITF
            BarcodeFormat.PDF_417 -> CodeType.PDF417
            BarcodeFormat.QR_CODE -> CodeType.QR_CODE
            BarcodeFormat.UPC_A -> CodeType.UPC_A
            BarcodeFormat.UPC_E -> CodeType.UPC_E
            BarcodeFormat.RSS_EXPANDED -> CodeType.RSS_EXPANDED
            else -> null
        }
    }

    private fun getPointsFromDecodeResult(decodeResult: com.google.zxing.Result): Array<Point>? {
        val resultPoints = decodeResult.resultPoints ?: return null
        val points = Array(resultPoints.size) { Point() }
        for (i in points.indices) {
            points[i] = Point(resultPoints[i].x.toInt(), resultPoints[i].y.toInt())
        }
        return points
    }
}