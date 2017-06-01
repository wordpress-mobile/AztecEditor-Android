import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.Spannable
import org.wordpress.aztec.Constants
import org.wordpress.aztec.plugins.html2visual.IAztecCommentHandler
import org.wordpress.aztec.plugins.wpcomments.R
import org.wordpress.aztec.plugins.wpcomments.spans.WordPressCommentSpan

class WordPressCommentsPlugin : IAztecCommentHandler {

    override fun handleComment(text: String, output: Editable, context: Context, nestingLevel: Int) : Boolean {

        val spanStart = output.length

        if (text.toLowerCase() == WordPressCommentSpan.Comment.MORE.html.toLowerCase()) {

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
            return true
        } else if (text.toLowerCase() == WordPressCommentSpan.Comment.PAGE.html.toLowerCase()) {

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
            return true
        }
        return false
    }
}