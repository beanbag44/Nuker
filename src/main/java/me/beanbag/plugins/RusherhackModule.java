package me.beanbag.plugins;

import me.beanbag.Nuker;
import me.beanbag.settings.FlattenMode;
import me.beanbag.settings.MineSort;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.events.network.EventPacket;
import org.rusherhack.client.api.events.render.EventRender3D;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.EnumSetting;
import org.rusherhack.core.setting.NumberSetting;

public class RusherhackModule extends ToggleableModule {

    public RusherhackModule() {
        super("Epic Nuker", "Epic nuker for nuking terrain", ModuleCategory.WORLD);
        NumberSetting<Integer> radius = new NumberSetting<>("Radius", 5, 0, 15)
                .onChange(v -> Nuker.radius = v);
        NumberSetting<Integer> blockTimeoutDelay = new NumberSetting<>("Block Timeout Delay", 300, 0, 1000)
                .onChange(v -> Nuker.blockTimeoutDelay = v);
        NumberSetting<Integer> placeBlockTimeoutDelay = new NumberSetting<>("Place Block Timeout Delay", 5000, 0, 5000)
                .onChange(v -> Nuker.placeBlockTimeoutDelay = v);
        NumberSetting<Integer> clientBreakGhostBlockTimeout = new NumberSetting<>("Client Side Break Ghost Block Timeout", 1000, 50, 3000)
                .onChange(v -> Nuker.clientBreakGhostBlockTimeout = v);
        BooleanSetting onGround = new BooleanSetting("On Ground", true)
                .onChange(v -> Nuker.onGround = v);
        BooleanSetting baritoneSelections = new BooleanSetting("Baritone Selections", false)
                .onChange(v -> Nuker.baritoneSelection = v);
        BooleanSetting litematica = new BooleanSetting("Litematica", false)
                .onChange(v -> Nuker.litematica = v);
        EnumSetting<FlattenMode> flattenMode = new EnumSetting<>("Flatten Mode", FlattenMode.STANDARD)
                .onChange(v -> Nuker.flattenMode = v);
        EnumSetting<MineSort> mineSort = new EnumSetting<>("Mine Sort", MineSort.CLOSEST)
                .onChange(v -> Nuker.mineSort = v);
        BooleanSetting clientBreak = new BooleanSetting("Client Side Break", true)
                .onChange(v -> Nuker.clientBreak = v);
        NumberSetting<Integer> packetLimit = new NumberSetting<>("Packet Limit", 10, 0, 15)
                .onChange(v -> Nuker.packetLimit = v);
        NumberSetting<Integer> blockMineTimeLimit = new NumberSetting<>("Block Mine Time Limit", 5000, 0, 5000)
                .onChange(v -> Nuker.blockMineTimeLimit = v);
        NumberSetting<Integer> instaMineThreshold = new NumberSetting<>("Insta Mine Threshold", 67, 0, 150)
                .onChange(v -> Nuker.instaMineThreshold = v);
        BooleanSetting avoidLiquids = new BooleanSetting("Avoid Liquids", true)
                .onChange(v -> Nuker.avoidLiquids = v);
        BooleanSetting canalMode = new BooleanSetting("Canal Mode", false)
                .onChange(v -> Nuker.canalMode = v);
        BooleanSetting packetPlace = new BooleanSetting("Packet Place", false)
                .onChange(v -> Nuker.packetPlace = v);
        BooleanSetting sourceRemover = new BooleanSetting("Source Remover", false)
                .onChange(v -> Nuker.sourceRemover = v);
        BooleanSetting expandBaritoneSelectionsForLiquids = new BooleanSetting("Expand Baritone Selections For Liquids", true)
                .onChange(v -> Nuker.expandBaritoneSelectionsForLiquids = v);
        BooleanSetting placeRotatePlace = new BooleanSetting("Place Rotate Place", true)
                .onChange(v -> Nuker.placeRotatePlace = v);
        BooleanSetting preventSprintingInWater = new BooleanSetting("Prevent Sprinting In Water", false)
                .onChange(v -> Nuker.preventSprintingInWater = v);
        BooleanSetting crouchLowerFlatten = new BooleanSetting("Crouch Lower Flatten", false)
                .onChange(v -> Nuker.crouchLowerFlatten = v);
        this.registerSettings(
                radius
                , blockTimeoutDelay
                , placeBlockTimeoutDelay
                , clientBreakGhostBlockTimeout
                , onGround
                , baritoneSelections
                , litematica
                , flattenMode
                , mineSort
                , clientBreak
                , packetLimit
                , blockMineTimeLimit
                , instaMineThreshold
                , avoidLiquids
                , canalMode
                , packetPlace
                , sourceRemover
                , expandBaritoneSelectionsForLiquids
                , placeRotatePlace
                , preventSprintingInWater
                , crouchLowerFlatten
        );
    }
    @Subscribe
    public void onPacketReceive(EventPacket.Receive event) {
        Nuker.onPacketReceive(event.getPacket());
    }
    @Subscribe
    public void onTick(EventUpdate event) {
        Nuker.onTick();
    }
    @Subscribe
    public void onRender3D(EventRender3D event) {
        Nuker.onRender3D();
    }
    @Override
    public void onEnable() {
        Nuker.enabled = true;
    }
    @Override
    public void onDisable() {
        Nuker.enabled = false;
    }
}

