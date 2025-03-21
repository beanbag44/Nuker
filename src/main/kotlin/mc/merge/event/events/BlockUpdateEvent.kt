package mc.merge.event.events

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

class BlockUpdateEvent(val pos:BlockPos, val state:BlockState): Event