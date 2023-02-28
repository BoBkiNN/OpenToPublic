package xyz.bobkinn_.opentopublic.client;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.TextComponent;

public class MaxPlayersInputTextField extends TextFieldWidget {
    private final int defaultVal;

    public MaxPlayersInputTextField(FontRenderer textRenderer, int x, int y, int width, int height, TextComponent name, int defaultVal) {
        super(textRenderer, x, y, width, height, name);
        this.defaultVal = defaultVal;
        this.setText(String.valueOf(defaultVal));
        // Check the format, make sure the text is a valid integer
        this.setResponder((text) -> this.setTextColor(validateNum(text) >= 0 ? 0xFFFFFF : 0xFF5555));
    }

    public int getVal() {
        int maxPlayers = validateNum(getText());
        return String.valueOf(maxPlayers).length() > 0 ? maxPlayers : defaultVal;
    }

    /**
     * @param text input
     * @return -1 if invalid, otherwise parsed int
     */
    public static int validateNum(String text) {
        boolean valid = true;
        int ret = -1;
        try {
            if (text.length() > 0) {
                ret = Integer.parseInt(text);
                if (ret < 1)
                    valid = false;
            }
            if (text.length() == 0) valid=false;
        } catch (NumberFormatException e) {
            valid = false;
        }

        return valid ? ret : -1;
    }
}
