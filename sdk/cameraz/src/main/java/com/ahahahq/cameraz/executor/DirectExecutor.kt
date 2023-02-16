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
package com.ahahahq.cameraz.executor

import java.util.concurrent.Executor

/**
 * An [Executor] that runs each task in the thread that invokes [ execute][Executor.execute].
 */
class DirectExecutor : Executor {
    override fun execute(command: Runnable) {
        command.run()
    }

    companion object {
        @Volatile
        private var directExecutor: DirectExecutor? = null
        val instance: Executor?
            get() {
                if (directExecutor != null) {
                    return directExecutor
                }
                synchronized(DirectExecutor::class.java) {
                    if (directExecutor == null) {
                        directExecutor = DirectExecutor()
                    }
                }
                return directExecutor
            }
    }
}