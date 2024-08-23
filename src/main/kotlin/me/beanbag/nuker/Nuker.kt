package me.beanbag.nuker

import me.beanbag.nuker.handlers.BrokenBlockHandler.onBlockUpdate
import me.beanbag.nuker.handlers.BrokenBlockHandler.updateBlockQueue
import me.beanbag.nuker.settings.Setting
import me.beanbag.nuker.settings.enumsettings.*
import me.beanbag.nuker.utils.BlockUtils.filterUnbreakableBlocks
import me.beanbag.nuker.utils.BlockUtils.getBlockVolume
import me.beanbag.nuker.utils.BlockUtils.filterImpossibleFlattenBlocks
import me.beanbag.nuker.utils.BlockUtils.filterLiquidAffectingBlocks
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color

object Nuker {

    /*
    settings
     */

    var enabled = false
    var radius by Setting("Radius", "The radius around the player blocks can be broken", 5f, 0f, 6f, 0f, 6f, 0.1f, null) { true }
    var shape by Setting("Shape", "The shape used to select the blocks to break", VolumeShape.Sphere, null) { true }
    var mineStyle by Setting("Mine Style", "The order which blocks are broken in", MineStyle.Closest, null) { true }
    var flattenMode by Setting("Flatten Mode", "The style which nuker flattens terrain with", FlattenMode.Standard, null) { true }
    var onGround by Setting("On Ground", "Only breaks blocks if the player is on ground", false, null) { true }
    var avoidLiquids by Setting("Avoid Spilling Liquids", "Doesnt break blocks that would in turn let fluids flow", false, null) { true }
    var crouchLowersFlatten by Setting("Crouch Lowers Flatten", "Lets crouching lower the flatten level by one block", false, null) { true }
    var validateBreak by Setting("Validate Break", "Waits for the server to validate breaks", true, null) { true }
    var ghostBlockTimeout by Setting("Ghost Block Timeout (Ticks)", "The delay after breaking a block to reset its state if the server hasn't validated the break", 30, 5, 50, 5, 50, 1, null) { validateBreak }
    var breakThreshold by Setting("Break Threshold", "The percentage mined a block should be broken at", 0.7f, 0f, 1f, 0f, 1f, 0.1f, null) { true }
    var packetLimit by Setting("Packet Limit", "How many packets can be sent per tick", 8, 0, 15, 0, 15, 1, null) { true }
    var blockTimeout by Setting("Block Timeout (Ticks)", "The delay after breaking a block to attempt to break it again", 20, 0, 100, 0, 100, 1, null) { true }
    var canalMode by Setting("Canal Mode", "Only breaks blocks that need to be removed for the southern canal", false, null) { true }
    var baritoneSelection by Setting("Baritone Selection", "Only breaks blocks inside baritone selections", false, null) { true }
    var litematicaMode by Setting("Litematica", "Only breaks blocks that are incorrectly placed in schematics", false, null) { true }

    var renders by Setting("Renders", "Draws animated boxes showing the current mining blocks and more", RenderType.Both, null) { true }
    var renderAnimation by Setting("Render Animation", "Changes the way box renders are animated", RenderAnimation.Out, null) { renders.enabled() }
    var fillColourMode by Setting("Fill Colour Mode", "Changes the box fill render colour style", ColourMode.Dynamic, null) { renders.enabled() && renders != RenderType.Line }
    var staticFillColour by Setting("Static Fill Colour", "The colour used to render the static fill of the box faces", Color.RED, null) { renders.enabled() && renders != RenderType.Line && fillColourMode == ColourMode.Static }
    var startFillColour by Setting("Start Fill Colour", "The colour used to render the start fill of the box faces", Color.RED, null) { renders.enabled() && renders != RenderType.Line && fillColourMode == ColourMode.Dynamic }
    var endFillColour by Setting("End Fill Colour", "The colour used to render the end fill of the box faces", Color.GREEN, null) { renders.enabled() && renders != RenderType.Line && fillColourMode == ColourMode.Dynamic }
    var outlineColourMode by Setting("Outline Colour Mode", "Changes the box outline render colour style", ColourMode.Dynamic, null) { renders.enabled() && renders != RenderType.Fill}
    var staticOutlineColour by Setting("Static Outline Colour", "The colour used to render the outline of the box", Color.RED, null) { renders.enabled() && renders != RenderType.Fill && outlineColourMode == ColourMode.Static }
    var startOutlineColour by Setting("Start Outline Colour", "The colour used to render the start outline of the box", Color.RED, null) { renders.enabled() && renders != RenderType.Fill && outlineColourMode == ColourMode.Dynamic }
    var endOutlineColour by Setting("End Outline Colour", "The colour used to render the end outline of the box", Color.GREEN, null) { renders.enabled() && renders != RenderType.Fill && outlineColourMode == ColourMode.Dynamic }
    var outlineWidth by Setting("Outline Width", "The width of the rendered box outline", 1.0f, 0.0f, 5.0f, 0.0f, 5.0f, 0.1f, null) { renders.enabled() && renders != RenderType.Fill }

    val mc: MinecraftClient = MinecraftClient.getInstance()
    var meteorIsPresent = false
    var rusherIsPresent = false
    val LOGGER: Logger = LoggerFactory.getLogger("Nuker")

    fun onTick() {
        if (!enabled || !nullSafe()) return

        updateBlockQueue()

        if (onGround && !mc.player!!.isOnGround) return

        val blockVolume = getBlockVolume()

        filterUnbreakableBlocks(blockVolume)

        if (flattenMode.isEnabled()) {
            filterImpossibleFlattenBlocks(blockVolume)
        }

        if (avoidLiquids) {
            filterLiquidAffectingBlocks(blockVolume)
        }
    }

    fun onPacketReceive(packet: Packet<*>) {
        when (packet) {
            is BlockUpdateS2CPacket -> {
                onBlockUpdate(packet.pos, packet.state)
            }

            is ChunkDeltaUpdateS2CPacket -> {
                packet.visitUpdates { pos, state ->
                    onBlockUpdate(pos, state)
                }
            }
        }
    }

    fun nullSafe() =
        mc.player != null
                && mc.world != null
                && mc.interactionManager != null
                && mc.networkHandler != null
}
