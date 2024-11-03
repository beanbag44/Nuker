package me.beanbag.nuker.module.modules.nuker

import me.beanbag.nuker.eventsystem.events.TickEvent
import me.beanbag.nuker.eventsystem.onInGameEvent
import me.beanbag.nuker.handlers.BreakingHandler.blockTimeouts
import me.beanbag.nuker.handlers.BreakingHandler.checkAttemptBreaks
import me.beanbag.nuker.module.Module
import me.beanbag.nuker.module.modules.CoreConfig
import me.beanbag.nuker.module.modules.nuker.enumsettings.FlattenMode
import me.beanbag.nuker.module.modules.nuker.enumsettings.VolumeShape
import me.beanbag.nuker.module.settings.SettingGroup
import me.beanbag.nuker.types.VolumeSort
import me.beanbag.nuker.utils.BlockUtils.getBlockCube
import me.beanbag.nuker.utils.BlockUtils.getBlockSphere
import me.beanbag.nuker.utils.BlockUtils.isBlockBreakable
import me.beanbag.nuker.utils.BlockUtils.isBlockInFlatten
import me.beanbag.nuker.utils.BlockUtils.isValidCanalBlock
import me.beanbag.nuker.utils.BlockUtils.isWithinABaritoneSelection
import me.beanbag.nuker.utils.BlockUtils.sortBlockVolume
import me.beanbag.nuker.utils.BlockUtils.willReleaseLiquids
import me.beanbag.nuker.utils.InGame
import me.beanbag.nuker.utils.LitematicaUtils
import me.beanbag.nuker.utils.LitematicaUtils.updateSchematicMismatches
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

object Nuker : Module("Epic Nuker", "Epic nuker for nuking terrain") {

    /*
    settings
     */

    val generalGroup = addGroup(SettingGroup("General", "General settings for nuker"))
    private val shape by setting(generalGroup,
        "Shape",
        "The shape used to select the blocks to break",
        VolumeShape.Sphere, null) { true }
    private val mineStyle by setting(generalGroup,
        "Mine Style",
        "The order which blocks are broken in",
        VolumeSort.Closest, null) { true }
    private val flattenMode by setting(generalGroup,
        "Flatten Mode",
        "The style which nuker flattens terrain with",
        FlattenMode.Standard, null) { true }
    private val avoidLiquids by setting(generalGroup,
        "Avoid Spilling Liquids",
        "Doesn't break blocks that would in turn let fluids flow",
        false, null) { true }
    private val crouchLowersFlatten by setting(generalGroup,
        "Crouch Lowers Flatten",
        "Lets crouching lower the flatten level by one block",
        false, null) { true }
    private val canalMode by setting(generalGroup,
        "Canal Mode",
        "Only breaks blocks that need to be removed for the southern canal",
        false, null) { true }
    private val baritoneSelection by setting(generalGroup,
        "Baritone Selection",
        "Only breaks blocks inside baritone selections",
        false, null) { true }
    private val litematicaMode by setting(generalGroup,
        "Litematica",
        "Only breaks blocks that are incorrectly placed in schematics",
        false, null) { true }

    /*
     */

    init {
        onInGameEvent<TickEvent.Pre> {
            if (!enabled) return@onInGameEvent

            if (CoreConfig.onGround && !player.isOnGround) return@onInGameEvent

            val blockVolume = getBlockVolume { pos, state ->
                if (!isBlockBreakable(pos, state)) return@getBlockVolume true

                if (flattenMode.isEnabled()
                    && !isBlockInFlatten(pos, crouchLowersFlatten, flattenMode)
                    ) {
                    return@getBlockVolume true
                }

                if (avoidLiquids && willReleaseLiquids(pos)) return@getBlockVolume true

                if (baritoneSelection && !isWithinABaritoneSelection(pos)) return@getBlockVolume true

                if (litematicaMode) {
                    updateSchematicMismatches()
                    if (!LitematicaUtils.schematicIncorrectBlockPlacements.contains(pos)
                        && !LitematicaUtils.schematicIncorrectStatePlacements.contains(pos)
                    ) {
                        return@getBlockVolume true
                    }
                }

                if (canalMode && isValidCanalBlock(pos)) return@getBlockVolume true

                return@getBlockVolume blockTimeouts.values().contains(pos)
            }

            sortBlockVolume(blockVolume, player.eyePos, mineStyle)

            checkAttemptBreaks(blockVolume)
        }
        for (settingGroup in CoreConfig.settingGroups) {
            settingGroups.add(settingGroup)
        }
    }

    private fun InGame.getBlockVolume(removeIf: ((BlockPos, BlockState) -> Boolean)?) =
        player.run {
            if (shape == VolumeShape.Sphere) {
                getBlockSphere(this.eyePos, CoreConfig.radius, removeIf)
            } else {
                getBlockCube(this.eyePos, CoreConfig.radius, removeIf)
            }
        }
}