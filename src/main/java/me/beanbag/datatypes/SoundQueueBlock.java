package me.beanbag.datatypes;

import lombok.AllArgsConstructor;
import me.beanbag.utils.Timer;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

@AllArgsConstructor
public class SoundQueueBlock {
    public double awaitReceiveTime;
    public BlockPos pos;
    public BlockState state;
    public Timer timer;
}
