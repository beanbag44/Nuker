package me.beanbag.nuker.module.modules

import mc.merge.ModCore.inventoryHandler
import mc.merge.event.EventBus.MAX_PRIORITY
import mc.merge.event.events.TickEvent
import mc.merge.event.onInGameEvent
import mc.merge.handler.PlacementHandler.airPlace
import mc.merge.handler.PlacementHandler.blockPlaceTimeouts
import mc.merge.inventory.SelectHotbarSlotAction
import mc.merge.module.Module
import mc.merge.module.modules.CoreConfig
import mc.merge.module.settings.SettingGroup
import mc.merge.types.VolumeSort
import mc.merge.util.BlockUtils
import mc.merge.util.BlockUtils.getBlockSphere
import mc.merge.util.BlockUtils.isSource
import mc.merge.util.InventoryUtils.getInHotbar
import net.minecraft.block.Blocks
import net.minecraft.util.math.Direction

class SourceRemover : Module("Source Remover", "Places blocks in water sources to remove them") {
    val generalGroup = addGroup(SettingGroup("General", "General settings for source remover"))
    private val sortMode by setting(generalGroup,
        "Sort Mode",
        "The order in which sources are removed",
        VolumeSort.Closest)
    private val whitelist by setting(generalGroup,
        "Whitelisted Blocks",
        "Sets what blocks can be used to fill the source blocks",
        arrayListOf(Blocks.STONE, Blocks.DIRT))

    init {
        onInGameEvent<TickEvent.Pre>(MAX_PRIORITY) {
            val blockVolume = getBlockSphere(player.eyePos, CoreConfig.placeRadius) { pos, state ->
                blockPlaceTimeouts.values().contains(pos)
                        || !isSource(state)
                        || !state.isReplaceable
            }
            if (blockVolume.isEmpty()) {
                inventoryHandler.releaseSlot(this@SourceRemover)
                return@onInGameEvent
            }

            BlockUtils.sortBlockVolume(blockVolume, player.eyePos, sortMode)

            val placeBlock = blockVolume.first()

            var blockSlot = -1
            for (block in whitelist) {
                blockSlot = getInHotbar(block.asItem())
                if (blockSlot != -1) break
            }
            if (blockSlot == -1) {
                inventoryHandler.releaseSlot(this@SourceRemover)
                return@onInGameEvent
            }
            inventoryHandler.selectSlot(this@SourceRemover, SelectHotbarSlotAction(blockSlot, true))
            airPlace(placeBlock.blockPos, Direction.UP, CoreConfig.swingOnPlace, CoreConfig.validatePlace)
        }
    }
}