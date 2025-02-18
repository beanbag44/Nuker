package mc.merge.event.events

import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult

class InteractBlockEvent(val hand:Hand, val hitResult:BlockHitResult):Event, ICancellable by Cancellable()