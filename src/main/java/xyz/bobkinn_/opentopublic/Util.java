package xyz.bobkinn_.opentopublic;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.WorldSavePath;

import java.nio.file.Path;

public class Util {
    static MinecraftClient mc = MinecraftClient.getInstance();
    public static Path savesFolder = MinecraftClient.getInstance().getLevelStorage().getSavesDirectory();

    public static TranslatableText on = new TranslatableText("options.on");
    public static TranslatableText off = new TranslatableText("options.off");

    /**
     * Get world folder name
     * @param world world
     * @return world folder name
     */
    public static String getLevelName(ServerWorld world){
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
    public static Path getWorldFolder(ServerWorld world){
        if (mc.getServer() == null) return null;
        return world.getServer().getSavePath(WorldSavePath.LEVEL_DAT).toAbsolutePath().getParent();
    }

    public static TranslatableText translateYN(boolean bool){
        return bool ? on : off;
    }

    public static TranslatableText parseYN(String key, boolean onlineMode) {
        return new TranslatableText(key,  Util.translateYN(onlineMode));
    }

    public static void addChatMsg(Text text){
        if (mc == null) return;
        mc.inGameHud.getChatHud().addMessage(text);
    }

    @SuppressWarnings("unused")
    public static void addChatMsg(String text){
        addChatMsg(new LiteralText(text));
    }

    public static void atSuccessOpen(boolean successOpen){
        TranslatableText successWAN;
        String ip = (OpenToPublic.upnpIp == null) ? "0.0.0.0" : OpenToPublic.upnpIp;
        if (!OpenToPublic.cfg.isHideIps()) {
            successWAN = new TranslatableText("opentopublic.publish.started_wan", ip + ":" + OpenToPublic.customPort);
        } else {
            successWAN = new TranslatableText("opentopublic.publish.started_wan_noIp", Integer.toString(OpenToPublic.customPort));
        }
        TranslatableText text;
        if (OpenToPublic.openPublic.isTrue() || OpenToPublic.openPublic.isThird()) text = successOpen ? successWAN : new TranslatableText("opentopublic.publish.failed_wan");
        else {
            text = successOpen ? new TranslatableText("commands.publish.started", OpenToPublic.customPort) : new TranslatableText("commands.publish.failed");
        }
        if (mc == null) return;
        Util.addChatMsg(text);
    }

    public static void displayToast(Text title, Text desc){
        mc.getToastManager().add(SystemToast.create(mc, SystemToast.Type.TUTORIAL_HINT, title, desc));
    }

}
