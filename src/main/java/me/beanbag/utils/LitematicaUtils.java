package me.beanbag.utils;

import me.beanbag.Nuker;
import me.beanbag.utils.litematica.LitematicaHelper;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LitematicaUtils {
    public static Set<BlockPos> schematicMismatches = Collections.synchronizedSet(new HashSet<>());
    public static void updateLitematicaBlocks() {
        // Litematica
        if (Nuker.litematica) {
            if (!LitematicaHelper.isLitematicaLoaded()) {
                Nuker.litematica = false;
            } else {
                schematicMismatches = LitematicaHelper.INSTANCE.getMismatches();
            }
        }
    }
}
