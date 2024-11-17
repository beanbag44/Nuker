package me.beanbag.nuker.module.modules

import me.beanbag.nuker.eventsystem.events.PacketEvent
import me.beanbag.nuker.eventsystem.events.RenderEvent
import me.beanbag.nuker.eventsystem.events.TickEvent
import me.beanbag.nuker.eventsystem.onInGameEvent
import me.beanbag.nuker.handlers.BreakingHandler.breakingContexts
import me.beanbag.nuker.handlers.BreakingHandler.checkAttemptBreaks
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

//    val generalGroup = group("General", "General settings for Fast Break")
//    val abortBreaking = setting(generalGroup,"Abort Breaking", "Aborts breaking blocks when the player stops breaking", true)

    val queueGroup = group("Queue", "Settings for the queue")
    val doQueue = setting(queueGroup, "Queue", "Queues blocks to break", true)
    val breakingBlockColor = setting(queueGroup, "Start Queue Color", "The color of blocks at the start of the queue", Color(0x2A8005))
    val endQueueColor = setting(queueGroup, "End Queue Color", "The color of blocks at the end of the queue", Color(0xF98819), visible = { doQueue.getValue() })


    var queue = ConcurrentLinkedQueue<PosAndState>()

    init {
        onInGameEvent<PacketEvent.Send.Pre> { event ->
            if (!enabled) return@onInGameEvent
            if (event.packet is PlayerActionC2SPacket
                    && event.packet.action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK
                    && breakingContexts.none { it?.pos == event.packet.pos }
                    && queue.none { it.blockPos == event.packet.pos }
                ) {
                event.cancel()
                if (doQueue.getValue()){
                    queue.add(PosAndState(event.packet.pos, world.getBlockState(event.packet.pos)))
                } else {
                    queue = ConcurrentLinkedQueue(
                        listOf(PosAndState(event.packet.pos, world.getBlockState(event.packet.pos)))
                    )
                }
                val startedBlocks = checkAttemptBreaks(queue.toList())
                queue.removeIf{
                    startedBlocks.any { startedBlock ->
                        startedBlock.blockPos == it.blockPos
                    }
                }
            }
        }

        onInGameEvent<TickEvent.Pre> {
            if (!enabled) return@onInGameEvent
            queue.removeIf{
                !canReach(player.pos, it.blockPos, CoreConfig.radius)
            }
            val startedBlocks = checkAttemptBreaks(queue.toList())
            queue.removeIf{
                startedBlocks.any { startedBlock ->
                    startedBlock.blockPos == it.blockPos
                }
            }
        }

        onInGameEvent<RenderEvent> { renderEvent ->
            if (!enabled) return@onInGameEvent
            queue.forEachIndexed { index, queueBlock ->
                val color = LerpUtils.lerp(breakingBlockColor.getValue(), endQueueColor.getValue(), index.toDouble() / queue.size)
                renderEvent.renderer.boxLines(Box.from(Vec3d.of(queueBlock.blockPos)), color)
            }
        }
    }
}