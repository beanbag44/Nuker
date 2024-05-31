package me.beanbag;

import me.beanbag.events.PacketReceiveCallback;
import me.beanbag.events.Render3DCallback;
import me.beanbag.settings.FlattenMode;
import me.beanbag.settings.MineSort;
import me.beanbag.utils.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Nuker implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("nuker");
	public static MinecraftClient mc = MinecraftClient.getInstance();


	public static List<BlockPos> spherePosList = Collections.synchronizedList(new ArrayList<>());
	public static List<Runnable> renderRunnables = Collections.synchronizedList(new ArrayList<>());

	public static boolean rusherhackLoaded = false;
	public static boolean meteorPresent = false;
	public static boolean initialized = false;

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
	public static int blockMineTimeLimit = 5000;
	public static int instaMineThreshold = 67;
	public static boolean onGround = true;
	public static boolean canalMode = false;
	public static boolean packetPlace = false;
	public static boolean sourceRemover = false;
	public static int placeBlockTimeoutDelay = 5000;
	public static boolean expandBaritoneSelectionsForLiquids = true;
	public static boolean placeRotatePlace = true;
	public static boolean preventSprintingInWater = false;
	public static boolean crouchLowerFlatten = false;


	public static void onPacketReceive(Packet<?> packet) {
		if (mc.world == null
				|| mc.player == null
				|| mc.getNetworkHandler() == null
				|| mc.interactionManager == null) {
			return;
		}
		if (packet instanceof BlockUpdateS2CPacket blockUpdatePacket) {
			BreakingHandler.onBlockUpdatePacket(blockUpdatePacket);
			SoundHandler.onBlockUdatePacket(blockUpdatePacket);

		} else if (packet instanceof ChunkDeltaUpdateS2CPacket chunkDeltaPacket) {
			BreakingHandler.onChunkDeltaPacket(chunkDeltaPacket);
			SoundHandler.onChunkDeltaPacket(chunkDeltaPacket);
		}
	}

	public static void onTick() {
		if (mc.world == null
				|| mc.player == null
				|| mc.getNetworkHandler() == null
				|| mc.interactionManager == null) {
			return;
		}

		// Removes check block positions if over the timeout limit
		SoundHandler.updateBlockLists();

		if (preventSprintingInWater
				&& mc.player.isSubmergedInWater()) {
			mc.player.setSprinting(false);
		}

		if (baritoneSelection) {
			BaritoneUtils.updateSelections();
		}

		if (!enabled
				|| (onGround && !mc.player.isOnGround())) {
			return;
		}

		// Updates the list of block positions around the player
		spherePosList.clear();
		spherePosList.addAll(BlockUtils.getPlayerBlockSphere(mc.player.getEyePos(), radius + 1));

		// Remove impossible to mine blocks like bedrock, barrier, etc...
		BlockUtils.filterImpossibleBlocks();

		// Returns true if nuker should wait until the next tick to continue
		if (PlacementHandler.excecutePlacements()) return;

		// Removes liquids
		BlockUtils.filterLiquids();

		// Sorts the blocks
		spherePosList = BlockUtils.sortBlocks(spherePosList);

		// Attempts to mine as many blocks as possible
		BreakingHandler.executeBreakAttempts(spherePosList);
	}

	public static void onRender3D() {
		if (mc.world == null) {
			renderRunnables.clear();
			return;
		}
		renderRunnables.forEach(Runnable::run);
		renderRunnables.clear();
	}

	@Override
	public void onInitialize() {

		meteorPresent = FabricLoader.getInstance().getModContainer("meteor-client").isPresent();

		/*
		  Initialize the event handlers
		 */

		ClientTickEvents.START_CLIENT_TICK.register(mc -> onTick());

		Render3DCallback.EVENT.register(() -> {
//			onRender3D();
			return ActionResult.PASS;
		});

		PacketReceiveCallback.EVENT.register(packet -> {
			onPacketReceive(packet);
			return ActionResult.PASS;
		});

		/*
		  Initialize the rotations manager
		 */

		RotationsManager.initEventHandler();

		/*
		  Initialize the movement handlers on tick event
		  I initialize it after the nuker on tick handler so that rotations are done by the time the movement handler decides how to move the player
		 */

		MovementHandler.initEventHandler();

		/*
		  Initialize the chat event handler if meteor isnt loaded
		 */

		if (!meteorPresent) {
			ChatEventHandler.initChatEventHandler();
		}

		initialized = true;

		LOGGER.info("Nuker!");
	}
}