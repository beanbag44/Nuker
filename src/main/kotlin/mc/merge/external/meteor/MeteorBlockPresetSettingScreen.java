package mc.merge.external.meteor;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.screens.settings.BlockListSettingScreen;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class MeteorBlockPresetSettingScreen extends WindowScreen {

    private final MeteorBlockPresetSetting setting;
    private WTable table;
    private String filterText = "";

    public MeteorBlockPresetSettingScreen(GuiTheme theme, MeteorBlockPresetSetting setting) {
        super(theme, "Configure Block Presets");
        this.setting = setting;
    }

    @Override
    public void initWidgets() {
        WTable header = add(theme.table()).expandX().widget();
        WTextBox filter = header.add(theme.textBox("")).minWidth(400).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            filterText = filter.get().trim();
            reset();
        };
        WPressable create = header.add(theme.plus()).widget();
        create.action = () -> {
            if (filterText.isEmpty() || setting.get().getEntries().containsKey(filterText)) return;
            setting.get().addEntry(filterText,new BlockListSetting.Builder().build());
            setting.get().setSelected(filterText);
            reset();
        };
        table = add(theme.table()).expandX().widget();
        initTable();
    }

    public void initTable() {
        for (Map.Entry<String, Setting<List<Block>>> entry : setting.get().getEntries().entrySet()) {
            String label = entry.getKey();
            List<Block> blocks = entry.getValue().get();
            WLabel wLabel = theme.label(label).color(Objects.equals(setting.get().getSelected(), label) ? Color.GREEN : Color.WHITE);
            if (!StringUtils.containsIgnoreCase(label, filterText)) continue;
            getValueWidget(wLabel, blocks);
            WButton select = table.add(theme.button("Select")).right().widget();
            select.action = () -> {
                setting.get().setSelected(label);
                reset();
            };
            WButton edit = table.add(theme.button(GuiRenderer.EDIT)).right().widget();
            edit.action = () -> {
                BlockListSettingScreen blockListSettingScreen = new BlockListSettingScreen(theme,entry.getValue());
                mc.setScreen(blockListSettingScreen);
                reset();
            };
            WPressable delete = table.add(theme.minus()).right().widget();
            delete.action = () -> {
                setting.get().removeEntry(label);
                reset();
            };
            table.row();
        }
    }

    private void getValueWidget(WLabel label, List<Block> list) {
        ItemStack itemStack = (list.isEmpty()) ? Items.BARRIER.getDefaultStack() : list.getFirst().asItem().getDefaultStack();
        table.add(theme.item(itemStack));
        table.add(label).expandX();
    }

    private void reset() {
        table.clear();
        setting.onChanged();
        initTable();
    }
}
