package me.beanbag.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.ActionResult;

public interface Render3DCallback {
    Event<Render3DCallback> EVENT = EventFactory.createArrayBacked(Render3DCallback.class,
            (listeners) -> () -> {
                for (Render3DCallback listener : listeners) {
                    ActionResult result = listener.interact();

                    if(result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    ActionResult interact();
}
