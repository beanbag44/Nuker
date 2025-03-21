package mc.merge.task

import mc.merge.ModCore.inventoryHandler
import mc.merge.event.events.TickEvent
import mc.merge.event.onInGameEvent
import mc.merge.handler.IHandlerController
import mc.merge.handler.PlacementHandler.airPlace
import mc.merge.module.modules.CoreConfig
import mc.merge.types.PosAndState
import mc.merge.util.BlockUtils.getBlockSphere
import mc.merge.util.BlockUtils.isStateEmpty
import mc.merge.util.InventoryUtils.getInHotbar
import mc.merge.util.runInGame
import net.minecraft.block.Block
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class PlaceTask(parent: IHandlerController, private val blockForPos: (pos: BlockPos) -> Block?, ) : Task(parent) {

    companion object {
        fun placeableBlocks(blockForPos: (BlockPos) -> Block?):  List<PosAndState> {
            return runInGame {

                val sphere = getBlockSphere(player.eyePos, CoreConfig.placeRadius) { pos, _ ->
                     blockForPos(pos) == null
                            || getInHotbar(blockForPos(pos)!!.asItem()) == -1
                            || !isStateEmpty(world.getBlockState(pos))
                }
                val mappedSphere = sphere.map { PosAndState(it.blockPos, blockForPos(it.blockPos)!!.defaultState) }
                return@runInGame mappedSphere
            } ?: listOf()
        }
    }

    override fun run() {
        super.run()
        onInGameEvent<TickEvent.Pre> {
            val blockPlacePos = placeableBlocks (blockForPos)

            if (blockPlacePos.isEmpty()) {
                this@PlaceTask.finish()
                return@onInGameEvent
            }
            val placeBlock = blockPlacePos.first()
            val blockSlot = getInHotbar(placeBlock.blockState.block.asItem())

            inventoryHandler.hotBarController.trySelectingSlot(blockSlot, this@PlaceTask)
            if (inventoryHandler.hotBarController.canUse(this@PlaceTask)) {
                airPlace(placeBlock.blockPos, Direction.DOWN, CoreConfig.swingOnPlace, CoreConfig.validatePlace)
            }


        }
    }
}