package me.beanbag.eventhandlers;

import me.beanbag.Nuker;
import me.beanbag.events.PacketSendCallback;
import me.beanbag.utils.PlaceUtils;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;

import static me.beanbag.Nuker.mc;

public class ChatEventHandler {
    private static final String prefix = "Nuker: ";
    public static void initChatEventHandler() {
        PacketSendCallback.EVENT.register(packet -> {
            if (packet instanceof ChatMessageC2SPacket p) {

                if (!p.chatMessage().startsWith("&&")) {
                    return ActionResult.PASS;
                }

                String message = p.chatMessage().toLowerCase();

                if (message.equals("&&list")) {
                    sendClientMessages(prefix + "Sorting Mode: " + Nuker.mineSort
                            , prefix + "Flatten Mode: " + Nuker.flattenMode
                            , prefix + "Packet Limit: " + Nuker.packetLimit
                            , prefix + "Client Side Break: " + Nuker.clientBreak
                            , prefix + "Radius: " + Nuker.radius
                            , prefix + "Baritone Selection mode: " + Nuker.baritoneSelection
                            , prefix + "Client Break Ghost Block Timeout: " + Nuker.clientBreakGhostBlockTimeout
                            , prefix + "Block Timeout Delay: " + Nuker.blockTimeoutDelay
                            , prefix + "InstaMine Threshold: " + Nuker.instaMineThreshold
                            , prefix + "On Ground: " + Nuker.onGround
                            , prefix + "Litematica " + Nuker.litematica
                    );

                } else if (message.startsWith("&&litematica")) {
                    message = message.replace("&&litematica", "").trim();
                    switch (message) {
                        case "" -> sendClientMessages(String.valueOf(Nuker.litematica));
                        case "true" -> {
                            Nuker.litematica = true;
                            sendClientMessages(prefix + "Litematica = true");
                        }
                        case "false" -> {
                            Nuker.litematica = false;
                            sendClientMessages(prefix + "litematica = false");
                        }
                    }

                } else if (message.equals("&&disable")) {
                    Nuker.enabled = false;
                    sendClientMessages(prefix + "Disabled");

                } else if (message.equals("&&enable")) {
                    Nuker.enabled = true;
                    sendClientMessages(prefix + "Enabled");

                } else if (message.startsWith("&&flattenmode")) {
                    message = message.replace("&&flattenmode", "").trim();
                    switch (message) {
                        case "" -> sendClientMessages(Nuker.FlattenMode.values());
                        case "none" -> {
                            Nuker.flattenMode = Nuker.FlattenMode.NONE;
                            sendClientMessages(prefix + "Flatten Mode = " + message);
                        }
                        case "standard" -> {
                            Nuker.flattenMode = Nuker.FlattenMode.STANDARD;
                            sendClientMessages(prefix + "Flatten Mode = " + message);
                        }
                        case "smart" -> {
                            Nuker.flattenMode = Nuker.FlattenMode.SMART;
                            sendClientMessages(prefix + "Flatten Mode = " + message);
                        }
                        case "reverse_smart" -> {
                            Nuker.flattenMode = Nuker.FlattenMode.REVERSE_SMART;
                            sendClientMessages(prefix + "Flatten Mode = " + message);
                        }
                    }

                } else if (message.startsWith("&&sortingmode")) {
                    message = message.replace("&&sortingmode", "").trim();
                    switch (message) {
                        case "" -> sendClientMessages(Nuker.MineSort.values());
                        case "closest" -> {
                            Nuker.mineSort = Nuker.MineSort.CLOSEST;
                            sendClientMessages(prefix + "Sorting Mode = " + message);
                        }
                        case "farthest" -> {
                            Nuker.mineSort = Nuker.MineSort.FARTHEST;
                            sendClientMessages(prefix + "Sorting Mode = " + message);
                        }
                        case "top_down" -> {
                            Nuker.mineSort = Nuker.MineSort.TOP_DOWN;
                            sendClientMessages(prefix + "Sorting Mode = " + message);
                        }
                        case "bottom_up" -> {
                            Nuker.mineSort = Nuker.MineSort.BOTTOM_UP;
                            sendClientMessages(prefix + "Sorting Mode = " + message);
                        }
                        case "random" -> {
                            Nuker.mineSort = Nuker.MineSort.RANDOM;
                            sendClientMessages(prefix + "Sorting Mode = " + message);
                        }
                    }

                } else if (message.startsWith("&&avoidliquids")) {
                    message = message.replace("&&avoidliquids", "").trim();
                    switch (message) {
                        case "" -> sendClientMessages(String.valueOf(Nuker.avoidLiquids));
                        case "true" -> {
                            Nuker.avoidLiquids = true;
                            sendClientMessages(prefix + "Avoid Liquids = true");
                        }
                        case "false" -> {
                            Nuker.avoidLiquids = false;
                            sendClientMessages(prefix + "Avoid Liquids = false");
                        }
                    }

                } else if (message.startsWith("&&packetlimit")) {
                    message = message.replace("&&packetlimit", "").trim();
                    if (message.isEmpty()) {
                        sendClientMessage(String.valueOf(Nuker.packetLimit));
                    } else if (isIntable(message)){
                        Nuker.packetLimit = Integer.parseInt(message);
                        sendClientMessage(prefix + "Packet Limit = " + message);
                    }

                } else if (message.startsWith("&&clientsidebreak")) {
                    message = message.replace("&&clientsidebreak", "").trim();
                    switch (message) {
                        case "" -> sendClientMessages(String.valueOf(Nuker.clientBreak));
                        case "true" -> {
                            Nuker.clientBreak = true;
                            sendClientMessages(prefix + "Client Side Break = True");
                        }
                        case "false" -> {
                            Nuker.clientBreak = false;
                            sendClientMessages(prefix + "Client Side Break = False");
                        }
                    }

                } else if (message.startsWith("&&radius")) {
                    message = message.replace("&&radius", "").trim();
                    if (message.isEmpty()) {
                        sendClientMessages(String.valueOf(Nuker.radius));
                    } else if (isIntable(message)) {
                        Nuker.radius = Integer.parseInt(message);
                        sendClientMessages(prefix + "Radius = " + message);
                    }

                } else if (message.startsWith("&&baritoneselectionmode")) {
                    message = message.replace("&&baritoneselectionmode", "").trim();
                    switch (message) {
                        case "" -> sendClientMessages(String.valueOf(Nuker.baritoneSelection));
                        case "true" -> {
                            Nuker.baritoneSelection = true;
                            sendClientMessages(prefix + "Baritone Selection Mode = true");
                        }
                        case "false" -> {
                            Nuker.baritoneSelection = false;
                            sendClientMessages(prefix + "Baritone Selection Mode = false");
                        }
                    }

                } else if (message.startsWith("&&clientbreakghostblocktimeout")) {
                    message = message.replace("&&clientbreakghostblocktimeout", "");
                    if (isIntable(message)) {
                        Nuker.clientBreakGhostBlockTimeout = Integer.parseInt(message);
                        sendClientMessage(prefix + "Client Break Ghost Block Timeout = " + message);
                    }

                } else if (message.startsWith("&&blocktimeoutdelay")) {
                    message = message.replace("&&blocktimeoutdelay", "");
                    if (isIntable(message)) {
                        Nuker.blockTimeoutDelay = Integer.parseInt(message);
                        sendClientMessage(prefix + "Block Timeout Delay = " + message);
                    }

                } else if (message.startsWith("&&instaminethreshold")) {
                    message = message.replace("&&instaminethreshold", "");
                    if (isIntable(message)) {
                        Nuker.instaMineThreshold = Integer.parseInt(message);
                        sendClientMessage(prefix + "InstaMine Threshold = " + message);
                    }

                } else if (message.startsWith("&&onground")) {
                    message = message.replace("&&onground", "").trim();
                    switch (message) {
                        case "" -> sendClientMessages(String.valueOf(Nuker.onGround));
                        case "true" -> {
                            Nuker.onGround = true;
                            sendClientMessages(prefix + "On Ground = true");
                        }
                        case "false" -> {
                            Nuker.onGround = false;
                            sendClientMessages(prefix + "On Ground = false");
                        }
                    }

                }
                return ActionResult.FAIL;
            } else {
                return ActionResult.PASS;
            }
        });
    }
    public static void sendClientMessage(String text) {
        mc.inGameHud.getChatHud().addMessage(Text.of(text));
    }
    public static void sendClientMessages(Object[] text) {
        sendClientMessage("======== Nuker ========");
        for (Object s : text) {
            sendClientMessage(s.toString());
        }
    }
    public static void sendClientMessages(String... text) {
        sendClientMessage("======== Nuker ========");
        for (Object s : text) {
            sendClientMessage(s.toString());
        }
    }
    private static boolean isIntable(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
