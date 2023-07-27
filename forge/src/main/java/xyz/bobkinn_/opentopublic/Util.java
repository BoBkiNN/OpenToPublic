package xyz.bobkinn_.opentopublic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.storage.FolderName;

import java.nio.file.Path;

public class Util {
    static Minecraft mc = Minecraft.getInstance();
    public static Path savesFolder = mc.gameDir.toPath().resolve("saves");

    public static TranslationTextComponent on = new TranslationTextComponent("options.on");
    public static TranslationTextComponent off = new TranslationTextComponent("options.off");

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
        if (mc.getIntegratedServer() == null) return null;
        return world.getServer().func_240776_a_(FolderName.LEVEL_DAT).toAbsolutePath().getParent();
    }

    public static TranslationTextComponent translateYN(boolean bool){
        return bool ? on : off;
    }

    public static TranslationTextComponent parseYN(String key, boolean onlineMode) {
        return new TranslationTextComponent(key,  Util.translateYN(onlineMode));
    }

    public static void addChatMsg(ITextComponent text){
        if (mc == null) return;
        mc.ingameGUI.getChatGUI().printChatMessage(text);
    }

    @SuppressWarnings("unused")
    public static void addChatMsg(String text){
        addChatMsg(new StringTextComponent(text));
    }

    public static void atSuccessOpen(boolean successOpen){
        IFormattableTextComponent successWAN;
        String ip = (OpenToPublic.upnpIp == null) ? "0.0.0.0" : OpenToPublic.upnpIp;
        if (!OpenToPublic.cfg.isHideIps()) {
            successWAN = new TranslationTextComponent("opentopublic.publish.started_wan", ip + ":" + OpenToPublic.customPort)
                    .modifyStyle((style -> style.setClickEvent(new ClickEvent(
                            ClickEvent.Action.COPY_TO_CLIPBOARD, ip+":"+OpenToPublic.customPort))
                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("chat.copy.click"))
                    )));
        } else {
            successWAN = new TranslationTextComponent("opentopublic.publish.started_wan_noIp", Integer.toString(OpenToPublic.customPort));
        }
        IFormattableTextComponent text;
        if (OpenToPublic.openPublic.isTrue() || OpenToPublic.openPublic.isThird()) text = successOpen ? successWAN : new TranslationTextComponent("opentopublic.publish.failed_wan");
        else {
            text = successOpen ? new TranslationTextComponent("commands.publish.started", OpenToPublic.customPort) : new TranslationTextComponent("commands.publish.failed");
        }
        if (mc == null) return;
        Util.addChatMsg(text);
    }

    public static void displayToast(ITextComponent title, ITextComponent desc){
        mc.getToastGui().add(SystemToast.func_238534_a_(mc, SystemToast.Type.TUTORIAL_HINT, title, desc));
    }

}
