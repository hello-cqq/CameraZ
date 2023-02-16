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
 * File: ErrorLevel
 * Description:
 * Version: 1.0
 * Date : 2023/2/16
 * Author: hey.cqq@gmail.com
 *
 * ---------------------Revision History: ---------------------
 *  <author>           <data>          <version >       <desc>
 *  AhahahQ            2023/2/16         1.0         build this module
*/
package com.ahahahq.barcode.qrcode

/**
 * The error correction level of the QR code.
 * The higher the level, the more information the QR code carries,
 * even if it is blocked or incomplete, it can be recognized, but the density of the  QR code will be greater, and it will not be easy for ordinary scanners Recognition.
 * for example, it is recommended to choose a higher error correction level in the QR code with logo.
 */
enum class ErrorLevel {
    L,  // 7%
    M,  // 15%
    Q,  // 25%
    H   // 30%
}