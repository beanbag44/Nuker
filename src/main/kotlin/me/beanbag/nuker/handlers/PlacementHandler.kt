package me.beanbag.nuker.handlers

import me.beanbag.nuker.utils.InGame
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

object PlacementHandler : IHandler {
    override var priority = 0
    override var currentlyBeingUsedBy: Module? = null

    fun InGame.airPlace(pos: BlockPos, direction: Direction, swing: Boolean, awaitServerResponse: Boolean) {
        offhandDoohickey()

        if (awaitServerResponse) {
            networkHandler.sendPacket(
                PlayerInteractBlockC2SPacket(
                    Hand.MAIN_HAND,
                    BlockHitResult(
                        pos.toCenterPos(),
                        direction,
                        pos,
                        true
                    ),
                    0
                )
            )
        } else {
            interactionManager.interactBlock(
                player,
                Hand.MAIN_HAND,
                BlockHitResult(
                    pos.toCenterPos(),
                    Direction.UP,
                    pos,
                    true
                )
            )
        }

        if (swing) {
            networkHandler.sendPacket(HandSwingC2SPacket(Hand.MAIN_HAND))
        }

        offhandDoohickey()
    }

    private fun InGame.offhandDoohickey() {
        networkHandler.sendPacket(
            PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                BlockPos(420, 69, 420),
                Direction.UP
            )
        )
    }
}