package me.beanbag.nuker.module.modules.nuker.enumsettings

enum class BreakType {
    Primary, Secondary;

    fun isPrimary() =
        this == Primary
}