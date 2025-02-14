package me.beanbag.nuker.module.modules.nuker.enumsettings

import me.beanbag.nuker.module.settings.Describable

enum class WhitelistMode : Describable {
    None,
    Blacklist,
    Whitelist;

    override val description: String
        get() = when (this) {
            None -> "All blocks are mined"
            Blacklist -> "All Blocks are mined except for the ones in the blacklist"
            Whitelist -> "Only blocks in the whitelist are mined"
        }
}