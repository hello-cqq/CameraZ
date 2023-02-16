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
package com.ahahahq.barcode

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.media.Image
import android.net.Uri
import android.util.Size
import com.ahahahq.barcode.base.BarcodeDecoder
import com.ahahahq.barcode.base.CodecFactory
import com.ahahahq.barcode.base.Result
import com.ahahahq.barcode.base.BarcodeOptions
import com.ahahahq.barcode.util.ImageUtil

/**
 * <p>Recognize and parse barcodes and QR codes, etc.</p>
 */
class Decoder {
    companion object {
        private const val TAG = "Decoder"
    }

    private val decoderFactory = CodecFactory()
    private val decoder: BarcodeDecoder = decoderFactory.getDecoder()

    /**
     * <p>Decoding method, generally applicable to camera api.</p>
     *
     * @param data Byte array representing the image captured by the camera, only support yuv format.
     * @param format Image format, only support yuv format.
     * The camera api is ImageFormat.NV21 by default, and the camera2 api is generally ImageFormat.YUV_420_888.
     * @param size The preview resolution of the camera or the actual size of the captured picture.
     * @param rect The app side generally draws a rectangular area on the preview interface and puts the QR code in it,
     * so that it will be cropped and then decoded during decoding to help improve efficiency.
     * Note: <b>The rectangular coordinates are not the coordinates displayed on the screen.
     * Are the actual coordinates transformed to the camera preview area.</b>
     * @param options The type of decoding included, the application can choose according to their actual needs,
     * the more code types supported, the longer the decoding time.
     * @return {@link Result}
     */
    @JvmOverloads
    fun decode(data: ByteArray, format: Int, size: Size, rect: Rect? = null, options: BarcodeOptions? = null): Array<Result>? {
        return decoder.decodeYUV(data, format, size, rect, options)
    }

    /**
     * <p>Decoding method, generally applicable to camera2 api.</p>
     *
     * @param image camera2 preview result.
     * {@link decode()}
     */
    @JvmOverloads
    fun decode(image: Image, rect: Rect? = null, options: BarcodeOptions? = null): Array<Result>? {
        return decode(
            ImageUtil.getDataFromYUVImage(image, ImageUtil.COLOR_FORMAT_NV21),
            ImageFormat.NV21,
            Size(image.width, image.height),
            rect,
            options
        )
    }

    fun decode(bitmap: Bitmap, options: BarcodeOptions? = null): Array<Result>? {
        return decoder.decodeRGB(bitmap, options)
    }

    fun decode(context: Context, uri: Uri, options: BarcodeOptions? = null): Array<Result>? {
        val bitmap = ImageUtil.getBitmapByUri(context, uri)
        bitmap ?: return null
        return decode(bitmap, options)
    }

    fun decode(path: String, options: BarcodeOptions? = null): Array<Result>? {
        val bitmap = ImageUtil.getBitmapByPath(path)
        bitmap ?: return null
        return decode(bitmap, options)
    }
}