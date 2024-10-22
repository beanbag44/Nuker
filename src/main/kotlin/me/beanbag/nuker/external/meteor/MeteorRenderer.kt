package me.beanbag.nuker.external.meteor

import me.beanbag.nuker.render.Renderer
import meteordevelopment.meteorclient.renderer.Renderer3D
import meteordevelopment.meteorclient.renderer.ShapeMode
import net.minecraft.util.math.Box
import java.awt.Color

class MeteorRenderer(private val renderer3D: Renderer3D) : Renderer {
    override fun boxLines(box: Box, color: Color) {
        renderer3D.box(
            box,
            meteordevelopment.meteorclient.utils.render.color.Color(color),
            meteordevelopment.meteorclient.utils.render.color.Color(color),
            ShapeMode.Lines,
            0
        )
    }

    override fun boxSides(box: Box, color: Color) {
        renderer3D.box(
            box,
            meteordevelopment.meteorclient.utils.render.color.Color(color),
            meteordevelopment.meteorclient.utils.render.color.Color(color),
            ShapeMode.Sides,
            0
        )
    }
}