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
 * Description: Decode options
 * Version: 1.0
 * Date : 2023/2/16
 * Author: hey.cqq@gmail.com
 *
 * ---------------------Revision History: ---------------------
 *  <author>           <data>          <version >       <desc>
 *  AhahahQ            2023/2/16         1.0         build this module
*/
package com.ahahahq.barcode.base

class BarcodeOptions private constructor(
    var mode: Int,
    /**
     * @sample saveBitmap
     * true means that after the decoding is successful, the bitmap corresponding to the result will be returned,
     * and false means that the bitmap does not need to be generated in the returned result.
     */
    var saveBitmap: Boolean
) {
    companion object {
        private const val TAG = "BarcodeOptions"
        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    class Builder {
        private var mode: Int = 0
        var saveBitmap: Boolean = false
        fun build() = BarcodeOptions(mode, saveBitmap)
        fun setCodeTypes(vararg typeArr: CodeType) {
            if (typeArr.isNotEmpty()) {
                for (t in typeArr) {
                    mode = mode or t.codeType
                }
            }
        }
    }

    override fun toString(): String {
        return "$TAG[mode: ${mode.toString(16)}]"
    }
}
