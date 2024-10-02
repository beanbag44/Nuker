package me.beanbag.nuker.mixins;

import io.netty.channel.ChannelHandlerContext;
import me.beanbag.nuker.eventsystem.EventBus;
import me.beanbag.nuker.eventsystem.events.PacketEvent;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection {
    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"), cancellable = true)
    private void send(Packet<?> packet, PacketCallbacks packetCallback, CallbackInfo callback) {
        PacketEvent.Send.Pre event = new PacketEvent.Send.Pre(packet);
        EventBus.INSTANCE.post(event);
        if (event.isCanceled()) {
            callback.cancel();
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