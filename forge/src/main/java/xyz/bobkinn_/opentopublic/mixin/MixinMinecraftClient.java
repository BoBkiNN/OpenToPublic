package xyz.bobkinn_.opentopublic.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.server.IntegratedServer;
import net.minecraftforge.fml.loading.FMLPaths;
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

import javax.annotation.Nullable;
import java.io.File;

import static net.minecraft.client.Minecraft.checkModStatus;

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

    @Shadow @javax.annotation.Nullable public abstract ClientPacketListener getConnection();

    @Shadow @Nullable private IntegratedServer singleplayerServer;

    @Shadow @Nullable public abstract ServerData getCurrentServer();

    /**
     * @author BoBkiNN_
     * @reason additional entries
     */
    @Overwrite
    private String createTitle(){
        StringBuilder stringBuilder = new StringBuilder("Minecraft");
        if (checkModStatus().shouldReportAsModified()) {
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
