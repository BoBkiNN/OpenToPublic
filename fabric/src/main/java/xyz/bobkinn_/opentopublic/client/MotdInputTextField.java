package xyz.bobkinn_.opentopublic.client;

// code by https://github.com/rikka0w0/LanServerProperties/

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * @author <a href="https://github.com/rikka0w0/LanServerProperties/">rikka0w0</a>
 */
public class MotdInputTextField extends TextFieldWidget {
    private String entered;

    public MotdInputTextField(TextRenderer textRenderer, int x, int y, int width, int height, Text name, String defaultMotd) {
        super(textRenderer, x, y, width, height, name);
        this.setMaxLength(80);
        this.setText(defaultMotd);
        this.entered = defaultMotd;

        this.setChangedListener((text) -> {
            this.setEditableColor(validate(text) != null ? 0xFFFFFF : 0xFF5555);
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
