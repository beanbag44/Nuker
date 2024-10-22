package me.beanbag.nuker.external.rusher

import me.beanbag.nuker.module.modules.CoreConfig
import me.beanbag.nuker.render.Renderer
import net.minecraft.util.math.Box
import org.rusherhack.client.api.RusherHackAPI
import java.awt.Color

class RusherRenderer : Renderer {
    override fun boxLines(box: Box, color: Color) {
        RusherHackAPI.getRenderer3D().setLineWidth(CoreConfig.outlineWidth)
        RusherHackAPI.getRenderer3D().drawBox(
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
    }

    override fun boxSides(box: Box, color: Color) {
        RusherHackAPI.getRenderer3D().drawBox(
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
    }

}