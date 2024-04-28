package xyz.bobkinn_.opentopublic.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

    protected MixinLanServerScreen(Text title) {
        super(title);
    }

    @Unique
    private ButtonWidget openToWan, onlineModeButton, pvpButton = null;
    @Unique
    private MotdInputTextField motdInput;

    @Shadow
    private GameMode gameMode = GameMode.SURVIVAL;
    @Shadow
    private boolean allowCommands;

    @Unique
    private int enteredPort = OpenToPublic.customPort;
    @Unique
    private int enteredMaxPN = OpenToPublic.maxPlayers;
    @Unique
    private String motd = null;

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title.asOrderedText(), this.width / 2, 50, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("opentopublic.gui.new_player_settings").asOrderedText(), this.width / 2, 82, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("opentopublic.gui.server_settings").asOrderedText(), this.width / 2, 130, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.translatable("opentopublic.button.port"), this.width / 2 - 154, this.height - 48, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.translatable("opentopublic.button.max_players"), this.width / 2 - 154, 168, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.translatable("opentopublic.button.motd"), this.width / 2 - 154, 204, 0xFFFFFF);
        ci.cancel();
    }

    @Redirect(method = "init", at = @At(value = "INVOKE",ordinal = 2, target = "Lnet/minecraft/client/gui/screen/OpenToLanScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;"))
    private Element redirectPort(OpenToLanScreen instance, Element element) {
        // port enter field
        PortInputTextField portField = new PortInputTextField(textRenderer, this.width / 2 - 154 + 147/2,
                this.height - 54,
                147/2, 20,
                Text.translatable("opentopublic.button.port"), OpenToPublic.customPort);
        portField.setChangedListener((text) -> {
            portField.setEditableColor(validatePort(text) >= 0 ? 0xFFFFFF : 0xFF5555);
            enteredPort = portField.getServerPort();
        });
        return this.addDrawableChild(portField);
    }

    @Redirect(method = "init", at = @At(value = "INVOKE",ordinal = 3, target = "Lnet/minecraft/client/gui/screen/OpenToLanScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;"))
    private Element addButtonRedirect(OpenToLanScreen instance, Element element) {
        ButtonWidget.PressAction act = (w) -> {
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
            server.getReloadableRegistries().createRegistryLookup();
            ps.saveToFile(server.getOverworld());
//          OpenToPublic.LOGGER.info("Saved");

            boolean doUPnP = OpenToPublic.openPublic.isThird();
            if (doUPnP) {
                PortContainer.self.mainPort = OpenToPublic.customPort;
                PortContainer.saveBackup(PortContainer.self, OpenToPublic.backupFile);
            }

            boolean successOpen = server.openToLan(this.gameMode, this.allowCommands, OpenToPublic.customPort);

            String parsedMotd = Util.parseValues(motd, playerName, worldName);
            server.setMotd(parsedMotd);
            ServerMetadata md = ((ServerMetadataAccessor) server).createMetadata();
            ((ServerMetadataAccessor) server).setMetadata(md);

            if (doUPnP) {
                Util.displayToast(Text.translatable("opentopublic.toast.upnp_in_process.title"), Text.translatable("opentopublic.toast.upnp_in_process.desc"));
                UpnpThread.runSetup();
            } else {
                Util.atSuccessOpen(successOpen);
            }
            this.client.updateWindowTitle();
        };
        ClickableWidget newBtn = ButtonWidget.builder(Text.translatable("lanServer.start"), act)
                .dimensions(this.width / 2 - 155, this.height - 28, 150, 20).build();
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
        this.motd = server.getServerMotd();

        OpenToPublic.updateConfig(OpenToPublic.modConfigPath.resolve("config.json")); // update config

        // load data
        // OpenToPublic.LOGGER.info("Loading world custom data...");
        OtpPersistentState ps = new OtpPersistentState();
        ps.loadFromFile(server.getOverworld());
        NbtCompound nbt = ps.getData();
        if (nbt.contains("motd", 8)) this.motd = nbt.getString("motd");
        if (nbt.contains("maxPlayers", 99)) this.enteredMaxPN = nbt.getInt("maxPlayers");
        if (nbt.contains("enablePvp")) OpenToPublic.enablePvp = nbt.getBoolean("enablePvp");
        OpenToPublic.maxPlayers = this.enteredMaxPN;
        // OpenToPublic.LOGGER.info("Loaded! "+ nbt);


        // open to wan button
        Tooltip wanTooltip = getWanTooltip();

        openToWan = ButtonWidget.builder(
                Text.translatable("opentopublic.button.open_public"),
                (w) -> {
                    OpenToPublic.openPublic.next();
                    updateButtonText();
                })
                .dimensions(this.width / 2 + 5, this.height - 54, 150, 20)
                .tooltip(wanTooltip)
                .build();
        this.addDrawableChild(openToWan);

        // online mode switch
        Tooltip onlineModeTooltip = Tooltip.of(Text.translatable("opentopublic.tooltip.online_mode_tooltip"));
        onlineModeButton = ButtonWidget.builder(Util.parseYN("opentopublic.button.online_mode", OpenToPublic.onlineMode),
                (w) -> {
                    OpenToPublic.onlineMode = !OpenToPublic.onlineMode;
                    updateButtonText();
                })
                .dimensions(this.width / 2 - 155, 144, 150, 20)
                .tooltip(onlineModeTooltip)
                .build();
        this.addDrawableChild(onlineModeButton);

        // pvp on/off button
        pvpButton = ButtonWidget.builder(Util.parseYN("opentopublic.button.enable_pvp", OpenToPublic.enablePvp),
                (w) -> {
                    OpenToPublic.enablePvp = ! OpenToPublic.enablePvp;
                    updateButtonText();
                })
                .dimensions(this.width / 2 + 5, 144, 150, 20)
                .build();
        this.addDrawableChild(pvpButton);

        // max player input field
        MaxPlayersInputTextField maxPlayers = getMaxPlayersInputTextField();
        this.addDrawableChild(maxPlayers);

        // motd input
        motdInput = new MotdInputTextField(this.textRenderer, this.width / 2 -155, 215, 311, 20, Text.translatable("opentopublic.button.motd"), motd);
        this.addDrawableChild(motdInput);
        updateButtonText();
    }

    @Unique
    private @NotNull MaxPlayersInputTextField getMaxPlayersInputTextField() {
        MaxPlayersInputTextField maxPlayers = new MaxPlayersInputTextField(this.textRenderer, this.width / 2 -155, 180, 150,20, Text.translatable("opentopublic.button.max_players"), OpenToPublic.maxPlayers);
        maxPlayers.setChangedListener((text) -> {
            maxPlayers.setEditableColor(MaxPlayersInputTextField.validateNum(text) >= 0 ? 0xFFFFFF : 0xFF5555);
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
        return Tooltip.of(Text.translatable(tooltipTextKey));
    }


    @Unique
    private void updateButtonText(){
        if (OpenToPublic.openPublic.isTrue()) this.openToWan.setMessage(Text.translatable("opentopublic.button.open_public", Text.translatable("opentopublic.text.manual")));
        else if (OpenToPublic.openPublic.isFalse()) this.openToWan.setMessage(Text.translatable("opentopublic.button.open_public", Util.off));
        else if (OpenToPublic.openPublic.isThird()) this.openToWan.setMessage(Text.translatable("opentopublic.button.open_public", "UPnP"));
        String wanTooltip;
        if (OpenToPublic.openPublic.isTrue()){
            wanTooltip = "opentopublic.tooltip.wan_tooltip.manual";
        } else if (OpenToPublic.openPublic.isFalse()) {
            wanTooltip = "opentopublic.tooltip.wan_tooltip.lan";
        } else {
            wanTooltip = "opentopublic.tooltip.wan_tooltip.upnp";
        }
        this.openToWan.setTooltip(Tooltip.of(Text.translatable(wanTooltip)));
        this.onlineModeButton.setMessage(Util.parseYN("opentopublic.button.online_mode", OpenToPublic.onlineMode));
        this.pvpButton.setMessage(Util.parseYN("opentopublic.button.enable_pvp",  OpenToPublic.enablePvp));
    }
}
