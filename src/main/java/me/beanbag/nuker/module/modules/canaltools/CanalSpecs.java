package me.beanbag.nuker.module.modules.canaltools;

import me.beanbag.nuker.ModConfigs;
import me.beanbag.nuker.utils.BlockUtils;
import me.beanbag.nuker.utils.InGameKt;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.math.BlockPos;


public class CanalSpecs {
    static IProtectionLocator protectionLocator = new GenericProtectionLocator();

    public static final int canalMinY = 59;
    public static final int canalMaxY = 319;
    public static final int canalMinX = -16;
    public static final int canalMaxX = 15;

    public static final int floorY = 59;
    public static final int floorMinX = -13;
    public static final int floorMaxX = 12;

    public static final int walkwayY = 62;
    public static final int westWalkwayMinX = -16;
    public static final int westWalkWayMaxX = -14;
    public static final int eastWalkwayMinX = 13;
    public static final int eastWalkwayMaxX = 15;

    public static final int wallMinY = 60;
    public static final int wallMaxY = 61;
    public static final int westWallX = -14;
    public static final int eastWallX = 13;

    public static final int ceilingY = 356;
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isInBounds(int x, int y) {
        return contains(x, y, canalMinX, canalMaxX, canalMinY, canalMaxY);
    }

    public static boolean isFloor(int x, int y) {
        return contains(x, y, floorMinX, floorMaxX, floorY, floorY);
    }

    public static boolean isWalkway(int x, int y) {
        return contains(x, y, westWalkwayMinX, westWalkWayMaxX, walkwayY, walkwayY)
                || contains(x, y, eastWalkwayMinX, eastWalkwayMaxX, walkwayY, walkwayY);
    }

    public static boolean isWall(int x, int y) {
        return contains(x, y, westWallX, westWallX, wallMinY, wallMaxY)
                || contains(x, y, eastWallX, eastWallX, wallMinY, wallMaxY);
    }

    public static boolean isBasin(int x, int y) {
        return isFloor(x, y) || isWalkway(x, y) || isWall(x, y);
    }

    public static boolean isProtection(int x, int y, int z) {
        return protectionLocator.isProtection(x, y, z);
    }

    public static boolean isCorrectInPosition(int x, int y, int z) {
        return isCorrectInPosition(new BlockPos(x, y, z));
    }

    public static boolean isCorrectInPosition(BlockPos pos) {
        assert MinecraftClient.getInstance().world != null;

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        // Everything outside doesn't matter
        if (!isInBounds(x, y))
            return true;

        // The 6 blocks below the sidewalk
        if ((x < westWallX || x > eastWallX) && (y >= floorY && y <= floorY + 2))
            return true;

        // The one block below the wall
        if ((x == westWallX || x == eastWallX) && y == floorY)
            return true;

        BlockState blockState = ModConfigs.INSTANCE.getMc().world.getBlockState(pos);

        if (isBasin(x, y) || y == ceilingY) {
            return stateForPosition(pos).getBlock() == blockState.getBlock();
        } else if (isProtection(x, y, z)) {
            return protectionLocator.isCorrectInPosition(x, y, z);
        } else
            return blockState.getBlock() == Blocks.AIR;
    }

    public static BlockState stateForPosition(BlockPos pos) {
        assert MinecraftClient.getInstance().world != null;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        // Everything outside doesn't matter
        if (!isInBounds(x, y) || (!isBasin(x, y) && y != ceilingY && !isProtection(x, y, z)))
            return MinecraftClient.getInstance().world.getBlockState(pos);

        if (isProtection(x,y,z)) {
            if (protectionLocator.statesForPosition(x, y, z).contains(ModConfigs.INSTANCE.getMc().world.getBlockState(pos))){
                return ModConfigs.INSTANCE.getMc().world.getBlockState(pos);
            } else if (y > walkwayY){
                return Blocks.AIR.getDefaultState();
            } else {
                return Blocks.WATER.getDefaultState();
            }
        }

        if (y == ceilingY) {
            final float FREEZING_TEMPERATURE = 0.15F;
            boolean canFreeze = MinecraftClient.getInstance().world.getBiome(pos.withY(walkwayY + 1)).value().getTemperature() < FREEZING_TEMPERATURE;
            if (canFreeze) {
                if (x == canalMinX || x == canalMaxX ) {
                    return Blocks.CRYING_OBSIDIAN.getDefaultState();
                } else if (x <= westWallX || x >= eastWallX || (x+z) % 2 == 0) {
                    return Blocks.OBSIDIAN.getDefaultState();
                } else {
                    return Blocks.GLASS.getDefaultState();
                }
            } else {
                return MinecraftClient.getInstance().world.getBlockState(pos);
            }
        }
        boolean isRiver = MinecraftClient.getInstance().world.getBiome(pos).isIn(BiomeTags.IS_RIVER);
        boolean isFloor = isFloor(x, y);
        boolean isLightSource = x == westWalkwayMinX || x == eastWalkwayMaxX;
        return isRiver && isFloor || isLightSource ? Blocks.CRYING_OBSIDIAN.getDefaultState() : Blocks.OBSIDIAN.getDefaultState();
    }

    public static boolean canIgnoreForBreak(BlockPos pos) {
        if (ModConfigs.INSTANCE.getMc().world == null) return false;
        Block block = ModConfigs.INSTANCE.getMc().world.getBlockState(pos).getBlock();
        return block instanceof LeavesBlock
                || block instanceof TallPlantBlock
                || block instanceof TallFlowerBlock
                || block instanceof FlowerBlock
                || block instanceof VineBlock
                || block instanceof NetherPortalBlock
                || block instanceof TorchBlock
                || block instanceof SnowBlock;
    }

    public static boolean isProtected(BlockPos pos) {
        return Boolean.TRUE.equals(InGameKt.runInGame(
                (inGame -> BlockUtils.INSTANCE.isSignOrBanner(pos)
                        || BlockUtils.INSTANCE.isSupportingSignOrBanner(inGame, pos))));
    }



    private static boolean contains(int x, int y, int minX, int maxX, int minY, int maxY) {
        return x >= minX && x <= maxX && y>= minY && y <= maxY;
    }




}