package me.beanbag.nuker.mixins;

import io.netty.channel.ChannelHandlerContext;
import me.beanbag.nuker.eventsystem.EventBus;
import me.beanbag.nuker.eventsystem.events.PacketEvent;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(ClientConnection.class)
public class MixinClientConnection {
    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void send(Packet<?> packet, CallbackInfo callback) {
        if (packet instanceof BundleS2CPacket bundle) {
            for (Iterator<Packet<ClientPlayPacketListener>> it = bundle.getPackets().iterator(); it.hasNext(); ) {
                PacketEvent.Send.Pre event = new PacketEvent.Send.Pre(packet);
                EventBus.INSTANCE.post(event);

                if (event.isCanceled()) {
                    it.remove();
                }
            }
        } else {
            PacketEvent.Send.Pre event = new PacketEvent.Send.Pre(packet);
            EventBus.INSTANCE.post(event);

            if (event.isCanceled()) {
                callback.cancel();
            }

        }
    }

    @Inject(method = "channelRead0*", at = @At("HEAD"), cancellable = true)
    private void channelReadPre(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo callback) {
        PacketEvent.Receive.Pre event = new PacketEvent.Receive.Pre(packet);
        EventBus.INSTANCE.post(event);
        if (event.isCanceled()) {
            callback.cancel();
        }
    }
}