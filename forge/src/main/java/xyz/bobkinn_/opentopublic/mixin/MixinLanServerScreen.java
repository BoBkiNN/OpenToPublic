package xyz.bobkinn_.opentopublic.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ShareToLanScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bobkinn_.opentopublic.OpenToPublic;
import xyz.bobkinn_.opentopublic.OtpPersistentState;
import xyz.bobkinn_.opentopublic.PortContainer;
import xyz.bobkinn_.opentopublic.Util;
import xyz.bobkinn_.opentopublic.client.MaxPlayersInputTextField;
import xyz.bobkinn_.opentopublic.client.MotdInputTextField;
import xyz.bobkinn_.opentopublic.client.PortInputTextField;
import xyz.bobkinn_.opentopublic.upnp.UpnpThread;

import static xyz.bobkinn_.opentopublic.client.PortInputTextField.validatePort;

@Mixin(value = ShareToLanScreen.class)
public abstract class MixinLanServerScreen extends Screen {
    Screen parent;
    protected MixinLanServerScreen(TextComponent title, Screen parent) {
        super(title);
        this.parent=parent;
    }

    @Shadow protected abstract void updateDisplayNames();

    Button openToWan = null;
    Button onlineModeButton = null;
    Button pvpButton = null;
    MotdInputTextField motdInput;

    @Shadow
    private String gameMode = "survival";
    @Shadow
    private boolean allowCheats;

    @Shadow public abstract void render(@NotNull MatrixStack matrices, int mouseX, int mouseY, float delta);

    int enteredPort = OpenToPublic.customPort;
    int enteredMaxPN = OpenToPublic.maxPlayers;
    String motd = null;

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.renderBackground(matrices);
        drawCenteredString(matrices, this.font, this.title, this.width / 2, 50, 0xFFFFFF);
        drawCenteredString(matrices, this.font, new TranslationTextComponent("opentopublic.gui.new_player_settings"), this.width / 2, 82, 0xFFFFFF);
        drawCenteredString(matrices, this.font, new TranslationTextComponent("opentopublic.gui.server_settings"), this.width / 2, 130, 0xFFFFFF);
        drawString(matrices, this.font, new TranslationTextComponent("opentopublic.button.port"), this.width / 2 - 154, this.height - 48, 0xFFFFFF);
        drawString(matrices, this.font, new TranslationTextComponent("opentopublic.button.max_players"), this.width / 2 - 154, 168, 0xFFFFFF);
        drawString(matrices, this.font, new TranslationTextComponent("opentopublic.button.motd"), this.width / 2 - 154, 204, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
        ci.cancel();
    }

