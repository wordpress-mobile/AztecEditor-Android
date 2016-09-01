package org.wordpress.aztec.util

import android.content.Context
import android.graphics.Typeface
import android.widget.TextView
import java.util.*

object TypefaceCache {

    val TYPEFACE_DEJAVU_SANS_MONO = "DejaVuSansMono.ttf"
    val TYPEFACE_MERRIWEATHER_REGULAR = "Merriweather-Regular.ttf"

    private val mTypefaceCache = Hashtable<String, Typeface>()

    fun getTypeface(context: Context, typefaceName: String): Typeface? {
        if (!mTypefaceCache.containsKey(typefaceName)) {
            var typeface: Typeface? = null
            try {
                typeface = Typeface.createFromAsset(context.applicationContext.assets, "fonts/" + typefaceName)
            }
            catch (e: RuntimeException) {
            }

            if (typeface != null) {
                mTypefaceCache.put(typefaceName, typeface)
            }
        }

        return mTypefaceCache[typefaceName]
    }

    /*
     * sets the typeface for a TextView (or TextView descendant such as EditText or Button) based on
     * the passed attributes, defaults to normal typeface
     */
    fun setCustomTypeface(context: Context, view: TextView, typefaceName: String) {
        // skip at design-time
        if (view.isInEditMode) return

        val typeface = getTypeface(context, typefaceName)
        if (typeface != null) {
            view.typeface = typeface
        }
    }
}
