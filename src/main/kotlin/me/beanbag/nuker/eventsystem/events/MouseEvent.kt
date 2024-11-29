package me.beanbag.nuker.eventsystem.events

import me.beanbag.nuker.types.KeyAction

data class MouseEvent(
    val button: Int,
    val action: KeyAction
) : Event, ICancellable by Cancellable()