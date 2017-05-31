import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.Spannable
import org.wordpress.aztec.Constants
import org.wordpress.aztec.R
import org.wordpress.aztec.plugins.IAztecPlugin
import org.wordpress.aztec.plugins.html2visual.IAztecCommentHandler
import org.wordpress.aztec.plugins.wpcomments.spans.WordPressCommentSpan
import org.wordpress.aztec.spans.AztecCommentSpan

class WordPressCommentsPlugin : IAztecCommentHandler {

    override fun handleComment(text: String, output: Editable, context: Context, nestingLevel: Int) {

        val spanStart = output.length

        if (text.toLowerCase() == AztecCommentSpan.Comment.MORE.html.toLowerCase()) {

            output.append(Constants.MAGIC_CHAR)

            output.setSpan(
                    WordPressCommentSpan(
                            text,
                            context,
                            ContextCompat.getDrawable(context, R.drawable.img_more),
                            nestingLevel
                    ),
                    spanStart,
                    text.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else if (text.toLowerCase() == AztecCommentSpan.Comment.PAGE.html.toLowerCase()) {

            output.append(Constants.MAGIC_CHAR)

            output.setSpan(
                    WordPressCommentSpan(
                            text,
                            context,
                            ContextCompat.getDrawable(context, R.drawable.img_page),
                            nestingLevel
                    ),
                    spanStart,
                    text.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
}