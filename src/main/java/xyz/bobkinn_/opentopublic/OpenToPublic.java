package xyz.bobkinn_.opentopublic;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OpenToPublic implements ModInitializer {
    public static boolean lanOpening = false;
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
//        Event<CommandRegistrationCallback> cmdRegEvent = CommandRegistrationCallback.EVENT;
//        cmdRegEvent.register(((dispatcher, dedicated) -> OpenToPublicCommand.register(dispatcher)));
        LOGGER.info("Initialized!");
    }
}
