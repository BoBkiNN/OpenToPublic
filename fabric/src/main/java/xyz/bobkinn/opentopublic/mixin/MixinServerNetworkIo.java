package xyz.bobkinn.opentopublic.mixin;

import net.minecraft.server.ServerNetworkIo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bobkinn.opentopublic.OpenToPublic;
import xyz.bobkinn.opentopublic.OpenedStatus;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Mixin(ServerNetworkIo.class)
public abstract class MixinServerNetworkIo {
    @Inject(method = "bind", at = @At("HEAD"))
    public void onBind(InetAddress address, int port, CallbackInfo ci){
        if (OpenToPublic.lanOpening) {
            if (OpenToPublic.openPublic.isTrue() || OpenToPublic.openPublic.isThird()) {
                OpenToPublic.isLan = false;
                if (OpenToPublic.openPublic.isTrue()) OpenedStatus.current = OpenedStatus.MANUAL;
                else OpenedStatus.current = OpenedStatus.UPNP;
                try {
                    //noinspection UnusedAssignment
                    address = InetAddress.getByName("0.0.0.0");
                } catch (UnknownHostException e) {
                    OpenToPublic.LOGGER.error(e);
                }
            } else {
                OpenedStatus.current = OpenedStatus.LAN;
                OpenToPublic.isLan = true;
            }
            OpenToPublic.lanOpening = false;
        }
    }
}
