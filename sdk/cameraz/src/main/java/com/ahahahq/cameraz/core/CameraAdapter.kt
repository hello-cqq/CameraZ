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
import android.os.Build
import android.os.Handler
import com.ahahahq.cameraz.callback.CameraStateCallback
import com.ahahahq.cameraz.util.CameraLog
import java.lang.Exception
import java.lang.RuntimeException
import java.util.concurrent.ConcurrentHashMap

/**
 * Adapters for different camera api, And is responsible for monitoring the status of the camera.
 */
internal class CameraAdapter constructor(private val context: Context, val cameraApi: Int) {
    private val cameraMap: ConcurrentHashMap<String, CameraClient> = ConcurrentHashMap()

    /**
     * Start the actual implementation of the camera.
     */
    fun open(cameraId: String, stateCallback: CameraStateCallback, handler: Handler) {
        CameraLog.d(TAG, "open: $cameraId")
        if (checkOpened(cameraId)) {
            CameraLog.i(TAG, "camera #$cameraId has opened, no need to open again.")
            return
        }
        if (checkOpening(cameraId)) {
            CameraLog.i(TAG, "camera #$cameraId is opening, wait...")
            return
        }
        val proxy = if (cameraApi == CameraZ.API_CAMERA1) {
            Camera1Proxy(context, handler)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Camera2Proxy(context, handler)
            } else {
                throw RuntimeException("API_CAMERA2 is only supported android version above 28!")
            }
        }
        try {
            cameraMap[cameraId] = CameraClient(proxy).apply {
                cameraState = CameraClient.CameraState.OPENING
            }
            proxy.open(cameraId, CameraStateProxy(stateCallback, cameraId))
        } catch (e: Exception) {
            cameraMap.remove(cameraId)
            throw CameraException(CameraException.ERR_RUNTIME, e)
        }
    }

    /**
     * Get an active camera client by specifying the id.
     */
    fun getClient(id: String): CameraClient? {
        return cameraMap[id]
    }

    /**
     * Determine whether the current camera is opened.
     */
    fun checkOpened(id: String): Boolean {
        val client = cameraMap[id]
        if ((client != null) && (client.cameraState == CameraClient.CameraState.OPENED)) {
            return true
        }
        return false
    }

    fun checkOpening(id: String): Boolean {
        val client = cameraMap[id]
        if ((client != null) && (client.cameraState == CameraClient.CameraState.OPENING)) {
            return true
        }
        return false
    }

    fun close(id: String) {
        cameraMap[id]?.close()
    }

    fun closeAll() {
        for ((_, value) in cameraMap.entries) {
            value.close()
        }
    }

    /**
     * The agent of the camera status. The purpose is to monitor and notify the business side
     * to facilitate the timely management and control of the camera.
     */
    inner class CameraStateProxy(
        private val callback: CameraStateCallback,
        private val cameraId: String
    ) : CameraProxyStateCallback {
        override fun onOpened(proxy: CameraProxy) {
            CameraLog.d(TAG, "onOpened $cameraId")
            if (cameraMap[cameraId] == null) {
                cameraMap[cameraId] = CameraClient(proxy)
            }
            val client = cameraMap[cameraId]
            client?.cameraState = CameraClient.CameraState.OPENED
            callback.onOpened(client!!)
        }

        override fun onDisconnected() {
            CameraLog.d(TAG, "onDisconnected $cameraId")
            cameraMap[cameraId]?.cameraState = CameraClient.CameraState.DISCONNECTED
            cameraMap.remove(cameraId)
        }

        override fun onError(o: Any?) {
            CameraLog.d(TAG, "onError $cameraId: $o")
            cameraMap[cameraId]?.cameraState = CameraClient.CameraState.ERROR
            cameraMap.remove(cameraId)
        }

        override fun onClosed() {
            CameraLog.d(TAG, "onClosed $cameraId")
            cameraMap[cameraId]?.cameraState = CameraClient.CameraState.CLOSED
            cameraMap.remove(cameraId)
        }
    }

    companion object {
        private const val TAG = "CameraAdapter"
    }
}