package me.beanbag.nuker.module.modules.nuker

import me.beanbag.nuker.ModConfigs.mc
import me.beanbag.nuker.eventsystem.EventBus
import me.beanbag.nuker.eventsystem.events.TickEvent
import me.beanbag.nuker.handlers.BreakingHandler.blockTimeouts
import me.beanbag.nuker.handlers.BreakingHandler.checkAttemptBreaks
import me.beanbag.nuker.handlers.BreakingHandler.updateBreakingContexts
import me.beanbag.nuker.module.Module
import me.beanbag.nuker.module.modules.CoreConfig
import me.beanbag.nuker.module.modules.nuker.enumsettings.FlattenMode
import me.beanbag.nuker.module.modules.nuker.enumsettings.VolumeShape
import me.beanbag.nuker.module.settings.SettingGroup
import me.beanbag.nuker.types.VolumeSort
import me.beanbag.nuker.utils.BlockUtils.filterBlocksToBaritoneSelections
import me.beanbag.nuker.utils.BlockUtils.filterBlocksToBreakable
import me.beanbag.nuker.utils.BlockUtils.filterBlocksToCanal
import me.beanbag.nuker.utils.BlockUtils.filterBlocksToFlatten
import me.beanbag.nuker.utils.BlockUtils.filterCorrectlyPlacedLitematicaBlocks
import me.beanbag.nuker.utils.BlockUtils.filterLiquidSupportingBlocks
import me.beanbag.nuker.utils.BlockUtils.getBlockCube
import me.beanbag.nuker.utils.BlockUtils.getBlockSphere
import me.beanbag.nuker.utils.BlockUtils.sortBlockVolume
import me.beanbag.nuker.utils.LitematicaUtils.updateSchematicMismatches

object Nuker : Module("Epic Nuker", "Epic nuker for nuking terrain") {

    /*
    settings
     */

    val generalGroup = addGroup(SettingGroup("General", "General settings for nuker"))
    val shape by setting(generalGroup,"Shape", "The shape used to select the blocks to break", VolumeShape.Sphere, null) { true }
    val mineStyle by setting(generalGroup, "Mine Style", "The order which blocks are broken in", VolumeSort.Closest, null) { true }
    val flattenMode by setting(generalGroup, "Flatten Mode", "The style which nuker flattens terrain with", FlattenMode.Standard, null) { true }
    val avoidLiquids by setting(generalGroup, "Avoid Spilling Liquids", "Doesn't break blocks that would in turn let fluids flow", false, null) { true }
    val crouchLowersFlatten by setting(generalGroup, "Crouch Lowers Flatten", "Lets crouching lower the flatten level by one block", false, null) { true }
    val canalMode by setting(generalGroup, "Canal Mode", "Only breaks blocks that need to be removed for the southern canal", false, null) { true }
    val baritoneSelection by setting(generalGroup, "Baritone Selection", "Only breaks blocks inside baritone selections", false, null) { true }
    val litematicaMode by setting(generalGroup, "Litematica", "Only breaks blocks that are incorrectly placed in schematics", false, null) { true }

    /*
     */

    init {
        EventBus.subscribe<TickEvent.Pre>(this) {
            if (!enabled) return@subscribe

            mc.player?.let { player ->
                if (CoreConfig.onGround && !player.isOnGround) return@subscribe

                val blockVolume = getBlockVolume()

                filterBlocksToBreakable(blockVolume)

                if (flattenMode.isEnabled()) {
                    filterBlocksToFlatten(blockVolume, crouchLowersFlatten, flattenMode)
                }

                if (avoidLiquids) {
                    filterLiquidSupportingBlocks(blockVolume)
                }

                if (baritoneSelection) {
                    filterBlocksToBaritoneSelections(blockVolume)
                }

                if (litematicaMode) {
                    updateSchematicMismatches()
                    filterCorrectlyPlacedLitematicaBlocks(blockVolume)
                }

                if (canalMode) {
                    filterBlocksToCanal(blockVolume)
                }

                blockVolume.removeIf {
                    blockTimeouts.values().contains(it.blockPos)
                }

                sortBlockVolume(blockVolume, player.eyePos, mineStyle)

                checkAttemptBreaks(blockVolume)
            }
        }
        for (settingGroup in CoreConfig.settingGroups) {
            settingGroups.add(settingGroup)
        }
    }

    private fun getBlockVolume() =
        mc.player?.run {
            if (shape == VolumeShape.Sphere) {
                getBlockSphere(this.eyePos, CoreConfig.radius)
            } else {
                getBlockCube(this.eyePos, CoreConfig.radius)
            }
        } ?: ArrayList()
}