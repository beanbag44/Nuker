package mc.merge.module.modules

import mc.merge.event.events.PlayerMoveEvent
import mc.merge.event.onInGameEvent
import mc.merge.module.Module
import mc.merge.util.Versioned.setSneaking

class SafeWalk : Module("Safe Walk", "Prevents you from falling off blocks") {
    val generalGroup = group("General", "General settings")

    val range by setting(generalGroup, "Range", "The range at which you start crouching",0.25)

    init {
        onInGameEvent<PlayerMoveEvent> {
            if(player.isOnGround && !player.isTouchingWater) {
                val boundingBox = player.boundingBox

                for (x in -1..1) {
                    for (z in -1..1) {
                        val supportingBlockExists = world.findSupportingBlockPos(
                            player,
                            boundingBox.offset(x * range, -1.0E-6, z * range)
                        ).isPresent
                        if (!supportingBlockExists) {
                            setSneaking(true)
                        }
                    }
                }
            }
        }
    }
}