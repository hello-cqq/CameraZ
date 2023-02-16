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
 * File: QRCodeParams
 * Description:
 * Version: 1.0
 * Date : 2023/2/16
 * Author: hey.cqq@gmail.com
 *
 * ---------------------Revision History: ---------------------
 *  <author>           <data>          <version >       <desc>
 *  AhahahQ            2023/2/16         1.0         build this module
*/
package com.ahahahq.barcode.qrcode

import android.graphics.Bitmap

/**
 * QRCode parameter entity, which represents the size, color, style, etc.
 */
class QRCodeParams private constructor(

    var text: String,
    var width: Int,
    var height: Int,
    // the color of the QR-code point
    var foreColor: Int,
    // the color of the QR-code background
    var backColor: Int,
    var errLevel: ErrorLevel,
    var logo: Bitmap?,
    var scale: Float
) {
    private var ratio = 1

    companion object {

        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    class Builder {
        var text: String = "Barcode"
        var width: Int = 200
        var height: Int = 200
        var foreColor: Int = 0xFF000000.toInt()
        var backColor: Int = 0xFFFFFFFF.toInt()
        var errLevel: ErrorLevel = ErrorLevel.Q
        var logo: Bitmap? = null
        var scale: Float = 0F
        fun build() = QRCodeParams(text, width, height, foreColor, backColor, errLevel, logo, scale)
    }

    internal fun setQRRatio(r: Int) {
        this.ratio = r
    }

    fun getQRRatio(): Int {
        return ratio
    }

    override fun toString(): String {
        return "QRCodeParams[" +
                "text: $text, " +
                "size: ($width,$height), " +
                "errLevel: $errLevel]"
    }
}