package org.wordpress.aztec.util

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wordpress.aztec.BuildConfig
import org.mockito.Mockito.`when` as whenever

/**
 * Tests for translating various strings to colors using [ColorConverter].
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(23))
class ColorConverterTest {

    /**
     * Test a valid android resource color string is properly
     * translated to a color int.
     */
    @Test
    fun getColorFromValidResource_valid() {
        val colorString = "@black"
        val colorInt = ColorConverter.getColorInt(colorString)
        Assert.assertTrue(colorInt != ColorConverter.COLOR_NOT_FOUND)
    }

    /**
     * Test an *invalid* android resource color string is handled
     * gracefully and the [ColorConverter.COLOR_NOT_FOUND] is returned
     * indicating the string could not be translated.
     */
    @Test
    fun getColorFromResource_invalid() {
        val colorString = "@blooper"
        val colorInt = ColorConverter.getColorInt(colorString)
        Assert.assertEquals(colorInt, ColorConverter.COLOR_NOT_FOUND)
    }

    /**
     * Test a valid hexidecimal color string is properly translated to a color int.
     */
    @Test
    fun getColorFromHex_valid() {
        val colorString = "#FF00FF00"
        val colorInt = ColorConverter.getColorInt(colorString)
        Assert.assertTrue(colorInt != ColorConverter.COLOR_NOT_FOUND)
    }

    /**
     * Test an *invalid* hexidecimal color string is handled
     * gracefully and the [ColorConverter.COLOR_NOT_FOUND] is returned
     * indicating the string could not be translated.
     */
    @Test
    fun getColorFromHex_invalid() {
        val colorString = "#Fdoo"
        val colorInt = ColorConverter.getColorInt(colorString)
        Assert.assertEquals(colorInt, ColorConverter.COLOR_NOT_FOUND)
    }

    /**
     * Test a valid color name string is properly translated to a color int.
     */
    @Test
    fun getColorFromName_valid() {
        val colorString = "blue"
        val colorInt = ColorConverter.getColorInt(colorString)
        Assert.assertTrue(colorInt != ColorConverter.COLOR_NOT_FOUND)
    }

    /**
     * Test an *invalid* color name string is handled
     * gracefully and the [ColorConverter.COLOR_NOT_FOUND] is returned
     * indicating the string could not be translated.
     */
    @Test
    fun getColorFromName_invalid() {
        val colorString = "jattlyn"
        val colorInt = ColorConverter.getColorInt(colorString)
        Assert.assertEquals(colorInt, ColorConverter.COLOR_NOT_FOUND)
    }

    /**
     * Test an *empty* color string is handled gracefully and the
     * [ColorConverter.COLOR_NOT_FOUND] is returned
     * indicating the string could not be translated.
     */
    @Test
    fun getColorFromEmptyString() {
        val colorString = ""
        val colorInt = ColorConverter.getColorInt(colorString)
        Assert.assertEquals(colorInt, ColorConverter.COLOR_NOT_FOUND)
    }
}