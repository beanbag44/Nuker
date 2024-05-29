package me.beanbag.plugins;

import me.beanbag.Nuker;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;

public class MeteorModule extends Module {

    public MeteorModule() {
        super(MeteorAddonLoader.CATEGORY, "Epic Nuker", "Epic nuker for nuking terrain");

        SettingGroup general = this.settings.getDefaultGroup();

        general.add(new IntSetting.Builder()
                .name("Radius")
                .defaultValue(5)
                .sliderRange(0, 15)
                .onChanged(v -> Nuker.radius = v)
                .build());
        general.add(new IntSetting.Builder()
                .name("Block Timeout Delay")
                .defaultValue(300)
                .sliderRange(0, 1000)
                .onChanged(v -> Nuker.blockTimeoutDelay = v)
                .build());
        general.add(new IntSetting.Builder()
                .name("Place Block Timeout Delay")
                .defaultValue(5000)
                .sliderRange(0, 5000)
                .onChanged(v -> Nuker.placeBlockTimeoutDelay = v)
                .build());
        general.add(new IntSetting.Builder()
                .name("Client Side Break Ghost Block Timeout")
                .defaultValue(1000)
                .sliderRange(50, 3000)
                .onChanged(v -> Nuker.clientBreakGhostBlockTimeout = v)
                .build());
        general.add(new BoolSetting.Builder()
                .name("On Ground")
                .defaultValue(true)
                .onChanged(v -> Nuker.onGround = v)
                .build());
        general.add(new BoolSetting.Builder()
                .name("Baritone Selections")
                .defaultValue(false)
                .onChanged(v -> Nuker.baritoneSelection = v)
                .build());
        general.add(new BoolSetting.Builder()
                .name("Litematica")
                .defaultValue(false)
                .onChanged(v -> Nuker.litematica = v)
                .build());
        general.add(new EnumSetting.Builder<>()
                .name("Flatten Mode")
                .defaultValue(Nuker.FlattenMode.STANDARD)
                .onChanged(v -> Nuker.flattenMode = (Nuker.FlattenMode) v)
                .build());
        general.add(new EnumSetting.Builder<>()
                .name("Mine Sort")
                .defaultValue(Nuker.MineSort.CLOSEST)
                .onChanged(v -> Nuker.mineSort = (Nuker.MineSort) v)
                .build());
        general.add(new BoolSetting.Builder()
                .name("Client Side Break")
                .defaultValue(true)
                .onChanged(v -> Nuker.clientBreak = v)
                .build());
        general.add(new IntSetting.Builder()
                .name("Packet Limit")
                .defaultValue(10)
                .sliderRange(0, 15)
                .onChanged(v -> Nuker.packetLimit = v)
                .build());
        general.add(new IntSetting.Builder()
                .name("Block Mine Time Limit")
                .defaultValue(5000)
                .sliderRange(0, 5000)
                .onChanged(v -> Nuker.blockMineTimeLimit = v)
                .build());
        general.add(new IntSetting.Builder()
                .name("Insta Mine Threshold")
                .defaultValue(67)
                .sliderRange(0, 150)
                .onChanged(v -> Nuker.instaMineThreshold = v)
                .build());
        general.add(new BoolSetting.Builder()
                .name("Avoid Liquids")
                .defaultValue(true)
                .onChanged(v -> Nuker.avoidLiquids = v)
                .build());
        general.add(new BoolSetting.Builder()
                .name("Canal Mode")
                .defaultValue(false)
                .onChanged(v -> Nuker.canalMode = v)
                .build());
        general.add(new BoolSetting.Builder()
                .name("Packet Place")
                .defaultValue(false)
                .onChanged(v -> Nuker.packetPlace = v)
                .build());
        general.add(new BoolSetting.Builder()
                .name("Source Remover")
                .defaultValue(false)
                .onChanged(v -> Nuker.sourceRemover = v)
                .build());
        general.add(new BoolSetting.Builder()
                .name("Expand Baritone Selections For Liquids")
                .defaultValue(true)
                .onChanged(v -> Nuker.expandBaritoneSelectionsForLiquids = v)
                .build());
    }

    @Override
    public void onActivate() {
        Nuker.enabled = true;
    }

    @Override
    public void onDeactivate() {
        Nuker.enabled = false;
    }
}
