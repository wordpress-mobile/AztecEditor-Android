package org.wordpress.aztec.util

import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.support.annotation.ColorInt
import android.text.TextUtils

/**
 * Converts color strings into color integers used by android. This translation process will attempt to
 * convert the string in the following order:
 * 1. If the color string starts with an "@" symbol then attempt to find a matching android color resource.
 * 2. Check the color string against the static map of CSS color translations.
 * 3. Finally, attempt to parse the color from the string using [Color.parseColor]
 * If no valid translation is found, then the [COLOR_NOT_FOUND] code is returned.
 */
class ColorConverter {

    companion object {
        /**
         * Indicates no valid color translation could be found.
         */
        const val COLOR_NOT_FOUND = -1

        /**
         * Stores static named color translations for CSS colors that differ
         * from the android [Color] class.
         */
        //region Static Color Mappings
        private val colorMap = mapOf(
            "aliceblue" to 0xFFF0F8FF.toInt(),
            "antiquewhite" to 0xFFFAEBD7.toInt(),
            "aqua" to 0xFF00FFFF.toInt(),
            "aquamarine" to 0xFF7FFFD4.toInt(),
            "azure" to 0xFFF0FFFF.toInt(),
            "beige" to 0xFFF5F5DC.toInt(),
            "bisque" to 0xFFFFE4C4.toInt(),
            "black" to 0xFF000000.toInt(),
            "blanchedalmond" to 0xFFFFEBCD.toInt(),
            "blue" to 0xFF0000FF.toInt(),
            "blueviolet" to 0xFF8A2BE2.toInt(),
            "brown" to 0xFFA52A2A.toInt(),
            "burlywood" to 0xFFDEB887.toInt(),
            "cadetblue" to 0xFF5F9EA0.toInt(),
            "chartreuse" to 0xFF7FFF00.toInt(),
            "chocolate" to 0xFFD2691E.toInt(),
            "coral" to 0xFFFF7F50.toInt(),
            "cornflowerblue" to 0xFF6495ED.toInt(),
            "cornsilk" to 0xFFFFF8DC.toInt(),
            "crimson" to 0xFFDC143C.toInt(),
            "cyan" to 0xFF00FFFF.toInt(),
            "darkblue" to 0xFF00008B.toInt(),
            "darkcyan" to 0xFF008B8B.toInt(),
            "darkgoldenrod" to 0xFFB8860B.toInt(),
            "darkgray" to 0xFFA9A9A9.toInt(),
            "darkgrey" to 0xFFA9A9A9.toInt(),
            "darkgreen" to 0xFF006400.toInt(),
            "darkkhaki" to 0xFFBDB76B.toInt(),
            "darkmagenta" to 0xFF8B008B.toInt(),
            "darkolivegreen" to 0xFF556B2F.toInt(),
            "darkorange" to 0xFFFF8C00.toInt(),
            "darkorchid" to 0xFF9932CC.toInt(),
            "darkred" to 0xFF8B0000.toInt(),
            "darksalmon" to 0xFFE9967A.toInt(),
            "darkseagreen" to 0xFF8FBC8F.toInt(),
            "darkslateblue" to 0xFF483D8B.toInt(),
            "darkslategray" to 0xFF2F4F4F.toInt(),
            "darkslategrey" to 0xFF2F4F4F.toInt(),
            "darkturquoise" to 0xFF00CED1.toInt(),
            "darkviolet" to 0xFF9400D3.toInt(),
            "deeppink" to 0xFFFF1493.toInt(),
            "deepskyblue" to 0xFF00BFFF.toInt(),
            "dimgray" to 0xFF696969.toInt(),
            "dimgrey" to 0xFF696969.toInt(),
            "dodgerblue" to 0xFF1E90FF.toInt(),
            "firebrick" to 0xFFB22222.toInt(),
            "floralwhite" to 0xFFFFFAF0.toInt(),
            "forestgreen" to 0xFF228B22.toInt(),
            "fuchsia" to 0xFFFF00FF.toInt(),
            "gainsboro" to 0xFFDCDCDC.toInt(),
            "ghostwhite" to 0xFFF8F8FF.toInt(),
            "gold" to 0xFFFFD700.toInt(),
            "goldenrod" to 0xFFDAA520.toInt(),
            "gray" to 0xFF808080.toInt(),
            "grey" to 0xFF808080.toInt(),
            "green" to 0xFF008000.toInt(),
            "greenyellow" to 0xFFADFF2F.toInt(),
            "honeydew" to 0xFFF0FFF0.toInt(),
            "hotpink" to 0xFFFF69B4.toInt(),
            "indianred " to 0xFFCD5C5C.toInt(),
            "indigo  " to 0xFF4B0082.toInt(),
            "ivory" to 0xFFFFFFF0.toInt(),
            "khaki" to 0xFFF0E68C.toInt(),
            "lavender" to 0xFFE6E6FA.toInt(),
            "lavenderblush" to 0xFFFFF0F5.toInt(),
            "lawngreen" to 0xFF7CFC00.toInt(),
            "lemonchiffon" to 0xFFFFFACD.toInt(),
            "lightblue" to 0xFFADD8E6.toInt(),
            "lightcoral" to 0xFFF08080.toInt(),
            "lightcyan" to 0xFFE0FFFF.toInt(),
            "lightgoldenrodyellow" to 0xFFFAFAD2.toInt(),
            "lightgray" to 0xFFD3D3D3.toInt(),
            "lightgrey" to 0xFFD3D3D3.toInt(),
            "lightgreen" to 0xFF90EE90.toInt(),
            "lightpink" to 0xFFFFB6C1.toInt(),
            "lightsalmon" to 0xFFFFA07A.toInt(),
            "lightseagreen" to 0xFF20B2AA.toInt(),
            "lightskyblue" to 0xFF87CEFA.toInt(),
            "lightslategray" to 0xFF778899.toInt(),
            "lightslategrey" to 0xFF778899.toInt(),
            "lightsteelblue" to 0xFFB0C4DE.toInt(),
            "lightyellow" to 0xFFFFFFE0.toInt(),
            "lime" to 0xFF00FF00.toInt(),
            "limegreen" to 0xFF32CD32.toInt(),
            "linen" to 0xFFFAF0E6.toInt(),
            "magenta" to 0xFFFF00FF.toInt(),
            "maroon" to 0xFF800000.toInt(),
            "mediumaquamarine" to 0xFF66CDAA.toInt(),
            "mediumblue" to 0xFF0000CD.toInt(),
            "mediumorchid" to 0xFFBA55D3.toInt(),
            "mediumpurple" to 0xFF9370DB.toInt(),
            "mediumseagreen" to 0xFF3CB371.toInt(),
            "mediumslateblue" to 0xFF7B68EE.toInt(),
            "mediumspringgreen" to 0xFF00FA9A.toInt(),
            "mediumturquoise" to 0xFF48D1CC.toInt(),
            "mediumvioletred" to 0xFFC71585.toInt(),
            "midnightblue" to 0xFF191970.toInt(),
            "mintcream" to 0xFFF5FFFA.toInt(),
            "mistyrose" to 0xFFFFE4E1.toInt(),
            "moccasin" to 0xFFFFE4B5.toInt(),
            "navajowhite" to 0xFFFFDEAD.toInt(),
            "navy" to 0xFF000080.toInt(),
            "oldlace" to 0xFFFDF5E6.toInt(),
            "olive" to 0xFF808000.toInt(),
            "olivedrab" to 0xFF6B8E23.toInt(),
            "orange" to 0xFFFFA500.toInt(),
            "orangered" to 0xFFFF4500.toInt(),
            "orchid" to 0xFFDA70D6.toInt(),
            "palegoldenrod" to 0xFFEEE8AA.toInt(),
            "palegreen" to 0xFF98FB98.toInt(),
            "paleturquoise" to 0xFFAFEEEE.toInt(),
            "palevioletred" to 0xFFDB7093.toInt(),
            "papayawhip" to 0xFFFFEFD5.toInt(),
            "peachpuff" to 0xFFFFDAB9.toInt(),
            "peru" to 0xFFCD853F.toInt(),
            "pink" to 0xFFFFC0CB.toInt(),
            "plum" to 0xFFDDA0DD.toInt(),
            "powderblue" to 0xFFB0E0E6.toInt(),
            "purple" to 0xFF800080.toInt(),
            "rebeccapurple" to 0xFF663399.toInt(),
            "red" to 0xFFFF0000.toInt(),
            "rosybrown" to 0xFFBC8F8F.toInt(),
            "royalblue" to 0xFF4169E1.toInt(),
            "saddlebrown" to 0xFF8B4513.toInt(),
            "salmon" to 0xFFFA8072.toInt(),
            "sandybrown" to 0xFFF4A460.toInt(),
            "seagreen" to 0xFF2E8B57.toInt(),
            "seashell" to 0xFFFFF5EE.toInt(),
            "sienna" to 0xFFA0522D.toInt(),
            "silver" to 0xFFC0C0C0.toInt(),
            "skyblue" to 0xFF87CEEB.toInt(),
            "slateblue" to 0xFF6A5ACD.toInt(),
            "slategray" to 0xFF708090.toInt(),
            "slategrey" to 0xFF708090.toInt(),
            "snow" to 0xFFFFFAFA.toInt(),
            "springgreen" to 0xFF00FF7F.toInt(),
            "steelblue" to 0xFF4682B4.toInt(),
            "tan" to 0xFFD2B48C.toInt(),
            "teal" to 0xFF008080.toInt(),
            "thistle" to 0xFFD8BFD8.toInt(),
            "tomato" to 0xFFFF6347.toInt(),
            "turquoise" to 0xFF40E0D0.toInt(),
            "violet" to 0xFFEE82EE.toInt(),
            "wheat" to 0xFFF5DEB3.toInt(),
            "white" to 0xFFFFFFFF.toInt(),
            "whitesmoke" to 0xFFF5F5F5.toInt(),
            "yellow" to 0xFFFFFF00.toInt(),
            "yellowgreen" to 0xFF9ACD32.toInt())
        //endregion

        /**
         * Checks if the named color string is a pointer to a resource color. Examples of valid
         * color strings:
         * + @white - uses the android color.white resource.
         * + lime - valid css or [android.graphics.Color] label.
         * + #FF00FF00 - valid hex.
         * @param [colorText] The color string to check.
         * @return True if the color string matches the pattern of a resource color, else false.
         */
        private fun isColorResource(colorText: String): Boolean {
            if (!TextUtils.isEmpty(colorText) && colorText.startsWith('@')) {
                val res = Resources.getSystem()
                val name = colorText.substring(1)
                val colorRes = res.getIdentifier(name, "color", "android")
                return colorRes != 0
            }
            return false
        }

        /**
         * Translates the provided color string into a color int. Examples of valid
         * color strings:
         * + @white - uses the android color.white resource
         * + lime - valid css or [android.graphics.Color] label
         * + #FF00FF00 - valid hex
         * @param [colorText] The color string to translate to a color integer.
         * @return An integer translation of the color string, or [COLOR_NOT_FOUND] if the string
         * cannot be translated.
         */
        @ColorInt
        fun getColorInt(colorText: String): Int {
            try {
                if (isColorResource(colorText)) {
                    val res = Resources.getSystem()
                    val name = colorText.substring(1)
                    val colorRes = res.getIdentifier(name, "color", "android")
                    if (colorRes != 0) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                            return res.getColor(colorRes)
                        } else {
                            return res.getColor(colorRes, null)
                        }
                    }
                }
                //
                // Attempt to pull color value from local CSS color map
                val c = colorMap.get(colorText)
                if (c != null) {
                    return c
                }
                //
                // No CSS values found, parse using android Color class
                return Color.parseColor(colorText)
            } catch (e: Exception) {
                //
                // Unrecognized color or empty string
                when (e) {
                    is IllegalArgumentException, is StringIndexOutOfBoundsException -> {
                        return COLOR_NOT_FOUND
                    }
                    else -> throw e
                }
            }
        }
    }
}