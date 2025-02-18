package mc.merge.task

import mc.merge.ModCore.baritoneProcess
import mc.merge.event.events.TickEvent
import mc.merge.event.onInGameEvent
import mc.merge.handler.IHandlerController
import mc.merge.util.runInGame
import net.minecraft.item.RangedWeaponItem

class DrawBowTask(parent: IHandlerController) :Task(parent){
    override fun run() {
        super.run()
        baritoneProcess.pauseBaritone()
        onInGameEvent<TickEvent.Pre> {
            if (player.mainHandStack.item !is RangedWeaponItem) {
                return@onInGameEvent
            }
            mc.crosshairTarget = null // might need to use a mixin for this. See meteor auto eat
            mc.options.useKey.isPressed = true
        }
    }

    override fun finish() {
        super.finish()
        runInGame {
            mc.options.useKey.isPressed = false
            baritoneProcess.resumeBaritone()
        }
    }
}