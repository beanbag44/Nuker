package me.beanbag;

import lombok.Getter;
import me.beanbag.datatypes.SoundQueueBlock;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.state.property.Properties;

import java.util.*;

import static me.beanbag.Nuker.mc;

public class SoundHandler {
    @Getter
    private static final List<SoundQueueBlock> soundQueue = Collections.synchronizedList(new ArrayList<>());

    public static void onBlockUdatePacket(BlockUpdateS2CPacket packet) {
        soundQueue.removeIf(soundQueueBlock -> {
            if (soundQueueBlock.pos.equals(packet.getPos())
                    && (packet.getState().isAir() || (soundQueueBlock.state.getProperties().contains(Properties.WATERLOGGED) && packet.getState().getFluidState().getFluid() instanceof WaterFluid))) {
                Nuker.renderRunnables.add(() -> soundQueueBlock.state.getBlock().onBreak(mc.world, soundQueueBlock.pos, soundQueueBlock.state, mc.player));
                return true;
            } else {
                return false;
            }
        });
    }
    public static void onChunkDeltaPacket(ChunkDeltaUpdateS2CPacket packet) {
        soundQueue.removeIf(soundQueueBlock -> {
            Set<BlockState> matchList = new HashSet<>();
            packet.visitUpdates((pos, state) -> {
                if (soundQueueBlock.pos.equals(pos)
                        && (state.isAir() || (soundQueueBlock.state.getProperties().contains(Properties.WATERLOGGED) && state.getFluidState().getFluid() instanceof WaterFluid))) {
                    Nuker.renderRunnables.add(() -> soundQueueBlock.state.getBlock().onBreak(mc.world, soundQueueBlock.pos, soundQueueBlock.state, mc.player));
                    matchList.add(state);
                }
            });
            return !matchList.isEmpty();
        });
    }
    public static void updateBlockLists() {
        // Sounds timeout check
        soundQueue.removeIf(block -> block.timer.getPassedTimeMs() > block.awaitReceiveTime);
    }
}
