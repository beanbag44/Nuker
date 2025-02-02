package mc.merge.util

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.network.ClientPlayerInteractionManager
import net.minecraft.client.world.ClientWorld

class InGame (
    val mc: MinecraftClient = MinecraftClient.getInstance(),
    val world: ClientWorld,
    val player: ClientPlayerEntity,
    val interactionManager: ClientPlayerInteractionManager,
    val networkHandler: ClientPlayNetworkHandler,
)

fun <T> runInGame(block: InGame.() -> T) : T? {
    MinecraftClient.getInstance().world?.let { world ->
        MinecraftClient.getInstance().player?.let { player ->
            MinecraftClient.getInstance().interactionManager?.let { interactionManager ->
                MinecraftClient.getInstance().networkHandler?.let { networkHandler ->
                    return InGame(
                        world = world,
                        player = player,
                        interactionManager = interactionManager,
                        networkHandler = networkHandler,
                    ).block()
                }
            }
        }
    }
    return null
}

