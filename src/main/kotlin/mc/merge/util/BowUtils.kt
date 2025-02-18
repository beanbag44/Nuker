package mc.merge.util

import mc.merge.util.BlockUtils.isLoaded
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.fluid.Fluids
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import kotlin.math.*

object BowUtils {
    fun InGame.getClosestValidTarget(
        minRange: Double,
        maxRange: Double,
        validEntities: List<EntityType<*>>,
        checkAccuracy: Float
    ): Entity? {
        val potentialTargets = world.entities
            .filter {
                val distance = it.distanceTo(player)
                val keepEntity = it != null
                        && distance <= maxRange
                        && distance >= minRange
                        && it != player
                        && it.isAlive
                        && it != mc.cameraEntity
                        && it.type in validEntities
                        && (player.canSee(it) //can see its eyes or its feet
                        || world.raycast(
                    RaycastContext(
                        player.eyePos,
                        it.pos,
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE,
                        player
                    )
                ).type == HitResult.Type.ENTITY)
                return@filter keepEntity
            }.sortedBy { it.distanceTo(player) }

        for (potentialTarget in potentialTargets) {
            if (calculatePitch(potentialTarget, true, checkAccuracy, maxRange) != null) {
                return potentialTarget
            }
        }
        return null
    }

    fun InGame.calculateYaw(target: Vec3d): Float { // meteor... idk
        return player.yaw + MathHelper.wrapDegrees(
            Math.toDegrees(
                atan2(target.z - player.z, target.x - player.x)
            ).toFloat() - 90f - player.yaw
        )
    }

    /** Based on
     *
     * @see net.minecraft.entity.projectile.PersistentProjectileEntity.tick()
     * */
    private fun InGame.calculateArrowPath(
        startingPos: Vec3d,
        maxPos: Vec3d,
        yaw: Float,
        pitch: Float,
        speed: Double = player.itemUseTime * 3.0,
        maxRange: Double
    ): HitResult? {
        val gravity = 0.05000000074505806
        val airDrag = 0.99
        val waterDrag = 0.6

        val velocity = Vec3d(
            -sin(yaw * 0.017453292) * cos(pitch * 0.017453292),
            -sin((pitch) * 0.017453292),
            cos(yaw * 0.017453292) * cos(pitch * 0.017453292)
        ).normalize().multiply(speed)

        var hitResult: HitResult? = null
        var previousPos: Vec3d
        var pos = startingPos
        while (hitResult == null
            && pos.y > world.bottomY
            && isLoaded(pos.x.toInt(), pos.z.toInt())
            && pos.distanceTo(pos) < maxRange
        ) {
            //update pos
            previousPos = pos.add(0.0, 0.0, 0.0)
            pos = pos.add(velocity)

            //update velocity
            velocity.multiply(
                if (listOf(Fluids.FLOWING_WATER, Fluids.WATER).contains(
                        world.getFluidState(
                            BlockPos(
                                pos.x.toInt(),
                                pos.y.toInt(),
                                pos.z.toInt()
                            )
                        ).fluid
                    )
                )
                    waterDrag
                else
                    airDrag
            )
            velocity.subtract(0.0, gravity, 0.0)


            val entityHitResult = ProjectileUtil.getEntityCollision(
                world, player, pos, previousPos,
                Box(pos.x, pos.y, pos.z, pos.x, pos.y, pos.z).stretch(player.velocity).expand(5.0)
            ) { entity: Entity -> !entity.isSpectator && entity.isAlive && entity.canHit() }
            if (entityHitResult != null && entityHitResult.type == HitResult.Type.ENTITY) {
                return entityHitResult
            }
            //check for collisions. uses weird stuff I don't fully understand
            hitResult = world.raycast(
                RaycastContext(
                    previousPos,
                    pos,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    player
                )
            )

//            if (hitResult.getType() != HitResult.Type.MISS) {
//                previousPos = hitResult.getPos()
//            }
//
//            val hitResult2 = ProjectileUtil.getEntityCollision(
//                world, player, pos, previousPos,
//                Box(pos.x, pos.y, pos.z, pos.x, pos.y, pos.z).stretch(player.velocity).expand(5.0)
//            ) { entity: Entity -> !entity.isSpectator && entity.isAlive && entity.canHit() }
//
//            if (hitResult2 != null) {
//                hitResult = hitResult2
//            }
            if (hitResult != null && hitResult.type != HitResult.Type.MISS
                || isOutOfBounds(startingPos.x, maxPos.x, pos.x)
                || isOutOfBounds(startingPos.z, maxPos.z, pos.z)
            ) {
                return hitResult
            }
            hitResult = null
        }
        return null
    }

