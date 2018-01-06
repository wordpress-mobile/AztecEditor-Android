package org.wordpress.aztec.watchers.event

import org.wordpress.aztec.watchers.event.text.TextWatcherEvent

interface IEventInjector {
    fun executeEvent(data: TextWatcherEvent)
}
