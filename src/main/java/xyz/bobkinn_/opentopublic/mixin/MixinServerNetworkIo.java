package xyz.bobkinn_.opentopublic.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.ServerNetworkIo;
import net.minecraft.text.LiteralText;
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
            if (MinecraftClient.getInstance().getServer() != null) {
                OpenToPublic.LOGGER.info("Bind: " + (address==null ? "localhost" : address.getHostAddress())+":"+port);
                if (MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.sendMessage(new LiteralText("Bind: " + MinecraftClient.getInstance().getServer().getServerIp()+":"+port),false);
                }
            }
            OpenToPublic.lanOpening = false;
        }
    }
}
