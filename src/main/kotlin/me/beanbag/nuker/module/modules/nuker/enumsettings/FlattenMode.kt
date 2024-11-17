package me.beanbag.nuker.module.modules.nuker.enumsettings

import me.beanbag.nuker.module.settings.Describable

enum class FlattenMode : Describable {
    None, Standard, Smart, ReverseSmart, Staircase;

    fun isEnabled() = this != None

    fun isSmart() = this == Smart || this == ReverseSmart

    override val description: String
        get() = when (this) {
            None -> "All blocks are mined"
            Standard -> "Only blocks on the same level or above the player's feet are mined"
            Smart -> "Only blocks on the same level or above the player's feet are mined, as well as all blocks behind the player"
            ReverseSmart -> "Only blocks on the same level or above the player's feet are mined, as well as all blocks in front of the player"
            Staircase -> "Only blocks with the block above, above left, above right, above forward, and above backwards being empty essentially forming stairs"
        }
}