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
package com.ahahahq.scandemo.common.tools

import android.util.Log

object DemoLog {
    private const val ROOT_TAG = "ScanDemo"
    private var rootTag = ROOT_TAG
    private var isLogDebug = false

    fun init(debuggable: Boolean) {
        isLogDebug = debuggable
    }

    private fun getTag(tag: String? = null): String {
        tag ?: return rootTag
        return "$rootTag.$tag"
    }

    @JvmOverloads
    fun d(tag: String? = null, content: String, throwable: Throwable? = null) {
        if (isLogDebug) {
            if (throwable != null) {
                Log.d(getTag(tag), content, throwable)
            } else {
                Log.d(getTag(tag), content)
            }
        }
    }

    @JvmOverloads
    fun v(tag: String? = null, content: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.v(getTag(tag), content, throwable)
        } else {
            Log.v(getTag(tag), content)
        }
    }

    @JvmOverloads
    fun i(tag: String? = null, content: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.i(getTag(tag), content, throwable)
        } else {
            Log.i(getTag(tag), content)
        }

    }

    @JvmOverloads
    fun w(tag: String? = null, content: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.w(getTag(tag), content, throwable)
        } else {
            Log.w(getTag(tag), content)
        }
    }

    @JvmOverloads
    fun e(tag: String? = null, content: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(getTag(tag), content, throwable)
        } else {
            Log.e(getTag(tag), content)
        }
    }
}