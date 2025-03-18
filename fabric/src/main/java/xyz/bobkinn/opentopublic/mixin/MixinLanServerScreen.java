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
public abstract class MixinLanServerScreen extends Screen {

    protected MixinLanServerScreen(Component title) {
        super(title);
    }

    @Unique
    private Button openToWan, onlineModeButton, pvpButton = null;
    @Unique
    private MotdInputTextField motdInput;

    @Shadow
    private GameType gameMode = GameType.SURVIVAL;
    @Shadow
    private boolean commands;

    @Unique
    private int enteredPort = OpenToPublic.customPort;
    @Unique
    private int enteredMaxPN = OpenToPublic.maxPlayers;
    @Unique
    private String motd = null;

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
            enteredPort = portField.getServerPort();
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

            if (validatePort(Integer.toString(enteredPort)) == -1) return;
            if (MaxPlayersInputTextField.validateNum(Integer.toString(enteredMaxPN)) == -1) return;
            String enteredMotd = motdInput.getMotd();
            if (enteredMotd == null) return;
            this.motd = enteredMotd;
            String worldName = server.getWorldData().getLevelName();

            OpenToPublic.customPort = enteredPort;
            OpenToPublic.maxPlayers = enteredMaxPN;

            this.minecraft.setScreen(null);

            if (OpenToPublic.maxPlayers != 8){
                ((PlayerManagerAccessor) server.getPlayerList()).setMaxPlayers(OpenToPublic.maxPlayers);
            }

            server.setPvpAllowed(OpenToPublic.enablePvp);
            server.setUsesAuthentication(OpenToPublic.onlineMode);
            server.setMotd(Util.parseValues(motd, playerName, worldName));

//          OpenToPublic.LOGGER.info("Saving world custom data...");
            OtpPersistentState ps = new OtpPersistentState();
            ps.setMotd(motd);
            ps.setMaxPlayers(OpenToPublic.maxPlayers);
            ps.setEnablePvp(OpenToPublic.enablePvp);
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

            String parsedMotd = Util.parseValues(motd, playerName, worldName);
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
        this.motd = server.getMotd();

        OpenToPublic.updateConfig(OpenToPublic.modConfigPath.resolve("config.json")); // update config

        // load data
        // OpenToPublic.LOGGER.info("Loading world custom data...");
        var psm = server.overworld().getDataStorage();
        var loaded = psm.get(OtpPersistentState.TYPE, OtpPersistentState.DATA_NAME);
        OtpPersistentState ps = loaded != null ? loaded : new OtpPersistentState();
        if (ps.getMotd() != null) this.motd = ps.getMotd();
        if (ps.getMaxPlayers() != null) this.enteredMaxPN = ps.getMaxPlayers();
        if (ps.getMaxPlayers() != null) OpenToPublic.enablePvp = ps.getEnablePvp();
        OpenToPublic.maxPlayers = this.enteredMaxPN;
        // OpenToPublic.LOGGER.info("Loaded! "+ nbt);


        // open to wan button
        Tooltip wanTooltip = getWanTooltip();

        openToWan = Button.builder(
                Component.translatable("opentopublic.button.open_public"),
                (w) -> {
                    OpenToPublic.openPublic.next();
                    updateButtonText();
                })
                .bounds(this.width / 2 + 5, this.height - 54, 150, 20)
                .tooltip(wanTooltip)
                .build();
        this.addRenderableWidget(openToWan);

        // online mode switch
        Tooltip onlineModeTooltip = Tooltip.create(Component.translatable("opentopublic.tooltip.online_mode_tooltip"));
        onlineModeButton = Button.builder(Util.parseYN("opentopublic.button.online_mode", OpenToPublic.onlineMode),
                (w) -> {
                    OpenToPublic.onlineMode = !OpenToPublic.onlineMode;
                    updateButtonText();
                })
                .bounds(this.width / 2 - 155, 144, 150, 20)
                .tooltip(onlineModeTooltip)
                .build();
        this.addRenderableWidget(onlineModeButton);

        // pvp on/off button
        pvpButton = Button.builder(Util.parseYN("opentopublic.button.enable_pvp", OpenToPublic.enablePvp),
                (w) -> {
                    OpenToPublic.enablePvp = ! OpenToPublic.enablePvp;
                    updateButtonText();
                })
                .bounds(this.width / 2 + 5, 144, 150, 20)
                .build();
        this.addRenderableWidget(pvpButton);

        // max player input field
        MaxPlayersInputTextField maxPlayers = getMaxPlayersInputTextField();
        this.addRenderableWidget(maxPlayers);

        // motd input
        motdInput = new MotdInputTextField(this.font, this.width / 2 -155, 215, 311, 20, Component.translatable("opentopublic.button.motd"), motd);
        this.addRenderableWidget(motdInput);
        updateButtonText();
    }

    @Unique
    private @NotNull MaxPlayersInputTextField getMaxPlayersInputTextField() {
        MaxPlayersInputTextField maxPlayers = new MaxPlayersInputTextField(this.font, this.width / 2 -155, 180, 150,20, Component.translatable("opentopublic.button.max_players"), OpenToPublic.maxPlayers);
        maxPlayers.setResponder((text) -> {
            maxPlayers.setTextColor(MaxPlayersInputTextField.validateNum(text) >= 0 ? 0xFFFFFF : 0xFF5555);
            enteredMaxPN = maxPlayers.getVal();
//            player.sendMessage(new LiteralText("max players change: "+enteredMaxPN), false);
        });
        return maxPlayers;
    }

    @Unique
    private static @NotNull Tooltip getWanTooltip() {
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
    private void updateButtonText(){
        if (OpenToPublic.openPublic.isTrue()) this.openToWan.setMessage(Component.translatable("opentopublic.button.open_public", Component.translatable("opentopublic.text.manual")));
        else if (OpenToPublic.openPublic.isFalse()) this.openToWan.setMessage(Component.translatable("opentopublic.button.open_public", Util.off));
        else if (OpenToPublic.openPublic.isThird()) this.openToWan.setMessage(Component.translatable("opentopublic.button.open_public", "UPnP"));
        String wanTooltip;
        if (OpenToPublic.openPublic.isTrue()){
            wanTooltip = "opentopublic.tooltip.wan_tooltip.manual";
        } else if (OpenToPublic.openPublic.isFalse()) {
            wanTooltip = "opentopublic.tooltip.wan_tooltip.lan";
        } else {
            wanTooltip = "opentopublic.tooltip.wan_tooltip.upnp";
        }
        this.openToWan.setTooltip(Tooltip.create(Component.translatable(wanTooltip)));
        this.onlineModeButton.setMessage(Util.parseYN("opentopublic.button.online_mode", OpenToPublic.onlineMode));
        this.pvpButton.setMessage(Util.parseYN("opentopublic.button.enable_pvp",  OpenToPublic.enablePvp));
    }
}
