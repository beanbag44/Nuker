package me.beanbag;

import lombok.Getter;
import me.beanbag.datatypes.SoundQueueBlock;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.state.property.Properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.beanbag.Nuker.mc;

public class SoundHandler {
    @Getter
    private static final List<SoundQueueBlock> soundQueue = Collections.synchronizedList(new ArrayList<>());
    private static final List<SoundQueueBlock> soundQueueRemove = Collections.synchronizedList(new ArrayList<>());

    public static void onBlockUdatePacket(BlockUpdateS2CPacket packet) {
        for (SoundQueueBlock soundQueueBlock : soundQueue) {
            if (soundQueueBlock.pos.equals(packet.getPos())
                    && (packet.getState().isAir() || (soundQueueBlock.state.getProperties().contains(Properties.WATERLOGGED) && packet.getState().getFluidState().getFluid() instanceof WaterFluid))) {
                Nuker.renderRunnables.add(() -> mc.interactionManager.breakBlock(soundQueueBlock.pos));
                soundQueueRemove.add(soundQueueBlock);
            }
        }
        soundQueue.removeAll(soundQueueRemove);
        soundQueueRemove.clear();
    }
    public static void onChunkDeltaPacket(ChunkDeltaUpdateS2CPacket packet) {
        for (SoundQueueBlock soundQueueBlock : soundQueue) {
            packet.visitUpdates((pos, state) -> {
                if (soundQueueBlock.pos.equals(pos)
                        && (state.isAir() || (soundQueueBlock.state.getProperties().contains(Properties.WATERLOGGED) && state.getFluidState().getFluid() instanceof WaterFluid))) {
                    Nuker.renderRunnables.add(() -> mc.interactionManager.breakBlock(soundQueueBlock.pos));
                    soundQueueRemove.add(soundQueueBlock);
                }
            });
        }
        soundQueue.removeAll(soundQueueRemove);
        soundQueueRemove.clear();
    }
    public static void updateBlockLists() {
        // Sounds timeout check
        soundQueue.forEach(block -> {
            if (block.timer.getPassedTimeMs() > block.timeToMine) {
                soundQueueRemove.add(block);
            }
        });
        soundQueue.removeAll(soundQueueRemove);
        soundQueueRemove.clear();
    }
}
