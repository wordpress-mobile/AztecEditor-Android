package org.wordpress.aztec

import org.xml.sax.Attributes

fun Attributes.isTaskList() = this.getValue(TYPE) == TASK_LIST_TYPE
fun AztecAttributes.setTaskList() {
    if (!this.hasAttribute(TYPE)) {
        this.setValue(TYPE, TASK_LIST_TYPE)
    }
}
private const val TYPE = "type"
private const val TASK_LIST_TYPE = "task-list"
