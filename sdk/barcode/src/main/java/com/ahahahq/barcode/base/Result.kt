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
 * File: Result
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
import android.graphics.ImageFormat
import android.graphics.Point
import android.os.Parcel
import android.os.Parcelable

/**
 * <p>The result of decoding using Decoder.</p>
 */
open class Result() : Parcelable {
    /**
     * Contents of the barcode
     */
    var text: String? = ""

    /**
     * type of the barcode {@link CodeType}
     */
    var codeType: CodeType? = null

    /**
     * Decoded image format
     */
    var format: Int = ImageFormat.UNKNOWN

    /**
     * Decoded original image data
     */
    var originValue: ByteArray? = null

    /**
     * Coordinates of the recognized barcode
     */
    var points: Array<Point>? = null

    var bitmap: Bitmap? = null

    constructor(parcel: Parcel) : this() {
        text = parcel.readString()
        codeType = parcel.readParcelable(CodeType::class.java.classLoader)
        format = parcel.readInt()
        originValue = parcel.createByteArray()
        points = parcel.createTypedArray(Point.CREATOR)
        bitmap = parcel.readParcelable(Bitmap::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeParcelable(codeType, flags)
        parcel.writeInt(format)
        parcel.writeByteArray(originValue)
        parcel.writeTypedArray(points, flags)
        parcel.writeParcelable(bitmap, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Result> {
        override fun createFromParcel(parcel: Parcel): Result {
            return Result(parcel)
        }

        override fun newArray(size: Int): Array<Result?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        var pointString = ""
        points?.let {
            for (point in points!!) {
                pointString += "(${point.x},${point.y})"
            }
        }
        return "Result[" +
                "text: $text, " +
                "codeType: $codeType, " +
                "points: $pointString" +
                "]"
    }
}