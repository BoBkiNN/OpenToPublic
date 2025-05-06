package xyz.bobkinn.opentopublic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.*;

public class Util {
    private static final Minecraft MC = Minecraft.getInstance();

    public static String parseValues(String text, String playerName, String worldName) {
        return text.replace("%owner%", playerName).replace("%world%", worldName).replace("&", "§");
    }

    public static Component translateYN(boolean bool) {
        return bool ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
    }

    public static Component parseYN(String key, boolean onlineMode) {
        return Component.translatable(key, Util.translateYN(onlineMode));
    }

    public static void addChatMsg(Component text) {
        MC.gui.getChat().addMessage(text);
    }

    @SuppressWarnings("unused")
    public static void addChatMsg(String text) {
        addChatMsg(Component.literal(text));
    }

    public static void atSuccessOpen(boolean successOpen) {
        MutableComponent successWAN;
        String ip = (OpenToPublic.upnpIp == null) ? "0.0.0.0" : OpenToPublic.upnpIp;
        if (!OpenToPublic.cfg.isHideIps()) {
            successWAN = Component.translatable("opentopublic.publish.started_wan", ip + ":" + OpenToPublic.customPort).withStyle((style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, ip + ":" + OpenToPublic.customPort)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.copy.click")))));
        } else {
            successWAN = Component.translatable("opentopublic.publish.started_wan_noIp", Integer.toString(OpenToPublic.customPort));
        }
        MutableComponent text;
        if (OpenToPublic.openedMode != null && OpenToPublic.openedMode != OpenMode.LAN)
            text = successOpen ? successWAN : Component.translatable("opentopublic.publish.failed_wan");
        else {
            text = successOpen ? Component.translatable("commands.publish.started", OpenToPublic.customPort) : Component.translatable("commands.publish.failed");
        }
        Util.addChatMsg(text);
    }

    public static void displayToast(Component title, Component desc) {
        MC.getToasts().addToast(SystemToast.multiline(MC, SystemToast.SystemToastIds.TUTORIAL_HINT, title, desc));
    }

}
