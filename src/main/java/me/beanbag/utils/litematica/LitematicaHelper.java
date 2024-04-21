package me.beanbag.utils.litematica;

import com.google.common.collect.ArrayListMultimap;
import me.beanbag.mixin.litematica.ISchematicVerifierAccessor;
import fi.dy.masa.litematica.Litematica;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.malilib.interfaces.ICompletionListener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.Pair;

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

    public Set<BlockPos> getMissingBlocks(){
        Set<BlockPos> pos = new HashSet<>();
        for(SchematicPlacement placement : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()){
            if(!placement.isEnabled()) continue;
            if(!placement.hasVerifier()) verifySchematic();
            pos.addAll(getMissingBlocks(placement.getSchematicVerifier()));
        }
        return pos;
    }

    public ArrayListMultimap<Pair<BlockState, BlockState>, BlockPos> getMissingBlocksAndStates(){
        ArrayListMultimap<Pair<BlockState, BlockState>, BlockPos> data = ArrayListMultimap.create();
        for(SchematicPlacement placement : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()){
            if(!placement.isEnabled()) continue;
            if(!placement.hasVerifier()) verifySchematic();
            data.putAll(getMissingBlocksAndStates(placement.getSchematicVerifier()));
        }
        return data;
    }


    public BlockPos getClosestMismatch(boolean ignoreLiquids){
        Vec3d pos = mc.player.getPos();
        Optional<BlockPos> closest;
        if(ignoreLiquids){
            closest = getMismatches().stream().filter(this::checkForLiquids).min(Comparator.comparing((bp) -> bp.getSquaredDistance(pos)));
        }else{
            closest = getMismatches().stream().min(Comparator.comparing((bp) -> bp.getSquaredDistance(pos)));
        }
        return closest.orElse(null);
    }
    public BlockPos getClosestMissionBlocksForItem(Block b){
        Vec3d pos = mc.player.getPos();
        Optional<Pair<Block, BlockPos>> closest = getMissingBlocksItem().stream().filter(p -> p.getLeft() == b).min(Comparator.comparing(p -> p.getRight().getSquaredDistance(pos)));
        return closest.map(Pair::getRight).orElse(null);
    }
    public BlockPos getClosestMissingBlock(boolean ignoreLiquids){
        Vec3d pos = mc.player.getPos();
        Optional<BlockPos> closest;
        if(ignoreLiquids){
            closest = getMissingBlocks().stream().filter(this::checkForLiquids).min(Comparator.comparing((bp) -> bp.getSquaredDistance(pos)));
        }else{
            closest = getMissingBlocks().stream().min(Comparator.comparing((bp) -> bp.getSquaredDistance(pos)));
        }

        return closest.orElse(null);
    }

    private List<BlockPos> getMismatches(SchematicVerifier verifier){
       return new ArrayList<>(((ISchematicVerifierAccessor)verifier).getMismatchedBlocksPositionsClosest());
    }
    private List<BlockPos> getWrongBlocks(SchematicVerifier verifier){
        return new ArrayList<>(((ISchematicVerifierAccessor)verifier).getWrongBlocksPositions().values());
    }
    private List<BlockPos> getMissingBlocks(SchematicVerifier verifier){
        return new ArrayList<>(((ISchematicVerifierAccessor)verifier).getMissingBlocksPositions().values());
    }
    private ArrayListMultimap<Pair<BlockState, BlockState>, BlockPos> getMissingBlocksAndStates(SchematicVerifier verifier){
        return (((ISchematicVerifierAccessor)verifier).getMissingBlocksPositions());
    }
    private List<Pair<Block,BlockPos>> getMissingBlocksItem(){
        ArrayList<Pair<Block,BlockPos>> positions = new ArrayList<>();
        getMissingBlocksAndStates().forEach((states,pos) -> {
            positions.add(Pair.of(states.getLeft().getBlock(),pos));
        });
        return positions;
    }

    private HashMap<Block,Integer> getRequiredBlocks(){
        HashMap<Block,Integer> itemMap = new HashMap<>();
        getMissingBlocksItem().forEach((p) ->{
            itemMap.computeIfPresent(p.getKey(),(k,v) -> v+1);
            itemMap.computeIfAbsent(p.getKey(),k -> 1);
        });
        return itemMap;
    }

    private boolean checkForLiquids(BlockPos pos){
        for(Direction dir : Direction.values()){
            FluidState liquidCheck = mc.world.getBlockState(pos.offset(dir)).getFluidState();
            if (liquidCheck.isOf(Fluids.WATER)) {
                return false;
            } else if (liquidCheck.isOf(Fluids.LAVA)) {
                return false;
            } else if (liquidCheck.isOf(Fluids.FLOWING_LAVA)) {
                return false;
            } else if (liquidCheck.isOf(Fluids.FLOWING_WATER)) {
                return false;
            }
        }
        return true;
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
