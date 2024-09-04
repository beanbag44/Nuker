package me.beanbag.nuker.external

interface ClientWrapper {
    fun loadSettings()
    fun onInitialize(): () -> Unit
}