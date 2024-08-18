package me.beanbag.nuker.client

import me.beanbag.nuker.client.settings.FlattenMode
import me.beanbag.nuker.client.settings.MineStyle
import net.fabricmc.api.ClientModInitializer

class NukerClient : ClientModInitializer {

    val enabled = false
    val radius = 5f
    val breakThreshold = 0.7f
    val packetLimit = 8
    val blockTimeoutDelay = 300
    val ghostBlockTimeout = 1500
    val canalMode = false
    val baritoneSelMode = false
    val litematicaMode = false
    val avoidLiquids = false
    val mineStyle = MineStyle.Closest
    val flattenMode = FlattenMode.Standard
    val crouchLowersFlatten = false
    val validateBreak = true
    val onGround = false

    override fun onInitializeClient() {
    }
}
