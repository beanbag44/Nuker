package me.beanbag.nuker.modules.nuker

import me.beanbag.nuker.Loader.Companion.mc
import me.beanbag.nuker.modules.Module
import me.beanbag.nuker.modules.nuker.handlers.BreakingHandler.blockTimeouts
import me.beanbag.nuker.modules.nuker.handlers.BreakingHandler.checkAttemptBreaks
import me.beanbag.nuker.modules.nuker.handlers.BreakingHandler.updateBreakingContexts
import me.beanbag.nuker.modules.nuker.enumsettings.*
import me.beanbag.nuker.modules.nuker.handlers.BreakingHandler
import me.beanbag.nuker.modules.nuker.handlers.BrokenBlockHandler
import me.beanbag.nuker.settings.Setting
import me.beanbag.nuker.settings.SettingGroup
import me.beanbag.nuker.types.PosAndState
import me.beanbag.nuker.types.VolumeSort
import me.beanbag.nuker.utils.BlockUtils.filterBlocksToBaritoneSelections
import me.beanbag.nuker.utils.BlockUtils.filterCorrectlyPlacedLitematicaBlocks
import me.beanbag.nuker.utils.BlockUtils.filterLiquidSupportingBlocks
import me.beanbag.nuker.utils.BlockUtils.filterBlocksToBreakable
import me.beanbag.nuker.utils.BlockUtils.filterBlocksToCanal
import me.beanbag.nuker.utils.BlockUtils.getBlockCube
import me.beanbag.nuker.utils.BlockUtils.getBlockSphere
import me.beanbag.nuker.utils.BlockUtils.sortBlockVolume
import me.beanbag.nuker.utils.LitematicaUtils.updateSchematicMismatches
import me.beanbag.nuker.utils.TimerUtils
import me.beanbag.nuker.utils.TimerUtils.updateTimeoutMaps
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket
import net.minecraft.util.math.Direction
import java.awt.Color
import java.util.ArrayList

object Nuker : Module("Epic Nuker", "Epic nuker for nuking terrain") {

    /*
    settings
     */

    val generalGroup = addGroup(SettingGroup("General", "General settings for nuker"))
    val radius by generalGroup.add(Setting<Float>("Radius", "The radius around the player blocks can be broken", 5f, 0f, 6f, 0f, 6f, 0.1f, null) { true })
    val shape by generalGroup.add(Setting<VolumeShape>("Shape", "The shape used to select the blocks to break", VolumeShape.Sphere, null) { true })
    val mineStyle by generalGroup.add(Setting<VolumeSort>("Mine Style", "The order which blocks are broken in", VolumeSort.Closest, null) { true })
    val flattenMode by generalGroup.add(Setting<FlattenMode>("Flatten Mode", "The style which nuker flattens terrain with", FlattenMode.Standard, null) { true })
    val onGround by generalGroup.add(Setting<Boolean>("On Ground", "Only breaks blocks if the player is on ground", false, null) { true })
    val avoidLiquids by generalGroup.add(Setting<Boolean>("Avoid Spilling Liquids", "Doesnt break blocks that would in turn let fluids flow", false, null) { true })
    val crouchLowersFlatten by generalGroup.add(Setting<Boolean>("Crouch Lowers Flatten", "Lets crouching lower the flatten level by one block", false, null) { true })
    val doubleBreak by generalGroup.add(Setting<Boolean>("Double Break", "Breaks two blocks at once", true, null) { true })
    val breakMode by generalGroup.add(Setting<BreakMode>("Break Mode", "Changes the way total break amount is calculated", BreakMode.Total, null) { true })
    val validateBreak by generalGroup.add(Setting<Boolean>("Validate Break", "Waits for the server to validate breaks", true, null) { true })
    val ghostBlockTimeout by generalGroup.add(Setting<Int>("Ghost Block Timeout (Ticks)", "The delay after breaking a block to reset its state if the server hasn't validated the break", 30, 5, 50, 5, 50, 1, null) { validateBreak })
    val breakThreshold by generalGroup.add(Setting<Float>("Break Threshold", "The percentage mined a block should be broken at", 0.7f, 0f, 1f, 0f, 1f, 0.1f, null) { true })
    val packetLimit by generalGroup.add(Setting<Int>("Packet Limit", "How many packets can be sent per tick", 8, 0, 15, 0, 15, 1, null) { true })
    val blockTimeout by generalGroup.add(Setting<Int>("Block Timeout (Ticks)", "The delay after breaking a block to attempt to break it again", 20, 0, 100, 0, 100, 1, null) { true })
    val canalMode by generalGroup.add(Setting<Boolean>("Canal Mode", "Only breaks blocks that need to be removed for the southern canal", false, null) { true })
    val baritoneSelection by generalGroup.add(Setting<Boolean>("Baritone Selection", "Only breaks blocks inside baritone selections", false, null) { true })
    val litematicaMode by generalGroup.add(Setting<Boolean>("Litematica", "Only breaks blocks that are incorrectly placed in schematics", false, null) { true })

