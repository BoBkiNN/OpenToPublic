package xyz.bobkinn.opentopublic.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bobkinn.opentopublic.OpenMode;
import xyz.bobkinn.opentopublic.OpenToPublic;
import xyz.bobkinn.opentopublic.PortContainer;
import xyz.bobkinn.opentopublic.upnp.UpnpThread;

import java.io.File;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

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

    // we don't use ordinal here because some other mods can add other calls
    @Redirect(method = "createTitle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/language/I18n;get(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"))
    public String onWindowTitleLanBranch(String key, Object[] args) {
        var enabled = OpenToPublic.cfg != null && OpenToPublic.cfg.isChangeWindowTitle();
        if (!enabled || !key.equals("title.multiplayer.lan")) { // process other calls as usual
            return I18n.get(key, args);
        }
        // now overriding only lan
        if (OpenToPublic.openedMode == OpenMode.UPNP) {
            return I18n.get("opentopublic.title.multiplayer.upnp");
        } else if (OpenToPublic.openedMode == OpenMode.MANUAL) {
            return I18n.get("opentopublic.title.multiplayer.wan");
        } else {
            return I18n.get("title.multiplayer.lan");
        }
    }

}
