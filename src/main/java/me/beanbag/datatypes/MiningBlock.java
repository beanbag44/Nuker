package me.beanbag.datatypes;

import lombok.AllArgsConstructor;
import me.beanbag.utils.Timer;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

@AllArgsConstructor
public class MiningBlock {
    public BlockPos pos;
    public Block block;
    public double ttm;
    public Timer timer;
    public boolean serverMine;
    public int tool;
}
