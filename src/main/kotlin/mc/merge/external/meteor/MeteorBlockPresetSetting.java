package mc.merge.external.meteor;

import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

    public class MeteorBlockPresetSetting extends Setting<MeteorPreset<Block>> {
    private final Predicate<Block> filter; // In case you want to filter out some blocks from the preset lists.
    public MeteorBlockPresetSetting(String name, String description, MeteorPreset<Block> defaultValue,
                                    Consumer<MeteorPreset<Block>> onChanged,
                                    Consumer<Setting<MeteorPreset<Block>>> onModuleActivated, IVisible visible,
                                    Predicate<Block> filter) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.filter = filter;
    }


    @Override
    public void resetImpl() {
        value = new MeteorPreset<>(null, new HashMap<>());
    }

    @Override
    protected MeteorPreset<Block> parseImpl(String str) {
        String[] values = str.split(",");
        MeteorPreset<Block> meteorPreset = new MeteorPreset<>(null,new HashMap<>());
        for (String value : values) {
            if (get().getEntries().containsKey(value)) meteorPreset.addEntry(value, get().getEntry(value));
        }
        return meteorPreset;
    }

    @Override
    protected boolean isValueValid(MeteorPreset<Block> value) {
        return true;
    }

    @Override
    protected NbtCompound save(NbtCompound nbt) {
        nbt.putString("selected",get().getSelected());
        for (Map.Entry<String, Setting<List<Block>>> entry : get().getEntries().entrySet()) {
            NbtList listTag = new NbtList();
            entry.getValue().get().forEach(block -> listTag.add(NbtString.of(Registries.BLOCK.getId(block).toString())));
            nbt.put(entry.getKey(),listTag);
        }
        return nbt;
        //return get().toTag();
    }

    @Override
    protected MeteorPreset<Block> load(NbtCompound nbt) {
        get().setSelected(nbt.getString("selected"));
        for (String key : nbt.getKeys()) {
            if (key.equals("selected") || key.equals("name")) continue;
            NbtList listTag = nbt.getList(key, 8);
            List<Block> blocks = listTag.stream().map(nbtElement -> {
                Block block = Registries.BLOCK.get(Identifier.of(nbtElement.asString()));
                if (filter == null || filter.test(block)) return block;
                return null;
            }).toList();
            get().addEntry(key,new BlockListSetting.Builder().defaultValue(blocks).build());
        }
        return get();
    }

    public static class Builder extends SettingBuilder<Builder, MeteorPreset<Block>, MeteorBlockPresetSetting> {
        Predicate<Block> filter;

        public Builder() {
            super(new MeteorPreset<>(null, new HashMap<>()));
        }

        public Builder filter(Predicate<Block> filter) {
            this.filter = filter;
            return this;
        }

        public Builder defaultValue(Map<String,Setting<List<Block>>> defaults) {
            return defaultValue(defaults != null ? new MeteorPreset<>(null,defaults): new MeteorPreset<>(null, new HashMap<>()));
        }

        @Override
        public MeteorBlockPresetSetting build() {
            return new MeteorBlockPresetSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, filter);
        }
    }
}
