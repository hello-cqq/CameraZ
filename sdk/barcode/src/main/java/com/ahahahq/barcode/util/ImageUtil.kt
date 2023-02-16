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
package com.ahahahq.barcode.util

import android.content.Context
import android.graphics.*
import android.media.Image
import java.nio.ByteBuffer
import android.media.ExifInterface
import android.net.Uri
import android.util.Size
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.lang.Exception
import kotlin.math.floor
import kotlin.math.sqrt


/**
 * Image tools
 */
object ImageUtil {
    private const val TAG = "ImageUtil"
    const val COLOR_FORMAT_I420 = 1 // COLOR_FormatYUV420Planar
    const val COLOR_FORMAT_NV21 = 2 // COLOR_FormatYUV420SemiPlanar

    fun isImageFormatSupported(format: Int): Boolean {
        return when (format) {
            ImageFormat.YUV_420_888, ImageFormat.NV21, ImageFormat.YV12 -> true
            else -> false
        }
    }

    fun isTargetFormatSupported(colorFormat: Int): Boolean {
        return when (colorFormat) {
            COLOR_FORMAT_I420, COLOR_FORMAT_NV21 -> true
            else -> false
        }
    }

    fun getDataFromYUVImage(image: Image, colorFormat: Int): ByteArray {
        require(isTargetFormatSupported(colorFormat)) { "only support COLOR_FormatI420 " + "and COLOR_FormatNV21" }
        require(isImageFormatSupported(image.format)) { "can't convert Image to byte array, format " + image.format }
        val crop = image.cropRect
        val format = image.format
        val width = crop.width()
        val height = crop.height()
        val planes = image.planes
        val data = ByteArray(width * height * ImageFormat.getBitsPerPixel(format) / 8)
        val rowData = ByteArray(planes[0].rowStride)
        var channelOffset = 0
        var outputStride = 1
        for (i in planes.indices) {
            when (i) {
                0 -> {
                    channelOffset = 0
                    outputStride = 1
                }
                1 -> if (colorFormat == COLOR_FORMAT_I420) {
                    channelOffset = width * height
                    outputStride = 1
                } else if (colorFormat == COLOR_FORMAT_NV21) {
                    channelOffset = width * height + 1
                    outputStride = 2
                }
                2 -> if (colorFormat == COLOR_FORMAT_I420) {
                    channelOffset = (width * height * 1.25).toInt()
                    outputStride = 1
                } else if (colorFormat == COLOR_FORMAT_NV21) {
                    channelOffset = width * height
                    outputStride = 2
                }
            }
            val buffer: ByteBuffer = planes[i].buffer
            val rowStride = planes[i].rowStride
            val pixelStride = planes[i].pixelStride
            val shift = if (i == 0) 0 else 1
            val w = width shr shift
            val h = height shr shift
            buffer.position(rowStride * (crop.top shr shift) + pixelStride * (crop.left shr shift))
            for (row in 0 until h) {
                var length: Int
                if (pixelStride == 1 && outputStride == 1) {
                    length = w
                    buffer.get(data, channelOffset, length)
                    channelOffset += length
                } else {
                    length = (w - 1) * pixelStride + 1
                    buffer.get(rowData, 0, length)
                    for (col in 0 until w) {
                        data[channelOffset] = rowData[col * pixelStride]
                        channelOffset += outputStride
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length)
                }
            }
        }
        return data
    }

    fun getBitmapByUri(context: Context, uri: Uri): Bitmap? {
        try {
            return BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
        } catch (e: IOException) {
            BarcodeLog.e(TAG, "getBitmapByUri err. $e")
        }
        return null
    }

    fun getBitmapByPath(path: String): Bitmap? {
        val srcImageFile = File(path)
        return if (srcImageFile != null && srcImageFile.exists()) {
            createThumbnail(srcImageFile, 1024, 1024)
        } else {
            null
        }
    }

    fun createThumbnail(srcImageFile: File?, width: Int, height: Int): Bitmap? {
        return createThumbnail(srcImageFile, width, height, false)
    }

    fun createThumbnail(
        srcImageFile: File?,
        width: Int,
        height: Int,
        adjustOrientation: Boolean
    ): Bitmap? {
        return if ((null != srcImageFile) && srcImageFile.exists()) {
            var resultBitmap: Bitmap? = null
            var opts: BitmapFactory.Options? = null
            var digree: Int
            if ((width > 0) && (height > 0)) {
                opts = BitmapFactory.Options()
                opts.inJustDecodeBounds = true
                BitmapFactory.decodeFile(srcImageFile.path, opts)
                digree = width.coerceAtMost(height)
                opts.inSampleSize = computeSampleSize(opts, digree, width * height)
                opts.inJustDecodeBounds = false
                opts.inInputShareable = true
                opts.inPurgeable = true
            }
            try {
                resultBitmap = BitmapFactory.decodeFile(srcImageFile.path, opts)
            } catch (var13: OutOfMemoryError) {

            } finally {
            }
            if ((resultBitmap != null) && adjustOrientation) {
                digree = getDegreeForPicturePath(srcImageFile.absolutePath)
                if (digree != 0) {
                    val m = Matrix()
                    m.postRotate(digree.toFloat())
                    try {
                        resultBitmap = Bitmap.createBitmap(
                            resultBitmap,
                            0,
                            0,
                            resultBitmap.width,
                            resultBitmap.height,
                            m,
                            true
                        )
                    } catch (var12: OutOfMemoryError) {
                    }
                }
            }
            resultBitmap
        } else {
            null
        }
    }

