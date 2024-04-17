package me.beanbag.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.ActionResult;

public interface PacketSendCallback {
    Event<PacketSendCallback> EVENT = EventFactory.createArrayBacked(PacketSendCallback.class,
            (listeners) -> (packet) -> {
                for (PacketSendCallback listener : listeners) {
                    ActionResult result = listener.interact(packet);

                    if(result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    ActionResult interact(Packet<?> packet);
}
