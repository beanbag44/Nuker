package me.beanbag.nuker.modules.nuker.enumsettings

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