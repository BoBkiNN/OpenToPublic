package xyz.bobkinn_.opentopublic.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
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

@Mixin(value = OpenToLanScreen.class)
public abstract class MixinLanServerScreen extends Screen {
    final Screen parent;
    protected MixinLanServerScreen(Text title, Screen parent) {
        super(title);
        this.parent=parent;
    }

    ButtonWidget openToWan = null;
    ButtonWidget onlineModeButton = null;
    ButtonWidget pvpButton = null;
    MotdInputTextField motdInput;

    @Shadow
    private GameMode gameMode = GameMode.SURVIVAL;
    @Shadow
    private boolean allowCommands;

    @Shadow public abstract void render(MatrixStack matrices, int mouseX, int mouseY, float delta);

    int enteredPort = OpenToPublic.customPort;
    int enteredMaxPN = OpenToPublic.maxPlayers;
    String motd = null;

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.renderBackground(matrices);
        OpenToLanScreen.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 50, 0xFFFFFF);
        OpenToLanScreen.drawCenteredText(matrices, this.textRenderer, Text.translatable("opentopublic.gui.new_player_settings"), this.width / 2, 82, 0xFFFFFF);
        OpenToLanScreen.drawCenteredText(matrices, this.textRenderer, Text.translatable("opentopublic.gui.server_settings"), this.width / 2, 130, 0xFFFFFF);
        OpenToLanScreen.drawTextWithShadow(matrices, this.textRenderer, Text.translatable("opentopublic.button.port"), this.width / 2 - 154, this.height - 48, 0xFFFFFF);
        OpenToLanScreen.drawTextWithShadow(matrices, this.textRenderer, Text.translatable("opentopublic.button.max_players"), this.width / 2 - 154, 168, 0xFFFFFF);
        OpenToLanScreen.drawTextWithShadow(matrices, this.textRenderer, Text.translatable("opentopublic.button.motd"), this.width / 2 - 154, 204, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
        ci.cancel();
    }

    @Redirect(method = "init", at = @At(value = "INVOKE",ordinal = 2, target = "Lnet/minecraft/client/gui/screen/OpenToLanScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;"))
    private Element addButtonRedirect(OpenToLanScreen instance, Element element) {
        ClickableWidget newBtn = new ButtonWidget(this.width / 2 - 155, this.height - 28, 150, 20, Text.translatable("lanServer.start"), buttonWidget -> {
            if (this.client == null) return;
            IntegratedServer server = this.client.getServer();
            String playerName = MinecraftClient.getInstance().getSession().getUsername();
            if (server == null) return;

            if (validatePort(Integer.toString(enteredPort)) == -1) return;
            if (MaxPlayersInputTextField.validateNum(Integer.toString(enteredMaxPN)) == -1) return;
            String enteredMotd = motdInput.getMotd();
            if (enteredMotd == null) return;
            this.motd = enteredMotd;
            String worldName = server.getSaveProperties().getLevelName();

            OpenToPublic.customPort = enteredPort;
            OpenToPublic.maxPlayers = enteredMaxPN;

            this.client.setScreen(null);

            if (OpenToPublic.maxPlayers != 8){
                ((PlayerManagerAccessor) server.getPlayerManager()).setMaxPlayers(OpenToPublic.maxPlayers);
            }

            server.setPvpEnabled(OpenToPublic.enablePvp);
            server.setOnlineMode(OpenToPublic.onlineMode);
            server.setMotd(Util.parseValues(motd, playerName, worldName));

//          OpenToPublic.LOGGER.info("Saving world custom data..");
            OtpPersistentState ps = new OtpPersistentState();
            NbtCompound nbt = ps.getData();
            nbt.putString("motd", motd);
            nbt.putInt("maxPlayers", OpenToPublic.maxPlayers);
            nbt.putBoolean("enablePvp", OpenToPublic.enablePvp);
//          OpenToPublic.LOGGER.info(nbt.toText().getString());
            ps.setData(nbt);
            ps.saveToFile(server.getOverworld());
//          OpenToPublic.LOGGER.info("Saved");

            boolean doUPnP = OpenToPublic.openPublic.isThird();
            if (doUPnP) {
                PortContainer.self.mainPort = OpenToPublic.customPort;
                PortContainer.saveBackup(PortContainer.self, OpenToPublic.backupFile);
            }

            boolean successOpen = server.openToLan(this.gameMode, this.allowCommands, OpenToPublic.customPort);
            ((ServerMetadataAccessor) server).getMetadata().setDescription(Text.translatable(Util.parseValues(motd, playerName, worldName)));

            if (doUPnP) {
                Util.displayToast(Text.translatable("opentopublic.toast.upnp_in_process.title"), Text.translatable("opentopublic.toast.upnp_in_process.desc"));
                UpnpThread.runSetup();
            } else {
                Util.atSuccessOpen(successOpen);
            }
            this.client.updateWindowTitle();
        });
        this.addDrawableChild(newBtn);
        return newBtn;
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void onInit(CallbackInfo ci){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (this.client == null) return;
        IntegratedServer server = this.client.getServer();
        if (player == null || server == null){
            return;
        }
        Screen self = this;
        this.motd = server.getServerMotd();

        OpenToPublic.updateConfig(OpenToPublic.modConfigPath.resolve("config.json")); // update config

        // load data
//      OpenToPublic.LOGGER.info("Loading world custom data..");
        OtpPersistentState ps = new OtpPersistentState();
        ps.loadFromFile(server.getOverworld());
        NbtCompound nbt = ps.getData();
        if (nbt.contains("motd", 8)) this.motd = nbt.getString("motd");
        if (nbt.contains("maxPlayers", 99)) this.enteredMaxPN = nbt.getInt("maxPlayers");
        if (nbt.contains("enablePvp")) OpenToPublic.enablePvp = nbt.getBoolean("enablePvp");
        OpenToPublic.maxPlayers = this.enteredMaxPN;
//      OpenToPublic.LOGGER.info("Loaded! "+ nbt);


        // open to wan button
        ButtonWidget.TooltipSupplier wanTooltip = (button, matrices, mouseX, mouseY) -> {
            String tooltipTextKey;
            if (OpenToPublic.openPublic.isTrue()){
                tooltipTextKey = "opentopublic.tooltip.wan_tooltip.manual";
            } else if (OpenToPublic.openPublic.isFalse()) {
                tooltipTextKey = "opentopublic.tooltip.wan_tooltip.lan";
            } else {
                tooltipTextKey = "opentopublic.tooltip.wan_tooltip.upnp";
            }
            self.renderTooltip(matrices, Text.translatable(tooltipTextKey), mouseX, mouseY);
        };
        openToWan = new ButtonWidget(this.width / 2 + 5,
                this.height - 54, 150, 20,
                Text.translatable("opentopublic.button.open_public"), button -> {
                    OpenToPublic.openPublic.next();
                    updateButtonText();
                }, wanTooltip);
        this.addDrawableChild(openToWan);

        // port enter field
        PortInputTextField portField = new PortInputTextField(textRenderer, this.width / 2 - 154 + 147/2,
                this.height - 54,
                147/2, 20,
                Text.translatable("opentopublic.button.port"), OpenToPublic.customPort);
        portField.setChangedListener((text) -> {
            portField.setEditableColor(validatePort(text) >= 0 ? 0xFFFFFF : 0xFF5555);
            enteredPort = portField.getServerPort();
        });
        this.addDrawableChild(portField);

        // online mode switch
        ButtonWidget.TooltipSupplier onlineModeTooltip = (button, matrices, mouseX, mouseY) ->
                self.renderTooltip(matrices,
                        Text.translatable("opentopublic.tooltip.online_mode_tooltip"), mouseX, mouseY);
        onlineModeButton =
                new ButtonWidget(this.width / 2 - 155, 144, 150, 20,
                        Util.parseYN("opentopublic.button.online_mode", OpenToPublic.onlineMode),
                        button -> {
                            OpenToPublic.onlineMode = !OpenToPublic.onlineMode;
                            updateButtonText();
                        },
                        onlineModeTooltip
                );
        this.addDrawableChild(onlineModeButton);

        // pvp on/off button
        pvpButton =
                new ButtonWidget(this.width / 2 + 5, 144, 150, 20, Util.parseYN("opentopublic.button.enable_pvp", OpenToPublic.enablePvp), button -> {
                    OpenToPublic.enablePvp = ! OpenToPublic.enablePvp;
                    updateButtonText();
                });
        this.addDrawableChild(pvpButton);

        // max player input field
        MaxPlayersInputTextField maxPlayers = new MaxPlayersInputTextField(this.textRenderer, this.width / 2 -155, 180, 150,20, Text.translatable("opentopublic.button.max_players"), OpenToPublic.maxPlayers);
        maxPlayers.setChangedListener((text) -> {
            maxPlayers.setEditableColor(MaxPlayersInputTextField.validateNum(text) >= 0 ? 0xFFFFFF : 0xFF5555);
            enteredMaxPN = maxPlayers.getVal();
//            player.sendMessage(new LiteralText("max players change: "+enteredMaxPN), false);
        });
        this.addDrawableChild(maxPlayers);

        // motd input
        motdInput = new MotdInputTextField(this.textRenderer, this.width / 2 -155, 215, 311, 20, Text.translatable("opentopublic.button.motd"), motd);
        this.addDrawableChild(motdInput);
        updateButtonText();
    }


    private void updateButtonText(){
        if (OpenToPublic.openPublic.isTrue()) this.openToWan.setMessage(Text.translatable("opentopublic.button.open_public", Text.translatable("opentopublic.text.manual")));
        else if (OpenToPublic.openPublic.isFalse()) this.openToWan.setMessage(Text.translatable("opentopublic.button.open_public", Util.off));
        else if (OpenToPublic.openPublic.isThird()) this.openToWan.setMessage(Text.translatable("opentopublic.button.open_public", "UPnP"));
        this.onlineModeButton.setMessage(Util.parseYN("opentopublic.button.online_mode", OpenToPublic.onlineMode));
        this.pvpButton.setMessage(Util.parseYN("opentopublic.button.enable_pvp",  OpenToPublic.enablePvp));
    }
}
