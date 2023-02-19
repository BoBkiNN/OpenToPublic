package xyz.bobkinn_.opentopublic.client;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ToggleButton extends ButtonWidget {
    private boolean state;

    public ToggleButton(int x, int y, int width, int height,
                        Text text, boolean defaultState,
                        PressAction onCheck, TooltipSupplier tooltipSupplier) {
        super(x, y, width, height, text, onCheck, tooltipSupplier);
        this.state = defaultState;
    }

    public ToggleButton(int x, int y, int width, int height,
                        Text text, boolean defaultState,
                        PressAction onCheck) {
        super(x, y, width, height, text, onCheck);
        this.state = defaultState;
    }

    public void setState(boolean newState) {
        this.state = newState;
    }

    @Override
    public void onPress() {
        setState(!this.state);
        super.onPress();
    }
}
