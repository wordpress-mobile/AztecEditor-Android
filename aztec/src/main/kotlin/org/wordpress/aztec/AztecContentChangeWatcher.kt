package org.wordpress.aztec

import java.lang.ref.WeakReference

class AztecContentChangeWatcher {
    private val observers = mutableListOf<WeakReference<AztecTextChangeObserver>>()
    fun registerObserver(observer: AztecTextChangeObserver) {
        if (observers.none { it.get() == observer }) {
            observers.add(WeakReference(observer))
        }
    }

    fun unregisterObserver(observer: AztecTextChangeObserver) {
        observers.removeAll { it.get() == observer }
    }

    internal fun notifyContentChanged() {
        val iterator = observers.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            val foundObserver = item.get()
            if (foundObserver == null) {
                iterator.remove()
            } else {
                foundObserver.onContentChanged()
            }
        }
    }

    interface AztecTextChangeObserver {
        fun onContentChanged()
    }
}
