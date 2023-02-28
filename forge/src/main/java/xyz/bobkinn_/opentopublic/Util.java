package xyz.bobkinn_.opentopublic;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.FolderName;

import java.nio.file.Path;

public class Util {
    public static Path savesFolder = Minecraft.getInstance().gameDir.toPath().resolve("saves");

    static TranslationTextComponent yes = new TranslationTextComponent("options.on");
    static TranslationTextComponent no = new TranslationTextComponent("options.off");

    /**
     * Get world folder name
     * @param world world
     * @return world folder name
     */
    public static String getLevelName(ServerWorld world){
        Path folder = getWorldFolder(world);
        if (folder == null) return "";
//        OpenToPublic.LOGGER.info(folder +" "+folder.getFileName());
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
        if (Minecraft.getInstance().getIntegratedServer() == null) return null;
        return world.getServer().func_240776_a_(FolderName.LEVEL_DAT).toAbsolutePath().getParent();
    }

    public static TranslationTextComponent translateYN(boolean bool){
        return bool ? yes : no;
    }

    public static TranslationTextComponent parseYN(String key, boolean onlineMode) {
        return new TranslationTextComponent(key,  Util.translateYN(onlineMode));
    }
}
