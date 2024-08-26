package me.beanbag.nuker.utils

import me.beanbag.nuker.types.BreakType
import me.beanbag.nuker.types.BreakingContext

object DoubleBreakUtils {
    fun Array<BreakingContext?>.shiftPrimaryDown() {
        this[0]?.breakType = BreakType.Secondary
        this[1] = this[0]
        this[0] = null
    }
}