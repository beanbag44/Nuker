package mc.merge.util

object TwoBUtils {
    fun InGame.isIn2bQueue(): Boolean {
        if (player.isSpectator) {
            return true
        }
        if (!world.isChunkLoaded(player.chunkPos.x, player.chunkPos.z)) {
            return true
        }
        return false
    }
}