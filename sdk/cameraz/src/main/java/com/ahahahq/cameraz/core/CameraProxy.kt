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
package com.ahahahq.cameraz.core

import android.content.Context
import android.os.Handler
import com.ahahahq.cameraz.callback.PictureCallback
import com.ahahahq.cameraz.model.CameraParams
import com.ahahahq.cameraz.model.Preview

internal abstract class CameraProxy internal constructor(val context: Context, val handler: Handler) {

    companion object {
        const val STATE_DESTROY = 0
        const val STATE_PREVIEWING = 1
        const val STATE_STOP = 2
        const val STATE_PICTURE = 3
    }
    protected var cameraStateCallback: CameraProxyStateCallback? = null
    @Volatile
    protected var previewState = STATE_DESTROY

    internal abstract fun open(cameraId: String, stateCallback: CameraProxyStateCallback)
    internal abstract fun getId(): String

    internal abstract fun preview(preview: Preview)
    internal abstract fun preview(previewList: List<Preview>)

    internal abstract fun setState(state: Int)

    internal abstract fun setParams(params: CameraParams)
    internal abstract fun getParams(): CameraParams

    internal abstract fun picture(id: String, callback: PictureCallback)
    internal abstract fun close()
}