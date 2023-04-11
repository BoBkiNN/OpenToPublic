package xyz.bobkinn_.opentopublic.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.SharedConstants;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bobkinn_.opentopublic.OpenToPublic;
import xyz.bobkinn_.opentopublic.OpenedStatus;
import xyz.bobkinn_.opentopublic.PortContainer;
import xyz.bobkinn_.opentopublic.upnp.UpnpThread;

import java.io.File;

@Mixin(Minecraft.class)
public abstract class MixinMinecraftClient {

    @Inject(method = "run", at = @At("HEAD"))
    public void run(CallbackInfo ci){
        OpenToPublic.modConfigPath = FMLPaths.CONFIGDIR.get().resolve(OpenToPublic.MOD_ID);
        OpenToPublic.backupFile = new File(OpenToPublic.modConfigPath.toFile(), "opened_ports.ser");
        PortContainer t = PortContainer.loadBackup(OpenToPublic.backupFile);
        PortContainer.self = t == null ? PortContainer.newEmpty() : t;
//        OpenToPublic.LOGGER.info("At client run container: " + PortContainer.self);
        if (!PortContainer.self.isEmpty()) {
            OpenToPublic.LOGGER.info("Closing opened ports from last session..");
            UpnpThread.runClose();
        }
        OpenToPublic.updateConfig(OpenToPublic.modConfigPath.resolve("config.json"));
    }

    @Shadow
    private @Nullable IntegratedServer integratedServer;

    @Shadow public abstract boolean isModdedClient();

    @Shadow public abstract boolean isConnectedToRealms();

    @Shadow @javax.annotation.Nullable public abstract ClientPlayNetHandler getConnection();

    /**
     * @author BoBkiNN_
     * @reason additional entries
     */
    @Overwrite
    private String getWindowTitle(){
        StringBuilder stringBuilder = new StringBuilder("Minecraft");
        if (this.isModdedClient()) {
            stringBuilder.append("*");
        }
        stringBuilder.append(" ");
        stringBuilder.append(SharedConstants.getVersion().getName());
        ClientPlayNetHandler clientPlayNetworkHandler = this.getConnection();
        if (clientPlayNetworkHandler != null && clientPlayNetworkHandler.getNetworkManager().isChannelOpen()) {
            stringBuilder.append(" - ");
            if (this.integratedServer != null && !this.integratedServer.getPublic() || OpenedStatus.current == null) {
                stringBuilder.append(I18n.format("title.singleplayer"));
            } else if (this.isConnectedToRealms()) {
                stringBuilder.append(I18n.format("title.multiplayer.realms"));
            } else if (OpenedStatus.current == OpenedStatus.UPNP) {
                stringBuilder.append(I18n.format("opentopublic.title.multiplayer.upnp"));
            } else if (OpenedStatus.current == OpenedStatus.MANUAL) {
                stringBuilder.append(I18n.format("opentopublic.title.multiplayer.wan"));
            } else if (OpenedStatus.current == OpenedStatus.LAN) {
                stringBuilder.append(I18n.format("title.multiplayer.lan"));
            } else {
                stringBuilder.append(I18n.format("title.multiplayer.other"));
            }
        }
        return stringBuilder.toString();
    }
}
