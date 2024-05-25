package me.beanbag.utils;

import lombok.Getter;
import lombok.Setter;

import static me.beanbag.Nuker.mc;
@Getter
public class FreeLook {
    @Setter
    private float yaw = 0;
    @Setter
    private float pitch = 0;
    private boolean enabled = false;
    public static final FreeLook INSTANCE = new FreeLook();
    public void enable() {
        if (mc.player == null) return;
        enabled = true;
        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();
    }
    public void disable() {
        if (mc.player != null) {
            mc.player.setYaw(yaw);
            mc.player.setPitch(pitch);
        }
        enabled = false;
        MovementHandler.disable();
    }
}
