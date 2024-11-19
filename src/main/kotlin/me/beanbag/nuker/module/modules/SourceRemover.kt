package me.beanbag.nuker.module.modules

import me.beanbag.nuker.eventsystem.EventBus.MAX_PRIORITY
import me.beanbag.nuker.eventsystem.events.TickEvent
import me.beanbag.nuker.eventsystem.onInGameEvent
import me.beanbag.nuker.handlers.PlacementHandler.airPlace
import me.beanbag.nuker.handlers.PlacementHandler.blockPlaceTimeouts
import me.beanbag.nuker.module.Module
import me.beanbag.nuker.module.settings.SettingGroup
import me.beanbag.nuker.types.PosAndState
import me.beanbag.nuker.utils.BlockUtils.getBlockSphere
import me.beanbag.nuker.utils.BlockUtils.isFlowing
import me.beanbag.nuker.utils.BlockUtils.isSource
import me.beanbag.nuker.utils.InventoryUtils.getInHotbar
import me.beanbag.nuker.utils.InventoryUtils.swapTo
import net.minecraft.block.Blocks
import net.minecraft.util.math.Direction

class SourceRemover : Module("Source Remover", "Places blocks in water sources to remove them") {
    val generalGroup = addGroup(SettingGroup("General", "General settings for source remover"))
    private val whitelist by setting(generalGroup,
        "Whitelisted Blocks",
        "Sets what blocks can be used to fill the source blocks",
        arrayListOf(Blocks.STONE, Blocks.DIRT))

    init {
        onInGameEvent<TickEvent.Pre>(MAX_PRIORITY) {
            if (!enabled) return@onInGameEvent

            val blockVolume = getBlockSphere(player.eyePos, CoreConfig.placeRadius) { pos, state ->
                blockPlaceTimeouts.values().contains(pos)
                        || !isSource(state)
                        || !state.isReplaceable
            }
            if (blockVolume.isEmpty()) return@onInGameEvent
            val placeBlock = blockVolume.first()

            var blockSlot = -1
            for (block in whitelist) {
                blockSlot = getInHotbar(block.asItem())
                if (blockSlot != -1) break
            }
            if (blockSlot == -1) return@onInGameEvent
            swapTo(blockSlot)
            airPlace(placeBlock.blockPos, Direction.UP, CoreConfig.swingOnPlace, CoreConfig.validatePlace)
        }
    }
}