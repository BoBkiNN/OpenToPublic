package xyz.bobkinn.opentopublic.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/**
 * @author <a href="https://github.com/rikka0w0/LanServerProperties/">rikka0w0</a>
 */
public class MotdInputTextField extends EditBox {
    private String entered;

    public MotdInputTextField(Font textRenderer, int x, int y, int width, int height, Component name, String defaultMotd) {
        super(textRenderer, x, y, width, height, name);
        this.setMaxLength(80);
        this.setValue(defaultMotd);
        this.entered = defaultMotd;

        this.setResponder((text) -> {
            this.setTextColor(validate(text) != null ? 0xFFFFFFFF : 0xFFFF5555);
            this.entered = text;
        });
    }

    public static String validate(String text) {
//        return text.length() < 59 ? text : null;
        return text;
    }

    public String getMotd() {
        return validate(this.entered) == null ? null : this.entered;
    }
}
