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
 * File: PublishExtension
 * Description:
 * Version: 1.0
 * Date : 2023/2/18
 * Author: hey.cqq@gmail.com
 *
 * ---------------------Revision History: ---------------------
 *  <author>           <data>          <version >       <desc>
 *  AhahahQ            2023/2/18         1.0         build this module
*/
package com.ahahahq.gradle

open class PublishExtension {
    companion object {
        const val EXTENSION_NAME = "QPublish"
    }

    var mavenName: String? = null
    var mavenUrl: String? = null
    var userName: String? = null
    var password: String? = null
    var groupId: String? = null
    var artifactId: String? = null
    var version: String? = null
    var description: String? = null
    var openUrl: String? = null
    var scmUrl: String? = null
    var needSource = false
}