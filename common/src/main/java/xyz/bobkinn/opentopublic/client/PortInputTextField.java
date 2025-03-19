package xyz.bobkinn.opentopublic.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/**
 * @author <a href="https://github.com/rikka0w0/LanServerProperties/">rikka0w0</a>
 */
public class PortInputTextField extends EditBox {
    private final int defaultPort;

    public PortInputTextField(Font textRenderer, int x, int y, int width, int height, Component name, int defaultPort) {
        super(textRenderer, x, y, width, height, name);
        this.defaultPort = defaultPort;
        this.setValue(String.valueOf(defaultPort));
        // Check the format, make sure the text is a valid integer
        this.setResponder((text) -> this.setTextColor(validatePort(text) >= 0 ? 0xFFFFFF : 0xFF5555));
    }

    /**
     * @param text input
     * @return -1 if port is invalid, otherwise the port number
     */
    public static int validatePort(String text) {
        try {
            var port = Integer.parseInt(text);
            return port <= 0 || port > 65535 ? -1 : port;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public int getServerPort() {
        int port = validatePort(getValue());
        return port > 0 ? port : defaultPort;
    }
}
