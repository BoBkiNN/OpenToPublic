package xyz.bobkinn_.opentopublic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;

public class Util {
    static Minecraft mc = Minecraft.getInstance();
    public static Path savesFolder = mc.gameDirectory.toPath().resolve("saves");

    public static MutableComponent on = Component.translatable("options.on");
    public static MutableComponent off = Component.translatable("options.off");

    /**
     * Get world folder name
     * @param world world
     * @return world folder name
     */
    public static String getLevelName(ServerLevel world){
        Path folder = getWorldFolder(world);
        if (folder == null) return "";
        return folder.getName(folder.getNameCount()-1).toString();
    }

    public static String parseValues(String text, String playerName, String worldName){
        return text.replace("%owner%",playerName).replace("%world%", worldName).replace("&", "§");
    }

    /**
     * Get world folder
     * @param world integrated server world
     * @return folder
     */
    public static Path getWorldFolder(ServerLevel world){
        if (mc.getSingleplayerServer() == null) return null;
        return world.getServer().getWorldPath(LevelResource.LEVEL_DATA_FILE).toAbsolutePath().getParent();
    }

    public static MutableComponent translateYN(boolean bool){
        return bool ? on : off;
    }

    public static MutableComponent parseYN(String key, boolean onlineMode) {
        return Component.translatable(key,  Util.translateYN(onlineMode));
    }

    public static void addChatMsg(Component text){
        if (mc == null) return;
        mc.gui.getChat().addMessage(text);
    }

    @SuppressWarnings("unused")
    public static void addChatMsg(String text){
        addChatMsg(Component.literal(text));
    }

    public static void atSuccessOpen(boolean successOpen){
        MutableComponent successWAN;
        String ip = (OpenToPublic.upnpIp == null) ? "0.0.0.0" : OpenToPublic.upnpIp;
        if (!OpenToPublic.cfg.isHideIps()) {
            successWAN = Component.translatable("opentopublic.publish.started_wan", ip + ":" + OpenToPublic.customPort)
                    .withStyle(
                            (style -> style.withClickEvent(
                                    new ClickEvent(
                                            ClickEvent.Action.COPY_TO_CLIPBOARD, ip + ":" + OpenToPublic.customPort
                                    ))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.copy.click")))
                            )
                    );
        } else {
            successWAN = Component.translatable("opentopublic.publish.started_wan_noIp", Integer.toString(OpenToPublic.customPort));
        }
        MutableComponent text;
        if (OpenToPublic.openPublic.isTrue() || OpenToPublic.openPublic.isThird()) text = successOpen ? successWAN : Component.translatable("opentopublic.publish.failed_wan");
        else {
            text = successOpen ? Component.translatable("commands.publish.started", OpenToPublic.customPort) : Component.translatable("commands.publish.failed");
        }
        if (mc == null) return;
        Util.addChatMsg(text);
    }

    public static void displayToast(Component title, Component desc){
        mc.getToasts().addToast(SystemToast.multiline(mc, SystemToast.SystemToastIds.TUTORIAL_HINT, title, desc));
    }

}