    @Redirect(method = "init", at = @At(value = "INVOKE",ordinal = 0, target = "Lnet/minecraft/client/gui/screen/ShareToLanScreen;addButton(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;"))
    private Widget addButtonRedirect(ShareToLanScreen instance, Widget widget) {
        Button newBtn = new Button(this.width / 2 - 155, this.height - 28, 150, 20, new TranslationTextComponent("lanServer.start"), buttonWidget -> {
            if (this.minecraft == null) return;
            IntegratedServer server = this.minecraft.getIntegratedServer();
            String playerName = Minecraft.getInstance().getSession().getUsername();
            if (server == null) return;

            if (validatePort(Integer.toString(enteredPort)) == -1) return;
            if (MaxPlayersInputTextField.validateNum(Integer.toString(enteredMaxPN)) == -1) return;
            String enteredMotd = motdInput.getMotd();
            if (enteredMotd == null) return;
            this.motd = enteredMotd;
            String worldName = server.getServerConfiguration().getWorldName();

            OpenToPublic.customPort = enteredPort;
            OpenToPublic.maxPlayers = enteredMaxPN;

            this.minecraft.displayGuiScreen(null);

            if (OpenToPublic.maxPlayers != 8){
                ((PlayerManagerAccessor) server.getPlayerList()).setMaxPlayers(OpenToPublic.maxPlayers);
            }

            server.setAllowPvp(OpenToPublic.enablePvp);
            server.setOnlineMode(OpenToPublic.onlineMode);
            server.setMOTD(Util.parseValues(motd, playerName, worldName));

//          OpenToPublic.LOGGER.info("Saving world custom data..");
            // func_241755_D_ == getOverworld(). -100 social credit to forge mapping
            OtpPersistentState ps = OtpPersistentState.get(server.func_241755_D_());
            CompoundNBT nbt = ps.getData();
            nbt.putString("motd", motd);
            nbt.putInt("maxPlayers", OpenToPublic.maxPlayers);
            nbt.putBoolean("enablePvp", OpenToPublic.enablePvp);
//          OpenToPublic.LOGGER.info(nbt.toText().getString());
            ps.setData(nbt);
            ps.saveToFile(server.func_241755_D_());
//          OpenToPublic.LOGGER.info("Saved");

            boolean doUPnP = OpenToPublic.openPublic.isThird();
            if (doUPnP) {
                PortContainer.self.mainPort = OpenToPublic.customPort;
                PortContainer.saveBackup(PortContainer.self, OpenToPublic.backupFile);
            }

            boolean successOpen = server.shareToLAN(GameType.getByName(this.gameMode), this.allowCheats, OpenToPublic.customPort);
            ((ServerMetadataAccessor) server).getStatusResponse().setServerDescription(new StringTextComponent(Util.parseValues(motd, playerName, worldName)));

            if (doUPnP) {
                Util.displayToast(new TranslationTextComponent("opentopublic.toast.upnp_in_process.title"), new TranslationTextComponent("opentopublic.toast.upnp_in_process.desc"));
                UpnpThread.runSetup();
            } else {
                Util.atSuccessOpen(successOpen);
            }
            this.minecraft.setDefaultMinecraftTitle();
        });
        this.addButton(newBtn);
        return newBtn;
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void onInit(CallbackInfo ci){
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (this.minecraft == null) return;
        IntegratedServer server = this.minecraft.getIntegratedServer();
        if (player == null || server == null){
            return;
        }
        Screen self = this;
        this.motd = server.getMOTD();

        OpenToPublic.updateConfig(OpenToPublic.modConfigPath.resolve("config.json")); // update config

        // load data
//      OpenToPublic.LOGGER.info("Loading world custom data..");
        OtpPersistentState ps = OtpPersistentState.get(server.func_241755_D_());
        ps.loadFromFile(server.func_241755_D_());
        CompoundNBT nbt = ps.getData();
        if (nbt.contains("motd", 8)) this.motd = nbt.getString("motd");
        if (nbt.contains("maxPlayers", 99)) this.enteredMaxPN = nbt.getInt("maxPlayers");
        if (nbt.contains("enablePvp")) OpenToPublic.enablePvp = nbt.getBoolean("enablePvp");
        OpenToPublic.maxPlayers = this.enteredMaxPN;
//      OpenToPublic.LOGGER.info("Loaded! "+ nbt);


        // open to wan button
        Button.ITooltip wanTooltip = (button, matrices, mouseX, mouseY) -> {
            String tooltipTextKey;
            if (OpenToPublic.openPublic.isTrue()){
                tooltipTextKey = "opentopublic.tooltip.wan_tooltip.manual";
            } else if (OpenToPublic.openPublic.isFalse()) {
                tooltipTextKey = "opentopublic.tooltip.wan_tooltip.lan";
            } else {
                tooltipTextKey = "opentopublic.tooltip.wan_tooltip.upnp";
            }
            self.renderTooltip(matrices, new TranslationTextComponent(tooltipTextKey), mouseX, mouseY);
        };
        openToWan = new Button(this.width / 2 + 5,
                this.height - 54, 150, 20,
                new TranslationTextComponent("opentopublic.button.open_public"), button -> {
                    OpenToPublic.openPublic.next();
                    updateDisplayNames();
                }, wanTooltip);
        this.addButton(openToWan);

        // port enter field
        PortInputTextField portField = new PortInputTextField(font, this.width / 2 - 154 + 147/2,
                this.height - 54,
                147/2, 20,
                new TranslationTextComponent("opentopublic.button.port"), OpenToPublic.customPort);
        portField.setResponder((text) -> {
            portField.setTextColor(validatePort(text) >= 0 ? 0xFFFFFF : 0xFF5555);
            enteredPort = portField.getServerPort();
        });
        this.addButton(portField);

        // online mode switch
        Button.ITooltip onlineModeTooltip = (button, matrices, mouseX, mouseY) ->
                self.renderTooltip(matrices,
                        new TranslationTextComponent("opentopublic.tooltip.online_mode_tooltip"), mouseX, mouseY);
        onlineModeButton =
                new Button(this.width / 2 - 155, 144, 150, 20,
                        Util.parseYN("opentopublic.button.online_mode", OpenToPublic.onlineMode),
                        button -> {
                            OpenToPublic.onlineMode = !OpenToPublic.onlineMode;
                            updateDisplayNames();
                        },
                        onlineModeTooltip
                );
        this.addButton(onlineModeButton);

        // pvp on/off button
        pvpButton =
                new Button(this.width / 2 + 5, 144, 150, 20, Util.parseYN("opentopublic.button.enable_pvp", OpenToPublic.enablePvp), button -> {
                    OpenToPublic.enablePvp = ! OpenToPublic.enablePvp;
                    updateDisplayNames();
                });
        this.addButton(pvpButton);

        // max player input field
        MaxPlayersInputTextField maxPlayers = new MaxPlayersInputTextField(this.font, this.width / 2 -155, 180, 150,20, new TranslationTextComponent("opentopublic.button.max_players"), OpenToPublic.maxPlayers);
        maxPlayers.setResponder((text) -> {
            maxPlayers.setTextColor(MaxPlayersInputTextField.validateNum(text) >= 0 ? 0xFFFFFF : 0xFF5555);
            enteredMaxPN = maxPlayers.getVal();
//            player.sendMessage(new LiteralText("max players change: "+enteredMaxPN), false);
        });
        this.addButton(maxPlayers);

        // motd input
        motdInput = new MotdInputTextField(this.font, this.width / 2 -155, 215, 311, 20, new TranslationTextComponent("opentopublic.button.motd"), motd);
        this.addButton(motdInput);

    }


    @Inject(method = "updateDisplayNames", at = @At("TAIL"))
    private void updateText(CallbackInfo ci){
        if (OpenToPublic.openPublic.isTrue()) this.openToWan.setMessage(new TranslationTextComponent("opentopublic.button.open_public", new TranslationTextComponent("opentopublic.text.manual")));
        else if (OpenToPublic.openPublic.isFalse()) this.openToWan.setMessage(new TranslationTextComponent("opentopublic.button.open_public", Util.off));
        else if (OpenToPublic.openPublic.isThird()) this.openToWan.setMessage(new TranslationTextComponent("opentopublic.button.open_public", "UPnP"));
        this.onlineModeButton.setMessage(Util.parseYN("opentopublic.button.online_mode", OpenToPublic.onlineMode));
        this.pvpButton.setMessage(Util.parseYN("opentopublic.button.enable_pvp",  OpenToPublic.enablePvp));
    }
}
