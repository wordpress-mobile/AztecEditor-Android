package org.wordpress.aztec.plugins

import android.content.Context
import android.view.KeyEvent
import android.view.ViewGroup
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.toolbar.IToolbarAction
import org.wordpress.aztec.toolbar.RippleToggleButton

/**
 * An interface for implementing toolbar plugins.
 *
 * @property action the toolbar action type.
 * @property context the Android context.
 */
interface IToolbarButton : IAztecPlugin {
    val action: IToolbarAction
    val context: Context

    /**
     * Toggles a particular style.
     *
     * This method is called when the associated toolbar button is tapped or key shortcut is pressed.
     */
    fun toggle()

    /**
     * Determines, whether a particular key shortcut should trigger the toolbar action.
     *
     * @return true, if the key combination matches the action shortcut, false otherwise.
     */
    fun matchesKeyShortcut(keyCode: Int, event: KeyEvent): Boolean {
        return false
    }

    /**
     * A callback method used during the toolbar initialization.
     *
     * The implementor is responsible for implementing the inflation of a [RippleToggleButton] under the *parent*.
     *
     * @param parent view to be the parent of the generated hierarchy.
     */
    fun inflateButton(parent: ViewGroup)

    /**
     * Signals the ToolbarButton that the toolbar is about to change its enabled/disabled state
     *
     * This method is called when the toolbar buttons get "disabled/enabled"
     */
    fun toolbarStateAboutToChange(toolbar: AztecToolbar, enable: Boolean)
}