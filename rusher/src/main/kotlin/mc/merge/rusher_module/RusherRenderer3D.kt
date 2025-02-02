package mc.merge.rusher_module

import mc.merge.module.modules.CoreConfig
import mc.merge.render.IRenderer3D
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Box
import java.awt.Color
import org.rusherhack.client.api.render.IRenderer3D as RusherIRenderer3D

class RusherRenderer3D(private val renderer: RusherIRenderer3D, private val matrixStack: MatrixStack) : IRenderer3D {
    override fun boxLines(box: Box, color: Color) {
        renderer.javaClass.getMethod("begin", MatrixStack::class.java).invoke(renderer, matrixStack)
        renderer.setLineWidth(CoreConfig.outlineWidth)
        renderer.drawBox(
            box.minX,
            box.minY,
            box.minZ,
            box.lengthX,
            box.lengthY,
            box.lengthZ,
            false,
            true,
            color.rgb
        )
        renderer.end()
    }

    override fun boxSides(box: Box, color: Color) {
        renderer.javaClass.getMethod("begin", MatrixStack::class.java).invoke(renderer, matrixStack)
        renderer.setLineWidth(CoreConfig.outlineWidth)
        renderer.drawBox(
            box.minX,
            box.minY,
            box.minZ,
            box.lengthX,
            box.lengthY,
            box.lengthZ,
            true,
            false,
            color.rgb
        )
        renderer.end()
    }
}