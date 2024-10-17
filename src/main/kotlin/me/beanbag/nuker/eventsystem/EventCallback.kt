package me.beanbag.nuker.eventsystem

import me.beanbag.nuker.eventsystem.events.Event

class EventCallback(
    val callback: (Event) -> Unit,
    val subscriber: Any,
)