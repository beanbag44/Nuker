package me.beanbag.nuker.eventsystem.events

import me.beanbag.nuker.types.KeyAction

data class KeyEvent(
    val key: Int,
    val modifiers: Int,
    val action: KeyAction
) : Event, ICancellable by Cancellable()