package me.beanbag.nuker.utils

import net.minecraft.util.math.Box
import java.awt.Color

object LerpUtils {
    fun lerp(start: Double, end: Double, factor: Double) =
        start + ((end - start) * factor.coerceIn(0.0, 1.0))

    fun lerp(start: Float, end: Float, factor: Float) =
        start + ((end - start) * factor.coerceIn(0.0f, 1.0f))

    fun lerp(start: Box, end: Box, factor: Double) =
        Box(
            lerp(start.minX, end.minX, factor),
            lerp(start.minY, end.minY, factor),
            lerp(start.minZ, end.minZ, factor),
            lerp(start.maxX, end.maxX, factor),
            lerp(start.maxY, end.maxY, factor),
            lerp(start.maxZ, end.maxZ, factor)
        )

    fun lerp(start: Int, end: Int, factor: Double, min: Int, max: Int) =
        (start + ((end - start) * factor).toInt()).coerceIn(min, max)

    fun lerp(colour1: Color, colour2: Color, factor: Double) =
        Color(
            lerp(colour1.red, colour2.red, factor, 0, 255),
            lerp(colour1.green, colour2.green, factor, 0, 255),
            lerp(colour1.blue, colour2.blue, factor, 0, 255),
            lerp(colour1.alpha, colour2.alpha, factor, 0, 255)
        )
}