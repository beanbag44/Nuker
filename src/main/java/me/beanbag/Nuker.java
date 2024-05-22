package me.beanbag;

import baritone.api.BaritoneAPI;
import baritone.api.selection.ISelection;
import lombok.AllArgsConstructor;
import me.beanbag.eventhandlers.ChatEventHandler;
import me.beanbag.events.PacketReceiveCallback;
import me.beanbag.events.Render3DCallback;
import me.beanbag.utils.Timer;
import me.beanbag.utils.*;
import me.beanbag.utils.litematica.LitematicaHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static me.beanbag.utils.BlockUtils.getBlockBreakingTimeMS;
import static me.beanbag.utils.RotationsManager.canSeeBlockFace;

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
	private final List<MBlock> mBlocks = Collections.synchronizedList(new ArrayList<>());
	private final ConcurrentHashMap<MBlock, Timer> ghostBlockCheckSet = new ConcurrentHashMap<>();
	private final Set<MBlock> ghostBlockCheckSetRemove = Collections.synchronizedSet(new HashSet<>());
	private final Set<Runnable> renderRunnables = Collections.synchronizedSet(new HashSet<>());
	private final Map<PosAndState, Timer> blockTimeout = new ConcurrentHashMap<>();
	private final Map<PosAndState, Timer> placeBlockTimeout = new ConcurrentHashMap<>();
	private final Set<ISelection> baritoneSelections = Collections.synchronizedSet(new HashSet<>());
	private Set<BlockPos> schematicMismatches = Collections.synchronizedSet(new HashSet<>());

	/*
	 * Nuker Settings
	 */

	public static boolean litematica = false;
	public static boolean enabled = false;
	public static MineSort mineSort = MineSort.CLOSEST;
	public static FlattenMode flattenMode = FlattenMode.STANDARD;
	public static boolean avoidLiquids = true;
	public static int packetLimit = 10;
	public static boolean clientBreak = true;
	public static int radius = 5;
	public static boolean baritoneSelection = false;
	public static double clientBreakGhostBlockTimeout = 1000;
	public static int blockTimeoutDelay = 300;
	public static int instaMineThreshold = 67;
	public static boolean onGround = true;

	/*
	 * Liquid Settings
	 */

	public static boolean sourceRemover = false;
	public static int placeBlockTimeoutDelay = 2500;
	public static boolean expandBaritoneSelectionsForLiquids = true;

	@Override
	public void onInitialize() {

		/*
		  Initialize the chat event handler
		 */

		RotationsManager.initEventHandler();

		/*
		  Initialize the chat event handler
		 */

		ChatEventHandler.initChatEventHandler();

		/*
		  On packet receive
		 */

		PacketReceiveCallback.EVENT.register(packet -> {

			if (mc.world == null
					|| mc.interactionManager == null) {
				return ActionResult.PASS;
			}

			if (packet instanceof BlockUpdateS2CPacket p) {
				// Sounds
                for (SQB sqb : sQueue) {
                    if (sqb.pos.equals(p.getPos())
                            && (p.getState().isAir() || p.getState().getBlock().equals(Blocks.WATER))) {
                        renderRunnables.add(() -> mc.interactionManager.breakBlock(sqb.pos));
                        sQueueRemove.add(sqb);
                    }
                }

				// Ghost block checks
                for (MBlock b : ghostBlockCheckSet.keySet()) {
                    if (b.pos.equals(p.getPos())
                            && (p.getState().isAir() || p.getState().getBlock().equals(Blocks.WATER))) {
                        ghostBlockCheckSetRemove.add(b);
                    }
                }
			} else if (packet instanceof ChunkDeltaUpdateS2CPacket p) {
				// Sounds
                for (SQB sqb : sQueue) {
                    p.visitUpdates((pos, state) -> {
                        if (sqb.pos.equals(pos)
                                && (state.isAir() || state.getBlock().equals(Blocks.WATER))) {
                            renderRunnables.add(() -> mc.interactionManager.breakBlock(sqb.pos));
                            sQueueRemove.add(sqb);
                        }
                    });
                }

				// Ghost block checks
                for (MBlock b : ghostBlockCheckSet.keySet()) {
                    p.visitUpdates((pos, state) -> {
                        if (b.pos.equals(pos)
                                && (state.isAir() || state.getBlock().equals(Blocks.WATER))) {
                            ghostBlockCheckSetRemove.add(b);
                        }
                    });
                }
            }
			ghostBlockCheckSetRemove.forEach(ghostBlockCheckSet::remove);
			ghostBlockCheckSetRemove.clear();

			sQueue.removeAll(sQueueRemove);
			sQueueRemove.clear();
			return ActionResult.PASS;
		});

		/*
		  On tick
		 */

		ClientTickEvents.START_CLIENT_TICK.register((mc) -> {

			if (mc.world == null
					|| mc.getNetworkHandler() == null) {
				return;
			}

			// Ghost block timeout check
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

			// Block timeout check
			blockTimeout.keySet().removeIf(
					next -> blockTimeout.get(next).getPassedTimeMs() > blockTimeoutDelay + next.ttm);

			// Place block timeout check
			placeBlockTimeout.keySet().removeIf(
					next -> placeBlockTimeout.get(next).getPassedTimeMs() > placeBlockTimeoutDelay + next.ttm);

			// Sounds timeout check
			sQueue.forEach(b -> {
				if (b.timer.getPassedTimeMs() > b.ttm) {
					sQueueRemove.add(b);
				}
			});
			sQueue.removeAll(sQueueRemove);
			sQueueRemove.clear();

			// Litematica
			if (litematica) {
				if (!LitematicaHelper.isLitematicaLoaded()) {
					litematica = false;
					return;
				}
				schematicMismatches = LitematicaHelper.INSTANCE.getMismatches();
			}

			// Mine checking
			if (mc.player == null
					|| !enabled
					|| (onGround && !mc.player.isOnGround())) {
				return;
			}

			// Reset the block interaction limit counter. This is done here because the player could have to switch back
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




			// Get a list of blocks around the player
			spherePosList.clear();
			spherePosList.addAll(getPlayerBlockSphere(mc.player.getEyePos(), radius + 1));

			// Remove impossible to mine blocks like bedrock, air, etc...
			filterBlocks(spherePosList, !sourceRemover);

			if (sourceRemover) {
				List<BlockPos> liquidList = new ArrayList<>(spherePosList);

				liquidList.removeIf(b -> {
					BlockState state = mc.world.getBlockState(b);
					return !(state.getBlock().equals(Blocks.WATER)
							|| state.getFluidState().getFluid().equals(Fluids.FLOWING_WATER)
							|| state.getBlock().equals(Blocks.LAVA)
							|| state.getFluidState().getFluid().equals(Fluids.FLOWING_LAVA)
					);
				});

				if (!liquidList.isEmpty()) {
					// Sort the blocks
					liquidList = sortBlocks(liquidList);
					for (BlockPos b : liquidList) {
						boolean inTimeout = false;
						for (PosAndState ps : placeBlockTimeout.keySet()) {
							if (ps.pos.equals(b)) {
								inTimeout = true;
								break;
							}
						}
						if (inTimeout) {
							continue;
						}
						BlockHitResult placeResult = canPlace(b);
						if (placeResult != null) {
							int netherrack = PlaceUtils.findSuitableBlock();
							if (netherrack != -1) {
								if (!PlaceUtils.isSuitableBlock(mc.player.getInventory().getMainHandStack().getItem())) {
									mc.player.getInventory().selectedSlot = netherrack;
									mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(netherrack));
									return;
								}
								RotationsManager.lookAt(placeResult.getPos());
								PlaceUtils.place(placeResult);
								placeBlockTimeout.put(new PosAndState(b, 0), new Timer().reset());
								return;
							} else {
								break;
							}
						}
					}
				}
			}

			// Sort the blocks
			spherePosList = sortBlocks(spherePosList);

			// Iterate through the list
			for (BlockPos b : spherePosList) {

				// If possible, mine / start mining the block
				if (canMine(b)) {

					// Top down gravity block checks to shift the block position to the top of the column
					if (mc.world.getBlockState(b).getBlock() instanceof FallingBlock) {
						while (mc.world.getBlockState(b.add(0, 1, 0)).getBlock() instanceof FallingBlock
								&& isWithinRadius(mc.player.getEyePos(), b.add(0, 1, 0), radius)
								&& canMine(b.add(0, 1, 0))) {
							b = b.add(0, 1, 0);
						}
					}

					// Swaps to the right tool
					int bestTool = InventoryUtils.getBestToolSlot(b);
					if (mc.player.getInventory().selectedSlot != bestTool) {
						mc.player.getInventory().selectedSlot = bestTool;
						mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(bestTool));
						packetCounter++;
					}

					// Mine block
					mineBlock(b);
				}
			}
		});

		/*
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

		/*
		  Initialize the movement handlers on tick event
		  I initialize it after the nuker on tick handler so that rotations are done by the time the movement handler decides how to move the player
		 */

		MovementHandler.initEventHandler();

		LOGGER.info("Nuker!");
	}
	private void mineBlock(BlockPos blockPos) {

		if (mc.player == null
				|| mc.world == null
				|| mc.interactionManager == null) {
			return;
		}

		double breakingTime = getBlockBreakingTimeMS(mc.player.getInventory().getMainHandStack(), blockPos, mc.player, mc.world);
		blockTimeout.put(new PosAndState(blockPos, breakingTime), new Timer().reset());
		MBlock mBlock = new MBlock(blockPos
				, mc.world.getBlockState(blockPos).getBlock()
				, breakingTime
				, new Timer().reset()
				, false
				, InventoryUtils.getBestToolSlot(blockPos)
		);
		// Double block
		if (mBlocks.size() == 1) {
			mBlocks.get(0).serverMine = true;
		}

		if (breakingTime > instaMineThreshold) {
			mBlocks.add(mBlock);
		}
		// Different mine packets for hardness values
		if (breakingTime <= instaMineThreshold) {
			if (breakingTime > 50) {
				stopDestroy(blockPos);
				startDestroy(blockPos);
				stopDestroy(blockPos);
			} else {
				startDestroy(blockPos);
			}
			if (clientBreak) {
				mc.interactionManager.breakBlock(blockPos);
				ghostBlockCheckSet.put(mBlock, new Timer().reset());
			}
		} else if (breakingTime > instaMineThreshold) {
			startDestroy(blockPos);
			abortDestroy(blockPos);
			stopDestroy(blockPos);
		}
		// Packet limiter
		if (packetLimit != -1) {
			if (breakingTime > 50) {
				packetCounter += 3;
			} else {
				packetCounter++;
			}
		}
		// Sounds
		if (breakingTime > 50) {
			sQueue.add(new SQB(
					breakingTime * 3 + 500
					, blockPos
					, new Timer().reset())
			);
		}
	}
	private boolean canMine(BlockPos pos) {

		if (mc.world == null
				|| mc.player == null
				|| mc.interactionManager == null) {
			return false;
		}

		ItemStack stack= mc.player.getInventory().getStack(InventoryUtils.getBestToolSlot(pos));
		// Flatten modes
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
					// without this, you sometimes get grim issues when walking down a staircase with smart flatten on
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
					// without this, you sometimes get grim issues when walking down a staircase with smart flatten on
					if (mc.player.supportingBlockPos.isPresent()
							&& mc.player.supportingBlockPos.get().equals(pos)) {
						return false;
					}
				}
			}
		}

		// Double block
		Iterator<MBlock> iterator = mBlocks.iterator();
		while (iterator.hasNext()) {
			MBlock b = iterator.next();
			if (b.serverMine) {
				if (b.timer.getPassedTimeMs() >= b.ttm
						|| mc.world.getBlockState(b.pos).getBlock() != b.block) {
					if (clientBreak
							&& isWithinRadius(mc.player.getEyePos(), b.pos, radius)) {
						mc.interactionManager.breakBlock(b.pos);
						ghostBlockCheckSet.put(b, new Timer().reset());
					}
					iterator.remove();
				}
			} else {
				if (b.timer.getPassedTimeMs() >= b.ttm * 0.7
						|| mc.world.getBlockState(b.pos).getBlock() != b.block) {
					if (clientBreak
							&& isWithinRadius(mc.player.getEyePos(), b.pos, radius)) {
						mc.interactionManager.breakBlock(b.pos);
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

		double timeToMine = getBlockBreakingTimeMS(
				stack
				, pos
				, mc.player
				, mc.world
		);
		// Block mine time limit
		if (timeToMine > 10000) {
			return false;
		}
		// Packet limit
		int packets = 1;
		if (timeToMine > 50) {
			packets = 3;
		}
		if (packetCounter >= packetLimit
				|| packetCounter + packets >= packetLimit) {
			return false;
		}
		// Block timeout
		for (PosAndState pas : blockTimeout.keySet()) {
			if (pos.equals(pas.pos)) {
				return false;
			}
		}
		// Avoid liquids
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
		// Litematica
		if (litematica
				&& !schematicMismatches.contains(pos)) {
			return false;
		}
		// baritone selection mode
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
            return withinBaritoneSelection;
		}
		return true;
	}
	private @Nullable BlockHitResult canPlace(BlockPos pos) {

		if (baritoneSelection) {
			if (packetCounter == 0) {
				baritoneSelections.clear();
				BaritoneAPI.getProvider().getAllBaritones().forEach(b ->
						baritoneSelections.addAll(Arrays.stream(b.getSelectionManager().getSelections()).toList()));
			}
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
					if (expandBaritoneSelectionsForLiquids) {
						minX -= 1;
						minY -= 1;
						minZ -= 1;
						maxX += 1;
						maxY += 1;
						maxZ += 1;
					}
					if (pos.getX() < minX || pos.getX() > maxX
							|| pos.getY() < minY || pos.getY() > maxY
							|| pos.getZ() < minZ || pos.getZ() > maxZ) {
						return null;
					}
				}
			}
		}

		if (mc.player == null
				|| mc.world == null) return null;
		Vec3d centerPos = pos.toCenterPos();
		if (mc.world.getBlockState(pos.add(0, 1, 0)).isFullCube(mc.world, pos.add(0, 1, 0))
				&& canSeeBlockFace(mc.player, pos.add(0, 1, 0), Direction.DOWN)
				&& mc.player.getEyePos().distanceTo(new Vec3d(centerPos.x, centerPos.y + 0.5, centerPos.z)) <= 4.5) {
			return new BlockHitResult(centerPos.add(0, 0.5, 0), Direction.DOWN, pos.add(0, 1, 0), false);

		} else if (mc.world.getBlockState(pos.add(0, -1, 0)).isFullCube(mc.world, pos.add(0, -1, 0))
				&& canSeeBlockFace(mc.player, pos.add(0, -1, 0), Direction.UP)
				&& mc.player.getEyePos().distanceTo(new Vec3d(centerPos.x, centerPos.y - 0.5, centerPos.z)) <= 4.5) {
			return new BlockHitResult(centerPos.add(0, -0.5, 0), Direction.UP, pos.add(0, -1, 0), false);

		} else if (mc.world.getBlockState(pos.add(1, 0, 0)).isFullCube(mc.world, pos.add(1, 0, 0))
				&& canSeeBlockFace(mc.player, pos.add(1, 0, 0), Direction.WEST)
				&& mc.player.getEyePos().distanceTo(new Vec3d(centerPos.x + 0.5, centerPos.y, centerPos.z)) <= 4.5) {
			return new BlockHitResult(centerPos.add(0.5, 0, 0), Direction.WEST, pos.add(1, 0, 0), false);

		} else if (mc.world.getBlockState(pos.add(0, 0, 1)).isFullCube(mc.world, pos.add(0, 0, 1))
				&& canSeeBlockFace(mc.player, pos.add(0, 0, 1), Direction.NORTH)
				&& mc.player.getEyePos().distanceTo(new Vec3d(centerPos.x, centerPos.y, centerPos.z + 0.5)) <= 4.5) {
			return new BlockHitResult(centerPos.add(0, 0, 0.5), Direction.NORTH, pos.add(0, 0, 1), false);

		} else if (mc.world.getBlockState(pos.add(-1, 0, 0)).isFullCube(mc.world, pos.add(-1, 0, 0))
				&& canSeeBlockFace(mc.player, pos.add(-1, 0, 0), Direction.EAST)
				&& mc.player.getEyePos().distanceTo(new Vec3d(centerPos.x - 0.5, centerPos.y, centerPos.z)) <= 4.5) {
			return new BlockHitResult(centerPos.add(-0.5, 0, 0), Direction.EAST, pos.add(-1, 0, 0), false);

		} else if (mc.world.getBlockState(pos.add(0, 0, -1)).isFullCube(mc.world, pos.add(0, 0, -1))
				&& canSeeBlockFace(mc.player, pos.add(0, 0, -1), Direction.SOUTH)
				&& mc.player.getEyePos().distanceTo(new Vec3d(centerPos.x, centerPos.y, centerPos.z - 0.5)) <= 4.5) {
			return new BlockHitResult(centerPos.add(0, 0, -0.5), Direction.SOUTH, pos.add(0, 0, -1), false);
		}
		return null;
	}
	private void startDestroy(BlockPos blockPos) {
		if (mc.getNetworkHandler() == null) return;
		mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
	}
	private void abortDestroy(BlockPos blockPos) {
		if (mc.getNetworkHandler() == null) return;
		mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, blockPos, Direction.UP));
	}
	private void stopDestroy(BlockPos blockPos) {
		if (mc.getNetworkHandler() == null) return;
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

		List<BlockPos> sortedList = new ArrayList<>();

		if (mc.player == null) return sortedList;

		sortedList.addAll(list.stream()
				.sorted(Comparator.comparingDouble(a -> mc.player.getEyePos().distanceTo(a.toCenterPos()))).toList());

		switch (mineSort) {
			case FARTHEST -> Collections.reverse(sortedList);
			case TOP_DOWN -> sortedList.sort(Comparator.comparingDouble((a) -> -a.getY()));
			case BOTTOM_UP -> sortedList.sort(Comparator.comparingDouble(BlockPos::getY));
			case RANDOM -> Collections.shuffle(sortedList);
		}

		return sortedList;
	}
	private void filterBlocks(List<BlockPos> positions, boolean removeLiquids) {

		if (mc.world == null) return;

		Iterator<BlockPos> iterator = positions.iterator();
		while(iterator.hasNext()) {

			BlockPos next = iterator.next();

			BlockState state = mc.world.getBlockState(next);
			Block block = state.getBlock();

			if (state.isAir()
					|| block.getHardness() == 600
					|| block.getHardness() == -1
					|| block.equals(Blocks.BARRIER)) {
				iterator.remove();
			}

			if (removeLiquids
					&& state.getBlock().equals(Blocks.WATER)
					|| state.getFluidState().getFluid().equals(Fluids.FLOWING_WATER)
					|| state.getBlock().equals(Blocks.LAVA)
					|| state.getFluidState().getFluid().equals(Fluids.FLOWING_LAVA)) {
				iterator.remove();
			}
		}
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
		NONE,
		STANDARD,
		SMART,
		REVERSE_SMART
	}
	@AllArgsConstructor
	private class PosAndState {
		public BlockPos pos;
		public double ttm;
	}
}