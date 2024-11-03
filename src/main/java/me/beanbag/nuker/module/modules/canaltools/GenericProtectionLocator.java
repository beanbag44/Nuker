package me.beanbag.nuker.module.modules.canaltools;

import me.beanbag.nuker.ModConfigs;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class GenericProtectionLocator implements IProtectionLocator {
    final static int underwaterMaxY = CanalSpecs.walkwayY - 1;
    final static int underwaterMinY = CanalSpecs.floorY + 1;
    final static int protectionMinX = CanalSpecs.westWallX + 1;
    final static int protectionMaxX = CanalSpecs.eastWallX - 1;

    final static int bridgeMinY = CanalSpecs.walkwayY;
    final static int bridgeMaxY = CanalSpecs.walkwayY + 3;
    @Override
    public boolean isProtection(int x, int y, int z) {
        return x >= protectionMinX && x <= protectionMaxX
                && (y >= underwaterMinY && y <= underwaterMaxY || y >= bridgeMinY && y <= bridgeMaxY);
    }

    @Override
    public boolean isCorrectInPosition(int x, int y, int z) {

        return statesForPosition(x, y, z).stream().map(AbstractBlock.AbstractBlockState::getBlock).toList().contains(
                ModConfigs.INSTANCE.getMc().world.getBlockState(new BlockPos(x, y, z)).getBlock());
    }

    @Override
    public List<BlockState> statesForPosition(int x, int y, int z) {
        if (!isProtection(x, y, z)) return List.of();
        if (y == underwaterMinY) {
            return List.of(Blocks.OBSIDIAN.getDefaultState());
        } else if (y == underwaterMaxY) {
            return  List.of(Blocks.OBSIDIAN.getDefaultState());
        } else if (y == bridgeMinY) {
            if (x > CanalSpecs.westWallX + 2 && x < CanalSpecs.eastWallX - 2) {
                return List.of();
            } else {
                return List.of(Blocks.AIR.getDefaultState());
            }
        } else if (y == bridgeMinY + 1) {
            if (x == protectionMinX + 1 || x == protectionMinX + 2 || x == protectionMaxX - 1 || x == protectionMaxX - 2) {
                return List.of(Blocks.OBSIDIAN.getDefaultState());
            } else {
                return List.of(Blocks.AIR.getDefaultState());
            }
        } else if (y == bridgeMinY + 2) {
            if (x <= protectionMaxX - 1 && x >= protectionMinX + 1) {
                if (x == protectionMaxX - 2 || x == protectionMinX + 2) {
                    return List.of(Blocks.OBSIDIAN.getDefaultState());
                } else {
                    return List.of(Blocks.OBSIDIAN.getDefaultState(), Blocks.CRYING_OBSIDIAN.getDefaultState(), Blocks.COBBLESTONE.getDefaultState());
                }
            } else {
                return List.of(Blocks.AIR.getDefaultState());
            }
        } else if (y == bridgeMinY + 3) {
            if (x <= protectionMaxX - 2 && x >= protectionMinX + 2) {
                if (x == protectionMaxX - 3 || x == protectionMinX + 3) {
                    return List.of(Blocks.OBSIDIAN.getDefaultState());
                } else {
                    return List.of(Blocks.OBSIDIAN.getDefaultState(), Blocks.CRYING_OBSIDIAN.getDefaultState());
                }
            } else {
                return List.of(Blocks.AIR.getDefaultState());
            }
        }
        return null;
    }
}
