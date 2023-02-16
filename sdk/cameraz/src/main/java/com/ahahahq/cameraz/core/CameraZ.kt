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

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import com.ahahahq.cameraz.callback.CameraStateCallback
import com.ahahahq.cameraz.callback.IDispatcher
import com.ahahahq.cameraz.util.CameraLog
import com.ahahahq.cameraz.common.SingletonHolder
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Entry class for camera open and shutdown
 */
class CameraZ private constructor(val context: Context) : IDispatcher {

    companion object : SingletonHolder<CameraZ, Context>(::CameraZ) {
        private const val TAG = "CameraZ"
        const val API_CAMERA1 = 1
        const val API_CAMERA2 = 2
    }

    private var cameraAdapter: CameraAdapter? = null

    //    private var cameraExecutor: Executor? = null
    private var workThread: WorkThread? = null
    private var isReleased = AtomicBoolean(false)

    /**
     * open the camera driver.
     *
     * @param cameraId Camera id
     * @param stateCallback Camera status callback, Successful opened will return a {@link CameraClient}
     * @param api Indicates which camera api is used. {@link API_CAMERA1} is camera1 and {@link API_CAMERA2} is camera2
     * @param handler the handler on which the stateCallback should be invoked, or
     * {@code null} to use the current workThread's {@link android.os.Looper}.
     */
    @JvmOverloads
    @Synchronized
    fun open(
        cameraId: String,
        stateCallback: CameraStateCallback,
        api: Int = API_CAMERA1,
        handler: Handler? = null
    ) {
        checkApiValid(api)
        if (!checkPermission(context)) {
            throw CameraException(CameraException.ERR_APP, "No requested Camera permission!")
        }
        isReleased.set(false)
        var thread = workThread
        if (!checkThreadWork()) {
            thread = WorkThread().apply { name = TAG }
            thread.start()
        }
        workThread = thread
        val workHandler = workThread?.acquireHandler()
        workHandler?.post {
            synchronized(isReleased) {
                if (!isReleased.get()) {
                    if (cameraAdapter == null) {
                        cameraAdapter = CameraAdapter(context, api)
                    }
                    cameraAdapter?.open(cameraId, stateCallback, handler ?: workHandler)
                }
            }
        }
    }

    /**
     * To determine whether the camera API is available in the current model, you need to pay attention to it when using {@link API_CAMERA2},
     * it can only be used if supported by the hal.
     */
    private fun checkApiValid(api: Int): Boolean {
        if ((api != API_CAMERA1) && (api != API_CAMERA2)) {
            throw CameraException(
                CameraException.ERR_ARG,
                "Input Api: $api is invalid! Please use API_CAMERA1 or API_CAMERA2."
            )
        }
        val adapter = cameraAdapter
        if ((adapter != null) && (adapter.cameraApi != api)) {
            throw CameraException(
                CameraException.ERR_ARG,
                "Input Api: $api is invalid! Only one Api is supported at the same time."
            )
        }
        if (api == API_CAMERA2) {
            // we only hope you using camera2 in a new version.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                throw CameraException(
                    CameraException.ERR_ARG,
                    "Input Api: $api is invalid! Camera2 only supported above ${Build.VERSION_CODES.P}"
                )
            }
        }
        return true
    }

    private fun checkPermission(context: Context): Boolean {
        return context.packageManager.checkPermission(
            Manifest.permission.CAMERA,
            context.packageName
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isOpening(cameraId: String): Boolean {
        return cameraAdapter?.checkOpening(cameraId) ?: false
    }

    fun isOpened(cameraId: String): Boolean {
        return cameraAdapter?.checkOpened(cameraId) ?: false
    }

    fun getClient(cameraId: String): CameraClient? {
        return cameraAdapter?.getClient(cameraId)
    }

    /**
     * Only close the camera with the specified id
     */
    @Synchronized
    fun close(cameraId: String) {
        CameraLog.d(TAG, "close $cameraId")
        if (checkThreadWork()) {
            workThread?.acquireHandler()?.post { cameraAdapter?.close(cameraId) }
        }
    }

    /**
     * Release the camera and interrupt the thread
     */
    @Synchronized
    fun closeAll() {
        if (checkThreadWork()) {
            CameraLog.d(TAG, "closeAll")
            workThread?.acquireHandler()?.post {
                synchronized(isReleased) {
                    cameraAdapter?.closeAll()
                    cameraAdapter = null
                    CameraLog.d(TAG, "closeAll success")
                }
            }
            isReleased.set(true)
        }
    }

    /**
     * 注意：确保不再使用相机时，再释放线程资源，否则在Activity生命周期变化极端情况下，重建线程可能会导致同步问题
     */
    @Synchronized
    fun release() {
        workThread?.interrupt(true)
        workThread = null
        isReleased.set(true)
    }

    private fun checkThreadWork(): Boolean {
        val thread = workThread
        val result = !((thread == null) || thread.isInterrupted || !thread.isAlive)
        CameraLog.d(TAG, "checkThreadWork $result")
        return result
    }

    @Synchronized
    override fun dispatch(r: Runnable) {
        //暂时没有实现具体逻辑，预留使用
        if (checkThreadWork()) {
            workThread?.acquireHandler()?.post(r)
        }
    }
}