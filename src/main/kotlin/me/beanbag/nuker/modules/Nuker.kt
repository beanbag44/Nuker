package me.beanbag.nuker.modules

import me.beanbag.nuker.Loader.Companion.mc
import me.beanbag.nuker.handlers.BrokenBlockHandler.onBlockUpdate
import me.beanbag.nuker.handlers.BrokenBlockHandler.updateBlockQueue
import me.beanbag.nuker.settings.Setting
import me.beanbag.nuker.settings.SettingGroup
import me.beanbag.nuker.settings.enumsettings.FlattenMode
import me.beanbag.nuker.settings.enumsettings.MineStyle
import me.beanbag.nuker.settings.enumsettings.VolumeShape
import me.beanbag.nuker.utils.BlockUtils.getBlockVolume
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket

object Nuker : Module("", "") {
    /*
    settings
     */

    val generalGroup = SettingGroup("General", "General settings for nuker")
    var radius by generalGroup.add(Setting<Float>("Radius", "The radius around the player blocks can be broken", 5f, 0f, 6f, 0f, 6f, 0.1f, null) { true })
    var shape by generalGroup.add(Setting<VolumeShape>("Shape", "The shape used to select the blocks to break", VolumeShape.Sphere, null) { true })
    var mineStyle by generalGroup.add(Setting<MineStyle>("Mine Style", "The order which blocks are broken in", MineStyle.Closest, null) { true })
    var flattenMode by generalGroup.add(Setting<FlattenMode>("Flatten Mode", "The style which nuker flattens terrain with", FlattenMode.Standard, null) { true })
    var onGround by generalGroup.add(Setting<Boolean>("On Ground", "Only breaks blocks if the player is on ground", false, null) { true })
    var avoidLiquids by generalGroup.add(Setting<Boolean>("Avoid Spilling Liquids", "Doesnt break blocks that would in turn let fluids flow", false, null) { true })
    var crouchLowersFlatten by generalGroup.add(Setting<Boolean>("Crouch Lowers Flatten", "Lets crouching lower the flatten level by one block", false, null) { true })
    var validateBreak by generalGroup.add(Setting<Boolean>("Validate Break", "Waits for the server to validate breaks", true, null) { true })
    var ghostBlockTimeout by generalGroup.add(Setting<Int>("Ghost Block Timeout (Ticks)", "The delay after breaking a block to reset its state if the server hasn't validated the break", 30, 5, 50, 5, 50, 1, null) { validateBreak })
    var breakThreshold by generalGroup.add(Setting<Float>("Break Threshold", "The percentage mined a block should be broken at", 0.7f, 0f, 1f, 0f, 1f, 0.1f, null) { true })
    var packetLimit by generalGroup.add(Setting<Int>("Packet Limit", "How many packets can be sent per tick", 8, 0, 15, 0, 15, 1, null) { true })
    var blockTimeout by generalGroup.add(Setting<Int>("Block Timeout (Ticks)", "The delay after breaking a block to attempt to break it again", 20, 0, 100, 0, 100, 1, null) { true })
    var canalMode by generalGroup.add(Setting<Boolean>("Canal Mode", "Only breaks blocks that need to be removed for the southern canal", false, null) { true })
    var baritoneSelection by generalGroup.add(Setting<Boolean>("Baritone Selection", "Only breaks blocks inside baritone selections", false, null) { true })
    var litematicaMode by generalGroup.add(Setting<Boolean>("Litematica", "Only breaks blocks that are incorrectly placed in schematics", false, null) { true })


    override fun onTick() {
        if (!enabled || !nullSafe()) return

        updateBlockQueue()

        if (onGround && !mc.player!!.isOnGround) return

        val blockVolume = getBlockVolume()
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
