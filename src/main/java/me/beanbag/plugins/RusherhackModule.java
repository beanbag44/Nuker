package me.beanbag.plugins;

import me.beanbag.Nuker;
import net.minecraft.network.packet.Packet;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.events.network.EventPacket;
import org.rusherhack.client.api.events.render.EventRender3D;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.EnumSetting;
import org.rusherhack.core.setting.NumberSetting;

import java.lang.reflect.Field;

public class RusherhackModule extends ToggleableModule {

    private final NumberSetting<Integer> radius = new NumberSetting<>("Radius", 10, 0, 15)
            .onChange(v -> Nuker.radius = v);
    private final NumberSetting<Integer> blockTimeoutDelay = new NumberSetting<>("Block Timeout Delay", 300, 0, 1000)
            .onChange(v -> Nuker.blockTimeoutDelay = v);
    private final NumberSetting<Integer> placeBlockTimeoutDelay = new NumberSetting<>("Place Block Timeout Delay", 5000, 0, 5000)
            .onChange(v -> Nuker.placeBlockTimeoutDelay = v);
    private final NumberSetting<Integer> clientBreakGhostBlockTimeout = new NumberSetting<>("Client Side Break Ghost Block Timeout", 1000, 50, 3000)
            .onChange(v -> Nuker.clientBreakGhostBlockTimeout = v);
    private final BooleanSetting onGround = new BooleanSetting("On Ground", true)
            .onChange(v -> Nuker.onGround = v);
    private final BooleanSetting baritoneSelections = new BooleanSetting("Baritone Selections", false)
            .onChange(v -> Nuker.baritoneSelection = v);
    private final BooleanSetting litematica = new BooleanSetting("Litematica", false)
            .onChange(v -> Nuker.litematica = v);
    private final EnumSetting<Nuker.FlattenMode> flattenMode = new EnumSetting<>("Flatten Mode", Nuker.FlattenMode.STANDARD)
            .onChange(v -> Nuker.flattenMode = v);
    private final EnumSetting<Nuker.MineSort> mineSort = new EnumSetting<>("Mine Sort", Nuker.MineSort.CLOSEST)
            .onChange(v -> Nuker.mineSort = v);
    private final BooleanSetting clientBreak = new BooleanSetting("Client Side Break", true)
            .onChange(v -> Nuker.clientBreak = v);
    private final NumberSetting<Integer> packetLimit = new NumberSetting<>("Packet Limit", 10, 0, 15)
            .onChange(v -> Nuker.packetLimit = v);
    private final NumberSetting<Integer> blockMineTimeLimit = new NumberSetting<>("Block Mine Time Limit", 5000, 0, 5000)
            .onChange(v -> Nuker.blockMineTimeLimit = v);
    private final NumberSetting<Integer> instaMineThreshold = new NumberSetting<>("Insta Mine Threshold", 67, 0, 150)
            .onChange(v -> Nuker.instaMineThreshold = v);
    private final BooleanSetting avoidLiquids = new BooleanSetting("Avoid Liquids", true)
            .onChange(v -> Nuker.avoidLiquids = v);
    private final BooleanSetting canalMode = new BooleanSetting("Canal Mode", false)
            .onChange(v -> Nuker.canalMode = v);
    private final BooleanSetting packetPlace = new BooleanSetting("Packet Place", false)
            .onChange(v -> Nuker.packetPlace = v);
    private final BooleanSetting sourceRemover = new BooleanSetting("Source Remover", false)
            .onChange(v -> Nuker.sourceRemover = v);
    private final BooleanSetting expandBaritoneSelectionsForLiquids = new BooleanSetting("Expand Baritone Selections For Liquids", true)
            .onChange(v -> Nuker.expandBaritoneSelectionsForLiquids = v);

    public RusherhackModule() {
        super("Epic Nuker", "Epic nuker for nuking terrain", ModuleCategory.WORLD);
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
        );
    }
    @Subscribe
    public void onPacketReceive(EventPacket.Receive event) {
        Nuker.onPacketReceive(event.getPacket());
//        try {
//            Nuker.onPacketReceive((Packet<?>) EventPacket.Receive.class.getMethod("getPacket").invoke(event));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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
