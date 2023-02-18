package xyz.bobkinn_.opentopublic.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.NetworkUtils;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bobkinn_.opentopublic.OpenToPublic;

@Mixin(OpenToLanScreen.class)
public abstract class MixinLanServerScreen extends Screen {

    @Shadow protected abstract void updateButtonText();

    ButtonWidget openToWan = null;
    TranslatableText yes = new TranslatableText("gui.yes");
    TranslatableText no = new TranslatableText("gui.no");

    protected MixinLanServerScreen(Text title) {
        super(title);
    }

    @Redirect(method = "method_19851", at = @At(value = "INVOKE", ordinal = 0, target = "net/minecraft/client/util/NetworkUtils.findLocalPort()I"))
    public int getPort(){
        return OpenToPublic.openPublic ? OpenToPublic.customPort : NetworkUtils.findLocalPort();
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void onInit(CallbackInfo ci){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null){
            return;
        }
        openToWan = new ButtonWidget(this.width / 2 - 155,
                this.height - 58, 150, 20,
                new TranslatableText("opentopublic.button.open_public"), button -> {
                    OpenToPublic.openPublic = !OpenToPublic.openPublic;
                    player.sendMessage(new LiteralText("clicked "+OpenToPublic.openPublic), false);
                    updateButtonText();
                });
        this.addButton(openToWan);
        updateButtonText();
    }

    public TranslatableText translateYN(boolean bool){
        return bool ? yes : no;
    }

    @Inject(method = "updateButtonText", at = @At("TAIL"))
    private void updateText(CallbackInfo ci){
        this.openToWan.setMessage(new TranslatableText("opentopublic.button.open_public", translateYN(OpenToPublic.openPublic)));
    }
}
