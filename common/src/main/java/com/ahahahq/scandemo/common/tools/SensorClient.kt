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

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs

class SensorClient(private val context: Context) {
    companion object {
        private const val UPDATE_INTERVAL: Long = 100
        private const val STATIC_DURATION = 1500L
        private const val STATUS_STATIC = 0
        private const val STATUS_MOVE = 1
        private const val STATIC_THRESHOLD = 0.5f
    }

    private var sensorManager: SensorManager? = null
    private var accelerometerSensor: Sensor? = null
    private var sensorListener: SensorEventListener = AccelerometerSensorListener()
    private val listenerList = ArrayList<SensorClientListener>()
    private var isRegistered = false
    fun start() {
        if (!isRegistered) {
            if (sensorManager == null) {
                sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            }
            if (accelerometerSensor == null) {
                accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            }
            sensorManager?.registerListener(sensorListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
            isRegistered = true
        }
    }

    fun stop() {
        if (isRegistered) {
            sensorManager?.unregisterListener(sensorListener)
            accelerometerSensor = null
        }
    }

    fun addListener(listener: SensorClientListener) {
        if (!listenerList.contains(listener)) {
            listenerList.add(listener)
        }
    }

    fun removeListener(listener: SensorClientListener) {
        listenerList.remove(listener)
    }

    private inner class AccelerometerSensorListener : SensorEventListener {

        private var mX = 0f
        private var mY = 0f
        private var mZ = 0f
        private var mLastStaticStamp: Long = 0
        private var mLastUpdateTime: Long = 0
        private var mStatus = STATUS_MOVE
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // ignore
        }

        override fun onSensorChanged(event: SensorEvent) {
            val currentTime = System.currentTimeMillis()
            val diffTime = abs(currentTime - mLastUpdateTime)
            if (diffTime < UPDATE_INTERVAL) {
                return
            }
            mLastUpdateTime = currentTime
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val stamp = System.currentTimeMillis()
            val deltaX = abs(mX - x)
            val deltaY = abs(mY - y)
            val deltaZ = abs(mZ - z)
            if (deltaX > STATIC_THRESHOLD || deltaY > STATIC_THRESHOLD || deltaZ > STATIC_THRESHOLD) {
                if (mStatus != STATUS_MOVE) {
                    for (listener in listenerList) {
                        listener.onMoving()
                    }
                }
                // phone is moving
                mStatus = STATUS_MOVE
                mLastStaticStamp = 0
            } else {
                // phone is no move
                if (mStatus == STATUS_MOVE) {
                    mLastStaticStamp = System.currentTimeMillis()
                }
                if (stamp - mLastStaticStamp > STATIC_DURATION) {
                    for (listener in listenerList) {
                        listener.onStatic()
                    }
                }
                mStatus = STATUS_STATIC
            }
            mX = x
            mY = y
            mZ = z
        }
    }

    interface SensorClientListener {
        fun onMoving()
        fun onStatic()
    }
}