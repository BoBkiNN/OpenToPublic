package xyz.bobkinn_.opentopublic.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;


public class PortInputTextField extends EditBox {
    private final int defaultPort;

    public PortInputTextField(Font textRenderer, int x, int y, int width, int height, Component name, int defaultPort) {
        super(textRenderer, x, y, width, height, name);
        this.defaultPort = defaultPort;
        this.setValue(String.valueOf(defaultPort));
        // Check the format, make sure the text is a valid integer
        this.setResponder((text) -> this.setTextColor(validatePort(text) >= 0 ? 0xFFFFFF : 0xFF5555));
    }

    public int getServerPort() {
        int port = validatePort(getValue());
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
