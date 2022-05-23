package org.wordpress.aztec.demo

import android.content.Context
import android.graphics.Color
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
import java.util.UUID

class PlaceholderManager(private val aztecText: AztecText, private val container: FrameLayout) : AztecContentChangeWatcher.AztecTextChangeObserver, IHtmlTagHandler, AztecText.OnMediaDeletedListener {
    private val drawers = mutableMapOf<String, PlaceholderDrawer>()
    private val positionToId = mutableSetOf<Placeholder>()

    init {
        aztecText.contentChangeWatcher.registerObserver(this)
    }

    fun registerDrawer(type: String, placeholderDrawer: PlaceholderDrawer) {
        drawers[type] = placeholderDrawer
    }

    fun insertPlaceholder(id: String, type: String, vararg attributes: Pair<String, String>) {
        val attrs = getAttributesForMedia(id, type, attributes)
        val drawable = buildPlaceholderDrawable(type) ?: return
        aztecText.insertSpan(AztecPlaceholderSpan(aztecText.context, drawable, 0, attrs,
                this, aztecText))
        insertContentOverSpanWithId(attrs.getValue(UUID_ATTRIBUTE), null)
    }

    private fun buildPlaceholderDrawable(type: String): Drawable? {
        val drawer = drawers[type] ?: return null
        val drawable = ContextCompat.getDrawable(aztecText.context, android.R.color.transparent)!!
        drawable.setBounds(0, 0, aztecText.maxImagesWidth, drawer.getHeight(aztecText.maxImagesWidth))
        return drawable
    }

    private fun updateAllBelowSelection(selectionStart: Int) {
        positionToId.filter {
            it.elementPosition >= selectionStart - 1
        }.forEach {
            insertContentOverSpanWithId(it.uuid, it.elementPosition)
        }
    }

    private fun insertContentOverSpanWithId(uuid: String, currentPosition: Int? = null) {
        var aztecAttributes: AztecAttributes? = null
        val predicate = object : AztecText.AttributePredicate {
            override fun matches(attrs: Attributes): Boolean {
                val match = attrs.getValue(UUID_ATTRIBUTE) == uuid
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
        val uuid = attrs.getValue(UUID_ATTRIBUTE)
        val type = attrs.getValue(TYPE_ATTRIBUTE)
        val textViewLayout: Layout = aztecText.layout
        val parentTextViewRect = Rect()
        val targetLineOffset = getLineForOffset(targetPosition)
        if (currentPosition != null) {
            if (targetLineOffset != 0 && currentPosition == targetPosition) {
                return
            } else {
                positionToId.removeAll {
                    it.uuid == uuid
                }
            }
        }
        textViewLayout.getLineBounds(targetLineOffset, parentTextViewRect)

        val parentTextViewLocation = intArrayOf(0, 0)
        aztecText.getLocationOnScreen(parentTextViewLocation)
        val parentTextViewTopAndBottomOffset = aztecText.scrollY + aztecText.compoundPaddingTop

        parentTextViewRect.top += parentTextViewTopAndBottomOffset
        parentTextViewRect.bottom += parentTextViewTopAndBottomOffset

        var box = container.findViewWithTag<View>(uuid)
        val exists = box != null
        val drawer = drawers[type]!!
        if (!exists) {
            box = drawer.onCreateView(container.context, id, attrs)
        }
        val params = FrameLayout.LayoutParams(
                parentTextViewRect.right - parentTextViewRect.left - 20,
                parentTextViewRect.bottom - parentTextViewRect.top - 20
        )
        val padding = 10
        params.setMargins(parentTextViewRect.left + padding, parentTextViewRect.top + padding, parentTextViewRect.right - padding, parentTextViewRect.bottom - padding)
        box.layoutParams = params
        box.tag = uuid
        box.setBackgroundColor(Color.TRANSPARENT)
        positionToId.add(Placeholder(targetPosition, id, uuid))
        if (!exists && box.parent == null) {
            container.addView(box)
            drawer.onViewCreated(box, id)
        }
    }

    private fun getLineForOffset(offset: Int): Int {
        var counter = 0
        var index = 0
        for (line in aztecText.text.split("\n")) {
            counter += line.length + 1
            if (counter > offset) {
                break
            }
            index += 1
        }
        return index
    }

    private fun validateAttributes(attributes: AztecAttributes): Boolean {
        if (!attributes.hasAttribute(UUID_ATTRIBUTE)) return false
        if (!attributes.hasAttribute(TYPE_ATTRIBUTE)) return false
        val type = attributes.getValue(TYPE_ATTRIBUTE)
        return drawers[type] != null
    }

    private fun getAttributesForMedia(id: String, type: String, attributes: Array<out Pair<String, String>>): AztecAttributes {
        val attrs = AztecAttributes()
        attrs.setValue(ID_ATTRIBUTE, id)
        attrs.setValue(UUID_ATTRIBUTE, UUID.randomUUID().toString())
        attrs.setValue(TYPE_ATTRIBUTE, type)
        attributes.forEach {
            attrs.setValue(it.first, it.second)
        }
        return attrs
    }

    override fun onContentChanged() {
        updateAllBelowSelection(aztecText.selectionStart)
    }

    override fun onMediaDeleted(attrs: AztecAttributes) {
        if (validateAttributes(attrs)) {
            val uuid = attrs.getValue(UUID_ATTRIBUTE)
            val id = attrs.getValue(ID_ATTRIBUTE)
            val drawer = drawers[attrs.getValue(TYPE_ATTRIBUTE)]
            drawer?.onPlaceholderDeleted(id)
            positionToId.removeAll { it.uuid == uuid }
            container.findViewWithTag<View>(uuid)?.let {
                it.visibility = View.GONE
                container.removeView(it)
            }
        }
    }

    override fun beforeMediaDeleted(attrs: AztecAttributes) {
        if (validateAttributes(attrs)) {
            val uuid = attrs.getValue(UUID_ATTRIBUTE)
            container.findViewWithTag<View>(uuid)?.let {
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
            aztecAttributes.setValue(UUID_ATTRIBUTE, UUID.randomUUID().toString())
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
        fun onCreateView(context: Context, id: String, attrs: AztecAttributes): View
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

    data class Placeholder(val elementPosition: Int, val id: String, val uuid: String)

    companion object {
        private const val ID_ATTRIBUTE = "id"
        private const val UUID_ATTRIBUTE = "uuid"
        private const val TYPE_ATTRIBUTE = "type"
    }
}
