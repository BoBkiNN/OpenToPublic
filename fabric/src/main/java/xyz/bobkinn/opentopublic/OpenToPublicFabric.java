package xyz.bobkinn.opentopublic;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

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
