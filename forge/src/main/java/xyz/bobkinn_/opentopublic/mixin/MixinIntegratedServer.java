package xyz.bobkinn_.opentopublic.mixin;

import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bobkinn_.opentopublic.OpenToPublic;
import xyz.bobkinn_.opentopublic.OpenedStatus;
import xyz.bobkinn_.opentopublic.upnp.UpnpThread;

@Mixin(IntegratedServer.class)
public abstract class MixinIntegratedServer {
    @Inject(method = "publishServer", at = @At("HEAD"))
    private void onLanStart(GameType p_120041_, boolean p_120042_, int p_120043_, CallbackInfoReturnable<Boolean> cir) {
        OpenToPublic.lanOpening = true;
    }

    @Inject(method = "initServer", at = @At("HEAD"))
    private void onStart(CallbackInfoReturnable<Boolean> cir){
        OpenToPublic.serverStopped = false;
    }

    @Inject(at = @At("HEAD"), method = "halt")
    private void atServerStop(boolean bl, CallbackInfo ci) {
        OpenedStatus.current = null;
        OpenToPublic.upnpIp = null;
        OpenToPublic.serverStopped = true;
        if (OpenToPublic.openPublic.isThird()) UpnpThread.runClose();
    }
}

