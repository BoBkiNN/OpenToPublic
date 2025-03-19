package xyz.bobkinn.opentopublic.mixin;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ServerChannel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.bobkinn.opentopublic.OpenToPublic;
import xyz.bobkinn.opentopublic.OpenedStatus;

import java.net.InetAddress;
import java.net.UnknownHostException;
import net.minecraft.server.network.ServerConnectionListener;

@Mixin(ServerConnectionListener.class)
public abstract class MixinServerNetworkIo {

    @Redirect(method = "startTcpServerListener", at = @At(value = "INVOKE", target = "Lio/netty/bootstrap/ServerBootstrap;localAddress(Ljava/net/InetAddress;I)Lio/netty/bootstrap/AbstractBootstrap;"))
    public AbstractBootstrap<ServerBootstrap, ServerChannel> onSetAddress(ServerBootstrap instance, InetAddress inetAddress, int port) {
        if (!OpenToPublic.lanOpening) {
            return instance.localAddress(inetAddress, port);
        }
        InetAddress bindAddress;
        if (OpenToPublic.openPublic.isTrue() || OpenToPublic.openPublic.isThird()) {
            if (OpenToPublic.openPublic.isTrue()) OpenedStatus.current = OpenedStatus.MANUAL;
            else OpenedStatus.current = OpenedStatus.UPNP;
            try {
                bindAddress = InetAddress.getByAddress(new byte[]{0, 0, 0, 0});
            } catch (UnknownHostException e) {
                throw new IllegalStateException("Exception creating InetAddress");
            }
        } else {
            OpenedStatus.current = OpenedStatus.LAN;
            bindAddress = inetAddress;
        }
        OpenToPublic.lanOpening = false;
        return instance.localAddress(bindAddress, port);
    }
}
