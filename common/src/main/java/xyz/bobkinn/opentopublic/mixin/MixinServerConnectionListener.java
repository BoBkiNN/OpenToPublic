package xyz.bobkinn.opentopublic.mixin;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ServerChannel;
import net.minecraft.server.network.ServerConnectionListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.bobkinn.opentopublic.OpenMode;
import xyz.bobkinn.opentopublic.OpenToPublic;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Mixin(ServerConnectionListener.class)
public abstract class MixinServerConnectionListener {

    @Redirect(method = "startTcpServerListener", at = @At(value = "INVOKE", remap = false,
            target = "Lio/netty/bootstrap/ServerBootstrap;localAddress(Ljava/net/InetAddress;I)Lio/netty/bootstrap/AbstractBootstrap;"))
    public AbstractBootstrap<ServerBootstrap, ServerChannel> onSetAddress(ServerBootstrap instance, InetAddress inetAddress, int port) {
        if (!OpenToPublic.lanOpening) {
            return instance.localAddress(inetAddress, port);
        }
        InetAddress bindAddress;

        var sMode = OpenToPublic.selectedMode;
        if (sMode == OpenMode.MANUAL || sMode == OpenMode.UPNP) {
            try {
                bindAddress = InetAddress.getByAddress(new byte[]{0, 0, 0, 0});
            } catch (UnknownHostException e) {
                throw new IllegalStateException("Exception creating InetAddress");
            }
        } else {
            bindAddress = inetAddress;
        }
        OpenToPublic.openedMode = sMode;
        OpenToPublic.lanOpening = false;
        return instance.localAddress(bindAddress, port);
    }
}
