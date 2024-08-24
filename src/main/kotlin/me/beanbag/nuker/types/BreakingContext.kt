package me.beanbag.nuker.types

import me.beanbag.nuker.Nuker.breakThreshold
import me.beanbag.nuker.Nuker.endFillColour
import me.beanbag.nuker.Nuker.endOutlineColour
import me.beanbag.nuker.Nuker.fillColourMode
import me.beanbag.nuker.Nuker.mc
import me.beanbag.nuker.Nuker.meteorIsPresent
import me.beanbag.nuker.Nuker.outlineColourMode
import me.beanbag.nuker.Nuker.outlineWidth
import me.beanbag.nuker.Nuker.renderAnimation
import me.beanbag.nuker.Nuker.renders
import me.beanbag.nuker.Nuker.rusherIsPresent
import me.beanbag.nuker.Nuker.startFillColour
import me.beanbag.nuker.Nuker.startOutlineColour
import me.beanbag.nuker.Nuker.staticFillColour
import me.beanbag.nuker.Nuker.staticOutlineColour
import me.beanbag.nuker.settings.enumsettings.ColourMode
import me.beanbag.nuker.settings.enumsettings.RenderAnimation
import me.beanbag.nuker.settings.enumsettings.RenderType
import me.beanbag.nuker.utils.LerpUtils.lerp
import me.beanbag.nuker.utils.RenderUtils.getLerpBox
import meteordevelopment.meteorclient.renderer.Renderer3D
import meteordevelopment.meteorclient.renderer.ShapeMode
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import org.rusherhack.client.api.RusherHackAPI
import java.awt.Color
import meteordevelopment.meteorclient.utils.render.color.Color as MeteorColour

class BreakingContext(
    val pos: BlockPos,
    var state: BlockState,
    var currentBreakDelta: Float,
    var bestTool: Int
) {
    var mineTicks: Int = 0
    var breakType = BreakType.Primary
    var additiveBreakDelta = currentBreakDelta
    var previousBreakDelta = 0f
    var lastLerpFillColour: Color? = null
    var lastLerpOutlineColour: Color? = null

    var boxList = if (renders.enabled()) {
        state.getOutlineShape(mc.world, pos).boundingBoxes.toSet()
    } else {
        null
    }

    val miningProgress: Float
        get() = additiveBreakDelta

    val previousMiningProgress: Float
        get() = additiveBreakDelta - currentBreakDelta

    fun transformProgress(percentage: Float) {
        additiveBreakDelta *= percentage
    }

    fun updateBreakDeltas(newBreakDelta: Float) {
        previousBreakDelta = currentBreakDelta
        currentBreakDelta = newBreakDelta
        additiveBreakDelta += newBreakDelta
    }

    fun updateRenders() {
        state.getOutlineShape(mc.world, pos)?.boundingBoxes?.toSet()
    }

    fun drawRenders(meteorRenderer: Renderer3D?) {
        val threshold = if (breakType.isPrimary()) 2f - breakThreshold else 1f
        val previousFactor = previousMiningProgress * threshold
        val nextFactor = miningProgress * threshold
        val currentFactor = lerp(previousFactor, nextFactor, mc.tickDelta)

        val fillColour = if (fillColourMode == ColourMode.Dynamic) {
            val lerpColour = lerp(startFillColour, endFillColour, currentFactor.toDouble())
            lastLerpFillColour = lerpColour
            lerpColour
        } else {
            staticFillColour
        }

        val outlineColour = if (outlineColourMode == ColourMode.Dynamic) {
            val lerpColour = lerp(startOutlineColour, endOutlineColour, currentFactor.toDouble())
            lastLerpOutlineColour = lerpColour
            lerpColour
        } else {
            staticOutlineColour
        }

        boxList?.forEach { box ->
            val positionedBox = box.offset(pos)

            val renderBox = if (renderAnimation == RenderAnimation.Static) {
                positionedBox
            } else {
                getLerpBox(positionedBox, currentFactor)
            }

            when {
                meteorIsPresent -> {
                    val shapeMode = when (renders) {
                        RenderType.Both -> ShapeMode.Both
                        RenderType.Fill -> ShapeMode.Sides
                        RenderType.Line -> ShapeMode.Lines
                        else -> null
                    }

                    meteorRenderer?.box(
                        renderBox,
                        MeteorColour(fillColour),
                        MeteorColour(outlineColour),
                        shapeMode,
                        0
                    )
                }

                rusherIsPresent -> {
                    if (renders != RenderType.Line) {
                        RusherHackAPI.getRenderer3D().drawBox(
                            renderBox.minX,
                            renderBox.minY,
                            renderBox.minZ,
                            renderBox.maxX - renderBox.minX,
                            renderBox.maxY - renderBox.minY,
                            renderBox.maxZ - renderBox.minZ,
                            true,
                            false,
                            fillColour.rgb
                        )
                    }

                    if (renders != RenderType.Fill) {
                        RusherHackAPI.getRenderer3D().setLineWidth(outlineWidth)
                        RusherHackAPI.getRenderer3D().drawBox(
                            renderBox.minX,
                            renderBox.minY,
                            renderBox.minZ,
                            renderBox.maxX - renderBox.minX,
                            renderBox.maxY - renderBox.minY,
                            renderBox.maxZ - renderBox.minZ,
                            false,
                            true,
                            outlineColour.rgb
                        )
                    }
                }
            }
        }
    }

    enum class BreakType {
        Primary, Secondary;

        fun isPrimary() =
            this == Primary
    }
}