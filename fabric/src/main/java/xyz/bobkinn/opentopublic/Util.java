package xyz.bobkinn.opentopublic;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;

import java.nio.file.Path;

public class Util {
    static MinecraftClient mc = MinecraftClient.getInstance();
    public static Path savesFolder = MinecraftClient.getInstance().getLevelStorage().getSavesDirectory();

    public static final MutableText on = Text.translatable("options.on");
    public static final MutableText off = Text.translatable("options.off");

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

    public static MutableText translateYN(boolean bool){
        return bool ? on : off;
    }

    public static MutableText parseYN(String key, boolean onlineMode) {
        return Text.translatable(key,  Util.translateYN(onlineMode));
    }

    public static void addChatMsg(Text text){
        if (mc == null) return;
        mc.inGameHud.getChatHud().addMessage(text);
    }

    @SuppressWarnings("unused")
    public static void addChatMsg(String text){
        addChatMsg(Text.literal(text));
    }

    public static void atSuccessOpen(boolean successOpen){
        MutableText successWAN;
        String ip = (OpenToPublic.upnpIp == null) ? "0.0.0.0" : OpenToPublic.upnpIp;
        if (!OpenToPublic.cfg.isHideIps()) {
            successWAN = Text.translatable("opentopublic.publish.started_wan", ip + ":" + OpenToPublic.customPort)
                    .styled(
                            (style -> style.withClickEvent(
                                    new ClickEvent(
                                            ClickEvent.Action.COPY_TO_CLIPBOARD, ip + ":" + OpenToPublic.customPort
                                    ))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.copy.click")))
                            )
                    );
        } else {
            successWAN = Text.translatable("opentopublic.publish.started_wan_noIp", Integer.toString(OpenToPublic.customPort));
        }
        MutableText text;
        if (OpenToPublic.openPublic.isTrue() || OpenToPublic.openPublic.isThird()) text = successOpen ? successWAN : Text.translatable("opentopublic.publish.failed_wan");
        else {
            text = successOpen ? Text.translatable("commands.publish.started", OpenToPublic.customPort) : Text.translatable("commands.publish.failed");
        }
        if (mc == null) return;
        Util.addChatMsg(text);
    }

    public static void displayToast(Text title, Text desc){
        mc.getToastManager().add(SystemToast.create(mc, new SystemToast.Type(), title, desc));
    }

}
