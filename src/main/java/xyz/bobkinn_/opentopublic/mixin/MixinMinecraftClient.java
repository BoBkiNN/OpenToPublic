package xyz.bobkinn_.opentopublic.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.server.integrated.IntegratedServer;
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

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Inject(method = "run", at = @At("HEAD"))
    public void run(CallbackInfo ci){
        OpenToPublic.modConfigPath = FabricLoader.getInstance().getConfigDir().resolve(OpenToPublic.MOD_ID);
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
    private @Nullable IntegratedServer server;

    @Shadow public abstract boolean isModded();

    @Shadow public abstract boolean isConnectedToRealms();

    @Shadow public abstract @Nullable ClientPlayNetworkHandler getNetworkHandler();

    /**
     * @author BoBkiNN_
     * @reason additional entries
     */
    @Overwrite
    private String getWindowTitle(){
        StringBuilder stringBuilder = new StringBuilder("Minecraft");
        if (this.isModded()) {
            stringBuilder.append("*");
        }
        stringBuilder.append(" ");
        stringBuilder.append(SharedConstants.getGameVersion().getName());
        ClientPlayNetworkHandler clientPlayNetworkHandler = this.getNetworkHandler();
        if (clientPlayNetworkHandler != null && clientPlayNetworkHandler.getConnection().isOpen()) {
            stringBuilder.append(" - ");
            if (this.server != null && !this.server.isRemote() || OpenedStatus.current == null) {
                stringBuilder.append(I18n.translate("title.singleplayer"));
            } else if (this.isConnectedToRealms()) {
                stringBuilder.append(I18n.translate("title.multiplayer.realms"));
            } else if (OpenedStatus.current == OpenedStatus.UPNP) {
                stringBuilder.append(I18n.translate("opentopublic.title.multiplayer.upnp"));
            } else if (OpenedStatus.current == OpenedStatus.MANUAL) {
                stringBuilder.append(I18n.translate("opentopublic.title.multiplayer.wan"));
            } else if (OpenedStatus.current == OpenedStatus.LAN) {
                stringBuilder.append(I18n.translate("title.multiplayer.lan"));
            } else {
                stringBuilder.append(I18n.translate("title.multiplayer.other"));
            }
        }
        return stringBuilder.toString();
    }
}
