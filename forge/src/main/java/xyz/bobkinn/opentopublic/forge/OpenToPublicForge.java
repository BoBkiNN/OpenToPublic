package xyz.bobkinn.opentopublic.forge;

import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import xyz.bobkinn.opentopublic.OpenToPublic;

import java.io.File;
import java.nio.file.Path;

@Mod("opentopublic")
public class OpenToPublicForge extends OpenToPublic {
    public OpenToPublicForge(FMLJavaModLoadingContext context) {
        context.registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> IExtensionPoint.DisplayTest.IGNORESERVERONLY, (a, b) -> true));
    }

    @Override
    public Path getConfigsFolder() {
        return new File("config").getAbsoluteFile().toPath();
    }
}
