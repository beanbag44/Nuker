package me.beanbag.utils;

import lombok.Getter;
import lombok.Setter;

import static me.beanbag.Nuker.mc;
@Getter
public class FreeLook {
    @Setter
    float yaw = 0;
    @Setter
    float pitch = 0;
    boolean enabled = false;
    public static final FreeLook INSTANCE = new FreeLook();
    public void enable() {
        if (mc.player == null) return;
        enabled = true;
        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();
    }
    public void disable() {
        enabled = false;
        MovementHandler.disable();
    }
}
