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

package org.wordpress.aztec

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.Parcel
import android.text.Layout
import android.text.TextUtils
import android.text.style.BulletSpan

class AztecBulletSpan : BulletSpan {

    private var bulletColor = DEFAULT_COLOR
    private var bulletRadius = DEFAULT_RADIUS
    private var bulletGapWidth = DEFAULT_GAP_WIDTH


    constructor(bulletColor: Int, bulletRadius: Int, bulletGapWidth: Int) {
        this.bulletColor = if (bulletColor != 0) bulletColor else DEFAULT_COLOR
        this.bulletRadius = if (bulletRadius != 0) bulletRadius else DEFAULT_RADIUS
        this.bulletGapWidth = if (bulletGapWidth != 0) bulletGapWidth else DEFAULT_GAP_WIDTH
    }

    constructor(src: Parcel) : super(src) {
        this.bulletColor = src.readInt()
        this.bulletRadius = src.readInt()
        this.bulletGapWidth = src.readInt()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeInt(bulletColor)
        dest.writeInt(bulletRadius)
        dest.writeInt(bulletGapWidth)
    }

    override fun getLeadingMargin(first: Boolean): Int {
        return 2 * bulletRadius + bulletGapWidth
    }

    override fun drawLeadingMargin(c: Canvas, p: Paint, x: Int, dir: Int,
                                   top: Int, baseline: Int, bottom: Int,
                                   text: CharSequence, start: Int, end: Int,
                                   first: Boolean, l: Layout) {
        TextUtils.split(text.toString(), "\n").forEach {
            val style = p.style

            val oldColor = p.color

            p.color = bulletColor
            p.style = Paint.Style.FILL

            if (c.isHardwareAccelerated) {
                if (bulletPath == null) {
                    bulletPath = Path()
                    // Bullet is slightly better to avoid aliasing artifacts on mdpi devices.
                    bulletPath!!.addCircle(0.0f, 0.0f, bulletRadius.toFloat(), Path.Direction.CW)
                }

                c.save()
                c.translate((x + dir * bulletRadius).toFloat(), (top + bottom) / 2.0f)
                c.drawPath(bulletPath!!, p)
                c.restore()
            } else {
                c.drawCircle((x + dir * bulletRadius).toFloat(), (top + bottom) / 2.0f, bulletRadius.toFloat(), p)
            }

            p.color = oldColor
            p.style = style
        }

    }

    companion object {
        private val DEFAULT_COLOR = 0
        private val DEFAULT_RADIUS = 3
        private val DEFAULT_GAP_WIDTH = 2
        private var bulletPath: Path? = null
    }
}
