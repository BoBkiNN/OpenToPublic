package xyz.bobkinn_.opentopublic.mixin;

import net.minecraft.server.ServerNetworkIo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bobkinn_.opentopublic.OpenToPublic;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Mixin(ServerNetworkIo.class)
public abstract class MixinServerNetworkIo {
    @Inject(method = "bind", at = @At("HEAD"))
    public void onBind(InetAddress address, int port, CallbackInfo ci){
        if (OpenToPublic.lanOpening) {
            if (OpenToPublic.openPublic) {
                try {
                    address = InetAddress.getByName("0.0.0.0");
                } catch (UnknownHostException e) {
                    OpenToPublic.LOGGER.error(e);
                }
            }
            OpenToPublic.lanOpening = false;
        }
    }
}
