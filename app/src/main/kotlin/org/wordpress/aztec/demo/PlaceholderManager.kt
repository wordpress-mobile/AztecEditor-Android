package org.wordpress.aztec.demo

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Handler
import android.text.Editable
import android.text.Layout
import android.text.Spanned
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecContentChangeWatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.plugins.html2visual.IHtmlTagHandler
import org.wordpress.aztec.spans.AztecMediaClickableSpan
import org.xml.sax.Attributes

class PlaceholderManager(private val aztecText: AztecText, private val container: FrameLayout) : AztecContentChangeWatcher.AztecTextChangeObserver, IHtmlTagHandler, AztecText.OnMediaDeletedListener {
    private val drawers = mutableMapOf<String, PlaceholderDrawer>()
    private val positionToId = mutableMapOf<Int, String>()

    init {
        aztecText.contentChangeWatcher.registerObserver(this)
    }

    fun registerDrawer(type: String, placeholderDrawer: PlaceholderDrawer) {
        drawers[type] = placeholderDrawer
    }

    fun insertPlaceholder(id: String, type: String) {
        val attrs = getAttributesForMedia(id, type)
        val drawable = buildPlaceholderDrawable(type) ?: return
        aztecText.insertSpan(AztecPlaceholderSpan(aztecText.context, drawable, 0, attrs,
                this, aztecText))
        insertContentOverSpanWithId(id, null)
    }

    private fun buildPlaceholderDrawable(type: String): Drawable? {
        val drawer = drawers[type] ?: return null
        val drawable = ContextCompat.getDrawable(aztecText.context, android.R.color.transparent)!!
        drawable.setBounds(0, 0, aztecText.maxImagesWidth, drawer.getHeight(aztecText.maxImagesWidth))
        return drawable
    }

    private fun updateAllBelowSelection(selectionStart: Int) {
        val textViewLayout: Layout = aztecText.layout ?: return
        val currentLineForOffset = textViewLayout.getLineForOffset(selectionStart)
        positionToId.filterKeys {
            it > currentLineForOffset
        }.forEach {
            insertContentOverSpanWithId(it.value, it.key)
        }
    }

    private fun insertContentOverSpanWithId(id: String, currentPosition: Int? = null) {
        var aztecAttributes: AztecAttributes? = null
        val predicate = object : AztecText.AttributePredicate {
            override fun matches(attrs: Attributes): Boolean {
                val match = attrs.getValue(ID_ATTRIBUTE) == id
                if (match) {
                    aztecAttributes = attrs as AztecAttributes
                }
                return match
            }
        }
        val targetPosition = aztecText.getElementPosition(predicate) ?: return

        insertInPosition(aztecAttributes ?: return, targetPosition, currentPosition)
    }

    private fun insertInPosition(attrs: AztecAttributes, targetPosition: Int, currentPosition: Int? = null) {
        validateAttributes(attrs)
        val id = attrs.getValue(ID_ATTRIBUTE)
        val type = attrs.getValue(TYPE_ATTRIBUTE)
        val textViewLayout: Layout = aztecText.layout
        val parentTextViewRect = Rect()
        val currentLineStartOffset = textViewLayout.getLineForOffset(targetPosition)
        if (currentPosition != null) {
            val previousLineStartOffset = textViewLayout.getLineForOffset(currentPosition)
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
        val drawer = drawers[type]!!
        if (!exists) {
            box = drawer.onCreateView(container.context, id)
        }
        val params = FrameLayout.LayoutParams(
                parentTextViewRect.right - parentTextViewRect.left - 20,
                parentTextViewRect.bottom - parentTextViewRect.top - 20
        )
        params.setMargins(parentTextViewRect.left + 10, parentTextViewRect.top + 10, parentTextViewRect.right - 10, parentTextViewRect.bottom - 10)
        box.layoutParams = params
        box.tag = id
        positionToId[currentLineStartOffset] = id
        if (!exists && box.parent == null) {
            container.addView(box)
            drawer.onViewCreated(box, id)
        }
    }

    private fun validateAttributes(attributes: AztecAttributes): Boolean {
        if (!attributes.hasAttribute(ID_ATTRIBUTE)) return false
        if (!attributes.hasAttribute(TYPE_ATTRIBUTE)) return false
        val type = attributes.getValue(TYPE_ATTRIBUTE)
        return drawers[type] != null
    }

    private fun getAttributesForMedia(id: String, type: String): AztecAttributes {
        val attrs = AztecAttributes()
        attrs.setValue(ID_ATTRIBUTE, id)
        attrs.setValue(TYPE_ATTRIBUTE, type)
        return attrs
    }

    override fun onContentChanged() {
        updateAllBelowSelection(aztecText.selectionStart)
    }

    override fun onMediaDeleted(attrs: AztecAttributes) {
        if (validateAttributes(attrs)) {
            val id = attrs.getValue(ID_ATTRIBUTE)
            val drawer = drawers[attrs.getValue(TYPE_ATTRIBUTE)]
            drawer?.onPlaceholderDeleted(id)
            container.findViewWithTag<View>(id)?.let {
                it.visibility = View.GONE
                container.removeView(it)
            }
        }
    }

    override fun beforeMediaDeleted(attrs: AztecAttributes) {
        if (validateAttributes(attrs)) {
            val id = attrs.getValue(ID_ATTRIBUTE)
            container.findViewWithTag<View>(id)?.let {
                it.visibility = View.GONE
            }
        }
    }

    override fun canHandleTag(tag: String): Boolean {
        return tag == "placeholder"
    }

    override fun handleTag(opening: Boolean, tag: String, output: Editable, attributes: Attributes, nestingLevel: Int): Boolean {
        if (opening) {
            val drawable = buildPlaceholderDrawable(attributes.getValue(TYPE_ATTRIBUTE))
            val aztecAttributes = AztecAttributes(attributes)
            val span = AztecPlaceholderSpan(
                    context = aztecText.context,
                    drawable = drawable,
                    nestingLevel = nestingLevel,
                    attributes = aztecAttributes,
                    onMediaDeletedListener = this
            )
            val clickableSpan = AztecMediaClickableSpan(span)
            val position = output.length
            output.setSpan(span, position, position, Spanned.SPAN_MARK_MARK)
            output.setSpan(clickableSpan, position, position, Spanned.SPAN_MARK_MARK)
            output.append(Constants.IMG_CHAR)
            output.setSpan(clickableSpan, position, output.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            output.setSpan(span, position, output.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            span.applyInlineStyleAttributes(output, position, output.length)

            Handler().postDelayed({
                insertInPosition(aztecAttributes, position)
            }, 500)
        }
        return tag == "placeholder"
    }

    interface PlaceholderDrawer {
        fun onCreateView(context: Context, id: String): View
        fun onViewCreated(view: View, id: String) {}
        fun onPlaceholderDeleted(id: String) {}

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
            data class Fixed(val height: Int) : PlaceholderHeight()
            data class Ratio(val ratio: Float) : PlaceholderHeight()
        }
    }

    companion object {
        private const val ID_ATTRIBUTE = "id"
        private const val TYPE_ATTRIBUTE = "type"
    }
}
