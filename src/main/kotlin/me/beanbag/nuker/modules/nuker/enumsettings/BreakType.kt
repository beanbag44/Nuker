package me.beanbag.nuker.modules.nuker.enumsettings

enum class BreakType {
    Primary, Secondary;

    fun isPrimary() =
        this == Primary
}