    private fun getDegreeForPicturePath(imgPath: String): Int {
        var digree = 0
        var exif: ExifInterface? = try {
            ExifInterface(imgPath)
        } catch (e: IOException) {
            null
        }
        if (exif != null) {
            val ori = exif.getAttributeInt("Orientation", 0)
            digree = when (ori) {
                3 -> 180
                6 -> 90
                8 -> 270
                else -> 0
            }
        }
        return digree
    }

    fun computeSampleSize(
        options: BitmapFactory.Options,
        minSideLength: Int,
        maxNumOfPixels: Int
    ): Int {
        val initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels)
        var roundedSize: Int
        if (initialSize <= 8) {
            roundedSize = 1
            while (roundedSize < initialSize) {
                roundedSize = roundedSize shl 1
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8
        }
        return roundedSize
    }

    fun computeInitialSampleSize(
        options: BitmapFactory.Options,
        minSideLength: Int,
        maxNumOfPixels: Int
    ): Int {
        val w = options.outWidth.toDouble()
        val h = options.outHeight.toDouble()
        val lowerBound =
            if (maxNumOfPixels == -1) 1 else floor(sqrt(w * h / maxNumOfPixels.toDouble())).toInt()
        val upperBound =
            if (minSideLength == -1)
                128
            else
                floor(w / minSideLength.toDouble()).coerceAtMost(floor(h / minSideLength.toDouble()))
                    .toInt()
        return if (upperBound < lowerBound) {
            lowerBound
        } else if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            1
        } else {
            if (minSideLength == -1) lowerBound else upperBound
        }
    }

    fun addLogo(src: Bitmap?, logo: Bitmap?, scale: Float): Bitmap? {
        if (src == null) {
            return null
        }
        if (logo == null) {
            return src
        }
        val srcWidth = src.width
        val srcHeight = src.height
        val logoWidth = logo.width
        val logoHeight = logo.height
        if (srcWidth == 0 || srcHeight == 0) {
            return null
        }
        if (logoWidth == 0 || logoHeight == 0) {
            return src
        }
        val scaleFactor: Float = srcWidth * scale / logoWidth
        var bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap!!)
            canvas.drawBitmap(src, 0f, 0f, null)
            canvas.scale(
                scaleFactor,
                scaleFactor,
                (srcWidth / 2).toFloat(),
                (srcHeight / 2).toFloat()
            )
            canvas.drawBitmap(
                logo,
                ((srcWidth - logoWidth) / 2).toFloat(),
                ((srcHeight - logoHeight) / 2).toFloat(),
                null
            )
            canvas.save()
            canvas.restore()
        } catch (e: Exception) {
            bitmap = null
        }
        return bitmap
    }

    fun mask(src: Bitmap, icons: Array<Bitmap>, points: Array<Point>): Bitmap {
        var bitmap = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap)
            canvas.drawBitmap(src, 0f, 0f, null)
            for (i in icons.indices) {
                canvas.drawBitmap(icons[i], points[i].x.toFloat(), points[i].y.toFloat(), null)
            }
            canvas.save()
            canvas.restore()
        } catch (e: Exception) {
            BarcodeLog.e(TAG, "mask err: $e")
        }
        return bitmap
    }

    fun getJpegByteArray(
        bytes: ByteArray,
        format: Int,
        width: Int,
        height: Int,
        rotation: Float,
        quality: Int
    ): ByteArray? {
        val stream = ByteArrayOutputStream()
        val image = YuvImage(bytes, format, width, height, null);
        image.compressToJpeg(Rect(0, 0, width, height), quality, stream)
        val byteArray = stream.toByteArray()
        stream.close()

        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        val matrix = Matrix()
        matrix.setRotate(rotation)
        val rotateBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        val baos = ByteArrayOutputStream()
        rotateBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        bitmap.recycle()
        val baosByteArray = baos.toByteArray()
        rotateBitmap.recycle()
        baos.close()
        return baosByteArray
    }

    fun createBitmapFromYUVImage(image: YuvImage, quality: Int): Bitmap? {
        val stream = ByteArrayOutputStream()
        var bmp: Bitmap? = null
        try {
            image.compressToJpeg(Rect(0, 0, image.width, image.height), quality, stream)
            bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
        } catch (e: Exception) {
            BarcodeLog.e(TAG, "createBitmapFromYUVImage fail! err: $e")
        } finally {
            stream.close()
        }
        return bmp
    }

    fun createBitmapFromYUVImage(data: ByteArray, format: Int, size: Size, quality: Int): Bitmap? {
        val yuvImage = YuvImage(data, format, size.width, size.height, null)
        return createBitmapFromYUVImage(yuvImage, quality)
    }

    fun rotate(bitmap: Bitmap, rotation: Float): Bitmap {
        val matrix = Matrix()
        matrix.setRotate(rotation)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}