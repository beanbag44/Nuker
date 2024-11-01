package me.beanbag.nuker.module.modules.canaltools;

import net.minecraft.block.BlockState;

import java.util.List;

public interface IProtectionLocator {
    boolean isProtection(int x, int y, int z);
    boolean isCorrectInPosition(int x, int y, int z);
    List<BlockState> statesForPosition(int x, int y, int z);
}
