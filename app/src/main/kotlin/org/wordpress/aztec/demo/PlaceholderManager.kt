package org.wordpress.aztec.demo

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.Layout
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecContentChangeWatcher
import org.wordpress.aztec.AztecText
import org.xml.sax.Attributes

class PlaceholderManager(private val aztecText: AztecText, private val container: FrameLayout) : AztecContentChangeWatcher.AztecTextChangeObserver {
    private val drawers = mutableMapOf<String, PlaceholderDrawer>()
    private val positionToId = mutableMapOf<Int, String>()
    init {
        aztecText.contentChangeWatcher.registerObserver(this)
        registerDrawer("sample1", SampleDrawer1(aztecText.context))
        registerDrawer("sample2", SampleDrawer2(aztecText.context))
    }
    fun registerDrawer(type: String, placeholderDrawer: PlaceholderDrawer) {
        drawers[type] = placeholderDrawer
    }

    fun insertPlaceholder(id: String, type: String) {
        val drawer = drawers[type] ?: return
        val drawable = drawer.placeholderBackground
        drawable.setBounds(0, 0, aztecText.maxImagesWidth, drawer.getHeight(aztecText.maxImagesWidth))
        val attrs = getAttributesForMedia(id, type)
        aztecText.insertPlaceholder(drawable, attrs)
        insertContent(id, null)
    }

    private fun updateAllBelowSelection(selectionStart: Int) {
        val textViewLayout: Layout = aztecText.layout ?: return
        val currentLineForOffset = textViewLayout.getLineForOffset(selectionStart)
        positionToId.filterKeys {
            it >= currentLineForOffset
        }.forEach {
            insertContent(it.value, it.key)
        }
    }

    private fun insertContent(id: String, currentPosition: Int? = null) {
        var type: String? = null
        val predicate = object : AztecText.AttributePredicate {
            override fun matches(attrs: Attributes): Boolean {
                val match = attrs.getValue("id") == id
                if (match) {
                    type = attrs.getValue("type")
                }
                return match
            }
        }
        val textViewLayout: Layout = aztecText.layout
        val placeholderPosition = aztecText.getElementPosition(predicate) ?: return

        val parentTextViewRect = Rect()
        val currentLineStartOffset = textViewLayout.getLineForOffset(placeholderPosition)
        Log.d("vojta", "Current line offset: $currentLineStartOffset")
        if (currentPosition != null) {
            val previousLineStartOffset = textViewLayout.getLineForOffset(currentPosition)
            Log.d("vojta", "Previous line offset: $previousLineStartOffset")
            if (previousLineStartOffset == currentLineStartOffset) {
                return
            } else {
                positionToId.remove(currentLineStartOffset)
            }
        }
        textViewLayout.getLineBounds(currentLineStartOffset, parentTextViewRect)

        val parentTextViewLocation = intArrayOf(0, 0)
        aztecText.getLocationOnScreen(parentTextViewLocation)
        val parentTextViewTopAndBottomOffset = aztecText.scrollY + aztecText.compoundPaddingTop

        parentTextViewRect.top += parentTextViewTopAndBottomOffset
        parentTextViewRect.bottom += parentTextViewTopAndBottomOffset

        var box = container.findViewWithTag<View>(id)
        val exists = box != null
        if (!exists) {
            val drawer = drawers[type]!!
            box = drawer.drawView(container.context)
        }
        val params = FrameLayout.LayoutParams(
                parentTextViewRect.right - parentTextViewRect.left,
                parentTextViewRect.bottom - parentTextViewRect.top
        )
        params.setMargins(parentTextViewRect.left, parentTextViewRect.top, parentTextViewRect.right, parentTextViewRect.bottom)
        Log.d("vojta", "Box exists: $exists, setting height to ${parentTextViewRect.bottom - parentTextViewRect.top}")
        Log.d("vojta", "- setting left margin to: ${parentTextViewRect.left}, top margin to ${parentTextViewRect.top}")
        Log.d("vojta", "- setting right margin to: ${parentTextViewRect.right}, top margin to ${parentTextViewRect.bottom}")
        box.layoutParams = params
        box.tag = id
        positionToId[currentLineStartOffset] = id
        Log.d("vojta", "- current line: $currentLineStartOffset")
        if (!exists) {
            container.addView(box)
        } else {
            box.invalidate()
        }
    }

    private fun getAttributesForMedia(id: String, type: String): AztecAttributes {
        val attrs = AztecAttributes()
        attrs.setValue("id", id)
        attrs.setValue("type", type)
        return attrs
    }

    override fun onContentChanged() {
        Log.d("vojta", "Selection changed: ${aztecText.selectionStart}")
        updateAllBelowSelection(aztecText.selectionStart)
    }

    interface PlaceholderDrawer {
        fun drawView(context: Context): View
        val placeholderBackground: Drawable
        val placeholderHeight: PlaceholderHeight
        fun getHeight(width: Int): Int {
            return placeholderHeight.let {
                when (it) {
                    is PlaceholderHeight.Fixed -> it.height
                    is PlaceholderHeight.Ratio -> (it.ratio * width).toInt()
                }
            }
        }

        sealed class PlaceholderHeight {
            data class Fixed(val height: Int): PlaceholderHeight()
            data class Ratio(val ratio: Float): PlaceholderHeight()
        }
    }

    class SampleDrawer1(context: Context) : PlaceholderDrawer {
        override fun drawView(context: Context): View {
            return View(context).apply {
                setBackgroundResource(R.color.test_color_2)
            }
        }
        override val placeholderBackground: Drawable = ColorDrawable(ContextCompat.getColor(context, R.color.test_color_1))
        override val placeholderHeight: PlaceholderDrawer.PlaceholderHeight = PlaceholderDrawer.PlaceholderHeight.Ratio(0.5f)
    }

    class SampleDrawer2(context: Context) : PlaceholderDrawer {
        override fun drawView(context: Context): View {
            return View(context).apply {
                setBackgroundResource(R.color.test_color_1)
            }
        }
        override val placeholderBackground: Drawable = ColorDrawable(ContextCompat.getColor(context, R.color.test_color_2))
        override val placeholderHeight: PlaceholderDrawer.PlaceholderHeight = PlaceholderDrawer.PlaceholderHeight.Ratio(0.5f)
    }
}
