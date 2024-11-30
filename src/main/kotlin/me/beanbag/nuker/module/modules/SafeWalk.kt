package me.beanbag.nuker.module.modules

import me.beanbag.nuker.eventsystem.events.PlayerMoveEvent
import me.beanbag.nuker.eventsystem.onInGameEvent
import me.beanbag.nuker.external.meteor.MeteorModule
import me.beanbag.nuker.module.Module

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
                            player.isSneaking = true
                            player.input.sneaking = true
                        }
                    }
                }
            }
        }
    }


    override fun createMeteorImplementation(): meteordevelopment.meteorclient.systems.modules.Module {
       return SafeWalkMeteorImplementation(this)
    }
    class SafeWalkMeteorImplementation(module:Module) : MeteorModule(module)

}