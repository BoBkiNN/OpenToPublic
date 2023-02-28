package xyz.bobkinn_.opentopublic.client;

// code by https://github.com/rikka0w0/LanServerProperties/

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.TextComponent;

/**
 * @author <a href="https://github.com/rikka0w0/LanServerProperties/">rikka0w0</a>
 */
public class PortInputTextField extends TextFieldWidget {
    private final int defaultPort;

    public PortInputTextField(FontRenderer textRenderer, int x, int y, int width, int height, TextComponent name, int defaultPort) {
        super(textRenderer, x, y, width, height, name);
        this.defaultPort = defaultPort;
        this.setText(String.valueOf(defaultPort));
        // Check the format, make sure the text is a valid integer
        this.setResponder((text) -> this.setTextColor(validatePort(text) >= 0 ? 0xFFFFFF : 0xFF5555));
    }

    public int getServerPort() {
        int port = validatePort(getText());
        return String.valueOf(port).length() > 0 ? port : defaultPort;
    }

    /**
     * @param text input
     * @return negative if port is invalid, otherwise the port number
     */
    public static int validatePort(String text) {
        boolean valid = true;
        int port = -1;
        try {
            if (text.length() > 0) {
                port = Integer.parseInt(text);
                if (port < 0 || port > 65535)
                    valid = false;
            }
            if (text.length() == 0) valid=false;
        } catch (NumberFormatException e) {
            valid = false;
        }

        return valid ? port : -1;
    }
}
