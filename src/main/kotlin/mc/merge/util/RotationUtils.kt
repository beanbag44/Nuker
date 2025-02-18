package mc.merge.util

import mc.merge.handler.RotationHandler.rotate
import net.minecraft.util.math.Vec3d
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max

object RotationUtils {

    fun InGame.adjustYaw(startingLocation:Vec3d, startingRotation:Vec3d, distance:Double, startingYaw: Float?) {
        val playerPos = Vec3d(player.x, startingLocation.y, player.z)
        val closestPoint = closestPointOnLine(playerPos, startingLocation, startingRotation)
        if (Vec3d(closestPoint.x, playerPos.y, closestPoint.z).distanceTo(playerPos) < 0.1) {
            if (startingYaw != null && player.yaw != startingYaw) {
                player.yaw = startingYaw
            }
        } else {
            val yawTowardLine = yawTowardsLine(startingLocation, startingRotation, distance)
            rotate(
                yawTowardLine.toFloat(), player.pitch,
                silent = false,
                useRusherIfPossible = false
            )
        }
    }

    fun InGame.yawTowardsLine(lineOrigin: Vec3d, initialRotation: Vec3d, lookaheadDistance: Double) : Double {
        val playerPos = Vec3d(player.x, lineOrigin.y, player.z)
        val closestPoint = closestPointOnLine(playerPos, lineOrigin, initialRotation)
        val target = closestPoint.add(initialRotation.multiply(max(2.0, player.velocity.length() * lookaheadDistance)))

        val dx: Double = target.x - player.eyePos.x
        val dz: Double = target.z - player.eyePos.z

        var yaw = atan2(dz, dx)
        yaw = Math.toDegrees(yaw) - 90
        return yaw
    }

    fun closestPointOnLine(position: Vec3d, lineOrigin: Vec3d, lineDirection: Vec3d): Vec3d {
        val vectorToPosition = position.subtract(lineOrigin)
        val t = vectorToPosition.dotProduct(lineDirection.normalize())
        return snapToAxis(lineOrigin.add(lineDirection.multiply(t)), lineOrigin, lineDirection)
    }

    private fun snapToAxis(closestPointOnLine: Vec3d, lineOrigin: Vec3d, lineDirection: Vec3d): Vec3d {
        var zeroOriginLine = closestPointOnLine.subtract(lineOrigin)

        if (abs(abs(lineDirection.x) - abs(lineDirection.z)) < 0.1) { //is diagonal
            val average = (abs(zeroOriginLine.x) + abs(zeroOriginLine.z)) / 2
            val x = if (lineDirection.x > 0) average else -average
            val z = if (lineDirection.z > 0) average else -average

            zeroOriginLine = Vec3d(x, 0.0, z)
        } else { //is straight
            val x = if(abs(lineDirection.x) < 0.1) 0.0 else zeroOriginLine.x
            val z = if(abs(lineDirection.z) < 0.1) 0.0 else zeroOriginLine.z

            if (x == 0.0) {
                zeroOriginLine = Vec3d(0.0, 0.0, z)
            } else if (z == 0.0) {
                zeroOriginLine = Vec3d(x, 0.0, 0.0)
            }

        }
        return lineOrigin.add(zeroOriginLine)
    }

    fun clampAngle(angle: Float): Float {
        var clampedAngle = angle
        while (clampedAngle < 0) clampedAngle += 360f

        return clampedAngle % 360
    }
}