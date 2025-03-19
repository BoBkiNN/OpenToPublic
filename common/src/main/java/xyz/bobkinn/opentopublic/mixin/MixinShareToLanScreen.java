package xyz.bobkinn.opentopublic.mixin;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bobkinn.opentopublic.OtpPersistentState;
import xyz.bobkinn.opentopublic.PortContainer;
import xyz.bobkinn.opentopublic.Util;
import xyz.bobkinn.opentopublic.OpenToPublic;
import xyz.bobkinn.opentopublic.client.MaxPlayersInputTextField;
import xyz.bobkinn.opentopublic.client.MotdInputTextField;
import xyz.bobkinn.opentopublic.client.PortInputTextField;
import xyz.bobkinn.opentopublic.upnp.UpnpThread;

import static xyz.bobkinn.opentopublic.client.PortInputTextField.validatePort;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;

@Mixin(value = ShareToLanScreen.class)
public abstract class MixinShareToLanScreen extends Screen {

    protected MixinShareToLanScreen(Component title) {
        super(title);
    }

    @Unique
    private Button openToPublic$openToWan, openToPublic$onlineModeButton, openToPublic$pvpButton = null;
    @Unique
    private MotdInputTextField openToPublic$motdInput;

    @Shadow
    private GameType gameMode = GameType.SURVIVAL;
    @Shadow
    private boolean commands;

    @Unique
    private int openToPublic$enteredPort = OpenToPublic.customPort;

