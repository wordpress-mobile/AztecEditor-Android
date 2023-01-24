package org.wordpress.aztec.spans

import android.text.Layout
import android.text.TextPaint
import android.text.style.CharacterStyle
import org.wordpress.aztec.AlignmentRendering
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.formatting.BlockFormatter
import java.lang.StringBuilder

fun createListItemSpan(nestingLevel: Int,
                       alignmentRendering: AlignmentRendering,
                       attributes: AztecAttributes = AztecAttributes(),
                       listItemStyle: BlockFormatter.ListItemStyle = BlockFormatter.ListItemStyle(false, 0)): IAztecBlockSpan = when (alignmentRendering) {
    AlignmentRendering.SPAN_LEVEL -> AztecListItemSpanAligned(nestingLevel, attributes, null, listItemStyle)
    AlignmentRendering.VIEW_LEVEL -> AztecListItemSpan(nestingLevel, attributes, listItemStyle)
}

/**
 * We need to have two classes for handling alignment at either the Span-level (ListItemSpanAligned)
 * or the View-level (ListItemSpan). IAztecAlignment implements AlignmentSpan, which has a
 * getAlignment method that returns a non-null Layout.Alignment. The Android system checks for
 * AlignmentSpans and, if present, overrides the view's gravity with their value. Having a class
 * that does not implement AlignmentSpan allows the view's gravity to control. These classes should
 * be created using the createListItemSpan(...) methods.
 */
open class AztecListItemSpan(
        override var nestingLevel: Int,
        override var attributes: AztecAttributes,
        var listItemStyle: BlockFormatter.ListItemStyle = BlockFormatter.ListItemStyle(
                false, 0)) : CharacterStyle(), IAztecCompositeBlockSpan {
    fun toggleCheck() {
        if (attributes.getValue(CHECKED) == "true") {
            attributes.setValue(CHECKED, "false")
        } else {
            attributes.setValue(CHECKED, "true")
        }
    }

    /**
     * This override makes sure that if the list item is in a task list (= the parameter "checked" is present), we
     * print the `<input type=checkbox/>` as the first element of the generated HTML. This shouldn't happen in any
     * other list type.
     */
    override val startTag: String
        get() {
            val shouldReplaceCheckedAttribute = attributes.hasAttribute(CHECKED)
            return if (shouldReplaceCheckedAttribute) {
                val sb = StringBuilder()
                sb.append(TAG)
                for (i in 0 until attributes.length) {
                    val localName = attributes.getLocalName(i)
                    if (localName != CHECKED) {
                        sb.append(" ")
                        sb.append(localName)
                        sb.append("=\"")
                        sb.append(attributes.getValue(i))
                        sb.append("\"")
                    }
                }
                sb.append(">")
                if (shouldReplaceCheckedAttribute) {
                    sb.append("<input type=\"checkbox\" class=\"task-list-item-checkbox\"")
                    if (attributes.getValue(CHECKED) == "true") {
                        sb.append(" checked")
                    }
                    sb.append(" /")
                }
                return sb.toString()
            } else {
                super.startTag
            }

        }
    override val textFormat: ITextFormat? = null

    override val TAG = "li"

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1

    companion object {
        const val CHECKED = "checked"
    }

    override fun updateDrawState(tp: TextPaint) {
        val isChecked = attributes.getValue(CHECKED) == "true"

        if (listItemStyle.strikeThroughCheckedItems) {
            tp.isStrikeThruText = isChecked
        }

        if (listItemStyle.checkedItemsTextColor != 0 && isChecked) {
            tp.color = listItemStyle.checkedItemsTextColor
        }
    }
}

class AztecListItemSpanAligned(
        nestingLevel: Int,
        attributes: AztecAttributes,
        override var align: Layout.Alignment?,
        listItemStyle: BlockFormatter.ListItemStyle = BlockFormatter.ListItemStyle(false, 0)
) : AztecListItemSpan(nestingLevel, attributes, listItemStyle), IAztecAlignmentSpan
