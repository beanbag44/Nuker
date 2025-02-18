package mc.merge.module.modules

import mc.merge.module.Module
import mc.merge.util.InGame
import mc.merge.util.Versioned.writeNbt

import meteordevelopment.meteorclient.mixininterface.IChatHud
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.*
import net.minecraft.util.Formatting
import net.minecraft.util.hit.BlockHitResult

class EntityNBT: Module("Entity NBT", "Logs focused entity NBT data to console.") {
    var lastFocusedEntity: Entity? = null
    var lastFocusedBlockEntity: BlockEntity? = null

    fun InGame.onMouseMoved() {
        if (Formatting.GRAY.colorValue == null || Formatting.BLUE.colorValue == null) return

        if (mc.targetedEntity != null && mc.targetedEntity !== lastFocusedEntity) {
            lastFocusedEntity = mc.targetedEntity

            val nbt = lastFocusedEntity!!.writeNbt(NbtCompound())
            val message = Text.empty().withColor(Formatting.GRAY.colorValue!!)
            message.append("[")
            message.append(
                Text.literal(lastFocusedEntity!!.name.string).withColor(Formatting.BLUE.colorValue!!)
            )
            message.append("] ")
            message.append(Text.literal(nbt.asString()).withColor(Formatting.GRAY.colorValue!!))
            message.styled { style: Style ->
                style.withClickEvent(
                    ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, nbt.asString())
                ).withHoverEvent(
                    HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.copy.click"))
                )
            }
            (mc.inGameHud.chatHud as IChatHud).`meteor$add`(message, 0)
        } else if (mc.crosshairTarget != null
            && mc.crosshairTarget is BlockHitResult
            && world.getBlockState((mc.crosshairTarget as BlockHitResult).blockPos).hasBlockEntity()) {
            val blockEntity: BlockEntity? = world.getBlockEntity((mc.crosshairTarget as BlockHitResult).blockPos)
            if (blockEntity != null && blockEntity !== lastFocusedBlockEntity) {
                lastFocusedBlockEntity = blockEntity
                val nbt = NbtCompound()
                writeNbt(blockEntity, nbt)
                val message = Text.empty().withColor(Formatting.GRAY.colorValue!!)
                message.append("[")
                message.append(
                    Text.literal(blockEntity.type.toString()).withColor(Formatting.BLUE.colorValue!!)
                )
                message.append("] ")
                message.append(Text.literal(nbt.asString()).withColor(Formatting.GRAY.colorValue!!))
                message.styled { style: Style ->
                    style.withClickEvent(
                        ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, nbt.asString())
                    ).withHoverEvent(
                        HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.copy.click"))
                    )
                }
                (mc.inGameHud.getChatHud() as IChatHud).`meteor$add`(message, 0)
            }
        }
    }
}