package me.beanbag.nuker.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.ActionResult;

public interface PacketReceiveCallback {
    Event<PacketReceiveCallback> EVENT = EventFactory.createArrayBacked(PacketReceiveCallback.class,
            (listeners) -> (packet) -> {
                for (PacketReceiveCallback listener : listeners) {
                    ActionResult result = listener.interact(packet);
                    if(result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
    });

    ActionResult interact(Packet<?> packet);
}
