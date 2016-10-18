/*
 * Copyright (C) 2016 Automattic
 * Copyright (C) 2015 Matthew Lee
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wordpress.aztec.spans

import android.os.Parcel
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.URLSpan

class AztecURLSpan : URLSpan, AztecContentSpan {

    private final val TAG: String = "a"

    private var linkColor = 0
    private var linkUnderline = true

    override var attributes: String? = null

    constructor(url: String, attributes: String? = null) : super(url) {
        if (attributes == null) {
            this.attributes = " href=\"$url\""
        } else {
            this.attributes = attributes
        }
    }

    constructor(url: String, linkColor: Int, linkUnderline: Boolean, attributes: String? = null) : this(url, attributes) {
        this.linkColor = linkColor
        this.linkUnderline = linkUnderline
    }

    constructor(src: Parcel) : super(src) {
        this.linkColor = src.readInt()
        this.linkUnderline = src.readInt() != 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeInt(linkColor)
        dest.writeInt(if (linkUnderline) 1 else 0)
    }

    override fun updateDrawState(ds: TextPaint) {
        ds.color = if (linkColor != 0) linkColor else ds.linkColor
        ds.isUnderlineText = linkUnderline
    }

    override fun getStartTag(): String {
        if (TextUtils.isEmpty(attributes)) {
            return TAG
        }
        return TAG + attributes
    }

    override fun getEndTag(): String {
        return TAG
    }
}
