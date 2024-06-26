package me.beanbag.mixin;

import io.netty.channel.ChannelHandlerContext;
import me.beanbag.events.PacketReceiveCallback;
import me.beanbag.events.PacketSendCallback;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection {
    @Inject(method = "channelRead0*", at = @At("HEAD"), cancellable = true)
    private void channelReadPre(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo callback) {
        if (packet != null) {
            ActionResult result = PacketReceiveCallback.EVENT.invoker().interact(packet);
            if (result != ActionResult.PASS) {
                callback.cancel();
            }
        }
    }
    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"), cancellable = true)
    private void send(Packet<?> packet, PacketCallbacks packetCallback, CallbackInfo callback) {
        ActionResult result = PacketSendCallback.EVENT.invoker().interact(packet);
        if (result != ActionResult.PASS) {
            callback.cancel();
        }
    }
}
