package xyz.bobkinn.opentopublic.mixin;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
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
import xyz.bobkinn.opentopublic.OpenMode;
import xyz.bobkinn.opentopublic.OpenToPublic;
import xyz.bobkinn.opentopublic.PortContainer;
import xyz.bobkinn.opentopublic.upnp.UpnpThread;

import java.io.File;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow
    private @Nullable IntegratedServer singleplayerServer;
    @Shadow
    @Final
    private Window window;

    @Inject(method = "run", at = @At("HEAD"))
    public void run(CallbackInfo ci) {
        if (OpenToPublic.modConfigPath == null) {
            OpenToPublic.LOGGER.error("Failed to get modConfigPath for loading");
            return;
        }
        OpenToPublic.backupFile = new File(OpenToPublic.modConfigPath.toFile(), "opened_ports.ser");
        PortContainer t = PortContainer.loadBackup(OpenToPublic.backupFile);
        PortContainer.INSTANCE = t == null ? PortContainer.newEmpty() : t;
//        OpenToPublic.LOGGER.info("At client run container: " + PortContainer.self);
        if (!PortContainer.INSTANCE.isEmpty()) {
            OpenToPublic.LOGGER.info("Closing opened ports from last session..");
            UpnpThread.runClose();
        }
        OpenToPublic.updateConfig(OpenToPublic.modConfigPath.resolve("config.json"));
    }

    @Shadow
    public abstract @Nullable ClientPacketListener getConnection();

    @Shadow public abstract boolean isConnectedToRealms();

    @Inject(method = "updateTitle", at = @At("RETURN"))
    public void onUpdateWindowTitle(CallbackInfo ci) {
        this.window.setTitle(openToPublic$getTitle());
    }

    @Unique
    public String openToPublic$getTitle() {
        StringBuilder stringBuilder = new StringBuilder("Minecraft");
        if (Minecraft.checkModStatus().shouldReportAsModified()) {
            stringBuilder.append("*");
        }
        stringBuilder.append(" ");
        stringBuilder.append(SharedConstants.getCurrentVersion().getName());
        ClientPacketListener clientPlayNetworkHandler = this.getConnection();
        if (clientPlayNetworkHandler != null && clientPlayNetworkHandler.getConnection().isConnected()) {
            stringBuilder.append(" - ");
            if (this.singleplayerServer != null && !this.singleplayerServer.isPublished() || OpenToPublic.openedMode == null) {
                stringBuilder.append(I18n.get("title.singleplayer"));
            } else if (isConnectedToRealms()) {
                stringBuilder.append(I18n.get("title.multiplayer.realms"));
            } else if (OpenToPublic.openedMode == OpenMode.UPNP) {
                stringBuilder.append(I18n.get("opentopublic.title.multiplayer.upnp"));
            } else if (OpenToPublic.openedMode == OpenMode.MANUAL) {
                stringBuilder.append(I18n.get("opentopublic.title.multiplayer.wan"));
            } else if (OpenToPublic.openedMode == OpenMode.LAN) {
                stringBuilder.append(I18n.get("title.multiplayer.lan"));
            } else {
                stringBuilder.append(I18n.get("title.multiplayer.other"));
            }
        }
        return stringBuilder.toString();
    }
}
