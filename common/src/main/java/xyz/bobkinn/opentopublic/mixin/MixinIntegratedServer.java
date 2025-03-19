package xyz.bobkinn.opentopublic.mixin;

import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bobkinn.opentopublic.OpenToPublic;
import xyz.bobkinn.opentopublic.OpenedStatus;
import xyz.bobkinn.opentopublic.upnp.UpnpThread;

@Mixin(IntegratedServer.class)
public abstract class MixinIntegratedServer {

    @Inject(method = "publishServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerConnectionListener;startTcpServerListener(Ljava/net/InetAddress;I)V"))
    private void onLanStart(GameType gameMode, boolean cheatsAllowed, int port, CallbackInfoReturnable<Boolean> cir) {
        OpenToPublic.lanOpening = true;
    }

    @Inject(method = "initServer", at = @At("HEAD"))
    private void onStart(CallbackInfoReturnable<Boolean> cir){
        OpenToPublic.serverStopped = false;
    }

    @Inject(at = @At("HEAD"), method = "halt")
    private void atServerStop(boolean bl, CallbackInfo ci) {
        OpenedStatus.current = null;
        var wasOpen = OpenToPublic.upnpIp != null;
        OpenToPublic.upnpIp = null;
        OpenToPublic.serverStopped = true;
        if (OpenToPublic.openPublic.isThird() && wasOpen) UpnpThread.runClose();
    }
}

