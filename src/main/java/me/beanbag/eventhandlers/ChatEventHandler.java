package me.beanbag.eventhandlers;

import me.beanbag.events.PacketSendCallback;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

import static me.beanbag.Nuker.mc;
import static me.beanbag.Nuker.nuker;

public class ChatEventHandler {
    private String prefix = "Nuker: ";
    public ChatEventHandler() {
        PacketSendCallback.EVENT.register(packet -> {
            if (packet instanceof ChatMessageC2SPacket p
                    && p.chatMessage().startsWith("&&")) {
                switch (p.chatMessage()) {
                    case "&&list" -> {
                        sendClientMessage(prefix + "Sorting Mode: " + nuker.mineSort);
                        sendClientMessage(prefix + "Flatten Mode: " + nuker.flattenMode);
                        sendClientMessage(prefix + "Packet Limit: " + nuker.packetLimit);
                        sendClientMessage(prefix + "Client Side Break: " + nuker.clientBreak);
                        sendClientMessage(prefix + "Radius: " + nuker.radius);
                        sendClientMessage(prefix + "Baritone Selection mode: " + nuker.baritoneSelection);
                        sendClientMessage(prefix + "Client Break Ghost Block Timeout: " + nuker.clientBreakGhostBlockTimeout);
                        sendClientMessage(prefix + "Block Timeout Delay: " + nuker.blockTimeoutDelay);
                        sendClientMessage(prefix + "InstaMine Threshold: " + nuker.instaMineThreshold);
                        sendClientMessage(prefix + "On Ground: " + nuker.onGround);
                    }
                }
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
    }
    public void sendClientMessage(String text) {
        mc.inGameHud.getChatHud().addMessage(Text.of(text));
    }
}
