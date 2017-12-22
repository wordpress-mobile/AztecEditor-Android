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

import android.text.TextPaint
import android.text.style.URLSpan
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.formatting.LinkFormatter

class AztecURLSpan : URLSpan, IAztecInlineSpan {
    override val TAG = "a"

    private var linkColor = 0
    private var linkUnderline = true

    override var attributes: AztecAttributes = AztecAttributes()

    constructor(url: String, attributes: AztecAttributes = AztecAttributes()) : super(url) {
        this.attributes = attributes

        if (!this.attributes.hasAttribute("href")) {
            this.attributes.setValue("href", url)
        }
    }

    constructor(url: String, linkStyle: LinkFormatter.LinkStyle, attributes: AztecAttributes = AztecAttributes()) : this(url, attributes) {
        this.linkColor = linkStyle.linkColor
        this.linkUnderline = linkStyle.linkUnderline
    }

    override fun updateDrawState(ds: TextPaint) {
        ds.color = if (linkColor != 0) linkColor else ds.linkColor
        ds.isUnderlineText = linkUnderline
    }
}
