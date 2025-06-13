package org.wordpress.aztec

import android.app.Activity
import android.widget.ToggleButton
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.toolbar.ToolbarAction

@RunWith(RobolectricTestRunner::class)
class AztecTextSelectionListenerTest {
    private lateinit var editText: AztecText
    private lateinit var toolbar: AztecToolbar
    private var clientListenerCallCount = 0
    private var lastClientSelectionStart = -1
    private var lastClientSelectionEnd = -1

    @Before
    fun init() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()
        editText = AztecText(activity)
        editText.setCalypsoMode(false)
        toolbar = AztecToolbar(activity)
        toolbar.setEditor(editText, null)

        // Reset counters
        clientListenerCallCount = 0
        lastClientSelectionStart = -1
        lastClientSelectionEnd = -1
    }

    @Test
    fun `test toolbar highlights styles when selection changes`() {
        // Add a client listener
        editText.setOnSelectionChangedListener(object : AztecText.OnSelectionChangedListener {
            override fun onSelectionChanged(selStart: Int, selEnd: Int) {
                clientListenerCallCount++
                lastClientSelectionStart = selStart
                lastClientSelectionEnd = selEnd
            }
        })

        // Add some text and make a selection
        editText.setText("Hello World")
        // setText triggers a selection change event, so we need to reset the counters
        clientListenerCallCount = 0
        editText.setSelection(0, 5)

        // Verify client listener was called
        Assert.assertEquals(1, clientListenerCallCount)
        Assert.assertEquals(0, lastClientSelectionStart)
        Assert.assertEquals(5, lastClientSelectionEnd)

        // Apply bold formatting
        val boldButton = toolbar.findViewById<ToggleButton>(ToolbarAction.BOLD.buttonId)
        boldButton.performClick()

        // Verify bold button is checked
        Assert.assertTrue("Bold button should be checked after applying bold formatting", boldButton.isChecked)

        // Make another selection
        editText.setSelection(6, 11)

        // Verify client listener was called again
        Assert.assertEquals(2, clientListenerCallCount)
        Assert.assertEquals(6, lastClientSelectionStart)
        Assert.assertEquals(11, lastClientSelectionEnd)

        // Verify bold button is unchecked for non-bold text
        Assert.assertFalse("Bold button should be unchecked for non-bold text", boldButton.isChecked)

        // Apply italic formatting
        val italicButton = toolbar.findViewById<ToggleButton>(ToolbarAction.ITALIC.buttonId)
        italicButton.performClick()

        // Verify italic button is checked
        Assert.assertTrue("Italic button should be checked after applying italic formatting", italicButton.isChecked)

        // Select the bold text again
        editText.setSelection(0, 5)

        // Verify bold button is checked again
        Assert.assertTrue("Bold button should be checked when selecting bold text", boldButton.isChecked)
        // Verify italic button is unchecked for non-italic text
        Assert.assertFalse("Italic button should be unchecked for non-italic text", italicButton.isChecked)
    }

    @Test
    fun `test client listener works before toolbar setup`() {
        // Add a client listener before setting up the toolbar
        editText.setOnSelectionChangedListener(object : AztecText.OnSelectionChangedListener {
            override fun onSelectionChanged(selStart: Int, selEnd: Int) {
                clientListenerCallCount++
                lastClientSelectionStart = selStart
                lastClientSelectionEnd = selEnd
            }
        })

        // Set up the toolbar after adding the listener
        toolbar.setEditor(editText, null)

        // Add some text and make a selection
        editText.setText("Hello World")
        // setText triggers a selection change event, so we need to reset the counters
        clientListenerCallCount = 0
        editText.setSelection(0, 5)

        // Verify client listener was called
        Assert.assertEquals(1, clientListenerCallCount)
        Assert.assertEquals(0, lastClientSelectionStart)
        Assert.assertEquals(5, lastClientSelectionEnd)
    }

    @Test
    fun `test client listener works after toolbar setup`() {
        // Set up the toolbar first
        toolbar.setEditor(editText, null)

        // Add a client listener after setting up the toolbar
        editText.setOnSelectionChangedListener(object : AztecText.OnSelectionChangedListener {
            override fun onSelectionChanged(selStart: Int, selEnd: Int) {
                clientListenerCallCount++
                lastClientSelectionStart = selStart
                lastClientSelectionEnd = selEnd
            }
        })

        // Add some text and make a selection
        editText.setText("Hello World")
        // setText triggers a selection change event, so we need to reset the counters
        clientListenerCallCount = 0
        editText.setSelection(0, 5)

        // Verify client listener was called
        Assert.assertEquals(1, clientListenerCallCount)
        Assert.assertEquals(0, lastClientSelectionStart)
        Assert.assertEquals(5, lastClientSelectionEnd)
    }

    @Test
    fun `test multiple client listeners work together`() {
        var secondClientListenerCallCount = 0
        var lastSecondClientSelectionStart = -1
        var lastSecondClientSelectionEnd = -1

        // Add first client listener
        editText.setOnSelectionChangedListener(object : AztecText.OnSelectionChangedListener {
            override fun onSelectionChanged(selStart: Int, selEnd: Int) {
                clientListenerCallCount++
                lastClientSelectionStart = selStart
                lastClientSelectionEnd = selEnd
            }
        })

        // Add second client listener
        editText.setOnSelectionChangedListener(object : AztecText.OnSelectionChangedListener {
            override fun onSelectionChanged(selStart: Int, selEnd: Int) {
                secondClientListenerCallCount++
                lastSecondClientSelectionStart = selStart
                lastSecondClientSelectionEnd = selEnd
            }
        })

        // Add some text and make a selection
        editText.setText("Hello World")
        // setText triggers a selection change event, so we need to reset the counters
        clientListenerCallCount = 0
        secondClientListenerCallCount = 0
        editText.setSelection(0, 5)

        // Verify both client listeners were called
        Assert.assertEquals(1, clientListenerCallCount)
        Assert.assertEquals(1, secondClientListenerCallCount)
        Assert.assertEquals(0, lastClientSelectionStart)
        Assert.assertEquals(5, lastClientSelectionEnd)
        Assert.assertEquals(0, lastSecondClientSelectionStart)
        Assert.assertEquals(5, lastSecondClientSelectionEnd)
    }
}
