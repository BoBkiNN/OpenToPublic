package xyz.bobkinn_.opentopublic.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class MotdInputTextField extends EditBox {
    private String entered;

    public MotdInputTextField(Font textRenderer, int x, int y, int width, int height, Component name, String defaultMotd) {
        super(textRenderer, x, y, width, height, name);
        this.setMaxLength(80);
        this.setValue(defaultMotd);
        this.entered = defaultMotd;
        this.setResponder((text) -> {
            this.setTextColor(validate(text) != null ? 0xFFFFFF : 0xFF5555);
            this.entered = text;
        });
    }

    public String getMotd(){
        return validate(this.entered) == null ? null : this.entered;
    }

    public static String validate(String text) {
//        return text.length() < 59 ? text : null;
        return text;
    }
}
