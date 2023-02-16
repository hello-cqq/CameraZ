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
 * File: BarcodeDecoder
 * Description:
 * Version: 1.0
 * Date : 2023/2/16
 * Author: hey.cqq@gmail.com
 *
 * ---------------------Revision History: ---------------------
 *  <author>           <data>          <version >       <desc>
 *  AhahahQ            2023/2/16         1.0         build this module
*/
package com.ahahahq.barcode.base

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Size

internal abstract class BarcodeDecoder {
    companion object {
        internal const val TAG = "BarcodeDecoder"
    }
    protected var codeOptions: BarcodeOptions = BarcodeOptions.build {
        saveBitmap = false
        setCodeTypes(CodeType.ALL_CODE)
    }

    abstract fun decodeYUV(data: ByteArray, format: Int, size: Size, rect: Rect?, options: BarcodeOptions?): Array<Result>?
    abstract fun decodeRGB(bitmap: Bitmap, options: BarcodeOptions?): Array<Result>?
    fun calculateScanRect(previewSize: Size, rect: Rect?): Rect {
        return if (rect == null) {
            Rect(0, 0, previewSize.width, previewSize.height)
        } else {
            val r = Rect(rect.left, rect.top, rect.right, rect.bottom)
            if (rect.left < 0) {
                r.left = 0
            }
            if (rect.top < 0) {
                r.top = 0
            }
            if (rect.right > previewSize.width) {
                r.right = previewSize.width
            }
            if (rect.bottom > previewSize.height) {
                r.bottom = previewSize.height
            }
            r
        }
    }
}