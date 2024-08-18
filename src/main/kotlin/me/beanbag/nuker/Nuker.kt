package me.beanbag.nuker

import me.beanbag.nuker.handlers.BrokenBlockHandler.onBlockUpdate
import me.beanbag.nuker.handlers.BrokenBlockHandler.updateBlockQueue
import me.beanbag.nuker.settings.FlattenMode
import me.beanbag.nuker.settings.MineStyle
import me.beanbag.nuker.settings.VolumeShape
import me.beanbag.nuker.utils.BlockUtils.getBlockVolume
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Nuker {

    /*
    settings
     */

    var enabled = false
    var radius = 5f
    var shape = VolumeShape.Sphere
    var breakThreshold = 0.7f
    var packetLimit = 8
    var blockTimeoutDelay = 300
    var ghostBlockTimeout = 1500
    var canalMode = false
    var baritoneSelMode = false
    var litematicaMode = false
    var avoidLiquids = false
    var mineStyle = MineStyle.Closest
    var flattenMode = FlattenMode.Standard
    var crouchLowersFlatten = false
    var validateBreak = true
    var onGround = false

    val mc: MinecraftClient = MinecraftClient.getInstance()
    var meteorIsPresent = false
    val LOGGER: Logger = LoggerFactory.getLogger("Nuker")

    fun onTick() {
        if (!enabled || !nullSafe()) return

        updateBlockQueue()

        if (onGround && !mc.player!!.isOnGround) return

        val blockShape = getBlockVolume()
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
