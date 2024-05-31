package me.beanbag;

import baritone.api.selection.ISelection;
import me.beanbag.datatypes.PosAndState;
import me.beanbag.utils.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static me.beanbag.Nuker.mc;
import static me.beanbag.Nuker.spherePosList;
import static me.beanbag.utils.RotationsManager.canSeeBlockFace;

public class PlacementHandler {

    private static final Map<PosAndState, Timer> placeBlockTimeout = new ConcurrentHashMap<>();
    private static Vec3d lookPos = new Vec3d(0, 0, 0);
    private static boolean preRotated = false;

    public static boolean excecutePlacements() {
        updateBlockLists();

        if (BreakingHandler.getMiningBlocks().isEmpty()) {
            if (Nuker.canalMode) {
                if (canalPlacements()) return true;
            }

            Nuker.spherePosList.removeIf(pos -> mc.world.getBlockState(pos).isAir());

            if (Nuker.sourceRemover) {
                if (sourceRemoverPlacements()) return true;
            }
        } else {
            Nuker.spherePosList.removeIf(pos -> mc.world.getBlockState(pos).isAir());
        }
        return false;
    }

    private static @Nullable BlockHitResult canPlace(BlockPos pos) {

        if (Nuker.baritoneSelection) {
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
                    if (Nuker.expandBaritoneSelectionsForLiquids) {
                        minX -= 1;
                        minY -= 1;
                        minZ -= 1;
                        maxX += 1;
                        maxY += 1;
                        maxZ += 1;
                    }
                    if (pos.getX() < minX || pos.getX() > maxX
                            || pos.getY() < minY || pos.getY() > maxY
                            || pos.getZ() < minZ || pos.getZ() > maxZ) {
                        return null;
                    }
                }
            }
        }
        if (mc.player == null
                || mc.world == null) return null;
        Vec3d centerPos = pos.toCenterPos();
        if (mc.world.getBlockState(pos.add(0, 1, 0)).isFullCube(mc.world, pos.add(0, 1, 0))
                && canSeeBlockFace(mc.player, pos.add(0, 1, 0), Direction.DOWN)
                && mc.player.getEyePos().distanceTo(new Vec3d(centerPos.x, centerPos.y + 0.5, centerPos.z)) <= 4.5) {
            return new BlockHitResult(centerPos.add(0, 0.5, 0), Direction.DOWN, pos.add(0, 1, 0), false);

        } else if (mc.world.getBlockState(pos.add(0, -1, 0)).isFullCube(mc.world, pos.add(0, -1, 0))
                && canSeeBlockFace(mc.player, pos.add(0, -1, 0), Direction.UP)
                && mc.player.getEyePos().distanceTo(new Vec3d(centerPos.x, centerPos.y - 0.5, centerPos.z)) <= 4.5) {
            return new BlockHitResult(centerPos.add(0, -0.5, 0), Direction.UP, pos.add(0, -1, 0), false);

        } else if (mc.world.getBlockState(pos.add(1, 0, 0)).isFullCube(mc.world, pos.add(1, 0, 0))
                && canSeeBlockFace(mc.player, pos.add(1, 0, 0), Direction.WEST)
                && mc.player.getEyePos().distanceTo(new Vec3d(centerPos.x + 0.5, centerPos.y, centerPos.z)) <= 4.5) {
            return new BlockHitResult(centerPos.add(0.5, 0, 0), Direction.WEST, pos.add(1, 0, 0), false);

        } else if (mc.world.getBlockState(pos.add(0, 0, 1)).isFullCube(mc.world, pos.add(0, 0, 1))
                && canSeeBlockFace(mc.player, pos.add(0, 0, 1), Direction.NORTH)
                && mc.player.getEyePos().distanceTo(new Vec3d(centerPos.x, centerPos.y, centerPos.z + 0.5)) <= 4.5) {
            return new BlockHitResult(centerPos.add(0, 0, 0.5), Direction.NORTH, pos.add(0, 0, 1), false);

        } else if (mc.world.getBlockState(pos.add(-1, 0, 0)).isFullCube(mc.world, pos.add(-1, 0, 0))
                && canSeeBlockFace(mc.player, pos.add(-1, 0, 0), Direction.EAST)
                && mc.player.getEyePos().distanceTo(new Vec3d(centerPos.x - 0.5, centerPos.y, centerPos.z)) <= 4.5) {
            return new BlockHitResult(centerPos.add(-0.5, 0, 0), Direction.EAST, pos.add(-1, 0, 0), false);

        } else if (mc.world.getBlockState(pos.add(0, 0, -1)).isFullCube(mc.world, pos.add(0, 0, -1))
                && canSeeBlockFace(mc.player, pos.add(0, 0, -1), Direction.SOUTH)
                && mc.player.getEyePos().distanceTo(new Vec3d(centerPos.x, centerPos.y, centerPos.z - 0.5)) <= 4.5) {
            return new BlockHitResult(centerPos.add(0, 0, -0.5), Direction.SOUTH, pos.add(0, 0, -1), false);
        }
        return null;
    }

    private static boolean canalPlacements() {
        if (mc.player == null
                || mc.world == null
                || mc.getNetworkHandler() == null) {
            return true;
        }
        List<BlockPos> blockList = new ArrayList<>(spherePosList);

        blockList.removeIf(b -> {
            int x = b.getX();
            int y = b.getY();
            return !(
                    (
                            (y == 59 && x >= -13 && x <= 12)
                                    || (x == 13 && y >= 60 && y <= 62)
                                    || (x == -14 && y >= 60 && y <= 62)
                                    || (y == 62 && x >= 13 && x <= 14)
                                    || (y == 62 && x >= -15 && x <= -14)
                    ) && mc.world.getBlockState(b).isReplaceable()
                            && b.getZ() > 0
            );
        });

        if (!blockList.isEmpty()) {
            List<BlockPos> playerBlocks = PlaceUtils.getBlocksPlayerOccupied();

            blockList = BlockUtils.sortBlocks(blockList);
            for (BlockPos b : blockList) {
                boolean inTimeout = false;
                for (PosAndState ps : placeBlockTimeout.keySet()) {
                    if (ps.pos.equals(b)) {
                        inTimeout = true;
                        break;
                    }
                }
                if (inTimeout) {
                    continue;
                }
                BlockHitResult placeResult = canPlace(b);
                if (placeResult != null
                        && !playerBlocks.contains(placeResult.getBlockPos())) {
                    int obi = PlaceUtils.findObsidian();
                    if (obi != -1) {
                        if (!mc.player.getMainHandStack().getItem().equals(Items.OBSIDIAN)) {
                            mc.player.getInventory().selectedSlot = obi;
                            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(obi));
                        }
                        RotationsManager.lookAt(placeResult.getPos());
                        if (Nuker.placeRotatePlace) {
                            if (placeResult.getPos().equals(lookPos)
                                    && preRotated) {
                                PlaceUtils.place(placeResult, Nuker.packetPlace);
                                placeBlockTimeout.put(new PosAndState(b, 0), new Timer().reset());
                                preRotated = false;
                            } else if (!placeResult.getPos().equals(lookPos)
                                    && preRotated) {
                                lookPos = placeResult.getPos();
                                return true;
                            } else if (placeResult.getPos().equals(lookPos)
                                    && !preRotated) {
                                preRotated = true;
                                return true;
                            } else if (!placeResult.getPos().equals(lookPos)
                                    && !preRotated) {
                                lookPos = placeResult.getPos();
                                preRotated = true;
                                return true;
                            }
                        } else {
                            PlaceUtils.place(placeResult, Nuker.packetPlace);
                            placeBlockTimeout.put(new PosAndState(b, 0), new Timer().reset());
                            return true;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        return false;
    }

    private static boolean sourceRemoverPlacements() {
        if (mc.player == null
                || mc.world == null
                || mc.getNetworkHandler() == null) {
            return true;
        }
        List<BlockPos> blockList = new ArrayList<>(spherePosList);

        blockList.removeIf(b -> {
            BlockState state = mc.world.getBlockState(b);
            return !state.getBlock().equals(Blocks.WATER)
                    && !state.getFluidState().getFluid().equals(Fluids.FLOWING_WATER)
                    && !state.getBlock().equals(Blocks.LAVA)
                    && !state.getFluidState().getFluid().equals(Fluids.FLOWING_LAVA
            );
        });

        if (!blockList.isEmpty()) {
            List<BlockPos> playerBlocks = PlaceUtils.getBlocksPlayerOccupied();

            // Sort the blocks
            blockList = BlockUtils.sortBlocks(blockList);
            for (BlockPos b : blockList) {
                boolean inTimeout = false;
                for (PosAndState ps : placeBlockTimeout.keySet()) {
                    if (ps.pos.equals(b)) {
                        inTimeout = true;
                        break;
                    }
                }
                if (inTimeout) {
                    continue;
                }
                BlockHitResult placeResult = canPlace(b);
                if (placeResult != null
                        && !playerBlocks.contains(placeResult.getBlockPos())) {
                    int suitableBlock = PlaceUtils.findSuitableBlock();
                    if (suitableBlock != -1) {
                        if (!PlaceUtils.isSuitableBlock(mc.player.getInventory().getMainHandStack().getItem())) {
                            mc.player.getInventory().selectedSlot = suitableBlock;
                            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(suitableBlock));
                        }
                        RotationsManager.lookAt(placeResult.getPos());
                        if (Nuker.placeRotatePlace) {
                            if (placeResult.getPos().equals(lookPos)
                                    && preRotated) {
                                PlaceUtils.place(placeResult, Nuker.packetPlace);
                                placeBlockTimeout.put(new PosAndState(b, 0), new Timer().reset());
                                preRotated = false;
                            } else if (!placeResult.getPos().equals(lookPos)
                                    && preRotated) {
                                lookPos = placeResult.getPos();
                                return true;
                            } else if (placeResult.getPos().equals(lookPos)
                                    && !preRotated) {
                                preRotated = true;
                                return true;
                            } else if (!placeResult.getPos().equals(lookPos)
                                    && !preRotated) {
                                lookPos = placeResult.getPos();
                                preRotated = true;
                                return true;
                            }
                        } else {
                            PlaceUtils.place(placeResult, Nuker.packetPlace);
                            placeBlockTimeout.put(new PosAndState(b, 0), new Timer().reset());
                            return true;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        return false;
    }
    private static void updateBlockLists() {
        // Place block timeout check
        placeBlockTimeout.keySet().removeIf(
                next -> placeBlockTimeout.get(next).getPassedTimeMs() > Nuker.placeBlockTimeoutDelay + next.ttm);
    }

}
