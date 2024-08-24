package me.beanbag.nuker.settings.enumsettings

enum class RenderType {
    None,
    Both,
    Fill,
    Line;

    fun enabled() =
        this != None
}