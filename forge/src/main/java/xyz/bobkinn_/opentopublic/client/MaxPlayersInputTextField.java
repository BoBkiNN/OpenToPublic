package xyz.bobkinn_.opentopublic.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class MaxPlayersInputTextField extends EditBox {
    private final int defaultVal;

    public MaxPlayersInputTextField(Font textRenderer, int x, int y, int width, int height, Component name, int defaultVal) {
        super(textRenderer, x, y, width, height, name);
        this.defaultVal = defaultVal;
        this.setValue(String.valueOf(defaultVal));
        // Check the format, make sure the text is a valid integer
        this.setResponder((text) -> this.setTextColor(validateNum(text) >= 0 ? 0xFFFFFF : 0xFF5555));
    }

    public int getVal() {
        int maxPlayers = validateNum(getValue());
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
