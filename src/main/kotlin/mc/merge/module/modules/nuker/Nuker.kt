package mc.merge.module.modules.nuker

import mc.merge.event.events.TickEvent
import mc.merge.event.onInGameEvent
import mc.merge.handler.BreakingHandler.blockBreakTimeouts
import mc.merge.handler.BreakingHandler.checkAttemptBreaks
import mc.merge.handler.PlacementHandler
import mc.merge.module.Module
import mc.merge.module.modules.CoreConfig
import mc.merge.module.modules.nuker.enumsettings.DigDirection
import mc.merge.module.modules.nuker.enumsettings.FlattenMode
import mc.merge.module.modules.nuker.enumsettings.VolumeShape
import mc.merge.module.modules.nuker.enumsettings.WhitelistMode
import mc.merge.module.settings.SettingGroup
import mc.merge.types.VolumeSort
import mc.merge.util.BlockUtils.getBlockCube
import mc.merge.util.BlockUtils.getBlockSphere
import mc.merge.util.BlockUtils.isBlockBreakable
import mc.merge.util.BlockUtils.isBlockInFlatten
import mc.merge.util.BlockUtils.isValidCanalBlock
import mc.merge.util.BlockUtils.isWithinABaritoneSelection
import mc.merge.util.BlockUtils.sortBlockVolume
import mc.merge.util.BlockUtils.sortBlockVolumeGravityBlocksDown
import mc.merge.util.BlockUtils.willReleaseLiquids
import mc.merge.util.InGame
import mc.merge.util.LitematicaUtils
import mc.merge.util.LitematicaUtils.updateSchematicMismatches
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

class Nuker : Module("Epic Nuker", "Epic nuker for nuking terrain") {

    /*
    settings
     */

    val generalGroup = addGroup(SettingGroup("General", "General settings for nuker"))
    private val shape by setting(generalGroup,
        "Shape",
        "The shape used to select the blocks to break",
        VolumeShape.Sphere)
    private val mineStyle by setting(generalGroup,
        "Mine Style",
        "The order which blocks are broken in",
        VolumeSort.Closest)
    private val flattenMode by setting(generalGroup,
        "Flatten Mode",
        "The style which nuker flattens terrain with",
        FlattenMode.Standard)
    private val directionalDig by setting(generalGroup,
        "Directional Dig",
        "Only breaks blocks in the direction chosen",
        DigDirection.None)
    private val avoidLiquids by setting(generalGroup,
        "Avoid Spilling Liquids",
        "Doesn't break blocks that would in turn let fluids flow",
        false)
    private val topDownGravityBlocks by setting(generalGroup,
        "Top Down Gravity Blocks",
        "Breaks gravity blocks starting from the top to avoid waiting for them to fall",
        true)
    private val crouchLowersFlatten by setting(generalGroup,
        "Crouch Lowers Flatten",
        "Lets crouching lower the flatten level by one block",
        false)
    private val onGround by setting(
        generalGroup,
        "On Ground",
        "Only breaks blocks if the player is on ground",
        false)
    private val canalMode by setting(generalGroup,
        "Canal Mode",
        "Only breaks blocks that need to be removed for the southern canal",
        false)
    private val baritoneSelection by setting(generalGroup,
        "Baritone Selection",
        "Only breaks blocks inside baritone selections",
        false)
    private val litematicaMode by setting(generalGroup,
        "Litematica",
        "Only breaks blocks that are incorrectly placed in schematics",
        false)
    private val incorrectStates by setting(generalGroup,
        "Incorrect States",
        "Allows nuker to break incorrect schematic block states",
        true) { litematicaMode }
    private val whitelistMode by setting(generalGroup,
        "Whitelist Mode",
        "What Type of List wil be used for filtering which blocks get broken",
        WhitelistMode.None)
    private val whitelist by setting(generalGroup,
        "Whitelist",
        "List of blocks that will be broken",
        mutableListOf<Block>(),
        visible =  { whitelistMode == WhitelistMode.Whitelist })
    private val blackList by setting(generalGroup,
        "Blacklist",
        "List of blocks that won't be broken",
        mutableListOf<Block>(),
        visible =  { whitelistMode == WhitelistMode.Blacklist })


    init {
        onInGameEvent<TickEvent.Pre> {
            if (PlacementHandler.usedThisTick) return@onInGameEvent

            if (onGround && !player.isOnGround) return@onInGameEvent

            val blockVolume = getBlockVolume { pos, state ->
                if (directionalDig != DigDirection.None && !isWithinDigDirection(pos)) return@getBlockVolume true

                if (baritoneSelection && !isWithinABaritoneSelection(pos)) return@getBlockVolume true

                if (!isBlockBreakable(pos, state)) return@getBlockVolume true

                if (flattenMode.isEnabled()
                    && !isBlockInFlatten(pos, crouchLowersFlatten, flattenMode, baritoneSelection)
                ) {
                    return@getBlockVolume true
                }

                if (avoidLiquids && willReleaseLiquids(pos)) return@getBlockVolume true

                if (litematicaMode) {
                    updateSchematicMismatches()
                    if (!LitematicaUtils.schematicIncorrectBlockPlacements.contains(pos)
                        && (!incorrectStates || !LitematicaUtils.schematicIncorrectStatePlacements.contains(pos))
                    ) {
                        return@getBlockVolume true
                    }
                }
                when (whitelistMode) {
                    WhitelistMode.Blacklist -> if (blackList.contains(state.block)) return@getBlockVolume true
                    WhitelistMode.Whitelist -> if (!whitelist.contains(state.block)) return@getBlockVolume true
                    else -> {}
                }

                if (canalMode && isValidCanalBlock(pos)) return@getBlockVolume true

                return@getBlockVolume blockBreakTimeouts.values().contains(pos)
            }

            sortBlockVolume(blockVolume, player.eyePos, mineStyle)

            if (topDownGravityBlocks) {
                sortBlockVolumeGravityBlocksDown(blockVolume)
            }

            checkAttemptBreaks(blockVolume)
        }
    }

    private fun InGame.isWithinDigDirection(pos: BlockPos): Boolean {
        val playerPos = player.blockPos
        return when (directionalDig) {
            DigDirection.East -> {
                return playerPos.x <= pos.x
            }

            DigDirection.West -> {
                return playerPos.x >= pos.x
            }

            DigDirection.North -> {
                return player.blockPos.z >= pos.z
            }

            DigDirection.South -> {
                return playerPos.z <= pos.z
            }

            DigDirection.None -> true
        }
    }

    private fun InGame.getBlockVolume(removeIf: ((BlockPos, BlockState) -> Boolean)?) =
        player.run {
            if (shape == VolumeShape.Sphere) {
                getBlockSphere(this.eyePos, CoreConfig.breakRadius, removeIf)
            } else {
                getBlockCube(this.eyePos, CoreConfig.breakRadius, removeIf)
            }
        }

}