package me.beanbag.nuker.settings.enumsettings

enum class FlattenMode {
    None,
    Standard,
    Smart,
    ReverseSmart;

    fun isEnabled() =
        this != None

    fun isSmart() =
        this == Smart || this == ReverseSmart
}