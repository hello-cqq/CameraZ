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
 * File: CodeType
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

import android.os.Parcel
import android.os.Parcelable

enum class CodeType(var codeType: Int) : Parcelable {

    EAN13(1),
    EAN8(2),
    UPC_A(4),
    UPC_E(8),
    EAN14(128),
    CODE39(16),
    CODE93(256),
    CODE128(32),
    ITF(64),
    QR_CODE(512),
    DM_CODE(1024),
    PDF417(2048),
    RSS_EXPANDED(4096),
    AZTEC_CODE(65536),
    ONE_CODE(EAN13.codeType or EAN8.codeType or UPC_A.codeType or UPC_E.codeType or CODE39.codeType or CODE128.codeType or ITF.codeType or EAN14.codeType or CODE93.codeType),
    INDUSTRY_CODE(CODE39.codeType or CODE128.codeType),
    PRODUCT(ONE_CODE.codeType),
    MEDICINE(ONE_CODE.codeType),
    EXPRESS(ONE_CODE.codeType),
    TB_ANTI_FAKE(QR_CODE.codeType),
    ALL_QR_CODE(QR_CODE.codeType),
    ALL_BARCODE(ONE_CODE.codeType),
    ALL_LOTTERY_CODE(DM_CODE.codeType or PDF417.codeType),
    ALL_CODE(ONE_CODE.codeType or ALL_QR_CODE.codeType or DM_CODE.codeType or AZTEC_CODE.codeType or PDF417.codeType),
    DEFAULT_CODE(ONE_CODE.codeType or ALL_QR_CODE.codeType or AZTEC_CODE.codeType or DM_CODE.codeType);

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CodeType> {
        override fun createFromParcel(parcel: Parcel): CodeType {
            return values()[parcel.readInt()]
        }

        override fun newArray(size: Int): Array<CodeType?> {
            return arrayOfNulls(size)
        }
    }
}