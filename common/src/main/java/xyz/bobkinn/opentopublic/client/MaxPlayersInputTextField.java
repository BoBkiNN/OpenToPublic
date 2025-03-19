package xyz.bobkinn.opentopublic.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/**
 * @author <a href="https://github.com/rikka0w0/LanServerProperties/">rikka0w0</a>
 */
public class MaxPlayersInputTextField extends EditBox {
    private final int defaultVal;

    public MaxPlayersInputTextField(Font textRenderer, int x, int y, int width, int height, Component name, int defaultVal) {
        super(textRenderer, x, y, width, height, name);
        this.defaultVal = defaultVal;
        this.setValue(String.valueOf(defaultVal));
        // Check the format, make sure the text is a valid integer
        this.setResponder((text) -> this.setTextColor(validateNum(text) >= 0 ? 0xFFFFFF : 0xFF5555));
    }

    /**
     * @param text input
     * @return -1 if invalid, otherwise parsed int
     */
    public static int validateNum(String text) {
        try {
            var parsed = Integer.parseInt(text);
            return parsed < 1 ? -1 : parsed;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public int getValidValue() {
        int maxPlayers = validateNum(getValue());
        return maxPlayers > 0 ? maxPlayers : defaultVal;
    }
}
