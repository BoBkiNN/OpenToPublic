package xyz.bobkinn_.opentopublic;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OpenToPublic implements ModInitializer {
    public static boolean lanOpening = false;
    public static boolean openPublic = true;
    public static int customPort = 25565;
    public static boolean onlineMode = true;
    public static Logger LOGGER = LogManager.getLogger("OpenToPublic");

    public static GameProfile getWorldOwner(){
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.isIntegratedServerRunning()){
            return mc.getSession().getProfile();
        }
        return null;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Initialized!");
    }
}
