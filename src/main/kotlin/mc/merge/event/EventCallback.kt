package mc.merge.event

import mc.merge.event.events.Event

class EventCallback(
    val callback: (Event) -> Unit,
    val subscriber: Any,
    val priority: Int,
)