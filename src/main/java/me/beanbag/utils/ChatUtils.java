package me.beanbag.utils;

import net.minecraft.text.Text;

import static me.beanbag.Nuker.mc;

public class ChatUtils {
    public static void sendClientMessage(String text) {
        mc.inGameHud.getChatHud().addMessage(Text.of(text));
    }
    public static void sendHeader() {
        sendClientMessage("======== Nuker ========");
    }
    public static void sendClientMessages(Object[] text) {
        for (Object s : text) {
            sendClientMessage(s.toString());
        }
    }
    public static void sendClientMessages(String... text) {
        for (Object s : text) {
            sendClientMessage(s.toString());
        }
    }
}
