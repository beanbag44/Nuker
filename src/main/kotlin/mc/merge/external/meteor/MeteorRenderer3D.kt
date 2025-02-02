package mc.merge.external.meteor

import mc.merge.render.IRenderer3D
import meteordevelopment.meteorclient.renderer.Renderer3D
import meteordevelopment.meteorclient.renderer.ShapeMode
import net.minecraft.util.math.Box
import java.awt.Color

class MeteorRenderer3D(private val renderer3D: Renderer3D) : IRenderer3D {
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