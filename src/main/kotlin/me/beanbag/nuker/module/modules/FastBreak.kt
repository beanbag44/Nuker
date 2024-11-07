package me.beanbag.nuker.module.modules

import me.beanbag.nuker.eventsystem.events.PacketEvent
import me.beanbag.nuker.eventsystem.events.RenderEvent
import me.beanbag.nuker.eventsystem.events.TickEvent
import me.beanbag.nuker.eventsystem.onInGameEvent
import me.beanbag.nuker.handlers.BreakingHandler.breakingContexts
import me.beanbag.nuker.handlers.BreakingHandler.checkAttemptBreaks
import me.beanbag.nuker.handlers.BreakingHandler.nullifyBreakingContext
import me.beanbag.nuker.module.Module
import me.beanbag.nuker.types.PosAndState
import me.beanbag.nuker.utils.BlockUtils.canReach
import me.beanbag.nuker.utils.LerpUtils
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import java.awt.Color
import java.util.concurrent.ConcurrentLinkedQueue

class FastBreak:Module("Fast Break", "Breaks blocks faster") {

    val generalGroup = group("General", "General settings for Fast Break")
    val abortBreaking = setting(generalGroup,"Abort Breaking", "Aborts breaking blocks when the player stops breaking", true)

    val queueGroup = group("Queue", "Settings for the queue")
    val doQueue = setting(queueGroup, "Queue", "Queues blocks to break", true, visible =  { !abortBreaking.getValue() })
    val breakingBlockColor = setting(queueGroup, "Start Queue Color", "The color of blocks at the start of the queue", Color(0x2c4f1d))
    val endQueueColor = setting(queueGroup, "End Queue Color", "The color of blocks at the end of the queue", Color(0xF98819), visible = { doQueue.getValue() })


    var queue = ConcurrentLinkedQueue<PosAndState>()

    init {
        onInGameEvent<PacketEvent.Send.Pre> { event ->
            if (event.packet is PlayerActionC2SPacket
                    && event.packet.action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK
                    && breakingContexts.none { it?.pos == event.packet.pos }
                    && queue.none { it.blockPos == event.packet.pos }
                ) {
                event.cancel()
                if (doQueue.getValue()){
                    queue.add(PosAndState(event.packet.pos, world.getBlockState(event.packet.pos)))
                } else {
                    queue = ConcurrentLinkedQueue()
                    queue.add(PosAndState(event.packet.pos, world.getBlockState(event.packet.pos)))
                }
            } else if (event.packet is PlayerActionC2SPacket
                    && event.packet.action == PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK
                ){
                if (abortBreaking.getValue()) {
                    breakingContexts.forEachIndexed() { index, it ->
                        if(it?.pos == event.packet.pos) {
                            nullifyBreakingContext(index)
                        }
                    }
                    queue.removeIf{ it.blockPos == event.packet.pos }
                } else {
                    event.cancel()
                }

            }
        }

//        onInGameEvent<PacketEvent.Receive.Post> { event ->
//            if (event.packet is BlockUpdateS2CPacket) {
//                queue.removeIf { it.blockPos == event.packet.pos && BlockUtils.isBlockBroken(it.blockState, event.packet.state)}
//            }
//        }

        onInGameEvent<TickEvent.Pre> {
            queue.removeIf{
                !canReach(player.pos, it, CoreConfig.radius)
            }
            checkAttemptBreaks(queue.toList())
            queue.removeIf{ queueBlock ->
                breakingContexts.any{
                    it?.pos == queueBlock.blockPos
                }
            }
        }

        onInGameEvent<RenderEvent> { renderEvent ->
            queue.forEachIndexed { index, queueBlock ->
                val color = LerpUtils.lerp(breakingBlockColor.getValue(), endQueueColor.getValue(), index.toDouble() / queue.size)
                renderEvent.renderer.boxLines(Box.from(Vec3d.of(queueBlock.blockPos)), color)
            }
        }
    }
}