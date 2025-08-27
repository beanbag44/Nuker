package mc.merge.external.meteor;

import meteordevelopment.meteorclient.settings.Setting;

import java.util.List;
import java.util.Map;

public class MeteorPreset<T> {
    private String selected;
    private Map<String, Setting<List<T>>> entries;
    protected MeteorPreset(String selected, Map<String, Setting<List<T>>> entries) {
        this.selected = selected;
        this.entries = entries;
    }

    protected String getSelected() {
        if (selected == null) {
            if (entries == null || entries.isEmpty()) return "";
            return entries.keySet().iterator().next();
        }
        return selected;
    }

    protected void setSelected(String selected) {
        this.selected = selected;
    }

    protected Map<String, Setting<List<T>>> getEntries() {
        return entries;
    }

    protected Setting<List<T>> getEntry(String key) {
        return entries.get(key);
    }

    protected void setEntries(Map<String, Setting<List<T>>> entries) {
        this.entries = entries;
    }

    protected void addEntry(String value, Setting<List<T>> setting) {
        entries.put(value, setting);
    }

    protected void removeEntry(String key) {
        entries.remove(key);
    }
}
