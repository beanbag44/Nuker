package me.beanbag.nuker.render

import net.minecraft.util.math.Box
import java.awt.Color

interface Renderer {
    fun boxLines(box:Box, color: Color)
    fun boxSides(box:Box, color: Color)
}