package me.beanbag.nuker.handlers

import me.beanbag.nuker.modules.Nuker.blockTimeout
import me.beanbag.nuker.types.PosAndState
import me.beanbag.nuker.types.TickCounter
import me.beanbag.nuker.utils.TimerUtils.subscribeTickTimerMap
import net.minecraft.util.math.BlockPos

object BreakingHandler {
    val blockTimeouts = hashMapOf<TickCounter, BlockPos>().subscribeTickTimerMap()

    fun checkAttemptBreaks(blockVolume: List<PosAndState>) {

    }

    fun updateBreakingContexts() {

    }

    fun updateBlockTimeouts() =
        blockTimeouts.keys.removeIf {
            it.counter >= blockTimeout
        }
}