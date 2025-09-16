package xyz.bobkinn.opentopublic;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ClothConfigFactory {

    public static Screen createConfigScreen(Screen parent) {
        var mcf = OpenToPublic.modConfigPath;
        if (mcf == null) return null;
        var configPath = mcf.resolve("config.json");
        OpenToPublic.updateConfig(configPath);
        var cfg = OpenToPublic.cfg;
        if (cfg == null) return null;
        var builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("opentopublic.config.title"));
        var eb = builder.entryBuilder();
        var general = builder.getOrCreateCategory(Component.literal("general"));
        general.addEntry(eb.startBooleanToggle(Component.translatable("opentopublic.config.hideIps.name"), cfg.isHideIps())
                .setSaveConsumer(cfg::setHideIps)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("opentopublic.config.hideIps.tooltip"))
                .build());
        general.addEntry(eb.startBooleanToggle(Component.translatable("opentopublic.config.changeTitle.name"), cfg.isChangeWindowTitle())
                .setSaveConsumer(changeWindowTitle -> {
                    cfg.setChangeWindowTitle(changeWindowTitle);
                    Minecraft.getInstance().updateTitle();
                })
                .setDefaultValue(true)
                .setTooltip(Component.translatable("opentopublic.config.changeTitle.tooltip"))
                .build());
        general.addEntry(eb.startIntList(Component.translatable("opentopublic.config.udpPorts.name"), cfg.getUdp())
                .setDefaultValue(cfg.getUdp())
                .setTooltip(Component.translatable("opentopublic.config.udpPorts.tooltip"))
                .setMin(1)
                .setMax(65535)
                .setSaveConsumer(ls -> {
                    cfg.getUdp().clear();
                    cfg.getUdp().addAll(ls);
                })
                .build());
        general.addEntry(eb.startIntList(Component.translatable("opentopublic.config.tcpPorts.name"), cfg.getTcp())
                .setDefaultValue(cfg.getTcp())
                .setTooltip(Component.translatable("opentopublic.config.tcpPorts.tooltip"))
                .setMin(1)
                .setMax(65535)
                .setSaveConsumer(ls -> {
                    cfg.getTcp().clear();
                    cfg.getTcp().addAll(ls);
                })
                .build());
        general.setBackground(ResourceLocation.tryParse("textures/block/blackstone.png"));
        builder.setSavingRunnable(() -> OpenToPublic.saveConfig(configPath));
        return builder.build();
    }
}
