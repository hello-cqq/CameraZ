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

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.Writer
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.Encoder
import com.google.zxing.qrcode.encoder.QRCode
import kotlin.math.max
import kotlin.math.min


class QRCodeWriter : Writer {
    var ratio = 1
    @Throws(WriterException::class)
    override fun encode(contents: String, format: BarcodeFormat, width: Int, height: Int): BitMatrix {
        return this.encode(contents, format, width, height, null)
    }

    @Throws(WriterException::class)
    override fun encode(contents: String, format: BarcodeFormat, width: Int, height: Int, hints: Map<EncodeHintType?, *>?): BitMatrix {
        return if (contents.isEmpty()) {
            throw IllegalArgumentException("Found empty contents")
        } else if (format != BarcodeFormat.QR_CODE) {
            throw IllegalArgumentException("Can only encode QR_CODE, but got $format")
        } else if (width >= 0 && height >= 0) {
            var errorCorrectionLevel = ErrorCorrectionLevel.L
            if (hints != null) {
                if (hints.containsKey(EncodeHintType.ERROR_CORRECTION)) {
                    errorCorrectionLevel = ErrorCorrectionLevel.valueOf(hints[EncodeHintType.ERROR_CORRECTION].toString())
                }
            }
            val code = Encoder.encode(contents, errorCorrectionLevel, hints)
            renderResult(code, width, height)
        } else {
            throw IllegalArgumentException("Requested dimensions are too small: " + width + 'x' + height)
        }
    }

    private fun renderResult(code: QRCode, width: Int, height: Int): BitMatrix {
        val input = code.matrix
        return if (input == null) {
            throw IllegalStateException()
        } else {
            val inputWidth = input.width
            val inputHeight = input.height
            val outputWidth = max(width, inputWidth)
            val outputHeight = max(height, inputHeight)
            val multiple = min(outputWidth / inputWidth, outputHeight / inputHeight)
            ratio = multiple
            val output = BitMatrix(multiple * inputWidth, multiple * inputHeight)
            var inputY = 0
            var outputY = 0
            while (inputY < inputHeight) {
                var inputX = 0
                var outputX = 0
                while (inputX < inputWidth) {
                    if (input[inputX, inputY].toInt() == 1) {
                        output.setRegion(outputX, outputY, multiple, multiple)
                    }
                    ++inputX
                    outputX += multiple
                }
                ++inputY
                outputY += multiple
            }
            output
        }
    }
}