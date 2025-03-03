package mc.merge.module.modules

import mc.merge.ModCore.breakingHandler
import mc.merge.event.events.PacketEvent
import mc.merge.event.onInGameEvent
import mc.merge.module.Module
import mc.merge.types.PosAndState
import mc.merge.util.InventoryUtils.getBestTool
import mc.merge.util.InventoryUtils.percentDamagePerTick
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import java.awt.Color

class FastBreak: Module("Fast Break", "Breaks blocks faster") {

//    val generalGroup = group("General", "General settings for Fast Break")
//    val abortBreaking = setting(generalGroup,"Abort Breaking", "Aborts breaking blocks when the player stops breaking", true)

    val queueGroup = group("Queue", "Settings for the queue")
    val doQueue = setting(queueGroup, "Queue", "Queues blocks to break", true)
    val breakingBlockColor = setting(queueGroup, "Start Queue Color", "The color of blocks at the start of the queue", Color(0x2A8005))
    val endQueueColor = setting(queueGroup, "End Queue Color", "The color of blocks at the end of the queue", Color(0xF98819), visible = { doQueue.getValue() })


//    var queue = ConcurrentLinkedQueue<PosAndState>()

    init {
        onInGameEvent<PacketEvent.Send.Pre> { event ->
            if (event.packet is PlayerActionC2SPacket) {
                val pos = event.packet.pos
                val state = world.getBlockState(pos)
                if (event.packet.action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK
                    && breakingHandler.primaryBreakContext?.pos != pos
                    && breakingHandler.doubleBreakContext?.pos != pos
                    && percentDamagePerTick(world.getBlockState(pos), event.packet.pos, getBestTool(state, pos)) < 1) {
                    event.cancel()
                    if (doQueue.getValue()){
                        breakingHandler.breakBlock(PosAndState(event.packet.pos, world.getBlockState(event.packet.pos)), this@FastBreak)
                    }
                }
            }
        }

        enabledSetting.getOnChange().add{
            breakingHandler.removeFromQueue {
                it.owner == this@FastBreak
            }
        }

//        onInGameEvent<RenderEvent.Render3DEvent> { renderEvent ->
//            if (!enabled) return@onInGameEvent
//            queue.forEachIndexed { index, queueBlock ->
//                val color = LerpUtils.lerp(breakingBlockColor.getValue(), endQueueColor.getValue(), index.toDouble() / queue.size)
//                renderEvent.renderer3D.boxLines(Box.from(Vec3d.of(queueBlock.blockPos)), color)
//            }
//        }
    }
}