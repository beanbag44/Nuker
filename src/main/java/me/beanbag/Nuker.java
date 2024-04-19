package me.beanbag;

import baritone.api.BaritoneAPI;
import baritone.api.selection.ISelection;
import lombok.AllArgsConstructor;
import me.beanbag.eventhandlers.ChatEventHandler;
import me.beanbag.events.Render3DCallback;
import me.beanbag.utils.Timer;
import me.beanbag.events.PacketReceiveCallback;
import me.beanbag.utils.InventoryUtils;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static me.beanbag.utils.BlockUtils.getBlockBreakingTimeMS;

public class Nuker implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("nuker");
	public static MinecraftClient mc = MinecraftClient.getInstance();

	/**
	 * Nuker vars
	 */

	private int packetCounter = 0;
	private List<BlockPos> spherePosList = Collections.synchronizedList(new ArrayList<>());
	private final List<SQB> sQueue = Collections.synchronizedList(new ArrayList<>());
	private final List<SQB> sQueueRemove = Collections.synchronizedList(new ArrayList<>());
	private final List<MBlock> mBlocks = new ArrayList<>();
	private final ConcurrentHashMap<MBlock, Timer> ghostBlockCheckSet = new ConcurrentHashMap<>();
	private final Set<MBlock> ghostBlockCheckSetRemove = new HashSet<>();
	private final Set<Runnable> renderRunnables = Collections.synchronizedSet(new HashSet<>());
	private final Map<PosAndState, Timer> blockTimeout = new HashMap<>();
	private final Set<ISelection> baritoneSelections = new HashSet<>();

	/**
	 * Nuker Settings
	 */

	public static boolean enabled = false;
	public static MineSort mineSort = MineSort.CLOSEST;
	public static FlattenMode flattenMode = FlattenMode.STANDARD;
	public static boolean avoidLiquids = true;
	public static int packetLimit = 10;
	public static boolean clientBreak = true;
	public static int radius = 5;
	public static boolean baritoneSelection = false;
	public static double clientBreakGhostBlockTimeout = 1000;
	public static double blockTimeoutDelay = 300;
	public static int instaMineThreshold = 67;
	public static boolean onGround = true;

	@Override
	public void onInitialize() {

		ChatEventHandler chatCommands = new ChatEventHandler();

		/**
		 * On player dig packet send
		 */

		PacketReceiveCallback.EVENT.register(packet -> {

			if (packet instanceof BlockUpdateS2CPacket p) {

				// --------------- SOUNDS ---------------------------------------------------------------------------------

				sQueue.forEach(b -> {
					if (b.pos.equals(p.getPos())
							&& p.getState().isAir()) {
						BlockState state = mc.world.getBlockState(b.pos);
						renderRunnables.add(() -> {
							mc.world.setBlockState(b.pos, state);
							mc.world.breakBlock(b.pos, false);
						});
						sQueueRemove.add(b);
					}
				});

				// --------------- GHOST BLOCK CHECKS ---------------------------------------------------------------------------------

				ghostBlockCheckSet.forEach((b, t) -> {
					if (b.pos.equals(p.getPos())
							&& p.getState().isAir()) {
						ghostBlockCheckSetRemove.add(b);
					}
				});

				// --------------------------------------------------------------------------------------------------------

			} else if (packet instanceof ChunkDeltaUpdateS2CPacket p) {

				// --------------- SOUNDS ---------------------------------------------------------------------------------

				sQueue.forEach(b -> p.visitUpdates((pos, state) -> {
					if (b.pos.equals(pos)
							&& state.isAir()) {
						BlockState oldState = mc.world.getBlockState(pos);
						renderRunnables.add(() -> {
							mc.world.setBlockState(b.pos, oldState);
							mc.world.breakBlock(b.pos, false);
						});
						sQueueRemove.add(b);
					}
				}));

				// --------------- GHOST BLOCK CHECKS ---------------------------------------------------------------------------------

				ghostBlockCheckSet.forEach((b, t) -> {
					p.visitUpdates((pos, state) -> {
						if (b.pos.equals(pos)
								&& state.isAir()) {
							ghostBlockCheckSetRemove.add(b);
						}
					});
				});
			}

			// --------------------------------------------------------------------------------------------------------

			ghostBlockCheckSetRemove.forEach(ghostBlockCheckSet::remove);
			ghostBlockCheckSetRemove.clear();

			sQueue.removeAll(sQueueRemove);
			sQueueRemove.clear();

			// --------------------------------------------------------------------------------------------------------

			return ActionResult.PASS;
		});


		/**
		 * On tick
		 */

		ClientTickEvents.START_CLIENT_TICK.register((mc) -> {

			// --------------- GHOST BLOCK CHECK TIMEOUT ---------------------------------------------------------------------------------

			ghostBlockCheckSet.forEach((b, t) -> {
				if (ghostBlockCheckSet.get(b).getPassedTimeMs() > clientBreakGhostBlockTimeout) {
					mc.world.setBlockState(b.pos, b.block.getDefaultState());
					mc.getNetworkHandler().sendPacket(
							new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND
									, new BlockHitResult(b.pos.toCenterPos()
									, Direction.UP
									, b.pos
									, true)
									, 0)
					);
					ghostBlockCheckSetRemove.add(b);
				}
			});
			ghostBlockCheckSetRemove.forEach(ghostBlockCheckSet::remove);
			ghostBlockCheckSetRemove.clear();

			// --------------- BLOCK TIMEOUT CHECK ---------------------------------------------------------------------------------

			blockTimeout.keySet().removeIf(
					next -> blockTimeout.get(next).getPassedTimeMs() > blockTimeoutDelay + next.ttm);

			// --------------- SOUNDS TIMEOUT CHECK ---------------------------------------------------------------------------------

			sQueue.forEach(b -> {
				if (b.timer.getPassedTimeMs() > b.ttm) {
					sQueueRemove.add(b);
				}
			});
			sQueue.removeAll(sQueueRemove);
			sQueueRemove.clear();

			// --------------- MINE CHECKING -------------------------------------------------------------------------------------------

			if (mc.world == null
					|| mc.player == null
					|| !enabled
					|| onGround
					&& !mc.player.isOnGround()) {
				return;
			}

			// reset the block interaction limit counter. This is done here because the player could have to switch back
			// to a currently mining blocks best tool and if a module like autoeat is on you could get kicked for packet spam
			packetCounter = 0;

			Iterator<MBlock> iterator = mBlocks.iterator();
			while(iterator.hasNext()) {
				MBlock b = iterator.next();
				if (!mc.world.getBlockState(b.pos).getBlock().equals(b.block)) {
					iterator.remove();
				} else {
					mc.player.getInventory().selectedSlot = b.tool;
					stopDestroy(b.pos);
				}
			}

			// ---------------------------------------------------------------------------------------------------------------------------------

			// get a list of blocks around the player
			spherePosList = getPlayerBlockSphere(mc.player.getEyePos(), radius + 1);

			// remove impossible to mine blocks like bedrock, water, etc...
			filterBlocks(spherePosList);

			// sort the blocks
			spherePosList = sortBlocks(spherePosList);

			// iterate through the list
			for (BlockPos b : spherePosList) {

				// if possible, mine / start mining the block
				if (canMine(b)) {

					// Top down gravity block checks to shift the block position to the top of the column
					if (mc.world.getBlockState(b).getBlock() instanceof FallingBlock) {
						while (mc.world.getBlockState(b.add(0, 1, 0)).getBlock() instanceof FallingBlock
								&& isWithinRadius(mc.player.getEyePos(), b.add(0, 1, 0), radius)
								&& canMine(b.add(0, 1, 0))) {
							b = b.add(0, 1, 0);
						}
					}

					// swaps to the right tool
					int bestTool = InventoryUtils.getBestToolSlot(b);
					if (mc.player.getInventory().selectedSlot != bestTool) {
						mc.player.getInventory().selectedSlot = bestTool;
						packetCounter++;
					}

					mineBlock(b);
				}
			}
		});


		/**
		 * On frame render
		 */

		Render3DCallback.EVENT.register(() -> {
			if (mc.world == null) {
				renderRunnables.clear();
				return ActionResult.PASS;
			}

			renderRunnables.forEach(Runnable::run);
			renderRunnables.clear();
			return ActionResult.PASS;
		});

		LOGGER.info("Nuker!");
	}
	private void mineBlock(BlockPos blockPos) {

		double breakingTime = getBlockBreakingTimeMS(mc.player.getInventory().getMainHandStack(), blockPos, mc.player, mc.world);
		blockTimeout.put(new PosAndState(blockPos, breakingTime), new Timer().reset());

		// ---------- DOUBLE BLOCK ----------------------------------------------

		if (mBlocks.size() == 1) {
			mBlocks.get(0).serverMine = true;
		}

		if (breakingTime > instaMineThreshold) {
			mBlocks.add(new MBlock(blockPos
					, mc.world.getBlockState(blockPos).getBlock()
					, breakingTime
					, new Timer().reset()
					, false
					, InventoryUtils.getBestToolSlot(blockPos))
			);
		}

		// ---------- DIFFERENT MINE PACKETS FOR HARDNESS'S ---------------------

		if (breakingTime <= instaMineThreshold) {
			if (breakingTime > 50) {
				stopDestroy(blockPos);
				startDestroy(blockPos);
				stopDestroy(blockPos);
			} else {
				mc.interactionManager.attackBlock(blockPos, Direction.UP);
			}
		} else if (breakingTime > instaMineThreshold) {
			startDestroy(blockPos);
			abortDestroy(blockPos);
			stopDestroy(blockPos);
		}

		// ---------- PACKET LIMITER ---------------------

		if (packetLimit != -1) {
			if (breakingTime > 50) {
				packetCounter += 3;
			} else {
				packetCounter++;
			}
		}

		// ---------- SOUNDS ---------------------

		if (breakingTime > 50) {
			sQueue.add(new SQB(
					breakingTime * 3 + 500
					, blockPos
					, new Timer().reset())
			);
		}
	}
	private boolean canMine(BlockPos pos) {

		ItemStack stack= mc.player.getInventory().getStack(InventoryUtils.getBestToolSlot(pos));

		// ---------------- FLATTEN MODES ---------------------------------------------------------------------

		switch (flattenMode) {
			case STANDARD -> {
				if (pos.getY() < mc.player.getBlockY()) {
					return false;
				}
			}
			case SMART -> {
				if (pos.getY() < mc.player.getBlockY()) {
					Direction playerDirection = mc.player.getHorizontalFacing();
					Vec3d blockVector = new Vec3d(pos.subtract(mc.player.getBlockPos()).getX(),
							pos.subtract(mc.player.getBlockPos()).getY(),
							pos.subtract(mc.player.getBlockPos()).getZ()
					);

					if ((blockVector.getX() < 1 && playerDirection == Direction.WEST)
							|| (blockVector.getX() > -1 && playerDirection == Direction.EAST)
							|| (blockVector.getZ() < 1 && playerDirection == Direction.NORTH)
							|| (blockVector.getZ() > -1 && playerDirection == Direction.SOUTH)) {
						return false;
					}
					// without this, you sometimes get grim issues when walking backwards down a staircase with smart flatten on
					if (mc.player.supportingBlockPos.isPresent()
							&& mc.player.supportingBlockPos.get().equals(pos)) {
						return false;
					}
				}
			}
			case REVERSE_SMART -> {
				if (pos.getY() < mc.player.getBlockY()) {
					Direction playerDirection = mc.player.getHorizontalFacing();
					Vec3d blockVector = new Vec3d(pos.subtract(mc.player.getBlockPos()).getX(),
							pos.subtract(mc.player.getBlockPos()).getY(),
							pos.subtract(mc.player.getBlockPos()).getZ()
					);

					if ((blockVector.getX() > -1 && playerDirection == Direction.WEST)
							|| (blockVector.getX() < 1 && playerDirection == Direction.EAST)
							|| (blockVector.getZ() > -1 && playerDirection == Direction.NORTH)
							|| (blockVector.getZ() < 1 && playerDirection == Direction.SOUTH)) {
						return false;
					}
					// without this, you sometimes get grim issues when walking backwards down a staircase with smart flatten on
					if (mc.player.supportingBlockPos.isPresent()
							&& mc.player.supportingBlockPos.get().equals(pos)) {
						return false;
					}
				}
			}
		}

		// ---------------- DOUBLE BLOCK ---------------------------------------------------------------------

		Iterator<MBlock> iterator = mBlocks.iterator();
		while (iterator.hasNext()) {
			MBlock b = iterator.next();
			if (b.serverMine) {
				if (b.timer.getPassedTimeMs() >= b.ttm
						|| mc.world.getBlockState(b.pos).getBlock() != b.block) {
					if (clientBreak
							&& isWithinRadius(mc.player.getEyePos(), b.pos, radius)) {
						mc.world.breakBlock(b.pos, false);
						ghostBlockCheckSet.put(b, new Timer().reset());
					}
					iterator.remove();
				}
			} else {
				if (b.timer.getPassedTimeMs() >= b.ttm * 0.7
						|| mc.world.getBlockState(b.pos).getBlock() != b.block) {
					if (clientBreak
							&& isWithinRadius(mc.player.getEyePos(), b.pos, radius)) {
						mc.world.breakBlock(b.pos, false);
						ghostBlockCheckSet.put(b, new Timer().reset());
					}
					iterator.remove();
				}
			}
		}

		if (mBlocks.size() >= 2) {
			return false;
		} else if (!mBlocks.isEmpty()) {
			MBlock b = mBlocks.get(0);

			if (b.pos.equals(pos)) {
				return false;
			} else if (stack.getItem() != mc.player.getInventory().getStack(b.tool).getItem()) {
				return false;
			}
		}

		// ----------------------------------------------------------------------------------------------------

		double timeToMine = getBlockBreakingTimeMS(
				stack
				, pos
				, mc.player
				, mc.world
		);

		// ---------------- BLOCK MINE TIME LIMIT ---------------------------------------------------------------------

		if (timeToMine > 10000) {
			return false;
		}

		// ---------------- PACKET LIMIT ---------------------------------------------------------------------

		int packets = 1;
		if (timeToMine > 50) {
			packets = 3;
		}
		if (packetCounter >= packetLimit
				|| packetCounter + packets >= packetLimit) {
			return false;
		}

		// ---------------- BLOCK TIMEOUT ---------------------------------------------------------------------

		for (PosAndState pas : blockTimeout.keySet()) {
			if (pos.equals(pas.pos)) {
				return false;
			}
		}

		// ---------------- AVOID LIQUIDS ---------------------------------------------------------------------

		if (avoidLiquids) {
			for (final Direction direction : Direction.values()) {
				BlockPos newPos = pos.offset(direction);
				BlockState newState = mc.world.getBlockState(newPos);
				if (newState.getFluidState().isIn(FluidTags.WATER)
						|| newState.getFluidState().isIn(FluidTags.LAVA)
						|| newState.isOf(Blocks.WATER)
						|| newState.isOf(Blocks.LAVA)) {
					return false;
				}
			}

			BlockPos newPos = pos;
			while (mc.world.getBlockState(newPos.offset(Direction.UP)).getBlock() instanceof FallingBlock) {
				newPos = newPos.up();
				for (Direction directionFallingColumn : Direction.values()) {
					BlockPos newPosTemp = newPos.offset(directionFallingColumn);
					BlockState bState = mc.world.getBlockState(newPosTemp);
					if (bState.getFluidState().isIn(FluidTags.WATER)
							|| bState.getFluidState().isIn(FluidTags.LAVA)
							|| bState.isOf(Blocks.WATER)
							|| bState.isOf(Blocks.LAVA)) {
						return false;
					}
				}
			}
		}
		// ---------------- BARITONE SELECTIONS ---------------------------------------------------------------------

		if (baritoneSelection) {
			if (packetCounter == 0) {
				baritoneSelections.clear();
				BaritoneAPI.getProvider().getAllBaritones().forEach(b ->
						baritoneSelections.addAll(Arrays.stream(b.getSelectionManager().getSelections()).toList()));
			}

			boolean withinBaritoneSelection = false;

			if (!baritoneSelections.isEmpty()) {
				for (ISelection selection : baritoneSelections) {
					BlockPos pos1 = new BlockPos(selection.pos1().x, selection.pos1().y, selection.pos1().z);
					BlockPos pos2 = new BlockPos(selection.pos2().x, selection.pos2().y, selection.pos2().z);
					int minX = Math.min(pos1.getX(), pos2.getX());
					int minY = Math.min(pos1.getY(), pos2.getY());
					int minZ = Math.min(pos1.getZ(), pos2.getZ());
					int maxX = Math.max(pos1.getX(), pos2.getX());
					int maxY = Math.max(pos1.getY(), pos2.getY());
					int maxZ = Math.max(pos1.getZ(), pos2.getZ());
					if (pos.getX() >= minX && pos.getX() <= maxX
							&& pos.getY() >= minY && pos.getY() <= maxY
							&& pos.getZ() >= minZ && pos.getZ() <= maxZ) {
						withinBaritoneSelection = true;
						break;
					}
				}
			}
			if (!withinBaritoneSelection) {
				return false;
			}
		}
		return true;
	}
	private void startDestroy(BlockPos blockPos) {
		mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
	}
	private void abortDestroy(BlockPos blockPos) {
		mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, blockPos, Direction.UP));
	}
	private void stopDestroy(BlockPos blockPos) {
		mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
	}
	private List<BlockPos> getPlayerBlockSphere(Vec3d playerPos, int r) {
		List<BlockPos> posList = getPlayerBlockCube(playerPos, r);
		posList.removeIf(next -> !isWithinRadius(playerPos, next, r));
		return new ArrayList<>(posList);
	}
	private List<BlockPos> getPlayerBlockCube(Vec3d playerPos, int r) {
		List<BlockPos> posList = new ArrayList<>();

		for (int x = -r; x <= r; x++) {
			for (int y = -r; y <= r; y++) {
				for (int z = -r; z <= r; z++) {
					posList.add(new BlockPos(
							(int) Math.floor(playerPos.x + x)
							, (int) Math.floor(playerPos.y + y)
							, (int) Math.floor(playerPos.z + z))
					);
				}
			}
		}
		return posList;
	}
	private Boolean isWithinRadius(Vec3d playerPos, BlockPos blockPos, int r) {
		return playerPos.distanceTo(blockPos.toCenterPos()) <= r;
	}
	private List<BlockPos> sortBlocks(List<BlockPos> list) {

		List<BlockPos> sortedList = new ArrayList<>(list.stream()
				.sorted(Comparator.comparingDouble(a -> mc.player.getEyePos().distanceTo(a.toCenterPos()))).toList());

		switch (mineSort) {
			case FARTHEST -> Collections.reverse(sortedList);
			case TOP_DOWN -> sortedList.sort(Comparator.comparingDouble((a) -> -a.getY()));
			case BOTTOM_UP -> sortedList.sort(Comparator.comparingDouble(BlockPos::getY));
			case RANDOM -> Collections.shuffle(sortedList);
		}

		return sortedList;
	}
	private void filterBlocks(List<BlockPos> positions) {
		Iterator<BlockPos> iterator = positions.iterator();
		while(iterator.hasNext()) {

			BlockPos next = iterator.next();

			BlockState state = mc.world.getBlockState(next);
			Block block = state.getBlock();

			if (state.isAir()
					|| block.getHardness() == 600
					|| block.getHardness() == -1
					|| block.equals(Blocks.BARRIER)
					|| state.getFluidState().getFluid().equals(Fluids.WATER)
					|| state.getFluidState().getFluid().equals(Fluids.FLOWING_WATER)
					|| state.getFluidState().getFluid().equals(Fluids.LAVA)
					|| state.getFluidState().getFluid().equals(Fluids.FLOWING_LAVA)) {
				iterator.remove();
			}
		}
	}
	private boolean rayCast(BlockPos blockPos) {
		BlockHitResult rayCastResult = mc.world.raycast(new RaycastContext(mc.player.getEyePos()
				, blockPos.toCenterPos()
				, RaycastContext.ShapeType.OUTLINE
				, RaycastContext.FluidHandling.NONE
				, mc.player
		));
		return (rayCastResult.getBlockPos().equals(blockPos));
	}
	@AllArgsConstructor
	private class MBlock {
		private BlockPos pos;
		private Block block;
		private double ttm;
		private Timer timer;
		private boolean serverMine;
		private int tool;
	}
	public enum MineSort {
		CLOSEST,
		FARTHEST,
		TOP_DOWN,
		BOTTOM_UP,
		RANDOM,
	}
	@AllArgsConstructor
	private class SQB {
		public double ttm;
		public BlockPos pos;
		public Timer timer;
	}
	public enum FlattenMode {
		NONE("None"),
		STANDARD("Standard"),
		SMART("Smart"),
		REVERSE_SMART("Reverse Smart");
		private final String name;
		FlattenMode(String name) {
			this.name = name;
		}
	}
	@AllArgsConstructor
	private class PosAndState {
		public BlockPos pos;
		public double ttm;
	}
}