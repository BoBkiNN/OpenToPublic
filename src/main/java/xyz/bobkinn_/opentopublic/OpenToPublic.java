package xyz.bobkinn_.opentopublic;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OpenToPublic implements ModInitializer {
    public static boolean lanOpening = false;
    public static boolean openPublic = true;
    public static int customPort = 25565;
    public static boolean onlineMode = true;
    public static int maxPlayers = 8;

    public static Logger LOGGER = LogManager.getLogger("OpenToPublic");

    public static void checkFolder(){

    }

    @Override
    public void onInitialize() {
        LOGGER.info("Initialized!");
    }
}
