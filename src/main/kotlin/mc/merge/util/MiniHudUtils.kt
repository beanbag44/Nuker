package mc.merge.util

import fi.dy.masa.minihud.renderer.shapes.*
import fi.dy.masa.minihud.util.RayTracer
import mc.merge.duck.IRayTracerDuck
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import kotlin.math.sqrt


object MiniHudUtils {
    fun isWithinMiniHudShape(pos: BlockPos, outline: Boolean): Boolean {
        for (shape in ShapeManager.INSTANCE.allShapes) {
            if (!shape.isEnabled) continue
            val inside = when (shape) {
                is ShapeSphereBlocky -> isPositionInsideOrClosestToRadiusOnBlockRing(shape, pos, outline)
                is ShapeCircleBase  -> isPositionOnOrInsideRing(shape, pos, outline)
                is ShapeBox         -> isPositionInsideBox(shape, pos, outline)
                is ShapeLineBlock   -> isPositionInsideLine(shape, pos)
                else -> false
            }
            if (inside) return true
        }
        return false
    }

    fun isPositionInsideOrClosestToRadiusOnBlockRing(shapeSphere: ShapeSphereBlocky, blockPos: BlockPos, outline: Boolean): Boolean {
        val x = blockPos.x.toDouble() + 0.5
        val y = blockPos.y.toDouble() + 0.5
        val z = blockPos.z.toDouble() + 0.5
        val dist = shapeSphere.effectiveCenter.distanceTo(Vec3d(x, y, z))
        return if (!outline) dist <= shapeSphere.radius else dist in (shapeSphere.radius - 1.5)..shapeSphere.radius
    }

    fun isPositionOnOrInsideRing(shapeCircle: ShapeCircleBase, blockPos: BlockPos, outline: Boolean): Boolean {
        val axis = shapeCircle.mainAxis.axis
        val center = shapeCircle.effectiveCenter
        val radius = shapeCircle.radius

        val dx = if (axis == Direction.Axis.X) 0.0 else blockPos.x + 0.5 - center.x
        val dy = if (axis == Direction.Axis.Y) 0.0 else blockPos.y + 0.5 - center.y
        val dz = if (axis == Direction.Axis.Z) 0.0 else blockPos.z + 0.5 - center.z

        val dist = sqrt(dx * dx + dy * dy + dz * dz)
        return if (!outline) dist <= radius else dist in (radius - 1.5)..radius
    }


    fun isPositionInsideLine(shapeLineBlock: ShapeLineBlock, blockPos: BlockPos): Boolean {
        val tracer = RayTracer(shapeLineBlock.startPos, shapeLineBlock.endPos)
        if ((tracer as IRayTracerDuck).`stonecutter_nuker$getBlockPos`() == blockPos) return true
        while (!tracer.advance()) if ((tracer as IRayTracerDuck).`stonecutter_nuker$getBlockPos`() == blockPos) return true
        return false
    }

    fun isPositionInsideBox(shapeBox: ShapeBox, blockPos: BlockPos, outline: Boolean): Boolean {
        val pos = blockPos.toCenterPos()
        val box = shapeBox.box

        return if (!outline) {
            box.contains(pos)
        } else {
            val insideX = pos.x in (box.minX + 1)..<box.maxX
            val insideY = pos.y in (box.minY + 1)..<box.maxY
            val insideZ = pos.z in (box.minZ + 1)..<box.maxZ
            box.contains(pos) && !(insideX && insideY && insideZ)
        }
    }

}