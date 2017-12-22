package org.wordpress.aztec.watchers.event;

import org.wordpress.aztec.watchers.event.text.TextWatcherEvent;

public interface IEventInjector {
    void executeEvent(TextWatcherEvent data);
}
