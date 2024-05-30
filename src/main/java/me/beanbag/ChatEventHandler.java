package me.beanbag;

import me.beanbag.Nuker;
import me.beanbag.events.PacketSendCallback;
import me.beanbag.settings.FlattenMode;
import me.beanbag.settings.MineSort;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.util.ActionResult;

import static me.beanbag.utils.ChatUtils.*;

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
                    sendHeader();
                    sendClientMessages(
                            prefix + "Sorting Mode: " + Nuker.mineSort
                            , prefix + "Flatten Mode: " + Nuker.flattenMode
                            , prefix + "Packet Limit: " + Nuker.packetLimit
                            , prefix + "Client Side Break: " + Nuker.clientBreak
                            , prefix + "Radius: " + Nuker.radius
                            , prefix + "Baritone Selection mode: " + Nuker.baritoneSelection
                            , prefix + "Client Break Ghost Block Timeout: " + Nuker.clientBreakGhostBlockTimeout
                            , prefix + "Block Timeout Delay: " + Nuker.blockTimeoutDelay
                            , prefix + "Place Block Timeout Delay: " + Nuker.placeBlockTimeoutDelay
                            , prefix + "InstaMine Threshold: " + Nuker.instaMineThreshold
                            , prefix + "On Ground: " + Nuker.onGround
                            , prefix + "Litematica: " + Nuker.litematica
                            , prefix + "Source Remover: " + Nuker.sourceRemover
                            , prefix + "Expand Baritone Selections For Liquids: " + Nuker.expandBaritoneSelectionsForLiquids
                            , prefix + "Canal Mode: " + Nuker.canalMode
                            , prefix + "Packet Place: " + Nuker.packetPlace
                            , prefix + "Place Rotate Place: " + Nuker.placeRotatePlace
                            , prefix + "Prevent Sprinting In Water: " + Nuker.preventSprintingInWater
                            , prefix + "Crouch Lower Flatten: " + Nuker.crouchLowerFlatten
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
                        case "" -> sendClientMessages(FlattenMode.values());
                        case "none" -> {
                            Nuker.flattenMode = FlattenMode.NONE;
                            sendClientMessages( prefix + "Flatten Mode = " + message);
                        }
                        case "standard" -> {
                            Nuker.flattenMode = FlattenMode.STANDARD;
                            sendClientMessages(prefix + "Flatten Mode = " + message);
                        }
                        case "smart" -> {
                            Nuker.flattenMode = FlattenMode.SMART;
                            sendClientMessages(prefix + "Flatten Mode = " + message);
                        }
                        case "reverse_smart" -> {
                            Nuker.flattenMode = FlattenMode.REVERSE_SMART;
                            sendClientMessages(prefix + "Flatten Mode = " + message);
                        }
                    }

                } else if (message.startsWith("&&sortingmode")) {
                    message = message.replace("&&sortingmode", "").trim();
                    switch (message) {
                        case "" -> sendClientMessages(MineSort.values());
                        case "closest" -> {
                            Nuker.mineSort = MineSort.CLOSEST;
                            sendClientMessages(prefix + "Sorting Mode = " + message);
                        }
                        case "farthest" -> {
                            Nuker.mineSort = MineSort.FARTHEST;
                            sendClientMessages(prefix + "Sorting Mode = " + message);
                        }
                        case "top_down" -> {
                            Nuker.mineSort = MineSort.TOP_DOWN;
                            sendClientMessages(prefix + "Sorting Mode = " + message);
                        }
                        case "bottom_up" -> {
                            Nuker.mineSort = MineSort.BOTTOM_UP;
                            sendClientMessages(prefix + "Sorting Mode = " + message);
                        }
                        case "random" -> {
                            Nuker.mineSort = MineSort.RANDOM;
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
                    message = message.replace("&&clientbreakghostblocktimeout", "").trim();
                    if (isIntable(message)) {
                        Nuker.clientBreakGhostBlockTimeout = Integer.parseInt(message);
                        sendClientMessage(prefix + "Client Break Ghost Block Timeout = " + message);
                    }

                } else if (message.startsWith("&&blocktimeoutdelay")) {
                    message = message.replace("&&blocktimeoutdelay", "").trim();
                    if (isIntable(message)) {
                        Nuker.blockTimeoutDelay = Integer.parseInt(message);
                        sendClientMessage(prefix + "Block Timeout Delay = " + message);
                    }

                } else if (message.startsWith("&&placeblocktimeoutdelay")) {
                    message = message.replace("&&placeblocktimeoutdelay", "").trim();
                    if (isIntable(message)) {
                        Nuker.placeBlockTimeoutDelay = Integer.parseInt(message);
                        sendClientMessage(prefix + "Place Block Timeout Delay = " + message);
                    }

                } else if (message.startsWith("&&instaminethreshold")) {
                    message = message.replace("&&instaminethreshold", "").trim();
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

                } else if (message.startsWith("&&sourceremover")) {
                    message = message.replace("&&sourceremover", "").trim();
                    switch (message) {
                        case "" -> sendClientMessages(String.valueOf(Nuker.sourceRemover));
                        case "true" -> {
                            Nuker.sourceRemover = true;
                            sendClientMessages(prefix + "Source Remover = true");
                        }
                        case "false" -> {
                            Nuker.sourceRemover = false;
                            sendClientMessages(prefix + "Source Remover = false");
                        }
                    }

                } else if (message.startsWith("&&expandbaritoneselectionsforliquids")) {
                    message = message.replace("&&expandbaritoneselectionsforliquids", "").trim();
                    switch (message) {
                        case "" -> sendClientMessages(String.valueOf(Nuker.expandBaritoneSelectionsForLiquids));
                        case "true" -> {
                            Nuker.expandBaritoneSelectionsForLiquids = true;
                            sendClientMessages(prefix + "Expand Baritone Selections For Liquids = true");
                        }
                        case "false" -> {
                            Nuker.expandBaritoneSelectionsForLiquids = false;
                            sendClientMessages(prefix + "Expand Baritone Selections For Liquids = false");
                        }
                    }

                } else if (message.startsWith("&&canalmode")) {
                    message = message.replace("&&canalmode", "").trim();
                    switch (message) {
                        case "" -> sendClientMessages(String.valueOf(Nuker.canalMode));
                        case "true" -> {
                            Nuker.canalMode = true;
                            sendClientMessages(prefix + "Canal Mode = true");
                        }
                        case "false" -> {
                            Nuker.canalMode = false;
                            sendClientMessages(prefix + "Canal Mode = false");
                        }
                    }

                } else if (message.startsWith("&&packetplace")) {
                    message = message.replace("&&packetplace", "").trim();
                    switch (message) {
                        case "" -> sendClientMessages(String.valueOf(Nuker.packetPlace));
                        case "true" -> {
                            Nuker.packetPlace = true;
                            sendClientMessages(prefix + "Packet Place = true");
                        }
                        case "false" -> {
                            Nuker.packetPlace = false;
                            sendClientMessages(prefix + "Packet Place = false");
                        }
                    }

                } else if (message.startsWith("&&placerotateplace")) {
                    message = message.replace("&&placerotateplace", "").trim();
                    switch (message) {
                        case "" -> sendClientMessages(String.valueOf(Nuker.placeRotatePlace));
                        case "true" -> {
                            Nuker.placeRotatePlace = true;
                            sendClientMessages(prefix + "Place Rotate Place = true");
                        }
                        case "false" -> {
                            Nuker.placeRotatePlace = false;
                            sendClientMessages(prefix + "Place Rotate Place = false");
                        }
                    }

                } else if (message.startsWith("&&preventsprintinginwater")) {
                    message = message.replace("&&preventsprintinginwater", "").trim();
                    switch (message) {
                        case "" -> sendClientMessages(String.valueOf(Nuker.preventSprintingInWater));
                        case "true" -> {
                            Nuker.preventSprintingInWater = true;
                            sendClientMessages(prefix + "Prevent Sprinting In Water = true");
                        }
                        case "false" -> {
                            Nuker.preventSprintingInWater = false;
                            sendClientMessages(prefix + "Prevent Sprinting In Water = false");
                        }
                    }

                } else if (message.startsWith("&&crouchlowerflatten")) {
                    message = message.replace("&&crouchlowerflatten", "").trim();
                    switch (message) {
                        case "" -> sendClientMessages(String.valueOf(Nuker.crouchLowerFlatten));
                        case "true" -> {
                            Nuker.crouchLowerFlatten = true;
                            sendClientMessages(prefix + "Crouch Lower Flatten = true");
                        }
                        case "false" -> {
                            Nuker.crouchLowerFlatten = false;
                            sendClientMessages(prefix + "Crouch Lower Flatten = false");
                        }
                    }

                }

                return ActionResult.FAIL;
            } else {
                return ActionResult.PASS;
            }
        });
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
