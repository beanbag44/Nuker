package me.beanbag.nuker.utils

import me.beanbag.nuker.module.modules.nuker.enumsettings.RenderAnimation
import me.beanbag.nuker.utils.LerpUtils.lerp
import net.minecraft.util.math.Box

object RenderUtils {
    fun getLerpBox(box: Box, factor: Float, renderAnimation: RenderAnimation): Box {
        val boxCenter = Box(box.center, box.center)
        when (renderAnimation) {
            RenderAnimation.Out -> {
                return lerp(boxCenter, box, factor.toDouble())
            }

            RenderAnimation.In -> {
                return lerp(box, boxCenter, factor.toDouble())
            }

            RenderAnimation.InOut -> {
                return if (factor >= 0.5f) {
                    lerp(boxCenter, box, (factor.toDouble() - 0.5) * 2)
                } else {
                    lerp(box, boxCenter, factor.toDouble() * 2)
                }
            }

            RenderAnimation.OutIn -> {
                return if (factor >= 0.5f) {
                    lerp(box, boxCenter, (factor.toDouble() - 0.5) * 2)
                } else {
                    lerp(boxCenter, box, factor.toDouble() * 2)
                }
            }

            else -> {
                return box
            }
        }
    }
}