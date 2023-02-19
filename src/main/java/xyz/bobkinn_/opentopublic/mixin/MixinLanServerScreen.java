package xyz.bobkinn_.opentopublic.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bobkinn_.opentopublic.OpenToPublic;
import xyz.bobkinn_.opentopublic.client.IPAddressTextField;

import static xyz.bobkinn_.opentopublic.client.IPAddressTextField.validatePort;

@Mixin(value = OpenToLanScreen.class, priority = 1005)
public abstract class MixinLanServerScreen extends Screen {
    Screen parent;
    protected MixinLanServerScreen(Text title, Screen parent) {
        super(title);
        this.parent=parent;
    }

    @Shadow protected abstract void updateButtonText();

    ButtonWidget openToWan = null;
    TranslatableText yes = new TranslatableText("gui.yes");
    TranslatableText no = new TranslatableText("gui.no");
    @Shadow
    private String gameMode = "survival";
    @Shadow
    private boolean allowCommands;
    int enteredPort = 25565;


    @Redirect(method = "init", at = @At(value = "INVOKE",ordinal = 0, target = "Lnet/minecraft/client/gui/screen/OpenToLanScreen;addButton(Lnet/minecraft/client/gui/widget/ClickableWidget;)Lnet/minecraft/client/gui/widget/ClickableWidget;"))
    private ClickableWidget addButtonRedirect(OpenToLanScreen instance, ClickableWidget btn) {
        // Your custom logic here
//        System.out.println("Redirecting addButton call");
        ClickableWidget newBtn = new ButtonWidget(this.width / 2 - 155, this.height - 28, 150, 20, new TranslatableText("lanServer.start"), buttonWidget -> {
            if (this.client == null) return;
            if (this.client.getServer() == null) return;

            if (validatePort(Integer.toString(enteredPort)) == -1) return;
            OpenToPublic.customPort = enteredPort;

            this.client.openScreen(null);

            boolean successOpen = this.client.getServer().openToLan(GameMode.byName(this.gameMode), this.allowCommands, OpenToPublic.customPort);
            TranslatableText successWAN = new TranslatableText("opentopublic.publish.started_wan", "0.0.0.0:"+OpenToPublic.customPort);
            TranslatableText text;
            if (OpenToPublic.openPublic) text = successOpen ? successWAN : new TranslatableText("opentopublic.publish.failed_wan");
            else {
                text = successOpen ? new TranslatableText("commands.publish.started", OpenToPublic.customPort) : new TranslatableText("commands.publish.failed");
            }
            this.client.inGameHud.getChatHud().addMessage(text);
            this.client.updateWindowTitle();
        });
        this.addButton(newBtn);
        return newBtn;
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void onInit(CallbackInfo ci){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null){
            return;
        }
        openToWan = new ButtonWidget(this.width / 2 + 5,
                this.height - 54, 150, 20,
                new TranslatableText("opentopublic.button.open_public"), button -> {
                    OpenToPublic.openPublic = !OpenToPublic.openPublic;
//                    player.sendMessage(new LiteralText("wan: "+OpenToPublic.openPublic), false);
                    updateButtonText();
                });
        this.addButton(openToWan);
        IPAddressTextField portField = new IPAddressTextField(textRenderer, this.width / 2 - 154, this.height - 54, 147, 20,
                new TranslatableText("opentopublic.button.port"), OpenToPublic.customPort);
        portField.setChangedListener((text) -> {
            portField.setEditableColor(validatePort(text) >= 0 ? 0xFFFFFF : 0xFF0000);
//            enteredPort = portField.getServerPort();
//            player.sendMessage(new LiteralText("port changed: "+enteredPort), false);
        });
        this.addButton(portField);
    }


    public TranslatableText translateYN(boolean bool){
        return bool ? yes : no;
    }

    @Inject(method = "updateButtonText", at = @At("TAIL"))
    private void updateText(CallbackInfo ci){
        this.openToWan.setMessage(new TranslatableText("opentopublic.button.open_public", translateYN(OpenToPublic.openPublic)));
    }
}