    val renderGroup = addGroup(SettingGroup("Renders", "Render settings for nuker"))
    val renders by renderGroup.add(Setting<RenderType>("Renders", "Draws animated boxes showing the current mining blocks and more", RenderType.Both, null) { true })
    val renderAnimation by renderGroup.add(Setting<RenderAnimation>("Render Animation", "Changes the way box renders are animated", RenderAnimation.Out, null) { renders.enabled() })
    val fillColourMode by renderGroup.add(Setting<ColourMode>("Fill Colour Mode", "Changes the box fill render colour style", ColourMode.Dynamic, null) { renders.enabled() && renders != RenderType.Line })
    val staticFillColour by renderGroup.add(Setting<Color>("Static Fill Colour", "The colour used to render the static fill of the box faces", Color.RED, null) { renders.enabled() && renders != RenderType.Line && fillColourMode == ColourMode.Static })
    val startFillColour by renderGroup.add(Setting<Color>("Start Fill Colour", "The colour used to render the start fill of the box faces", Color.RED, null) { renders.enabled() && renders != RenderType.Line && fillColourMode == ColourMode.Dynamic })
    val endFillColour by renderGroup.add(Setting<Color>("End Fill Colour", "The colour used to render the end fill of the box faces", Color.GREEN, null) { renders.enabled() && renders != RenderType.Line && fillColourMode == ColourMode.Dynamic })
    val outlineColourMode by renderGroup.add(Setting<ColourMode>("Outline Colour Mode", "Changes the box outline render colour style", ColourMode.Dynamic, null) { renders.enabled() && renders != RenderType.Fill })
    val staticOutlineColour by renderGroup.add(Setting<Color>("Static Outline Colour", "The colour used to render the outline of the box", Color.RED, null) { renders.enabled() && renders != RenderType.Fill && outlineColourMode == ColourMode.Static })
    val startOutlineColour by renderGroup.add(Setting<Color>("Start Outline Colour", "The colour used to render the start outline of the box", Color.RED, null) { renders.enabled() && renders != RenderType.Fill && outlineColourMode == ColourMode.Dynamic })
    val endOutlineColour by renderGroup.add(Setting<Color>("End Outline Colour", "The colour used to render the end outline of the box", Color.GREEN, null) { renders.enabled() && renders != RenderType.Fill && outlineColourMode == ColourMode.Dynamic })
    val outlineWidth by renderGroup.add(Setting<Float>("Outline Width", "The width of the rendered box outline", 1.0f, 0.0f, 5.0f, 0.0f, 5.0f, 0.1f, null) { renders.enabled() && renders != RenderType.Fill })

    /**/

    override fun onTick() {
        TimerUtils.tickTickTimerMaps()

        if (!enabled) return

        updateTimeoutMaps()
        updateBreakingContexts()

        mc.player?.let { player ->
            if (onGround && !player.isOnGround) return

            val blockVolume = getBlockVolume()

            filterBlocksToBreakable(blockVolume)

            if (flattenMode.isEnabled()) {
                filterBlocksToFlatten(blockVolume)
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

    private fun getBlockVolume(): ArrayList<PosAndState> =
        mc.player?.run {
            if (shape == VolumeShape.Sphere) {
                getBlockSphere(this.eyePos, radius.toDouble())
            } else {
                getBlockCube(this.eyePos, radius.toDouble())
            }
        } ?: ArrayList<PosAndState>()

    private fun filterBlocksToFlatten(posAndStateList: ArrayList<PosAndState>) =
        posAndStateList.apply {
            mc.player?.let { player ->
                val playerPos = player.blockPos
                val flattenLevel = if (crouchLowersFlatten && player.isSneaking) {
                    playerPos.y - 1
                } else {
                    playerPos.y
                }

                if (!flattenMode.isSmart()) {
                    removeIf {
                        it.blockPos.y < flattenLevel
                    }
                    return@apply
                }

                val playerLookDir = player.horizontalFacing
                val smartFlattenDir = if (flattenMode == FlattenMode.Smart) {
                    playerLookDir
                } else {
                    playerLookDir?.opposite
                }

                removeIf {
                    if (it.blockPos.y >= flattenLevel) return@removeIf false

                    val zeroedPos = it.blockPos.add(-playerPos.x, -playerPos.y, -playerPos.z)

                    return@removeIf (zeroedPos.x >= 0 && smartFlattenDir == Direction.EAST)
                            || (zeroedPos.z >= 0 && smartFlattenDir == Direction.SOUTH)
                            || (zeroedPos.x <= 0 && smartFlattenDir == Direction.WEST)
                            || (zeroedPos.z <= 0 && smartFlattenDir == Direction.NORTH)
                }
            }
        }

    fun onPacketReceive(packet: Packet<*>) {
        when (packet) {
            is BlockUpdateS2CPacket -> {
                BrokenBlockHandler.onBlockUpdate(packet.pos, packet.state)
                BreakingHandler.onBlockUpdate(packet.pos, packet.state)
            }

            is ChunkDeltaUpdateS2CPacket -> {
                packet.visitUpdates { pos, state ->
                    BrokenBlockHandler.onBlockUpdate(pos, state)
                    BreakingHandler.onBlockUpdate(pos, state)
                }
            }
        }
    }
}