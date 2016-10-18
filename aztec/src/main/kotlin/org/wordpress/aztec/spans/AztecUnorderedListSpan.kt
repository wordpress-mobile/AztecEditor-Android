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

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.Parcel
import android.text.Layout
import android.text.TextUtils
import android.text.style.BulletSpan

class AztecUnorderedListSpan : BulletSpan, AztecListSpan {

    private final val TAG = "ul"

    private var bulletColor: Int = 0
    private var bulletMargin: Int = 0
    private var bulletPadding: Int = 0
    private var bulletWidth: Int = 0


    //used for marking
    constructor() : super(0) {
    }

    override var attributes: String? = null

    constructor(attributes: String) {
        this.attributes = attributes
    }

    constructor(bulletColor: Int, bulletMargin: Int, bulletWidth: Int, bulletPadding: Int, attributes: String? = null) {
        this.bulletColor = bulletColor
        this.bulletMargin = bulletMargin
        this.bulletWidth = bulletWidth
        this.bulletPadding = bulletPadding
        this.attributes = attributes
    }

    constructor(src: Parcel) : super(src) {
        this.bulletColor = src.readInt()
        this.bulletMargin = src.readInt()
        this.bulletWidth = src.readInt()
        this.bulletPadding = src.readInt()
        this.attributes = src.readString()
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

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeInt(bulletColor)
        dest.writeInt(bulletMargin)
        dest.writeInt(bulletWidth)
        dest.writeInt(bulletPadding)
        dest.writeString(attributes)
    }

    override fun getLeadingMargin(first: Boolean): Int {
        return bulletMargin + 2 * bulletWidth + bulletPadding
    }

    override fun drawLeadingMargin(c: Canvas, p: Paint, x: Int, dir: Int,
                                   top: Int, baseline: Int, bottom: Int,
                                   text: CharSequence, start: Int, end: Int,
                                   first: Boolean, l: Layout) {
        if (!first) return

        val style = p.style

        val oldColor = p.color

        p.color = bulletColor
        p.style = Paint.Style.FILL

        if (c.isHardwareAccelerated) {
            if (bulletPath == null) {
                bulletPath = Path()
                // Bullet is slightly better to avoid aliasing artifacts on mdpi devices.
                bulletPath!!.addCircle(0.0f, 0.0f, bulletWidth.toFloat(), Path.Direction.CW)
            }

            c.save()
            c.translate((x + bulletMargin + dir * bulletWidth).toFloat(), (top + bottom) / 2.0f)
            c.drawPath(bulletPath!!, p)
            c.restore()
        } else {
            c.drawCircle((x + bulletMargin + dir * bulletWidth).toFloat(), (top + bottom) / 2.0f, bulletWidth.toFloat(), p)
        }

        p.color = oldColor
        p.style = style

    }

    companion object {
        private var bulletPath: Path? = null
    }
}
