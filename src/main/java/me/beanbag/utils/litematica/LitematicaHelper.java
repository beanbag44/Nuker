package me.beanbag.utils.litematica;

import me.beanbag.mixin.litematica.ISchematicVerifierAccessor;
import fi.dy.masa.litematica.Litematica;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.malilib.interfaces.ICompletionListener;
import net.minecraft.util.math.BlockPos;

import java.util.*;

import static me.beanbag.Nuker.mc;

public class LitematicaHelper {
    public static LitematicaHelper INSTANCE = new LitematicaHelper();


    public static boolean isLitematicaLoaded() {
        try {
            Class.forName(Litematica.class.getName());
            return true;
        } catch (ClassNotFoundException | NoClassDefFoundError ex) {
            return false;
        }
    }

    public void verifySchematic(){
        for(SchematicPlacement placement : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()){
            SchematicVerifier verifier = placement.getSchematicVerifier();
            verifier.startVerification(mc.world, SchematicWorldHandler.getSchematicWorld(),placement, new VerifierCompletionListener());
        }
    }

    public Set<BlockPos> getMismatches(){
        Set<BlockPos> pos = new HashSet<>();
        for(SchematicPlacement placement : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()){
            if(!placement.isEnabled()) continue;
            if(!placement.hasVerifier()) verifySchematic();
            pos.addAll(getWrongBlocks(placement.getSchematicVerifier()));
        }
        return pos;
    }
    private List<BlockPos> getWrongBlocks(SchematicVerifier verifier){
        return new ArrayList<>(((ISchematicVerifierAccessor)verifier).getWrongBlocksPositions().values());
    }
}

class VerifierCompletionListener implements ICompletionListener{

    @Override
    public void onTaskCompleted() {
        System.out.println("Schematic Verification Completed");
    }

    @Override
    public void onTaskAborted() {
        ICompletionListener.super.onTaskAborted();
        System.out.println("Schematic Verification Failed");
    }
}
