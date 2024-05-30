package me.beanbag.utils;

import baritone.api.BaritoneAPI;
import baritone.api.selection.ISelection;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BaritoneUtils {
    public static final Set<ISelection> baritoneSelections = Collections.synchronizedSet(new HashSet<>());
    public static void updateSelections() {
        baritoneSelections.clear();
        BaritoneAPI.getProvider().getAllBaritones().forEach(b ->
                baritoneSelections.addAll(Arrays.stream(b.getSelectionManager().getSelections()).toList()));
    }
}
