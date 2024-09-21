package me.beanbag.nuker.eventsystem.events

sealed class TickEvent(
    val tickDelta: Float
) : Event {
    class Pre(tickDelta: Float) : TickEvent(tickDelta)
    class Post(tickDelta: Float) : TickEvent(tickDelta)
}