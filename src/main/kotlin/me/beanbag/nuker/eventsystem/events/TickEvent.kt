package me.beanbag.nuker.eventsystem.events

open class TickEvent: Event {
    class Pre : TickEvent()
    class Post : TickEvent()
}