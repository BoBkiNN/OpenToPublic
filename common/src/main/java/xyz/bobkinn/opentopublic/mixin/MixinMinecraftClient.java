package xyz.bobkinn.opentopublic.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.server.IntegratedServer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bobkinn.opentopublic.OpenedStatus;
import xyz.bobkinn.opentopublic.PortContainer;
import xyz.bobkinn.opentopublic.OpenToPublic;
import xyz.bobkinn.opentopublic.upnp.UpnpThread;
import com.mojang.blaze3d.platform.Window;
import java.io.File;

@Mixin(Minecraft.class)
public abstract class MixinMinecraftClient {

    @Inject(method = "run", at = @At("HEAD"))
    public void run(CallbackInfo ci){
        if (OpenToPublic.modConfigPath == null) {
            OpenToPublic.LOGGER.error("Failed to get modConfigPath for loading");
            return;
        }
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
    private @Nullable IntegratedServer singleplayerServer;

    @Shadow public abstract @Nullable ClientPacketListener getConnection();

    @Shadow @Final private Window window;

    @Shadow @Nullable public abstract ServerData getCurrentServer();

    @Inject(method = "updateTitle", at = @At("RETURN"))
    public void onUpdateWindowTitle(CallbackInfo ci){
        this.window.setTitle(openToPublic$getTitle());
    }

    @Unique
    public String openToPublic$getTitle(){
        StringBuilder stringBuilder = new StringBuilder("Minecraft");
        if (Minecraft.checkModStatus().shouldReportAsModified()) {
            stringBuilder.append("*");
        }
        stringBuilder.append(" ");
        stringBuilder.append(SharedConstants.getCurrentVersion().getName());
        ClientPacketListener clientPlayNetworkHandler = this.getConnection();
        if (clientPlayNetworkHandler != null && clientPlayNetworkHandler.getConnection().isConnected()) {
            stringBuilder.append(" - ");
            var info = this.getCurrentServer();
            if (this.singleplayerServer != null && !this.singleplayerServer.isPublished() || OpenedStatus.current == null) {
                stringBuilder.append(I18n.get("title.singleplayer"));
            } else if (info != null && info.isRealm()) {
                stringBuilder.append(I18n.get("title.multiplayer.realms"));
            } else if (OpenedStatus.current == OpenedStatus.UPNP) {
                stringBuilder.append(I18n.get("opentopublic.title.multiplayer.upnp"));
            } else if (OpenedStatus.current == OpenedStatus.MANUAL) {
                stringBuilder.append(I18n.get("opentopublic.title.multiplayer.wan"));
            } else if (OpenedStatus.current == OpenedStatus.LAN) {
                stringBuilder.append(I18n.get("title.multiplayer.lan"));
            } else {
                stringBuilder.append(I18n.get("title.multiplayer.other"));
            }
        }
        return stringBuilder.toString();
    }
}
