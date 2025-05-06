package xyz.bobkinn.opentopublic.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import xyz.bobkinn.opentopublic.OpenToPublic;

import java.nio.file.Path;

public class OpenToPublicFabric extends OpenToPublic implements ModInitializer {
    @Override
    public void onInitialize() {

    }

    @Override
    public Path getConfigsFolder() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
