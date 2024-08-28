package me.beanbag.nuker.settings.enumsettings

enum class FlattenMode : Describable {
    None {
        override val description: String
            get() = "All blocks are mined"
    },
    Standard {
        override val description: String
            get() = "Only blocks on the same level or above the player's feet are mined"
    },
    Smart {
        override val description: String
            get() = "Only blocks on the same level or above the player's feet are mined, as well as all blocks behind the player"
    },
    ReverseSmart {
        override val description: String
            get() = "Only blocks on the same level or above the player's feet are mined, as well as all blocks in front of the player"
    };

    fun isEnabled() =
        this != None

    fun isSmart() =
        this == Smart || this == ReverseSmart
}