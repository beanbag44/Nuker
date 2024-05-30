package me.beanbag.datatypes;

import lombok.AllArgsConstructor;
import me.beanbag.utils.Timer;
import net.minecraft.util.math.BlockPos;

@AllArgsConstructor
public class SoundQueueBlock {
    public double timeToMine;
    public BlockPos pos;
    public Timer timer;
}
