package me.beanbag.nuker.module.modules

import me.beanbag.nuker.ModConfigs
import me.beanbag.nuker.external.meteor.MeteorModule
import me.beanbag.nuker.module.Module
import me.beanbag.nuker.module.modules.nuker.enumsettings.BreakMode
import me.beanbag.nuker.module.modules.nuker.enumsettings.ColourMode
import me.beanbag.nuker.module.modules.nuker.enumsettings.RenderAnimation
import me.beanbag.nuker.module.modules.nuker.enumsettings.RenderType
import net.minecraft.item.Items
import java.awt.Color

object CoreConfig : Module("Core Configs", "General configs") {

    private val breaking = group("Breaking", "Settings for breaking blocks")
    val breakRadius by setting(breaking,
        "Break Radius",
        "The radius around the player blocks can be broken",
        5.0, null, { true }, 0.0, 6.0, 0.0, 6.0, 0.1)
    val doubleBreak by setting(breaking,
        "Double Break",
        "Breaks two blocks at once",
        true, null) { true }
    val breakMode by setting(breaking,
        "Break Mode",
        "Changes the way total break amount is calculated",
        BreakMode.Total, null) { true }
    val validateBreak by setting(breaking,
        "Validate Break",
        "Waits for the server to validate breaks",
        true, null) { true }
    val ghostBlockTimeout by setting(breaking,
        "Ghost Block Timeout (Ticks)",
        "The delay after breaking a block to reset its state if the server hasn't validated the break",
        30, null, { !validateBreak }, 5, 50, 5, 50)
    val breakThreshold by setting(breaking,
        "Break Threshold",
        "The percentage mined a block should be broken at",
        0.70f, null, { true },  0f, 1f, 0f, 1f, 0.01f)
    val packetLimit by setting(breaking,
        "Packet Limit",
        "How many packets can be sent per tick",
        10, null, { true }, 0, 15, 0, 15)
    val blockBreakTimeout by setting(breaking,
        "Break Timeout",
        "The delay (in ticks) after breaking a block to attempt to break it again",
        20, null, { true }, 0, 100, 0, 100)

    private val breakingRender = group("Breaking Render", "Settings for rendering breaking blocks")
    val renders by setting(this.breakingRender,
        "Renders",
        "Draws animated boxes showing the current mining blocks and more",
        RenderType.Both, null) { true }
    val renderAnimation by setting(this.breakingRender,
        "Render Animation",
        "Changes the way box renders are animated", RenderAnimation.Out,
        null) { renders.enabled() }
    val fillColourMode by setting(this.breakingRender,
        "Fill Colour Mode",
        "Changes the box fill render colour style",
        ColourMode.Dynamic, null) { renders.enabled() && renders != RenderType.Line }
    val staticFillColour by setting(this.breakingRender,
        "Static Fill Colour",
        "The colour used to render the static fill of the box faces",
        Color(1.0f, 0.0f, 0.0f, 0.22f).brighter(), null) { renders.enabled() && renders != RenderType.Line && fillColourMode == ColourMode.Static }
    val startFillColour by setting(this.breakingRender,
        "Start Fill Colour",
        "The colour used to render the start fill of the box faces",
        Color(1.0f, 0.0f, 0.0f, 0.22f).brighter(), null) { renders.enabled() && renders != RenderType.Line && fillColourMode == ColourMode.Dynamic }
    val endFillColour by setting(this.breakingRender,
        "End Fill Colour",
        "The colour used to render the end fill of the box faces",
        Color(0.0f, 1.0f, 0.0f, 0.22f).brighter(), null) { renders.enabled() && renders != RenderType.Line && fillColourMode == ColourMode.Dynamic }
    val outlineColourMode by setting(this.breakingRender,
        "Outline Colour Mode",
        "Changes the box outline render colour style",
        ColourMode.Dynamic, null) { renders.enabled() && renders != RenderType.Fill }
    val staticOutlineColour by setting(this.breakingRender,
        "Static Outline Colour",
        "The colour used to render the outline of the box",
        Color.RED.brighter(), null) { renders.enabled() && renders != RenderType.Fill && outlineColourMode == ColourMode.Static }
    val startOutlineColour by setting(this.breakingRender,
        "Start Outline Colour",
        "The colour used to render the start outline of the box",
        Color.RED.brighter(), null) { renders.enabled() && renders != RenderType.Fill && outlineColourMode == ColourMode.Dynamic }
    val endOutlineColour by setting(this.breakingRender,
        "End Outline Colour",
        "The colour used to render the end outline of the box",
        Color.GREEN.brighter(), null) { renders.enabled() && renders != RenderType.Fill && outlineColourMode == ColourMode.Dynamic }
    val outlineWidth by setting(this.breakingRender,
        "Outline Width",
        "The width of the rendered box outline",
        1.0f, null, { renders.enabled() && renders != RenderType.Fill }, 0.0f, 5.0f, 0.0f, 5.0f, 0.1f)


    private val placing = group("Placing", "Settings for placing blocks")
    val placeRadius by setting(placing,
        "Place Radius",
        "The radius around the player blocks can be placed",
        4.5)
    val validatePlace by setting(placing,
        "Validate Place",
        "Waits for the server to respond before placing the block",
        true)
    val blockPlaceTimeout by setting(placing,
        "Place Timeout",
        "The delay (in ticks) after placing a block to attempt to place it again",
        20, null, { true }, 0, 50, 0, 50)
    val swingOnPlace by setting(placing,
        "Swing",
        "Swings the players hand when placing",
        false)


    private val inventory = group("Inventory", "Settings for inventory management")
    val swapCooldownTicks = setting(inventory, "Swap Cooldown (Ticks)", "The delay between inventory actions with SlotActionType.SWAP", 1, null, { true }, 0, 20, 0, 20)
    val throwawayBlocks = setting(inventory, "Throwaway Blocks", "Blocks that can be thrown away if needed. For example, when moving a piece of armor to your inventory", listOf(Items.COBBLESTONE, Items.STONE, Items.DIRT, Items.NETHERRACK, Items.DIORITE, Items.ANDESITE, Items.GRANITE, Items.GRAVEL, Items.ROTTEN_FLESH, Items.BONE), null)

    override fun createMeteorImplementation(): meteordevelopment.meteorclient.systems.modules.Module {
        return CoreConfigMeteorImplementation()
    }

    class CoreConfigMeteorImplementation : MeteorModule(this)
}