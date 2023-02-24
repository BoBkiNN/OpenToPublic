package xyz.bobkinn_.opentopublic;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.WorldSavePath;

import java.nio.file.Path;

public class Util {
    public static Path savesFolder = MinecraftClient.getInstance().getLevelStorage().getSavesDirectory();

    static TranslatableText yes = new TranslatableText("options.on");
    static TranslatableText no = new TranslatableText("options.off");

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
        if (MinecraftClient.getInstance().getServer() == null) return null;
        return world.getServer().getSavePath(WorldSavePath.LEVEL_DAT).toAbsolutePath().getParent();
    }

    public static TranslatableText translateYN(boolean bool){
        return bool ? yes : no;
    }

    public static TranslatableText parseYN(String key, boolean onlineMode) {
        return new TranslatableText(key,  Util.translateYN(onlineMode));
    }
}
