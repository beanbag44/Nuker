package mc.merge.event.events

import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult

class UseBlockEvent(val hitResult: BlockHitResult) : Event