package xyz.bobkinn_.opentopublic.client;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.TextComponent;

public class MotdInputTextField extends TextFieldWidget {
    private String entered;

    public MotdInputTextField(FontRenderer textRenderer, int x, int y, int width, int height, TextComponent name, String defaultMotd) {
        super(textRenderer, x, y, width, height, name);
        this.setMaxStringLength(80);
        this.setText(defaultMotd);
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
