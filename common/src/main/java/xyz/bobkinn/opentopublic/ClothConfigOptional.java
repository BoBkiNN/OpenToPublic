package xyz.bobkinn.opentopublic;

import net.minecraft.client.gui.screens.Screen;

public class ClothConfigOptional {
    public static Screen createConfigScreen(Screen parent) {
        return ClothConfigFactory.createConfigScreen(parent);
    }
}