    fun InGame.calculatePitch(target: Entity, maxVelocity: Boolean, checkAccuracy: Float, maxRange: Double): Float? {
        //the yaw the player needs to look directly at the target
        val yawToTarget = calculateYaw(target.pos)

        //Do three quick checks first, then do the intense calculation
        for (pos in listOf(target.boundingBox.center, target.pos, target.eyePos)) {
            val pitch = calculateAirOnlyPitch(
                pos,
                if (maxVelocity) 1.0f else (player.itemUseTime - player.itemUseTimeLeft) / 20f
            )
            val hitResult = calculateArrowPath(player.eyePos, pos, yawToTarget, pitch, speed = if (maxVelocity) 3.0 else (player.itemUseTime - player.itemUseTimeLeft) / 20.0 * 3.0, maxRange)

            if (hitResult != null && hitResult.type == HitResult.Type.ENTITY && hitResult.pos.distanceTo(target.boundingBox.center) < maxOf(
                    target.boundingBox.lengthX,
                    target.boundingBox.lengthY,
                    target.boundingBox.lengthZ
                )
            ) {
                return pitch
            }
        }

        //Check lots of angles
        val angleToFeet = calculateAirOnlyPitch(
            target.pos,
            if (maxVelocity) 1.0f else (player.itemUseTime - player.itemUseTimeLeft) / 20f
        )
        var testPitch = angleToFeet
        while (testPitch > -90) {
            val hitResult =
//                calculateArrowCollision(createArrow(speed = if (maxVelocity) 3.0f else (player.itemUseTime - player.itemUseTimeLeft) / 20.0f * 3.0f))
                calculateArrowPath(
                player.eyePos,
                target.boundingBox.center,
                yawToTarget,
                testPitch,
                speed = if (maxVelocity) 3.0 else (player.itemUseTime - player.itemUseTimeLeft) / 20.0 * 3.0,
                maxRange
            )
            if (hitResult != null && hitResult.type == HitResult.Type.ENTITY && hitResult.pos.distanceTo(target.boundingBox.center) < maxOf(
                    target.boundingBox.lengthX,
                    target.boundingBox.lengthY,
                    target.boundingBox.lengthZ
                )
            ) {
                return testPitch
            }
            testPitch -= checkAccuracy
        }
        return null
    }

    private fun InGame.calculateAirOnlyPitch(targetPos: Vec3d, velocity: Float): Float {
        // Velocity based on bow charge.
        var modifiedVelocity: Float = velocity //(player.getItemUseTime() - player.getItemUseTimeLeft()) / 20f
        modifiedVelocity = (modifiedVelocity * modifiedVelocity + modifiedVelocity * 2) / 3
        if (modifiedVelocity > 1) modifiedVelocity = 1f

        // Adjusting for hit box heights
        val arrowStartingYOffset = 0.1
        val relativeX: Double = targetPos.x - player.x
        val relativeY: Double =
            targetPos.y - player.eyePos.y - arrowStartingYOffset//+ if (targetEntityFeet) 0.0 else (target.height / 2.0) -
        val relativeZ: Double = targetPos.z - player.z

        // Calculate the pitch
        val distance = sqrt(relativeX * relativeX + relativeZ * relativeZ)
        val distanceSquared = distance.pow(2)
        val gravity = 0.006f
        val velocitySq = modifiedVelocity * modifiedVelocity
        val pitch = -Math.toDegrees(
            atan(
                (velocitySq - sqrt(velocitySq * velocitySq - gravity * (gravity * distanceSquared + 2 * relativeY * velocitySq)))
                        / (gravity * distance)
            )
        ).toFloat()
        return pitch
    }

    private fun isOutOfBounds(bound1: Double, bound2: Double, value: Double): Boolean =
        min(bound1, bound2) > value || max(bound1, bound2) < value

//    private fun updateRotation(prevRot: Float, newRot: Float): Float {
//        var mutablePrevRotation = prevRot
//        while (newRot - mutablePrevRotation < -180.0f) {
//            mutablePrevRotation -= 360.0f
//        }
//
//        while (newRot - mutablePrevRotation >= 180.0f) {
//            mutablePrevRotation += 360.0f
//        }
//
//        return MathHelper.lerp(0.2f, mutablePrevRotation, newRot)
//    }
}