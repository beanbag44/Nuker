package me.beanbag;

import baritone.api.selection.ISelection;
import lombok.Getter;
import me.beanbag.datatypes.MiningBlock;
import me.beanbag.datatypes.PosAndState;
import me.beanbag.datatypes.SoundQueueBlock;
import me.beanbag.utils.*;
import me.beanbag.utils.Timer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static me.beanbag.Nuker.mc;
import static me.beanbag.utils.BlockUtils.getBlockBreakingTimeMS;

public class BreakingHandler {
    private static int packetCounter = 0;
    @Getter
    private static final List<MiningBlock> miningBlocks = Collections.synchronizedList(new ArrayList<>());
    private static final ConcurrentHashMap<MiningBlock, Timer> ghostBlockCheckSet = new ConcurrentHashMap<>();
    private static final Set<MiningBlock> ghostBlockCheckSetRemove = Collections.synchronizedSet(new HashSet<>());
    private static final Map<PosAndState, Timer> blockTimeout = new ConcurrentHashMap<>();
    public static void executeBreakAttempts(List<BlockPos> blockList) {
        updateBlockLists();

        packetCounter = 0;

        // Iterate through the list
        for (BlockPos block : blockList) {

            // If possible, mine / start mining the block
            if (canMine(block)) {

                // Top down gravity block checks to shift the block position to the top of the column
                if (mc.world.getBlockState(block).getBlock() instanceof FallingBlock) {
                    while (mc.world.getBlockState(block.add(0, 1, 0)).getBlock() instanceof FallingBlock
                            && BlockUtils.isWithinRadius(mc.player.getEyePos(), block.add(0, 1, 0), Nuker.radius)
                            && canMine(block.add(0, 1, 0))) {
                        block = block.add(0, 1, 0);
                    }
                }

                // Swaps to the right tool
                int bestTool = InventoryUtils.getBestToolSlot(block);
                if (mc.player.getInventory().selectedSlot != bestTool) {
                    mc.player.getInventory().selectedSlot = bestTool;
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(bestTool));
                    packetCounter++;
                }

                // Mine block
                mineBlock(block);
            }
        }
    }
    private static void mineBlock(BlockPos blockPos) {
        double breakingTime = getBlockBreakingTimeMS(mc.player.getInventory().getMainHandStack(), blockPos, mc.player, mc.world);
        blockTimeout.put(new PosAndState(blockPos, breakingTime), new Timer().reset());
        BlockState state = mc.world.getBlockState(blockPos);
        MiningBlock miningBlock = new MiningBlock(state
                , blockPos
                , state.getBlock()
                , breakingTime
                , new Timer().reset()
                , false
                , InventoryUtils.getBestToolSlot(blockPos)
        );
        // Double block
        if (miningBlocks.size() == 1) {
            miningBlocks.get(0).serverMine = true;
        }

        if (breakingTime > Nuker.instaMineThreshold) {
            miningBlocks.add(miningBlock);
        }
        // Different mine packets for hardness values
        if (breakingTime <= Nuker.instaMineThreshold) {
            if (breakingTime > 50) {
                stopDestroy(blockPos);
                startDestroy(blockPos);
                stopDestroy(blockPos);
            } else {
                startDestroy(blockPos);
            }
            if (Nuker.clientBreak) {
                ghostBlockCheckSet.put(miningBlock, new Timer().reset());
                Nuker.renderRunnables.add(() -> mc.interactionManager.breakBlock(blockPos));
            }
        } else if (breakingTime > Nuker.instaMineThreshold) {
            startDestroy(blockPos);
            abortDestroy(blockPos);
            stopDestroy(blockPos);
        }
        // Packet limiter
        if (Nuker.packetLimit != -1) {
            if (breakingTime > 50) {
                packetCounter += 3;
            } else {
                packetCounter++;
            }
        }
        // Sounds
        SoundHandler.getSoundQueue().add(new SoundQueueBlock(
                breakingTime * 3 + 500
                , blockPos
                , state
                , new Timer().reset())
        );
    }
    private static boolean canMine(BlockPos pos) {
        ItemStack stack= mc.player.getInventory().getStack(InventoryUtils.getBestToolSlot(pos));
        // Flatten modes
        switch (Nuker.flattenMode) {
            case STANDARD -> {
                if (Nuker.crouchLowerFlatten && mc.player.isSneaking() ? pos.getY() < mc.player.getBlockY() - 1 : pos.getY() < mc.player.getBlockY()) {
                    return false;
                }
            }
            case SMART -> {
                if (Nuker.crouchLowerFlatten && mc.player.isSneaking() ? pos.getY() < mc.player.getBlockY() - 1 : pos.getY() < mc.player.getBlockY()) {
                    Direction playerDirection = mc.player.getHorizontalFacing();
                    Vec3d blockVector = new Vec3d(pos.subtract(mc.player.getBlockPos()).getX(),
                            pos.subtract(mc.player.getBlockPos()).getY(),
                            pos.subtract(mc.player.getBlockPos()).getZ()
                    );

                    if ((blockVector.getX() < 1 && playerDirection == Direction.WEST)
                            || (blockVector.getX() > -1 && playerDirection == Direction.EAST)
                            || (blockVector.getZ() < 1 && playerDirection == Direction.NORTH)
                            || (blockVector.getZ() > -1 && playerDirection == Direction.SOUTH)) {
                        return false;
                    }
                    // without this, you sometimes get grim issues when walking down a staircase with smart flatten on
                    if (mc.player.supportingBlockPos.isPresent()
                            && mc.player.supportingBlockPos.get().equals(pos)) {
                        return false;
                    }
                }
            }
            case REVERSE_SMART -> {
                if (Nuker.crouchLowerFlatten && mc.player.isSneaking() ? pos.getY() < mc.player.getBlockY() - 1 : pos.getY() < mc.player.getBlockY()) {
                    Direction playerDirection = mc.player.getHorizontalFacing();
                    Vec3d blockVector = new Vec3d(pos.subtract(mc.player.getBlockPos()).getX(),
                            pos.subtract(mc.player.getBlockPos()).getY(),
                            pos.subtract(mc.player.getBlockPos()).getZ()
                    );

                    if ((blockVector.getX() > -1 && playerDirection == Direction.WEST)
                            || (blockVector.getX() < 1 && playerDirection == Direction.EAST)
                            || (blockVector.getZ() > -1 && playerDirection == Direction.NORTH)
                            || (blockVector.getZ() < 1 && playerDirection == Direction.SOUTH)) {
                        return false;
                    }
                    // without this, you sometimes get grim issues when walking down a staircase with smart flatten on
                    if (mc.player.supportingBlockPos.isPresent()
                            && mc.player.supportingBlockPos.get().equals(pos)) {
                        return false;
                    }
                }
            }
        }

        if (miningBlocks.size() >= 2) {
            return false;
        } else if (!miningBlocks.isEmpty()) {
            MiningBlock block = miningBlocks.get(0);

            if (block.pos.equals(pos)) {
                return false;
            } else if (stack.getItem() != mc.player.getInventory().getStack(block.tool).getItem()) {
                return false;
            }
        }

        double timeToMine = getBlockBreakingTimeMS(
                stack
                , pos
                , mc.player
                , mc.world
        );
        // Block mine time limit
        if (timeToMine > 10000) {
            return false;
        }
        // Packet limit
        int packets = 1;
        if (timeToMine > 50) {
            packets = 3;
        }
        if (packetCounter >= Nuker.packetLimit
                || packetCounter + packets >= Nuker.packetLimit) {
            return false;
        }
        // Block timeout
        for (PosAndState pas : blockTimeout.keySet()) {
            if (pos.equals(pas.pos)) {
                return false;
            }
        }
        // Avoid liquids
        if (Nuker.avoidLiquids) {
            for (final Direction direction : Direction.values()) {
                BlockPos newPos = pos.offset(direction);
                BlockState newState = mc.world.getBlockState(newPos);
                if (newState.getFluidState().isIn(FluidTags.WATER)
                        || newState.getFluidState().isIn(FluidTags.LAVA)
                        || newState.isOf(Blocks.WATER)
                        || newState.isOf(Blocks.LAVA)) {
                    return false;
                }
            }

            BlockPos newPos = pos;
            while (mc.world.getBlockState(newPos.offset(Direction.UP)).getBlock() instanceof FallingBlock) {
                newPos = newPos.up();
                for (Direction directionFallingColumn : Direction.values()) {
                    BlockPos newPosTemp = newPos.offset(directionFallingColumn);
                    BlockState bState = mc.world.getBlockState(newPosTemp);
                    if (bState.getFluidState().isIn(FluidTags.WATER)
                            || bState.getFluidState().isIn(FluidTags.LAVA)
                            || bState.isOf(Blocks.WATER)
                            || bState.isOf(Blocks.LAVA)) {
                        return false;
                    }
                }
            }
        }
        // Litematica
        if (Nuker.litematica
                && !LitematicaUtils.schematicMismatches.contains(pos)) {
            return false;
        }

        if (Nuker.canalMode) {
            int x = pos.getX();
            int y = pos.getY();

            if (!(((y >= 59 && x >= -13 && x <= 12)
                    || (x == 13 && y >= 60)
                    || (x == -14 && y >= 60)
                    || (y >= 62 && x >= 13 && x <= 15)
                    || (y >= 62 && x >= -16 && x <= -14))
                    && pos.getZ() > 0)) {
                return false;
            }

            if ((y == 59
                    || x == 13 && y <= 62
                    || x == -14 && y <= 62
                    || y == 62 && x >= 13
                    || y == 62 && x <= -14)
                    && mc.world.getBlockState(pos).getBlock().equals(Blocks.OBSIDIAN)) {
                return false;
            }
        }

        // baritone selection mode
        if (Nuker.baritoneSelection) {
            boolean withinBaritoneSelection = false;

            if (!BaritoneUtils.baritoneSelections.isEmpty()) {
                for (ISelection selection : BaritoneUtils.baritoneSelections) {
                    BlockPos pos1 = new BlockPos(selection.pos1().x, selection.pos1().y, selection.pos1().z);
                    BlockPos pos2 = new BlockPos(selection.pos2().x, selection.pos2().y, selection.pos2().z);
                    int minX = Math.min(pos1.getX(), pos2.getX());
                    int minY = Math.min(pos1.getY(), pos2.getY());
                    int minZ = Math.min(pos1.getZ(), pos2.getZ());
                    int maxX = Math.max(pos1.getX(), pos2.getX());
                    int maxY = Math.max(pos1.getY(), pos2.getY());
                    int maxZ = Math.max(pos1.getZ(), pos2.getZ());
                    if (pos.getX() >= minX && pos.getX() <= maxX
                            && pos.getY() >= minY && pos.getY() <= maxY
                            && pos.getZ() >= minZ && pos.getZ() <= maxZ) {
                        withinBaritoneSelection = true;
                        break;
                    }
                }
            }
            return withinBaritoneSelection;
        }
        return true;
    }
    private static void updateBlockLists() {

        // Ghost block timeout check
        ghostBlockCheckSet.forEach((b, t) -> {
            if (ghostBlockCheckSet.get(b).getPassedTimeMs() > Nuker.clientBreakGhostBlockTimeout) {
                mc.world.setBlockState(b.pos, b.block.getDefaultState());
                if (!(mc.player.getMainHandStack().getItem() instanceof BlockItem)) {
                    mc.getNetworkHandler().sendPacket(
                            new PlayerInteractBlockC2SPacket(
                                    Hand.MAIN_HAND
                                    , new BlockHitResult(b.pos.toCenterPos()
                                    , Direction.UP
                                    , b.pos
                                    , true)
                                    , 0)
                    );
                }
                ghostBlockCheckSetRemove.add(b);
            }
        });
        ghostBlockCheckSetRemove.forEach(ghostBlockCheckSet::remove);
        ghostBlockCheckSetRemove.clear();

        // Block timeout check
        blockTimeout.keySet().removeIf(
                next -> blockTimeout.get(next).getPassedTimeMs() > Nuker.blockTimeoutDelay + next.ttm);

        // Checks the currently mining blocks
        Iterator<MiningBlock> iterator = miningBlocks.iterator();
        while(iterator.hasNext()) {
            MiningBlock block = iterator.next();
            if (!mc.world.getBlockState(block.pos).getBlock().equals(block.block)
                    || block.serverMine ? block.timer.getPassedTimeMs() >= block.ttm : block.timer.getPassedTimeMs() >= block.ttm * 0.7) {
                if (Nuker.clientBreak) {
                    ghostBlockCheckSet.put(block, new Timer().reset());
                    Nuker.renderRunnables.add(() -> mc.interactionManager.breakBlock(block.pos));
                }
                iterator.remove();
            } else if (!block.serverMine) {
                mc.player.getInventory().selectedSlot = block.tool;
                stopDestroy(block.pos);
            }
        }
    }
    public static void onBlockUpdatePacket(BlockUpdateS2CPacket packet) {
        // Ghost block checks
        for (MiningBlock block : ghostBlockCheckSet.keySet()) {
            if (block.pos.equals(packet.getPos())
                    && (packet.getState().isAir() || (block.state.getProperties().contains(Properties.WATERLOGGED) && packet.getState().getFluidState().getFluid() instanceof WaterFluid))) {
                ghostBlockCheckSetRemove.add(block);
            }
        }
        ghostBlockCheckSetRemove.forEach(ghostBlockCheckSet::remove);
        ghostBlockCheckSetRemove.clear();
    }
    public static void onChunkDeltaPacket(ChunkDeltaUpdateS2CPacket packet) {
        // Ghost block checks
        for (MiningBlock block : ghostBlockCheckSet.keySet()) {
            packet.visitUpdates((pos, state) -> {
                if (block.pos.equals(pos)
                        && (state.isAir() || (block.state.getProperties().contains(Properties.WATERLOGGED) && state.getFluidState().getFluid() instanceof WaterFluid))) {
                    ghostBlockCheckSetRemove.add(block);
                }
            });
        }
        ghostBlockCheckSetRemove.forEach(ghostBlockCheckSet::remove);
        ghostBlockCheckSetRemove.clear();
    }
    private static void startDestroy(BlockPos blockPos) {
        if (mc.getNetworkHandler() == null) return;
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
    }
    private static void abortDestroy(BlockPos blockPos) {
        if (mc.getNetworkHandler() == null) return;
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, blockPos, Direction.UP));
    }
    private static void stopDestroy(BlockPos blockPos) {
        if (mc.getNetworkHandler() == null) return;
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
    }
}
