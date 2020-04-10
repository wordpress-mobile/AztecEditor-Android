package org.wordpress.aztec

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AztecContentChangeWatcherTest {
    private lateinit var aztecContentChangeWatcher: AztecContentChangeWatcher
    @Before
    fun setUp() {
        aztecContentChangeWatcher = AztecContentChangeWatcher()
    }

    @Test
    fun `notifies registered observer`() {
        // Given
        var contentChanged = false
        setupRegisteredObserver {
            contentChanged = true
        }

        // When
        aztecContentChangeWatcher.notifyContentChanged()

        // Then
        assertTrue(contentChanged)
    }

    @Test
    fun `does not notify unregistered observer`() {
        // Given
        var contentChanged = false
        val observer = setupRegisteredObserver {
            contentChanged = true
        }

        // When
        aztecContentChangeWatcher.unregisterObserver(observer)
        aztecContentChangeWatcher.notifyContentChanged()

        // Then
        assertFalse(contentChanged)
    }

    @Test
    fun `observer is garbage collected and reference is lost`() {
        // Given
        var contentChanged = false
        setupRegisteredObserver {
            contentChanged = true
        }
        System.gc()

        // When
        aztecContentChangeWatcher.notifyContentChanged()

        // Then
        assertFalse(contentChanged)
    }

    private fun setupRegisteredObserver(onContentChanged: () -> Unit): AztecContentChangeWatcher.AztecTextChangeObserver {
        val observer = object : AztecContentChangeWatcher.AztecTextChangeObserver {
            override fun onContentChanged() {
                onContentChanged()
            }
        }
        aztecContentChangeWatcher.registerObserver(observer)
        return observer
    }
}
