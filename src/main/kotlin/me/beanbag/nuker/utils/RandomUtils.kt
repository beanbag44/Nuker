package me.beanbag.nuker.utils

import me.beanbag.nuker.ModConfigs.mc

object RandomUtils {
    fun clampAngle(angle: Float): Float {
        var clampedAngle = angle
        while (clampedAngle < 0) clampedAngle += 360f

        return clampedAngle % 360
    }

    fun isIn2bQueue(): Boolean {
        mc.player?.let { player ->
            mc.world?.let { world ->
                if (player.isSpectator) {
                    return true
                }
                if (!world.isChunkLoaded(player.chunkPos.x, player.chunkPos.z)) {
                    return true
                }
            }
            return false
        }
        return true
    }
}