package xyz.bobkinn_.opentopublic.mixin;

import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bobkinn_.opentopublic.OpenToPublic;

@Mixin(IntegratedServer.class)
public abstract class MixinIntegratedServer {
    @Inject(method = "openToLan", at = @At("HEAD"))
    private void onLanStart(GameMode gameMode, boolean cheatsAllowed, int port, CallbackInfoReturnable<Boolean> cir) {
        OpenToPublic.lanOpening = true;
    }
}

