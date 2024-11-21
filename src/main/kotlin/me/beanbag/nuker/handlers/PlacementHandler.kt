package me.beanbag.nuker.handlers

import me.beanbag.nuker.ModConfigs.inventoryHandler
import me.beanbag.nuker.eventsystem.EventBus
import me.beanbag.nuker.eventsystem.EventBus.MAX_PRIORITY
import me.beanbag.nuker.eventsystem.EventBus.MIN_PRIORITY
import me.beanbag.nuker.eventsystem.events.TickEvent
import me.beanbag.nuker.eventsystem.onInGameEvent
import me.beanbag.nuker.module.modules.CoreConfig
import me.beanbag.nuker.types.PosAndState
import me.beanbag.nuker.types.TimeoutSet
import me.beanbag.nuker.utils.BlockUtils.canReach
import me.beanbag.nuker.utils.BlockUtils.isStateEmpty
import me.beanbag.nuker.utils.InGame
import me.beanbag.nuker.utils.InventoryUtils.getInHotbar
import me.beanbag.nuker.utils.InventoryUtils.swapTo
import net.minecraft.item.BlockItem
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

object PlacementHandler : IHandler {
    override var priority = 0
    override var currentlyBeingUsedBy: Module? = null
    val blockPlaceTimeouts = TimeoutSet<BlockPos> { CoreConfig.blockPlaceTimeout }
    private val airPlaceList = mutableMapOf<ArrayList<PosAndState>, PlacementPreference>()
    var usedThisTick = false

    init {
        EventBus.onInGameEvent<TickEvent.Pre>(MAX_PRIORITY) {
            airPlaceList.keys.forEach {
                it.removeIf {
                    !canPlace(it)
                }
            }

            airPlaceList.keys.removeIf {
                it.isEmpty()
            }

            if (airPlaceList.isEmpty()) return@onInGameEvent

            with(airPlaceList.keys.elementAt(0)) {
                val preferences = airPlaceList[this]
                with(this[0]) {
                    swapTo(blockState.block.asItem())
                    preferences?.let { preferences ->
                        airPlace(blockPos, preferences.direction, preferences.swing, preferences.awaitServerResponse)
                    }
                }
                remove(this[0])

                if (isEmpty()) {
                    airPlaceList.remove(this)
                }
            }
        }

        EventBus.onInGameEvent<TickEvent.Post>(MIN_PRIORITY) {
            usedThisTick = false
        }
    }

    fun InGame.attemptPlaceAll(posAndStateList: ArrayList<PosAndState>, direction: Direction, swing: Boolean, awaitServerResponse: Boolean) {
        posAndStateList.removeIf {
            !canPlace(it) || blockPlaceTimeouts.values().contains(it.blockPos)
        }

        if (posAndStateList.isEmpty()) return

        if (airPlaceList.isNotEmpty()) {
            airPlaceList[posAndStateList] = PlacementPreference(direction, swing, awaitServerResponse)
            return
        }

        with(posAndStateList[0]) {
            swapTo(blockState.block.asItem())
            airPlace(blockPos, direction, swing, awaitServerResponse)
        }
        posAndStateList.removeAt(0)

        if (posAndStateList.isEmpty()) return

        airPlaceList[posAndStateList] = PlacementPreference(direction, swing, awaitServerResponse)
    }

    fun InGame.canPlace(posAndState: PosAndState): Boolean {
        return canReach(player.eyePos, posAndState.blockPos, CoreConfig.breakRadius)
                && getInHotbar(posAndState.blockState.block.asItem()) != -1
                && isStateEmpty(world.getBlockState(posAndState.blockPos))
    }

    fun InGame.airPlace(pos: BlockPos, direction: Direction, swing: Boolean, awaitServerResponse: Boolean): Boolean {
        if (blockPlaceTimeouts.values().contains(pos)) return false

        val handStack = player.mainHandStack
        if (handStack.item !is BlockItem) return false

        inventoryHandler.offhandDoohickey()

        interactionManager.interactBlock(
            player,
            Hand.OFF_HAND,
            BlockHitResult(
                pos.toCenterPos(),
                direction,
                pos,
                true
            )
        )

        if (!awaitServerResponse) {
            val state = (handStack.item as BlockItem).block.defaultState
            world.setBlockState(pos, state)
            val blockSoundGroup = state.soundGroup
            world.playSound(
                player,
                pos,
                state.soundGroup.placeSound,
                SoundCategory.BLOCKS,
                (blockSoundGroup.getVolume() + 1.0f) / 2.0f,
                blockSoundGroup.getPitch() * 0.8f
            )
        }

        if (swing) {
            player.swingHand(Hand.MAIN_HAND)
        }

        inventoryHandler.offhandDoohickey()

        usedThisTick = true
        blockPlaceTimeouts.put(pos)
        return true
    }


    class PlacementPreference(
        val direction: Direction,
        val swing: Boolean,
        val awaitServerResponse: Boolean
    )
}