    @Unique
    public boolean openToPublic$onlineMode = true;
    @Unique
    public int openToPublic$maxPlayers = 8;
    @Unique
    public boolean openToPublic$enablePvp = true;
    @Unique
    private int openToPublic$enteredMaxPN = openToPublic$maxPlayers;
    @Unique
    private String openToPublic$motd = null;

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.title.getVisualOrderText(), this.width / 2, 50, 0xFFFFFF);
        context.drawCenteredString(this.font, Component.translatable("opentopublic.gui.new_player_settings").getVisualOrderText(), this.width / 2, 82, 0xFFFFFF);
        context.drawCenteredString(this.font, Component.translatable("opentopublic.gui.server_settings").getVisualOrderText(), this.width / 2, 130, 0xFFFFFF);
        context.drawString(this.font, Component.translatable("opentopublic.button.port"), this.width / 2 - 154, this.height - 48, 0xFFFFFF);
        context.drawString(this.font, Component.translatable("opentopublic.button.max_players"), this.width / 2 - 154, 168, 0xFFFFFF);
        context.drawString(this.font, Component.translatable("opentopublic.button.motd"), this.width / 2 - 154, 204, 0xFFFFFF);
        ci.cancel();
    }

    @Redirect(method = "init", at = @At(value = "INVOKE",ordinal = 2, target = "Lnet/minecraft/client/gui/screens/ShareToLanScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;"))
    private GuiEventListener redirectPort(ShareToLanScreen instance, GuiEventListener element) {
        // port enter field
        PortInputTextField portField = new PortInputTextField(font, this.width / 2 - 154 + 147/2,
                this.height - 54,
                147/2, 20,
                Component.translatable("opentopublic.button.port"), OpenToPublic.customPort);
        portField.setResponder((text) -> {
            portField.setTextColor(validatePort(text) >= 0 ? 0xFFFFFF : 0xFF5555);
            openToPublic$enteredPort = portField.getServerPort();
        });
        return this.addRenderableWidget(portField);
    }

    @Redirect(method = "init", at = @At(value = "INVOKE",ordinal = 3, target = "Lnet/minecraft/client/gui/screens/ShareToLanScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;"))
    private GuiEventListener addButtonRedirect(ShareToLanScreen instance, GuiEventListener element) {
        Button.OnPress act = (w) -> {
            if (this.minecraft == null) return;
            IntegratedServer server = this.minecraft.getSingleplayerServer();
            String playerName = Minecraft.getInstance().getUser().getName();
            if (server == null) return;

            if (validatePort(Integer.toString(openToPublic$enteredPort)) == -1) return;
            if (MaxPlayersInputTextField.validateNum(Integer.toString(openToPublic$enteredMaxPN)) == -1) return;
            String enteredMotd = openToPublic$motdInput.getMotd();
            if (enteredMotd == null) return;
            this.openToPublic$motd = enteredMotd;
            String worldName = server.getWorldData().getLevelName();

            OpenToPublic.customPort = openToPublic$enteredPort;
            openToPublic$maxPlayers = openToPublic$enteredMaxPN;

            this.minecraft.setScreen(null);

            if (openToPublic$maxPlayers != 8){
                ((PlayerListAccessor) server.getPlayerList()).setMaxPlayers(openToPublic$maxPlayers);
            }

            server.setPvpAllowed(openToPublic$enablePvp);
            server.setUsesAuthentication(openToPublic$onlineMode);
            server.setMotd(Util.parseValues(openToPublic$motd, playerName, worldName));

//          OpenToPublic.LOGGER.info("Saving world custom data...");
            OtpPersistentState ps = new OtpPersistentState();
            ps.setMotd(openToPublic$motd);
            ps.setMaxPlayers(openToPublic$maxPlayers);
            ps.setEnablePvp(openToPublic$enablePvp);
            ps.setDirty();
//          OpenToPublic.LOGGER.info(nbt.toText().getString());
            var psm = server.overworld().getDataStorage();
            psm.set(OtpPersistentState.DATA_NAME, ps);
//          OpenToPublic.LOGGER.info("Saved");

            boolean doUPnP = OpenToPublic.openPublic.isThird();
            if (doUPnP) {
                PortContainer.self.mainPort = OpenToPublic.customPort;
                PortContainer.saveBackup(PortContainer.self, OpenToPublic.backupFile);
            }

            boolean successOpen = server.publishServer(this.gameMode, this.commands, OpenToPublic.customPort);

            String parsedMotd = Util.parseValues(openToPublic$motd, playerName, worldName);
            server.setMotd(parsedMotd);

            if (doUPnP) {
                Util.displayToast(Component.translatable("opentopublic.toast.upnp_in_process.title"), Component.translatable("opentopublic.toast.upnp_in_process.desc"));
                UpnpThread.runSetup();
            } else {
                Util.atSuccessOpen(successOpen);
            }
            this.minecraft.updateTitle();
        };
        AbstractWidget newBtn = Button.builder(Component.translatable("lanServer.start"), act)
                .bounds(this.width / 2 - 155, this.height - 28, 150, 20).build();
        this.addRenderableWidget(newBtn);
        return newBtn;
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void onInit(CallbackInfo ci){
        LocalPlayer player = Minecraft.getInstance().player;
        if (this.minecraft == null) return;
        IntegratedServer server = this.minecraft.getSingleplayerServer();
        if (player == null || server == null){
            return;
        }
        this.openToPublic$motd = server.getMotd();

        OpenToPublic.updateConfig(OpenToPublic.modConfigPath.resolve("config.json")); // update config

        // load data
        // OpenToPublic.LOGGER.info("Loading world custom data...");
        var psm = server.overworld().getDataStorage();
        var loaded = psm.get(OtpPersistentState.TYPE, OtpPersistentState.DATA_NAME);
        OtpPersistentState ps = loaded != null ? loaded : new OtpPersistentState();
        if (ps.getMotd() != null) this.openToPublic$motd = ps.getMotd();
        if (ps.getMaxPlayers() != null) this.openToPublic$enteredMaxPN = ps.getMaxPlayers();
        if (ps.getMaxPlayers() != null) openToPublic$enablePvp = ps.getEnablePvp();
        openToPublic$maxPlayers = this.openToPublic$enteredMaxPN;
        // OpenToPublic.LOGGER.info("Loaded! "+ nbt);


        // open to wan button
        Tooltip wanTooltip = openToPublic$getWanTooltip();

        openToPublic$openToWan = Button.builder(
                Component.translatable("opentopublic.button.open_public"),
                (w) -> {
                    OpenToPublic.openPublic.next();
                    openToPublic$updateButtonText();
                })
                .bounds(this.width / 2 + 5, this.height - 54, 150, 20)
                .tooltip(wanTooltip)
                .build();
        this.addRenderableWidget(openToPublic$openToWan);

        // online mode switch
        Tooltip onlineModeTooltip = Tooltip.create(Component.translatable("opentopublic.tooltip.online_mode_tooltip"));
        openToPublic$onlineModeButton = Button.builder(Util.parseYN("opentopublic.button.online_mode", openToPublic$onlineMode),
                (w) -> {
                    openToPublic$onlineMode = !openToPublic$onlineMode;
                    openToPublic$updateButtonText();
                })
                .bounds(this.width / 2 - 155, 144, 150, 20)
                .tooltip(onlineModeTooltip)
                .build();
        this.addRenderableWidget(openToPublic$onlineModeButton);

        // pvp on/off button
        openToPublic$pvpButton = Button.builder(Util.parseYN("opentopublic.button.enable_pvp", openToPublic$enablePvp),
                (w) -> {
                    openToPublic$enablePvp = !openToPublic$enablePvp;
                    openToPublic$updateButtonText();
                })
                .bounds(this.width / 2 + 5, 144, 150, 20)
                .build();
        this.addRenderableWidget(openToPublic$pvpButton);

        // max player input field
        MaxPlayersInputTextField maxPlayersField = openToPublic$getMaxPlayersInputTextField();
        this.addRenderableWidget(maxPlayersField);

        // motd input
        openToPublic$motdInput = new MotdInputTextField(this.font, this.width / 2 -155, 215, 311, 20, Component.translatable("opentopublic.button.motd"), openToPublic$motd);
        this.addRenderableWidget(openToPublic$motdInput);
        openToPublic$updateButtonText();
    }

    @Unique
    private @NotNull MaxPlayersInputTextField openToPublic$getMaxPlayersInputTextField() {
        MaxPlayersInputTextField maxPlayersField = new MaxPlayersInputTextField(this.font, this.width / 2 -155, 180, 150,20, Component.translatable("opentopublic.button.max_players"), openToPublic$maxPlayers);
        maxPlayersField.setResponder((text) -> {
            maxPlayersField.setTextColor(MaxPlayersInputTextField.validateNum(text) >= 0 ? 0xFFFFFF : 0xFF5555);
            openToPublic$enteredMaxPN = maxPlayersField.getVal();
//            player.sendMessage(new LiteralText("max players change: "+enteredMaxPN), false);
        });
        return maxPlayersField;
    }

    @Unique
    private static @NotNull Tooltip openToPublic$getWanTooltip() {
        String tooltipTextKey;
        if (OpenToPublic.openPublic.isTrue()){
            tooltipTextKey = "opentopublic.tooltip.wan_tooltip.manual";
        } else if (OpenToPublic.openPublic.isFalse()) {
            tooltipTextKey = "opentopublic.tooltip.wan_tooltip.lan";
        } else {
            tooltipTextKey = "opentopublic.tooltip.wan_tooltip.upnp";
        }
        return Tooltip.create(Component.translatable(tooltipTextKey));
    }


    @Unique
    private void openToPublic$updateButtonText(){
        if (OpenToPublic.openPublic.isTrue()) this.openToPublic$openToWan.setMessage(Component.translatable("opentopublic.button.open_public", Component.translatable("opentopublic.text.manual")));
        else if (OpenToPublic.openPublic.isFalse()) this.openToPublic$openToWan.setMessage(Component.translatable("opentopublic.button.open_public", Util.off));
        else if (OpenToPublic.openPublic.isThird()) this.openToPublic$openToWan.setMessage(Component.translatable("opentopublic.button.open_public", "UPnP"));
        String wanTooltip;
        if (OpenToPublic.openPublic.isTrue()){
            wanTooltip = "opentopublic.tooltip.wan_tooltip.manual";
        } else if (OpenToPublic.openPublic.isFalse()) {
            wanTooltip = "opentopublic.tooltip.wan_tooltip.lan";
        } else {
            wanTooltip = "opentopublic.tooltip.wan_tooltip.upnp";
        }
        this.openToPublic$openToWan.setTooltip(Tooltip.create(Component.translatable(wanTooltip)));
        this.openToPublic$onlineModeButton.setMessage(Util.parseYN("opentopublic.button.online_mode", openToPublic$onlineMode));
        this.openToPublic$pvpButton.setMessage(Util.parseYN("opentopublic.button.enable_pvp", openToPublic$enablePvp));
    }
}
